package org.mydrugs.mydrugs.effects.addiction.config;

public final class SymptomFlags {
    public static final int CONFUSION = 1 << 0;
    public static final int FRAGILITY = 1 << 1;
    public static final int VISION = 1 << 2;
    public static final int HALLUCINATION = 1 << 3;
    public static final int STRESS = 1 << 4;
    public static final int DISSOCIATION = 1 << 5;
    public static final int FATIGUE = 1 << 6;
    public static final int INTRUSIVE_THOUGHTS = 1 << 7;
    public static final int INSOMNIA = 1 << 8;

    private SymptomFlags() {
    }

    public static boolean has(int flags, int flag) {
        return (flags & flag) != 0;
    }
}