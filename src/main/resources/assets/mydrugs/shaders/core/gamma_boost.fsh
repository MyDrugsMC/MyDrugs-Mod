#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform GammaBoostUniforms {
    float Time;
    vec2 ScreenSize;
    float Strength;
};

void main() {
    vec4 src = texture(InSampler, texCoord);

    vec3 col = clamp(src.rgb, 0.0, 1.0);

    // Real gamma correction.
    // gamma = 1.0 means no change.
    // gamma > 1.0 brightens.
    // gamma < 1.0 darkens.
    float gamma = clamp(Strength + 1, 0.05, 8.0);

    vec3 corrected = pow(col, vec3(1.0 / gamma));

    fragColor = vec4(corrected, src.a);
}