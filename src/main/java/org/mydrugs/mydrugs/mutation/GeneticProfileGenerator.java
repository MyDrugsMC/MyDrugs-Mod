package org.mydrugs.mydrugs.mutation;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.mydrugs.mydrugs.items.data.AdnScrapData;
import org.mydrugs.mydrugs.items.data.MutationStatValue;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

public final class GeneticProfileGenerator {
    private static final String SOURCE_PLAYER = "player";
    private static final String SOURCE_ENTITY = "entity";
    private static final String GENERATION_VERSION = "adn_v1";

    private GeneticProfileGenerator() {
    }

    public static AdnScrapData fromEntity(LivingEntity entity) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        String sourceKind = entity instanceof Player ? SOURCE_PLAYER : SOURCE_ENTITY;
        GeneticRarityTier tier = tierForProfile(entity);
        List<MutationStatValue> stats = new ArrayList<>();

        for (MutationStat stat : selectedStats(entity, typeId, sourceKind, tier)) {
            float u = deterministicFloat(entity.getStringUUID(), typeId.toString(), sourceKind, stat.serializedName());

            // Long-tail inverse rarity curve: common rolls cluster near 1-3%, while strong values need rare high-tail rolls.
            double base = Math.pow(1.0D - u, rarityExponent(tier));
            float speciesMultiplier = speciesMultiplier(entity, stat);
            float raw = (float) (base * 0.85D * tier.multiplier() * speciesMultiplier);
            float value = clampValue(0.01F + raw);
            float improbability = Mth.clamp(raw, 0.0F, 1.0F);
            stats.add(new MutationStatValue(stat.serializedName(), value, improbability));
        }

