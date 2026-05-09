package org.mydrugs.mydrugs.effects.addiction.manager.effect;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.network.DrugEffectSyncPayload;
import org.mydrugs.mydrugs.effects.addiction.network.VomitOverlayPayload;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class DrugEffectRuntimeManager {
    private static final Map<UUID, EnumMap<EffectType, ActiveDrugEffect>> ACTIVE = new HashMap<>();
    private static final Map<UUID, Integer> VOMIT_COOLDOWNS = new HashMap<>();
    private static final Map<UUID, Float> LAST_MOVEMENT_MULTIPLIER = new HashMap<>();
    private static final Map<UUID, Integer> LAST_SYNC_SIGNATURE = new HashMap<>();
    private static final Map<UUID, Long> LAST_ADRENALINE_TRIGGER = new HashMap<>();
    private static final Set<UUID> DIRTY_PLAYERS = new java.util.HashSet<>();
    private static final ResourceLocation MOVEMENT_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drug_effect_movement_speed");

    private DrugEffectRuntimeManager() {
    }

    public static void addEffect(ServerPlayer player, EffectType rawType, float intensity, int duration) {
        if (player == null || rawType == null || duration <= 0 || intensity <= 0.0F) {
            return;
        }

        EffectType type = normalize(rawType);
        EnumMap<EffectType, ActiveDrugEffect> effects = ACTIVE.computeIfAbsent(player.getUUID(), ignored -> new EnumMap<>(EffectType.class));
        ActiveDrugEffect existing = effects.get(type);
        if (existing == null) {
            effects.put(type, new ActiveDrugEffect(type, intensity, duration));
        } else {
            existing.merge(intensity, duration);
        }
        DIRTY_PLAYERS.add(player.getUUID());
    }

    public static float getServerIntensity(ServerPlayer player, EffectType type) {
        EnumMap<EffectType, ActiveDrugEffect> effects = ACTIVE.get(player.getUUID());
        if (effects == null) {
            return 0.0F;
        }
        ActiveDrugEffect effect = effects.get(normalize(type));
        return effect == null ? 0.0F : effect.intensity();
    }

    public static void tickServer(ServerPlayer player) {
        UUID id = player.getUUID();
        EnumMap<EffectType, ActiveDrugEffect> effects = ACTIVE.get(id);
        boolean dirty = false;

        if (effects != null) {
            Iterator<Map.Entry<EffectType, ActiveDrugEffect>> iterator = effects.entrySet().iterator();
            while (iterator.hasNext()) {
                ActiveDrugEffect effect = iterator.next().getValue();
                boolean wasFading = effect.isFading();
                if (effect.tick()) {
                    iterator.remove();
                    dirty = true;
                } else if (!wasFading && effect.isFading()) {
                    dirty = true;
                } else if (effect.isFading() && effect.fadeTicksRemaining() % 10 == 0) {
                    dirty = true;
                }
            }

            applyMovementAttribute(player, effects);
            maybeVomit(player, effects);

            if (effects.isEmpty()) {
                ACTIVE.remove(id);
                dirty = true;
            }
        } else {
            removeMovementAttribute(player);
        }

        if (VOMIT_COOLDOWNS.computeIfPresent(id, (ignored, value) -> value > 0 ? value - 1 : null) != null) {
            dirty = dirty || false;
        }

        boolean forceSync = dirty || DIRTY_PLAYERS.remove(id);
        if (forceSync || player.level().getGameTime() % 100L == 0L) {
            syncIfChanged(player, effects, forceSync);
        }
    }

    public static float getMiningSpeedMultiplier(ServerPlayer player) {
        float haste = getServerIntensity(player, EffectType.MINING_SPEED);
        float precision = getServerIntensity(player, EffectType.PRECISION);
        float adrenaline = getServerIntensity(player, EffectType.ADRENALINE_SURGE);
        return getMiningSpeedMultiplier(haste, precision, adrenaline);
    }

    public static float getMiningSpeedMultiplier(float haste, float precision, float adrenaline) {
        return 1.0F + Math.min(2.5F, Math.max(0.0F, haste + precision * 0.55F + adrenaline * 0.65F));
    }

    public static float getDamageResistance(ServerPlayer player) {
        return Math.min(0.80F, Math.max(0.0F, getServerIntensity(player, EffectType.DAMAGE_RESISTANCE)));
    }

    public static float getAttackDamageMultiplier(ServerPlayer player) {
        float damage = getServerIntensity(player, EffectType.ATTACK_DAMAGE);
        float adrenaline = getServerIntensity(player, EffectType.ADRENALINE_SURGE);
        return 1.0F + Math.min(1.5F, Math.max(0.0F, damage + adrenaline * 0.45F));
    }

    public static void triggerStimulantAdrenaline(ServerPlayer player, float damageTaken) {
        EnumMap<EffectType, ActiveDrugEffect> effects = ACTIVE.get(player.getUUID());
        if (effects == null || effects.isEmpty() || damageTaken <= 0.0F) {
            return;
        }

        float stimulant = Math.max(intensity(effects, EffectType.VOID_PULSE) * 0.45F,
                intensity(effects, EffectType.MANUAL_WORK_SPEED) - 0.30F);
        stimulant = Math.max(stimulant, intensity(effects, EffectType.HEARTBEAT) * 0.20F);
        if (stimulant <= 0.08F) {
            return;
        }

        long now = player.level().getGameTime();
        long previous = LAST_ADRENALINE_TRIGGER.getOrDefault(player.getUUID(), -200L);
        if (now - previous < 30L) {
            return;
        }

        LAST_ADRENALINE_TRIGGER.put(player.getUUID(), now);
        float intensity = Math.min(1.6F, 0.25F + stimulant * 0.55F + Math.min(8.0F, damageTaken) * 0.06F);
        addEffect(player, EffectType.ADRENALINE_SURGE, intensity, 20 * 12);
    }

    private static EffectType normalize(EffectType type) {
        return switch (type) {
            case NAUSEA -> EffectType.CUSTOM_NAUSEA;
            case SLOWNESS -> EffectType.MOVEMENT_SLOWDOWN;
            case LOW_LIGHT_VISION, BRIGHTNESS_BOOST -> EffectType.GAMMA_BOOST;
            default -> type;
        };
    }

    private static void applyMovementAttribute(ServerPlayer player, EnumMap<EffectType, ActiveDrugEffect> effects) {
        float boost = intensity(effects, EffectType.MOVEMENT_SPEED);
        float slow = intensity(effects, EffectType.MOVEMENT_SLOWDOWN);
        float adrenaline = intensity(effects, EffectType.ADRENALINE_SURGE);
        float multiplier = Math.max(0.05F, 1.0F + boost + adrenaline * 0.12F - slow);
        float previous = LAST_MOVEMENT_MULTIPLIER.getOrDefault(player.getUUID(), 1.0F);

        if (Math.abs(multiplier - previous) < 0.005F) {
            return;
        }

        removeMovementAttribute(player);
        if (Math.abs(multiplier - 1.0F) > 0.005F) {
            var instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (instance != null) {
                instance.addOrUpdateTransientModifier(new AttributeModifier(
                        MOVEMENT_MODIFIER_ID,
                        multiplier - 1.0F,
                        AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
                ));
            }
        }
        LAST_MOVEMENT_MULTIPLIER.put(player.getUUID(), multiplier);
    }

    private static void removeMovementAttribute(ServerPlayer player) {
        var instance = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null) {
            instance.removeModifier(MOVEMENT_MODIFIER_ID);
        }
        LAST_MOVEMENT_MULTIPLIER.remove(player.getUUID());
    }

    private static void maybeVomit(ServerPlayer player, EnumMap<EffectType, ActiveDrugEffect> effects) {
        float vomit = intensity(effects, EffectType.VOMIT);
        if (vomit <= 0.0F || VOMIT_COOLDOWNS.getOrDefault(player.getUUID(), 0) > 0) {
            return;
        }

        float chance = Math.min(0.02F, vomit * 0.004F);
        if (player.getRandom().nextFloat() >= chance) {
            return;
        }

        VOMIT_COOLDOWNS.put(player.getUUID(), 20 * 25);
        player.setSprinting(false);
        FoodData foodData = player.getFoodData();
        foodData.addExhaustion(3.0F + vomit * 2.0F);
        if (vomit > 0.75F) {
            player.hurt(player.damageSources().magic(), Math.min(2.0F, vomit));
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.8F, 0.6F);
        if (player.level() instanceof ServerLevel serverLevel) {
            placeVomitSplash(player, serverLevel);
            Vec3 look = player.getLookAngle();
            serverLevel.sendParticles(
                    ParticleTypes.SPLASH,
                    player.getX() + look.x * 0.65D,
                    player.getY() + 1.35D + look.y * 0.25D,
                    player.getZ() + look.z * 0.65D,
                    10,
                    0.12D, 0.08D, 0.12D,
                    0.03D
            );
        }
        PacketDistributor.sendToPlayer(player, new VomitOverlayPayload(vomit));
        addEffect(player, EffectType.BLUR, Math.max(0.15F, vomit * 0.35F), 60);
        addEffect(player, EffectType.CAMERA_SWAY, Math.max(0.10F, vomit * 0.25F), 50);
    }

    private static void placeVomitSplash(ServerPlayer player, ServerLevel level) {
        Direction forward = player.getDirection();
        BlockPos feet = player.blockPosition();
        BlockPos[] candidates = {
                feet.relative(forward),
                feet,
                feet.relative(forward.getClockWise()),
                feet.relative(forward.getCounterClockWise()),
                feet.relative(forward).relative(forward.getClockWise()),
                feet.relative(forward).relative(forward.getCounterClockWise())
        };

        for (BlockPos candidate : candidates) {
            if (tryPlaceVomitSplash(level, candidate)) {
                return;
            }
        }
    }

    private static boolean tryPlaceVomitSplash(ServerLevel level, BlockPos pos) {
        BlockState current = level.getBlockState(pos);
        if (current.is(ModBlocks.VOMIT_SPLASH.get()) || !current.canBeReplaced()) {
            return false;
        }

        BlockState splash = ModBlocks.VOMIT_SPLASH.get().defaultBlockState();
        if (!splash.canSurvive(level, pos)) {
            return false;
        }

        return level.setBlock(pos, splash, Block.UPDATE_ALL);
    }

    private static float intensity(EnumMap<EffectType, ActiveDrugEffect> effects, EffectType type) {
        ActiveDrugEffect effect = effects.get(type);
        return effect == null ? 0.0F : effect.intensity();
    }

    private static void syncIfChanged(ServerPlayer player, Map<EffectType, ActiveDrugEffect> effects, boolean force) {
        UUID id = player.getUUID();
        int signature = syncSignature(effects);
        Integer previous = LAST_SYNC_SIGNATURE.get(id);
        if (!force && previous != null && previous == signature) {
            return;
        }

        if (signature == 0) {
            LAST_SYNC_SIGNATURE.remove(id);
        } else {
            LAST_SYNC_SIGNATURE.put(id, signature);
        }
        sync(player, effects);
    }

    private static int syncSignature(Map<EffectType, ActiveDrugEffect> effects) {
        if (effects == null || effects.isEmpty()) {
            return 0;
        }

        int hash = 1;
        for (ActiveDrugEffect effect : effects.values()) {
            if (effect.remainingTicks() <= 0 || effect.intensity() <= 0.0F) {
                continue;
            }
            hash = 31 * hash + effect.type().serializedName().hashCode();
            hash = 31 * hash + Math.round(effect.intensity() * 100.0F);
            hash = 31 * hash + Math.max(1, effect.remainingTicks() / 20);
            hash = 31 * hash + Math.max(0, effect.fadeTicksRemaining() / 10);
        }
        return hash;
    }

    private static void sync(ServerPlayer player, Map<EffectType, ActiveDrugEffect> effects) {
        List<DrugEffectSyncPayload.Entry> entries = new ArrayList<>();
        if (effects != null) {
            for (ActiveDrugEffect effect : effects.values()) {
                if (effect.remainingTicks() > 0 && effect.intensity() > 0.0F) {
                    entries.add(new DrugEffectSyncPayload.Entry(
                            effect.type(),
                            effect.baseIntensity(),
                            effect.remainingTicks(),
                            effect.fadeTicksRemaining(),
                            effect.fadeDurationTicks()
                    ));
                }
            }
        }
        PacketDistributor.sendToPlayer(player, new DrugEffectSyncPayload(entries));
    }
}
