#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform SpectralPosterUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float PosterLevels;
    float EdgeAmount;
    float DriftAmount;
    float RainbowAmount;
    float GrainAmount;
    vec4 Accent;
};

vec3 palette(float t) {
    vec3 a = vec3(0.50, 0.50, 0.50);
    vec3 b = vec3(0.50, 0.50, 0.50);
    vec3 c = vec3(1.00, 1.00, 1.00);
    vec3 d = vec3(0.00, 0.33, 0.67);
    return a + b * cos(6.28318 * (c * t + d));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453123);
}

vec3 posterize(vec3 c, float levels) {
    levels = max(levels, 2.0);
    return floor(c * levels) / levels;
}

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 px = vec2(1.0 / ScreenSize.x, 1.0 / ScreenSize.y);

    vec2 drift = vec2(
        sin(t * 0.8 + uv.y * 10.0),
        cos(t * 0.6 + uv.x * 11.0)
    ) * DriftAmount;

    vec2 suv = uv + drift;

    vec3 src = texture(InSampler, suv).rgb;
    float lC = dot(src, vec3(0.299, 0.587, 0.114));
    float lR = dot(texture(InSampler, suv + vec2(px.x, 0.0)).rgb, vec3(0.299, 0.587, 0.114));
    float lL = dot(texture(InSampler, suv - vec2(px.x, 0.0)).rgb, vec3(0.299, 0.587, 0.114));
    float lU = dot(texture(InSampler, suv + vec2(0.0, px.y)).rgb, vec3(0.299, 0.587, 0.114));
    float lD = dot(texture(InSampler, suv - vec2(0.0, px.y)).rgb, vec3(0.299, 0.587, 0.114));

    float edge = abs(lR - lL) + abs(lU - lD);
    edge = smoothstep(0.04, 0.25, edge) * EdgeAmount;

    vec3 post = posterize(src, PosterLevels);
    float posterLuma = dot(post, vec3(0.299, 0.587, 0.114));

    vec3 rainbow = palette(posterLuma + t * 0.08 + edge * 0.35);
    vec3 ink = mix(post, rainbow * Accent.rgb, RainbowAmount * (0.35 + 0.65 * edge));
    ink += rainbow * edge * 0.22;

    float grain = hash(uv * ScreenSize.xy + fract(t) * 100.0) - 0.5;
    ink += grain * GrainAmount;

    float luma = dot(ink, vec3(0.299, 0.587, 0.114));
    ink = mix(vec3(luma), ink, 1.18);

    fragColor = vec4(ink, 1.0);
}
