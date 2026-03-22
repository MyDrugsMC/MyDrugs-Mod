#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform LucidDreamUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float BreathAmount;
    float LensWarp;
    float EchoAmount;
    float ChromaAmount;
    float Diffusion;
    vec4 Tint;
};

mat2 rot(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(113.1, 271.9))) * 43758.5453);
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

vec3 sampleDiffused(sampler2D tex, vec2 uv, vec2 dir, float amount) {
    vec3 c = vec3(0.0);
    c += texture(tex, uv).rgb * 0.34;
    c += texture(tex, uv + dir * amount).rgb * 0.22;
    c += texture(tex, uv - dir * amount).rgb * 0.22;
    c += texture(tex, uv + dir * amount * 2.0).rgb * 0.11;
    c += texture(tex, uv - dir * amount * 2.0).rgb * 0.11;
    return c;
}

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 p = uv - 0.5;
    p.x *= ScreenSize.x / ScreenSize.y;

    float r = length(p);
    float a = atan(p.y, p.x);

    float breath = 1.0 + sin(t * 1.1 + r * 10.0) * BreathAmount;
    vec2 q = p * breath;

    float n = noise(q * 4.0 + vec2(t * 0.12, -t * 0.08));
    float lens = sin(a * 4.0 - t * 0.7 + n * 3.0) * LensWarp;

    vec2 lensDir = normalize(q + vec2(1e-5));
    vec2 baseUV = q / vec2(ScreenSize.x / ScreenSize.y, 1.0) + 0.5 + lensDir * lens;

    vec2 echoOffset = vec2(
        sin(t * 0.8 + uv.y * 9.0),
        cos(t * 0.6 + uv.x * 8.0)
    ) * EchoAmount;

    vec2 chromaDir = lensDir * ChromaAmount * (0.4 + 0.8 * r);

    float rr = texture(InSampler, baseUV + chromaDir).r;
    float gg = texture(InSampler, baseUV).g;
    float bb = texture(InSampler, baseUV - chromaDir).b;

    vec3 split = vec3(rr, gg, bb);
    vec3 echo = texture(InSampler, baseUV + echoOffset).rgb;
    vec3 diff = sampleDiffused(InSampler, baseUV, normalize(echoOffset + vec2(1e-5)), Diffusion);

    vec3 col = mix(split, echo, 0.18);
    col = mix(col, diff, 0.22);

    float pulse = 0.5 + 0.5 * sin(t + r * 16.0 + n * 5.0);
    col *= mix(vec3(1.0), Tint.rgb, 0.12 + 0.10 * pulse);

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.12);

    float vignette = smoothstep(0.98, 0.22, r);
    col *= 0.93 + 0.07 * vignette;

    fragColor = vec4(col, 1.0);
}
