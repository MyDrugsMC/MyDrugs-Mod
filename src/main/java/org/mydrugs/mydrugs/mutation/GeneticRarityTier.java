package org.mydrugs.mydrugs.mutation;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Optional;

public enum GeneticRarityTier {
    COMMON(0.55F, "common"),
    UNCOMMON(0.75F, "uncommon"),
    RARE(1.00F, "rare"),
    DANGEROUS(1.20F, "dangerous"),
    MYTHIC(1.45F, "mythic");

    private final float multiplier;
    private final String serializedName;

    GeneticRarityTier(float multiplier, String serializedName) {
        this.multiplier = multiplier;
        this.serializedName = serializedName;
    }

    public float multiplier() {
        return this.multiplier;
    }

    public String serializedName() {
        return this.serializedName;
    }

    public String translationKey() {
        return "mutation.mydrugs.rarity." + this.serializedName;
    }

    public static GeneticRarityTier fromEntity(LivingEntity entity) {
        EntityType<?> type = entity.getType();

        if (type == EntityType.WITHER || type == EntityType.ENDER_DRAGON) {
            return MYTHIC;
        }

        if (type == EntityType.ELDER_GUARDIAN
                || type == EntityType.EVOKER
                || type == EntityType.RAVAGER
                || type == EntityType.WARDEN
                || type == EntityType.WITHER_SKELETON
                || type == EntityType.PIGLIN_BRUTE
                || type == EntityType.VINDICATOR) {
            return DANGEROUS;
        }

        if (type == EntityType.ENDERMAN
                || type == EntityType.BLAZE
                || type == EntityType.GUARDIAN
                || type == EntityType.PHANTOM
                || type == EntityType.SHULKER
                || type == EntityType.STRIDER
                || type == EntityType.ALLAY
                || type == EntityType.AXOLOTL
                || type == EntityType.GHAST) {
            return RARE;
        }

        if (type == EntityType.CREEPER
                || type == EntityType.WITCH
                || type == EntityType.SLIME
                || type == EntityType.WOLF
                || type == EntityType.FOX
                || type == EntityType.GOAT
                || type == EntityType.PANDA
                || type == EntityType.DOLPHIN
                || type == EntityType.BEE) {
            return UNCOMMON;
        }

        if (type == EntityType.ZOMBIE
                || type == EntityType.SKELETON
                || type == EntityType.SPIDER
                || type == EntityType.COW
                || type == EntityType.PIG
                || type == EntityType.SHEEP
                || type == EntityType.CHICKEN
                || type == EntityType.RABBIT
                || type == EntityType.BAT) {
            return COMMON;
        }

        return entity instanceof Enemy ? UNCOMMON : COMMON;
    }

    public static Optional<GeneticRarityTier> bySerializedName(String name) {
        return Optional.ofNullable(bySerializedNameOrNull(name));
    }

    public static @Nullable GeneticRarityTier bySerializedNameOrNull(String name) {
        if (name == null) {
            return null;
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (GeneticRarityTier tier : values()) {
            if (tier.serializedName.equals(normalized)) {
                return tier;
            }
        }
        return null;
    }
}
