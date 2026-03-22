#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform AuroraRibbonsUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float RibbonStrength;
    float RibbonScale;
    float DriftAmount;
    float GlowAmount;
    float ChromaAmount;
    vec4 ColorA;
    vec4 ColorB;
};

mat2 rot(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    f = f * f * (3.0 - 2.0 * f);

    float a = hash(i);
    float b = hash(i + vec2(1.0, 0.0));
    float c = hash(i + vec2(0.0, 1.0));
    float d = hash(i + vec2(1.0, 1.0));

    return mix(mix(a, b, f.x), mix(c, d, f.x), f.y);
}

float fbm(vec2 p) {
    float v = 0.0;
    float a = 0.5;
    for (int i = 0; i < 5; i++) {
        v += noise(p) * a;
        p = rot(0.55) * p * 2.0 + vec2(2.7, 1.9);
        a *= 0.5;
    }
    return v;
}

vec3 sampleGlow(sampler2D tex, vec2 uv, vec2 dir, float amount) {
    vec3 c = vec3(0.0);
    c += texture(tex, uv).rgb * 0.34;
    c += texture(tex, uv + dir * amount).rgb * 0.23;
    c += texture(tex, uv - dir * amount).rgb * 0.23;
    c += texture(tex, uv + dir * amount * 2.0).rgb * 0.10;
    c += texture(tex, uv - dir * amount * 2.0).rgb * 0.10;
    return c;
}

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 p = uv - 0.5;
    float aspect = ScreenSize.x / ScreenSize.y;
    p.x *= aspect;

    vec2 q = p * RibbonScale;
    float flow1 = fbm(q + vec2(t * 0.08, -t * 0.04));
    float flow2 = fbm(rot(0.9) * q + vec2(-t * 0.05, t * 0.11));

    float ribbons = sin(q.y * 3.0 + flow1 * 5.0 - t * 0.8);
    ribbons += 0.6 * sin(q.y * 5.5 - q.x * 0.8 + flow2 * 4.0 + t * 1.1);
    ribbons += 0.35 * cos(q.x * 2.2 + flow1 * 6.0 - t * 0.4);
    ribbons /= 1.95;

    float band = smoothstep(0.15, 0.95, 0.5 + 0.5 * ribbons);
    vec2 flowDir = normalize(vec2(flow1 - 0.5, flow2 - 0.5) + vec2(1e-5));
    vec2 drift = flowDir * DriftAmount * (0.3 + 0.7 * band);

    vec2 sampleUV = uv + drift;
    vec2 chroma = flowDir * ChromaAmount * (0.25 + 0.75 * band);

    vec3 base = vec3(
        texture(InSampler, sampleUV + chroma).r,
        texture(InSampler, sampleUV).g,
        texture(InSampler, sampleUV - chroma).b
    );

    vec3 soft = sampleGlow(InSampler, sampleUV, flowDir, GlowAmount);
    vec3 col = mix(base, soft, 0.25);

    vec3 ribbonColor = mix(ColorA.rgb, ColorB.rgb, band);
    vec3 ribbonColor2 = mix(ColorB.rgb, ColorA.rgb, 0.5 + 0.5 * sin(t * 0.6 + flow2 * 6.0));
    vec3 aurora = mix(ribbonColor, ribbonColor2, 0.35 + 0.35 * flow1);

    float veil = smoothstep(0.0, 0.8, band) * RibbonStrength;
    col += aurora * veil * (0.18 + 0.22 * sin(t + flow1 * 7.0));
    col *= mix(vec3(1.0), aurora, 0.10 + 0.15 * veil);

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.12);

    float vignette = smoothstep(1.0, 0.10, length(p));
    col *= 0.93 + 0.07 * vignette;

    fragColor = vec4(col, 1.0);
}
