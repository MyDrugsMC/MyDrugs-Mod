#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform AcidWarpUniforms {
    float Time;
    vec2 ScreenSize;
    float Strength;
    float Speed;
    float Frequency;
    vec3 Tint;
};

void main() {
    vec2 uv = texCoord;
    float t = Time * Speed;

    vec2 centered = uv - 0.5;
    float r = length(centered);
    float a = atan(centered.y, centered.x);

    float wave1 = sin(a * Frequency + t * 2.0 + r * 12.0) * Strength;
    float wave2 = cos(uv.y * Frequency * 3.0 - t * 1.7) * Strength * 0.5;

    uv.x += wave1;
    uv.y += wave2;

    vec4 base = texture(InSampler, uv);

    float pulse = 0.5 + 0.5 * sin(t + r * 20.0);
    vec3 color = mix(base.rgb, base.rgb * Tint, pulse * 0.25);

    fragColor = vec4(color, 1.0);
}