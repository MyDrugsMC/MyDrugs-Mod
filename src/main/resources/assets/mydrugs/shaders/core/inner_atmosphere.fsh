#version 150

uniform sampler2D InSampler;

layout(std140) uniform InnerAtmosphereUniforms {
    float Time;
    vec2 ScreenSize;
    float Strength;
    float Bloom;
    float Vignette;
    float Desat;
    float Warmth;
    float GodRay;
};

in vec2 texCoord0;
out vec4 fragColor;

const vec3 LUMA = vec3(0.299, 0.587, 0.114);

// Soft bright-pass: keep only the upper luminance band, knee'd so it ramps in smoothly.
vec3 brightPass(vec3 c) {
    float l = dot(c, LUMA);
    float k = smoothstep(0.62, 0.95, l);
    return c * k;
}

void main() {
    vec2 uv = texCoord0;
    vec2 texel = 1.0 / ScreenSize;
    vec3 base = texture(InSampler, uv).rgb;
    vec3 color = base;

    // ---- Bloom: a cheap cross/diagonal blur of the bright-pass, added back additively. ----
    if (Bloom > 0.001) {
        vec3 b = vec3(0.0);
        float r1 = 2.0;
        float r2 = 4.0;
        b += brightPass(texture(InSampler, uv + vec2( r1,  0.0) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2(-r1,  0.0) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2( 0.0,  r1) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2( 0.0, -r1) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2( r2,  r2) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2(-r2,  r2) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2( r2, -r2) * texel).rgb);
        b += brightPass(texture(InSampler, uv + vec2(-r2, -r2) * texel).rgb);
        b *= 0.125;
        color += b * Bloom * Strength * 1.4;
    }

    // ---- God rays: radial blur of the bright-pass toward screen centre. ----
    if (GodRay > 0.001) {
        vec2 center = vec2(0.5);
        vec2 dir = (uv - center);
        vec3 ray = vec3(0.0);
        const int STEPS = 8;
        float decay = 0.92;
        float w = 1.0;
        float total = 0.0;
        for (int i = 0; i < STEPS; i++) {
            float t = float(i) / float(STEPS);
            vec2 sampleUv = uv - dir * t * 0.5;
            ray += brightPass(texture(InSampler, sampleUv).rgb) * w;
            total += w;
            w *= decay;
        }
        ray /= max(total, 0.001);
        color += ray * GodRay * Strength * 0.9;
    }

    // ---- Warmth: push toward a warm tint where calm is high. ----
    if (Warmth > 0.001) {
        vec3 warm = color * vec3(1.08, 1.02, 0.92);
        color = mix(color, warm, Warmth * Strength);
    }

    // ---- Desaturation: bleed toward luminance where danger is high. ----
    if (Desat > 0.001) {
        float g = dot(color, LUMA);
        color = mix(color, vec3(g), Desat * Strength);
    }

    // ---- Vignette: darken edges where danger is high (oppressive scar fields). ----
    if (Vignette > 0.001) {
        vec2 p = uv - vec2(0.5);
        float d = dot(p, p);
        float vig = smoothstep(0.10, 0.55, d);
        color *= 1.0 - vig * Vignette * Strength * 0.65;
    }

    fragColor = vec4(color, 1.0);
}
