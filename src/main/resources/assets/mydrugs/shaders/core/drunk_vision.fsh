#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform DrunkVisionUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float SwayAmount;
    float BarrelAmount;
    float SpinAmount;
    float RadiusStart;
    float JitterAmount;
    float BlurAmount;
    float ChromaAmount;
    float EchoAmount;
    float PulseAmount;
    vec3 Tint;
};

mat2 rot(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

vec3 sampleBlurred(sampler2D tex, vec2 uv, vec2 dir, float amount) {
    vec3 c = vec3(0.0);
    c += texture(tex, uv - dir * amount * 2.0).rgb * 0.10;
    c += texture(tex, uv - dir * amount).rgb * 0.20;
    c += texture(tex, uv).rgb * 0.40;
    c += texture(tex, uv + dir * amount).rgb * 0.20;
    c += texture(tex, uv + dir * amount * 2.0).rgb * 0.10;
    return c;
}

vec3 radialBlur(sampler2D tex, vec2 uv, vec2 center, float amount) {
    vec2 dir = uv - center;
    vec3 c = vec3(0.0);
    c += texture(tex, uv - dir * amount * 1.5).rgb * 0.16;
    c += texture(tex, uv - dir * amount * 0.5).rgb * 0.22;
    c += texture(tex, uv).rgb * 0.24;
    c += texture(tex, uv + dir * amount * 0.5).rgb * 0.22;
    c += texture(tex, uv + dir * amount * 1.5).rgb * 0.16;
    return c;
}

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 p = uv - 0.5;
    float aspect = ScreenSize.x / ScreenSize.y;
    p.x *= aspect;

    float r = length(p);

    // Whole-screen sway / head wobble.
    float swayAngle = sin(t * 0.90) * SwayAmount + cos(t * 0.53 + r * 4.0) * SwayAmount * 0.55;
    vec2 q = rot(swayAngle) * p;

    // Mild drunken barrel distortion.
    q *= 1.0 + BarrelAmount * r * r;

    // Edge-only spin for dizziness.
    float edge = smoothstep(RadiusStart, 0.98, r);
    float spin = sin(t * 0.95 + r * 8.0) * SpinAmount * edge;
    q = rot(spin) * q;

    vec2 baseUV = q / vec2(aspect, 1.0) + 0.5;

    // Staggered instability / horizontal drunken wobble.
    float lineWave = sin(baseUV.y * 18.0 - t * 1.35) + cos(baseUV.y * 31.0 + t * 0.78);
    float scan = sin(baseUV.y * 82.0 + t * 2.1) * 0.5 + 0.5;
    vec2 jitter = vec2(
        lineWave * JitterAmount,
        sin(t + baseUV.x * 9.0) * JitterAmount * 0.45
    );

    vec2 warpedUV = baseUV + jitter;

    // Double-vision echo.
    vec2 echoDir = vec2(
        sin(t * 0.78 + baseUV.y * 10.0),
        cos(t * 0.62 + baseUV.x * 8.5)
    );
    vec2 echoOffset = echoDir * EchoAmount * (1.0 + r * 0.75 + edge * 0.4);

    // Vertical split inspired by stagger blur.
    vec2 verticalSplit = vec2(0.0, JitterAmount * 1.35 * (0.30 + 0.70 * scan));

    vec2 blurDir = normalize(echoOffset + vec2(0.55, 0.15) + vec2(1e-5));

    vec3 softCol = sampleBlurred(InSampler, warpedUV, blurDir, BlurAmount * (0.75 + edge * 0.65));
    vec3 edgeBlur = radialBlur(InSampler, warpedUV, vec2(0.5), BlurAmount * 0.65 * edge);
    vec3 echoCol = texture(InSampler, warpedUV + echoOffset).rgb;

    vec2 chromaDir = normalize(q + vec2(1e-5)) * ChromaAmount * (0.28 + r + edge * 0.2);
    vec3 splitCol = vec3(
        texture(InSampler, warpedUV + chromaDir + verticalSplit + echoOffset * 0.35).r,
        texture(InSampler, warpedUV).g,
        texture(InSampler, warpedUV - chromaDir - verticalSplit - echoOffset * 0.35).b
    );

    vec3 col = mix(softCol, edgeBlur, 0.28 * edge);
    col = mix(col, echoCol, 0.22);
    col = mix(col, splitCol, 0.40);

    float pulse = 0.5 + 0.5 * sin(t * 1.05 + r * 15.0);
    col *= mix(vec3(1.0), Tint, PulseAmount * pulse);

    float vignette = smoothstep(1.04, 0.18, r);
    col *= 0.92 + 0.08 * vignette;

    fragColor = vec4(col, 1.0);
}
