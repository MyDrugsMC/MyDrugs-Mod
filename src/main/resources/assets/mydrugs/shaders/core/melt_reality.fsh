#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform MeltRealityUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float FlowStrength;
    float FlowScale;
    float ChromaAmount;
    float Softness;
    float PulseAmount;
    vec4 Tint;
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
        p = rot(0.55) * p * 2.0 + vec2(3.1, 1.7);
        a *= 0.5;
    }
    return v;
}

vec3 sampleSoft(sampler2D tex, vec2 uv, vec2 dir, float amt) {
    vec3 c = vec3(0.0);
    c += texture(tex, uv - dir * amt * 2.0).rgb * 0.12;
    c += texture(tex, uv - dir * amt).rgb * 0.23;
    c += texture(tex, uv).rgb * 0.30;
    c += texture(tex, uv + dir * amt).rgb * 0.23;
    c += texture(tex, uv + dir * amt * 2.0).rgb * 0.12;
    return c;
}

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 p = uv - 0.5;
    p.x *= ScreenSize.x / ScreenSize.y;

    float r = length(p);

    vec2 flowP = p * FlowScale;
    float n1 = fbm(flowP + vec2(t * 0.18, -t * 0.11));
    float n2 = fbm(rot(0.8) * flowP + vec2(-t * 0.13, t * 0.16));

    vec2 flowDir = normalize(vec2(n1 - 0.5, n2 - 0.5) + vec2(1e-5));
    vec2 disp = flowDir * FlowStrength * (0.55 + 0.45 * sin(t + r * 10.0));

    vec2 sampleUV = uv + disp;

    vec2 radial = normalize(p + vec2(1e-5));
    vec2 chromaDir = radial * ChromaAmount * (0.35 + r);

    float rr = texture(InSampler, sampleUV + chromaDir).r;
    float gg = texture(InSampler, sampleUV).g;
    float bb = texture(InSampler, sampleUV - chromaDir).b;
    vec3 split = vec3(rr, gg, bb);

    vec3 soft = sampleSoft(InSampler, sampleUV, flowDir, Softness);
    vec3 col = mix(split, soft, 0.35);

    float pulse = 0.5 + 0.5 * sin(t * 1.2 + n1 * 5.0 + r * 14.0);
    col *= mix(vec3(1.0), Tint.rgb, PulseAmount * pulse);

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.10);

    float vignette = smoothstep(0.95, 0.20, r);
    col *= 0.92 + 0.08 * vignette;

    fragColor = vec4(col, 1.0);
}
