#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform ChromaticDreamUniforms {
    float Time;
    vec2 ScreenSize;
    float Strength;
    float Speed;
    float Zoom;
    float Saturation;
};

vec3 saturateColor(vec3 c, float amount) {
    float luma = dot(c, vec3(0.299, 0.587, 0.114));
    return mix(vec3(luma), c, amount);
}

vec2 safeUv(vec2 uv) {
    return clamp(uv, vec2(0.001), vec2(0.999));
}

void main() {
    vec2 uv = texCoord;
    vec2 centered = uv - 0.5;
    float t = Time * Speed;

    float r = length(centered);
    vec2 dir = normalize(centered + vec2(0.0001));

    float wobble = sin(t * 1.5 + r * 18.0) * Strength;
    vec2 offset = dir * wobble;

    vec2 zoomUv = 0.5 + centered * (1.0 + Zoom * sin(t + r * 10.0));

    vec2 uvR = safeUv(zoomUv + offset * 1.2);
    vec2 uvG = safeUv(zoomUv);
    vec2 uvB = safeUv(zoomUv - offset * 1.2);

    float red   = texture(InSampler, uvR).r;
    float green = texture(InSampler, uvG).g;
    float blue  = texture(InSampler, uvB).b;

    vec3 color = vec3(red, green, blue);
    color = saturateColor(color, Saturation);

    fragColor = vec4(color, 1.0);
}