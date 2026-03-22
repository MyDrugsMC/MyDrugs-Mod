#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

layout(std140) uniform EventHorizonUniforms {
    float Time;
    vec2 ScreenSize;
    float Speed;
    float CenterPull;
    float RingRadius;
    float RingWidth;
    float Lensing;
    float ChromaAmount;
    float GlowAmount;
    vec4 Tint;
};

const float PI = 3.14159265359;

mat2 rot(float a) {
    float s = sin(a);
    float c = cos(a);
    return mat2(c, -s, s, c);
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(41.0, 289.0))) * 45758.5453);
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

void main() {
    float t = Time * Speed;

    vec2 uv = texCoord;
    vec2 p = uv - 0.5;
    p.x *= ScreenSize.x / ScreenSize.y;

    float r = length(p);
    float a = atan(p.y, p.x);

    vec2 radial = normalize(p + vec2(1e-5));
    vec2 tangent = vec2(-radial.y, radial.x);

    float innerPull = exp(-r * 10.0) * CenterPull;
    float ringDelta = abs(r - RingRadius);
    float ringMask = exp(-(ringDelta * ringDelta) / max(RingWidth * RingWidth, 1e-5));
    float swirl = sin(a * 8.0 - t * 2.0 + r * 30.0);
    float turbulence = noise(radial * 5.0 + tangent * 2.0 + vec2(t * 0.15, -t * 0.09));

    vec2 disp = -radial * innerPull;
    disp += radial * ringMask * Lensing * (0.65 + 0.35 * swirl);
    disp += tangent * ringMask * Lensing * 0.55 * (turbulence - 0.5);

    vec2 baseUV = uv + disp;

    float fringe = ringMask * (0.3 + 0.7 * (0.5 + 0.5 * swirl));
    vec2 chroma = radial * ChromaAmount * (0.2 + fringe);

    vec3 split = vec3(
        texture(InSampler, baseUV + chroma).r,
        texture(InSampler, baseUV).g,
        texture(InSampler, baseUV - chroma).b
    );

    float photonRing = pow(ringMask, 1.5) * (0.55 + 0.45 * (0.5 + 0.5 * sin(a * 12.0 - t * 3.0)));
    float coreDim = 1.0 - exp(-r * 22.0);

    vec3 col = split * coreDim;
    col += photonRing * GlowAmount * 0.35 * Tint.rgb;
    col += ringMask * GlowAmount * 0.08 * vec3(1.0, 0.95, 1.05);

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.10);

    float vignette = smoothstep(1.0, 0.12, r);
    col *= 0.92 + 0.08 * vignette;

    fragColor = vec4(col, 1.0);
}
