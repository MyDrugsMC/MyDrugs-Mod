#version 150

uniform sampler2D InSampler;
uniform sampler2D DepthSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform VoidPulseUniforms {
    float Time;
    vec2 ScreenSize;
    float PulseSpeed;
    float DistortionStrength;
    float Darkness;
    float DepthBoost;
    vec3 GlowColor;
};

void main() {
    vec2 uv = texCoord;
    vec2 centered = uv - 0.5;
    float r = length(centered);
    float t = Time * PulseSpeed;

    float depth = texture(DepthSampler, uv).r;
    float farFactor = smoothstep(0.2, 1.0, depth);

    float pulse = 0.5 + 0.5 * sin(t * 2.0 - r * 25.0);
    float distort = pulse * DistortionStrength * (0.3 + farFactor * DepthBoost);

    uv += normalize(centered + vec2(0.0001)) * distort;

    vec4 base = texture(InSampler, uv);

    float vignette = smoothstep(0.85, 0.2, r);
    float darken = Darkness * (0.4 + 0.6 * pulse) * vignette;

    vec3 color = base.rgb * (1.0 - darken);
    color += GlowColor * pulse * farFactor * 0.25;

    fragColor = vec4(color, 1.0);
}