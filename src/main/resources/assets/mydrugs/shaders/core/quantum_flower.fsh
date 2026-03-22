#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform QuantumFlowerUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float Petals;
    float WarpAmount;
    float BloomAmount;
    float ChromaAmount;
    float Iridescence;
    vec4 Tint;
};

const float PI = 3.14159265359;
const float TAU = 6.28318530718;

mat2 rot(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

vec3 sampleSoft(sampler2D tex, vec2 uv, vec2 dir, float amt) {
    vec3 c = vec3(0.0);
    c += texture(tex, uv).rgb * 0.34;
    c += texture(tex, uv + dir * amt).rgb * 0.23;
    c += texture(tex, uv - dir * amt).rgb * 0.23;
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

    float breath = 1.0 + 0.025 * sin(t * 1.2 + r * 12.0);
    vec2 q = p * breath;

    float sector = TAU / max(Petals, 1.0);
    float foldedAngle = mod(a + 0.5 * sector, sector) - 0.5 * sector;
    foldedAngle = abs(foldedAngle);

    vec2 petalSpace = vec2(cos(foldedAngle), sin(foldedAngle)) * r;
    petalSpace *= rot(sin(t * 0.35) * 0.18);

    float petalMask = 0.5 + 0.5 * cos(a * Petals - t * 0.6 + sin(r * 16.0 - t) * 0.8);
    float ringBands = 0.5 + 0.5 * sin(r * 30.0 - t * 1.7 + petalMask * 4.0);

    vec2 radial = normalize(q + vec2(1e-5));
    vec2 tangent = vec2(-radial.y, radial.x);
    vec2 warpDir = normalize(mix(radial, tangent, petalMask - 0.5) + vec2(1e-5));

    vec2 warped = mix(q, petalSpace, 0.22) + warpDir * WarpAmount * (0.35 + 0.65 * ringBands);
    vec2 baseUV = warped / vec2(ScreenSize.x / ScreenSize.y, 1.0) + 0.5;

    vec2 chroma = radial * ChromaAmount * (0.35 + 0.65 * petalMask + r * 0.5);

    vec3 split = vec3(
        texture(InSampler, baseUV + chroma).r,
        texture(InSampler, baseUV).g,
        texture(InSampler, baseUV - chroma).b
    );

    vec3 soft = sampleSoft(InSampler, baseUV, warpDir, BloomAmount);
    vec3 col = mix(split, soft, 0.28);

    float sheen = 0.5 + 0.5 * sin(foldedAngle * 8.0 - t * 0.9 + r * 22.0);
    vec3 petalTint = mix(vec3(1.0), Tint.rgb, Iridescence * (0.35 + 0.65 * sheen));
    col *= petalTint;

    float sparkle = pow(petalMask, 3.0) * ringBands;
    col += 0.04 * sparkle * Tint.rgb;

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.10);

    float vignette = smoothstep(1.0, 0.18, r);
    col *= 0.94 + 0.06 * vignette;

    fragColor = vec4(col, 1.0);
}
