#version 150

uniform sampler2D InSampler;

layout(std140) uniform WithdrawalTunnelUniforms {
    float Time;
    vec2 ScreenSize;
    float Strength;
    float BlurPixels;
    float Darkness;
    float PulseAmount;
    float BeatPulse;
};

in vec2 texCoord0;
out vec4 fragColor;

float hash12(vec2 p) {
    float h = dot(p, vec2(127.1, 311.7));
    return fract(sin(h) * 43758.5453123);
}

float noise2d(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);

    float a = hash12(i);
    float b = hash12(i + vec2(1.0, 0.0));
    float c = hash12(i + vec2(0.0, 1.0));
    float d = hash12(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);

    return mix(a, b, u.x)
    + (c - a) * u.y * (1.0 - u.x)
    + (d - b) * u.x * u.y;
}

vec4 blurAlongAxis(vec2 uv, vec2 axis, float radiusPx) {
    vec2 texel = 1.0 / ScreenSize;
    vec2 off = axis * texel * radiusPx;

    vec4 sum = texture(InSampler, uv) * 0.22;
    sum += texture(InSampler, uv + off * 0.45) * 0.18;
    sum += texture(InSampler, uv - off * 0.45) * 0.18;
    sum += texture(InSampler, uv + off * 1.00) * 0.14;
    sum += texture(InSampler, uv - off * 1.00) * 0.14;
    sum += texture(InSampler, uv + off * 1.80) * 0.08;
    sum += texture(InSampler, uv - off * 1.80) * 0.08;
    sum += texture(InSampler, uv + off * 2.80) * 0.04;
    sum += texture(InSampler, uv - off * 2.80) * 0.04;

    return sum;
}

void main() {
    vec2 uv = texCoord0;
    float beat = BeatPulse;

    // Centered coordinates, aspect-corrected so the tunnel stays circular.
    vec2 p = uv - vec2(0.5);
    p.x *= ScreenSize.x / ScreenSize.y;

    float dist0 = length(p);
    vec2 dir0 = normalize(p + vec2(0.0001, 0.0));
    vec2 tangent0 = vec2(-dir0.y, dir0.x);

    // Breathing / unstable motion
    float breathe = sin(Time * 1.05) * 0.5 + 0.5;
    float drift1 = sin(Time * 1.15 + dist0 * 18.0) * 0.0045 * Strength;
    float drift2 = sin(Time * 2.60 - dist0 * 27.0) * 0.0030 * Strength;
    float drift3 = (noise2d(p * 10.0 + Time * 0.8) - 0.5) * 0.010 * Strength;

    // Heartbeat squeeze toward center
    float beatWarp = beat * mix(0.005, 0.020, Strength);

    p += dir0 * (drift1 + drift3 + beatWarp);
    p += tangent0 * drift2;

    float dist = length(p);
    vec2 dir = normalize(p + vec2(0.0001, 0.0));
    vec2 tangent = vec2(-dir.y, dir.x);

    // Main tunnel radius
    float pulse = sin(Time * 2.35) * PulseAmount;
    float breathingRadius = (breathe - 0.5) * 0.02 * Strength;

    float innerRadius = mix(0.62, 0.16, Strength)
    - pulse
    - beat * mix(0.01, 0.07, Strength)
    + breathingRadius;

    float outerRadius = innerRadius + mix(0.22, 0.06, Strength);

    float edgeMask = smoothstep(innerRadius, outerRadius, dist);

    // Extra outer ring emphasis
    float farEdge = smoothstep(innerRadius + 0.05, outerRadius + 0.12, dist);

    vec4 base = texture(InSampler, uv);

    // Blur is stronger on the edge, stronger again on heartbeat
    float dynamicBlur = BlurPixels;
    dynamicBlur += edgeMask * mix(0.5, 5.0, Strength);
    dynamicBlur += farEdge * mix(0.0, 3.0, Strength);
    dynamicBlur += beat * mix(0.5, 4.0, Strength);

    vec4 radialBlur = blurAlongAxis(uv, dir, dynamicBlur);
    vec4 tangentialBlur = blurAlongAxis(uv, tangent, dynamicBlur * 0.75 + 0.75);
    vec4 blurred = mix(radialBlur, tangentialBlur, 0.48);

    float blurMix = edgeMask * (0.34 + Strength * 0.66);
    blurMix += farEdge * 0.20 * Strength;
    blurMix += edgeMask * beat * 0.20;
    blurMix = clamp(blurMix, 0.0, 1.0);

    vec4 color = mix(base, blurred, blurMix);

    // Slight chromatic aberration at the periphery
    vec2 ca = dir * edgeMask * (0.0008 + 0.0035 * Strength + 0.0020 * beat);

    vec3 splitColor = vec3(
    texture(InSampler, uv + ca).r,
    texture(InSampler, uv).g,
    texture(InSampler, uv - ca).b
    );

    color.rgb = mix(color.rgb, splitColor, edgeMask * (0.08 + 0.34 * Strength));

    // Peripheral desaturation
    float gray = dot(color.rgb, vec3(0.299, 0.587, 0.114));
    color.rgb = mix(color.rgb, vec3(gray), edgeMask * Strength * 0.35);

    // Edge shimmer / instability
    float ringNoise = noise2d(vec2(dist * 18.0, Time * 1.7 + uv.x * 4.0));
    float shimmer = (ringNoise - 0.5) * 0.08 * Strength * edgeMask;
    shimmer += (hash12(uv * ScreenSize + vec2(Time * 37.0, Time * 11.0)) - 0.5)
    * 0.025 * Strength * edgeMask;

    color.rgb += vec3(shimmer);

    // Darken edge heavily, especially on heartbeat
    float dark = edgeMask * Darkness;
    dark += farEdge * 0.18 * Strength;
    dark += beat * edgeMask * 0.08;
    dark = clamp(dark, 0.0, 0.95);

    color.rgb *= (1.0 - dark);

    fragColor = vec4(color.rgb, 1.0);
}