        return new AdnScrapData(
                entity.getStringUUID(),
                typeId.toString(),
                entity.getName().getString(),
                sourceKind,
                geneticSignature(entity.getStringUUID(), typeId.toString(), sourceKind),
                tier.serializedName(),
                stats
        );
    }

    public static AdnScrapData fromPlayerMutations(Player player, List<MutationStatValue> stats) {
        ResourceLocation typeId = BuiltInRegistries.ENTITY_TYPE.getKey(player.getType());
        return new AdnScrapData(
                player.getStringUUID(),
                typeId.toString(),
                player.getName().getString(),
                SOURCE_PLAYER,
                playerMutationSignature(player.getStringUUID(), stats),
                GeneticRarityTier.UNCOMMON.serializedName(),
                stats
        );
    }

    public static float clampValue(float value) {
        return Mth.clamp(value, 0.01F, 1.0F);
    }

    private static GeneticRarityTier tierForProfile(LivingEntity entity) {
        if (entity instanceof Player) {
            return GeneticRarityTier.UNCOMMON;
        }
        return GeneticRarityTier.fromEntity(entity);
    }

    private static List<MutationStat> selectedStats(
            LivingEntity entity,
            ResourceLocation typeId,
            String sourceKind,
            GeneticRarityTier tier
    ) {
        int count = statCount(entity, typeId, sourceKind, tier);
        List<MutationStat> stats = new ArrayList<>(List.of(MutationStat.values()));
        stats.sort(Comparator
                .comparingDouble((MutationStat stat) -> selectionScore(entity, typeId, sourceKind, stat))
                .reversed()
                .thenComparing(MutationStat::serializedName));
        return List.copyOf(stats.subList(0, Math.min(count, stats.size())));
    }

    private static int statCount(LivingEntity entity, ResourceLocation typeId, String sourceKind, GeneticRarityTier tier) {
        int min;
        int max;
        if (entity instanceof Player) {
            min = 4;
            max = 4;
        } else {
            switch (tier) {
                case COMMON -> {
                    min = 2;
                    max = 3;
                }
                case UNCOMMON -> {
                    min = 3;
                    max = 4;
                }
                case RARE -> {
                    min = 4;
                    max = 5;
                }
                case DANGEROUS -> {
                    min = 5;
                    max = 6;
                }
                case MYTHIC -> {
                    min = 7;
                    max = 8;
                }
                default -> {
                    min = 2;
                    max = 3;
                }
            }
        }

        if (min == max) {
            return min;
        }
        float roll = deterministicFloat(entity.getStringUUID(), typeId.toString(), sourceKind, "stat_count");
        return min + Math.min(max - min, (int) Math.floor(roll * (max - min + 1)));
    }

    private static double selectionScore(LivingEntity entity, ResourceLocation typeId, String sourceKind, MutationStat stat) {
        float roll = deterministicFloat(entity.getStringUUID(), typeId.toString(), sourceKind, "select_" + stat.serializedName());
        return roll * speciesMultiplier(entity, stat);
    }

    private static double rarityExponent(GeneticRarityTier tier) {
        return switch (tier) {
            case COMMON -> 36.0D;
            case UNCOMMON -> 28.0D;
            case RARE -> 20.0D;
            case DANGEROUS -> 14.0D;
            case MYTHIC -> 9.0D;
        };
    }

    private static float speciesMultiplier(LivingEntity entity, MutationStat stat) {
        EntityType<?> type = entity.getType();

        if (entity instanceof Player && stat == MutationStat.GENETIC_STABILITY) {
            return 1.12F;
        }

        if (type == EntityType.ENDERMAN) {
            return switch (stat) {
                case MENTAL_STRENGTH, VISUAL_ACCURACY, METABOLIC_CONTROL -> 1.18F;
                default -> 1.0F;
            };
        }
        if (type == EntityType.BLAZE) {
            return switch (stat) {
                case METABOLIC_CONTROL, HEALTH_STABILITY -> 1.16F;
                default -> 1.0F;
            };
        }
        if (type == EntityType.WARDEN) {
            return switch (stat) {
                case MENTAL_STRENGTH, HEALTH_STABILITY, VISUAL_ACCURACY -> 1.20F;
                default -> 1.0F;
            };
        }
        if (type == EntityType.WITCH) {
            return switch (stat) {
                case ADDICTION_RESISTANCE, METABOLIC_CONTROL -> 1.18F;
                default -> 1.0F;
            };
        }
        if (type == EntityType.SHULKER) {
            return switch (stat) {
                case GENETIC_STABILITY, HEALTH_STABILITY -> 1.18F;
                default -> 1.0F;
            };
        }

        return 1.0F;
    }

    private static String geneticSignature(String uuid, String entityType, String sourceKind) {
        byte[] hash = sha256(GENERATION_VERSION, uuid, entityType, sourceKind);
        return HexFormat.of().formatHex(hash, 0, 4).toUpperCase(Locale.ROOT);
    }

    private static String playerMutationSignature(String uuid, List<MutationStatValue> stats) {
        List<String> parts = new ArrayList<>();
        parts.add("player_body_v1");
        parts.add(uuid);
        stats.stream()
                .sorted(Comparator.comparing(MutationStatValue::statId))
                .forEach(stat -> parts.add(stat.statId() + "=" + Math.round(stat.value() * 1000.0F)));
        byte[] hash = sha256(parts.toArray(String[]::new));
        return HexFormat.of().formatHex(hash, 0, 4).toUpperCase(Locale.ROOT);
    }

    private static float deterministicFloat(String uuid, String entityType, String sourceKind, String statId) {
        byte[] hash = sha256(GENERATION_VERSION, uuid, entityType, sourceKind, statId);
        long bits = ByteBuffer.wrap(hash, 0, Long.BYTES).getLong() & Long.MAX_VALUE;
        return (bits + 0.5F) / (float) Long.MAX_VALUE;
    }

    private static byte[] sha256(String... parts) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            for (String part : parts) {
                digest.update(part.getBytes(StandardCharsets.UTF_8));
                digest.update((byte) 0);
            }
            return digest.digest();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is required for ADN generation", exception);
        }
    }
}
