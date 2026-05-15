package org.mydrugs.mydrugs.mutation;

import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum MutationStat {
    VISUAL_ACCURACY("visual"),
    HEALTH_STABILITY("health"),
    ADDICTION_RESISTANCE("addiction"),
    MENTAL_STRENGTH("mental"),
    PLEASURE_SENSITIVITY("pleasure"),
    WITHDRAWAL_RESILIENCE("physical"),
    METABOLIC_CONTROL("metabolism"),
    RITUAL_NEURAL_SYNC("ritual"),
    INFECTION_RESISTANCE("infection"),
    GENETIC_STABILITY("genetic");

    private final String category;

    MutationStat(String category) {
        this.category = category;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public String category() {
        return this.category;
    }

    public String translationKey() {
        return "mutation.mydrugs.stat." + serializedName();
    }

    public String descriptionKey() {
        return translationKey() + ".desc";
    }

    public static Optional<MutationStat> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable MutationStat bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (MutationStat stat : values()) {
            if (stat.serializedName().equals(normalized)) {
                return stat;
            }
        }
        return null;
    }
}
