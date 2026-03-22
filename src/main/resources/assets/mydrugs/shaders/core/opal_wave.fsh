#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform OpalWaveUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float FlowScale;
    float Distortion;
    float SheenAmount;
    float Dispersion;
    float Softness;
    vec4 TintA;
    vec4 TintB;
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
        p = rot(0.55) * p * 2.0 + vec2(3.1, 1.6);
        a *= 0.5;
    }
    return v;
}

vec3 sampleSoft(sampler2D tex, vec2 uv, vec2 dir, float amt) {
    vec3 c = vec3(0.0);
    c += texture(tex, uv).rgb * 0.36;
    c += texture(tex, uv + dir * amt).rgb * 0.22;
    c += texture(tex, uv - dir * amt).rgb * 0.22;
    c += texture(tex, uv + dir * amt * 2.0).rgb * 0.10;
    c += texture(tex, uv - dir * amt * 2.0).rgb * 0.10;
    return c;
}

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 p = uv - 0.5;
    p.x *= ScreenSize.x / ScreenSize.y;

    float r = length(p);
    float a = atan(p.y, p.x);

    vec2 flowP = p * FlowScale;
    float n1 = fbm(flowP + vec2(t * 0.12, -t * 0.08));
    float n2 = fbm(rot(0.9) * flowP + vec2(-t * 0.10, t * 0.14));
    float n3 = fbm(rot(-0.5) * flowP * 1.6 - vec2(t * 0.07, t * 0.05));

    vec2 flowDir = normalize(vec2(n1 - n2, n3 - n1) + vec2(1e-5));
    vec2 disp = flowDir * Distortion * (0.7 + 0.3 * sin(t + r * 10.0));
    vec2 baseUV = uv + disp;

    vec2 radial = normalize(p + vec2(1e-5));
    vec2 chroma = radial * Dispersion * (0.2 + r);

    vec3 split = vec3(
        texture(InSampler, baseUV + chroma).r,
        texture(InSampler, baseUV).g,
        texture(InSampler, baseUV - chroma).b
    );

    vec3 soft = sampleSoft(InSampler, baseUV, flowDir, Softness);
    vec3 col = mix(split, soft, 0.24);

    float pearl = 0.5 + 0.5 * sin(n1 * 7.0 + n2 * 5.0 + a * 2.5 - t * 0.8 + r * 16.0);
    float caustic = pow(0.5 + 0.5 * sin(n3 * 10.0 - t * 1.2 + r * 24.0), 3.0);
    vec3 sheen = mix(TintA.rgb, TintB.rgb, pearl);

    col *= mix(vec3(1.0), sheen, SheenAmount * (0.35 + 0.65 * pearl));
    col += caustic * 0.05 * sheen;

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.08);

    float vignette = smoothstep(1.0, 0.16, r);
    col *= 0.95 + 0.05 * vignette;

    fragColor = vec4(col, 1.0);
}
