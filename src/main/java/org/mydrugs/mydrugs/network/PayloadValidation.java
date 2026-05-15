package org.mydrugs.mydrugs.network;

/**
 * Small helpers for server-side payload sanitization. Server-bound packets are
 * untrusted input; numeric fields must be checked for NaN/infinity and clamped
 * before being applied to gameplay state.
 */
public final class PayloadValidation {
    private PayloadValidation() {
    }

    /**
     * @return true if the float is finite (not NaN, not infinite). Reject payloads
     *         whose floats fail this check rather than coercing — that exposes the
     *         caller, while silent coercion can mask buggy or malicious senders.
     */
    public static boolean isFinite(float value) {
        return !Float.isNaN(value) && !Float.isInfinite(value);
    }

    /**
     * Clamp a float into [min, max]. Returns {@code min} if the input is NaN.
     * Use this for clean, gameplay-bounded values when rejection is undesirable.
     */
    public static float clamp(float value, float min, float max) {
        if (Float.isNaN(value)) {
            return min;
        }
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    /**
     * Clamp a non-negative magnitude into [0, max]. Returns 0 for NaN or negative.
     */
    public static float clampNonNegative(float value, float max) {
        return clamp(value, 0.0F, max);
    }
}
