package org.mydrugs.mydrugs.effects.addiction.manager.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomThresholds;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.TemporaryRecoveryEffects;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.mutation.MutationStat;

public final class SymptomManager {
    private static final ResourceLocation FATIGUE_SPEED_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "withdrawal_fatigue_speed");

    private SymptomManager() {
    }

    public static int buildFlags(float severity) {
        return buildFlags(severity, severity);
    }

    public static int buildFlags(float mentalSeverity, float physicalSeverity) {
        int flags = 0;

        if (mentalSeverity >= SymptomThresholds.CONFUSION) flags |= SymptomFlags.CONFUSION;
        if (physicalSeverity >= SymptomThresholds.FRAGILITY) flags |= SymptomFlags.FRAGILITY;
        if (mentalSeverity >= SymptomThresholds.VISION) flags |= SymptomFlags.VISION;
        if (mentalSeverity >= SymptomThresholds.HALLUCINATION) flags |= SymptomFlags.HALLUCINATION;
        if (mentalSeverity >= SymptomThresholds.STRESS) flags |= SymptomFlags.STRESS;
        if (mentalSeverity >= SymptomThresholds.DISSOCIATION) flags |= SymptomFlags.DISSOCIATION;
        if (physicalSeverity >= SymptomThresholds.FATIGUE) flags |= SymptomFlags.FATIGUE;
        if (mentalSeverity >= SymptomThresholds.INTRUSIVE_THOUGHTS) flags |= SymptomFlags.INTRUSIVE_THOUGHTS;
        if (physicalSeverity >= SymptomThresholds.INSOMNIA) flags |= SymptomFlags.INSOMNIA;

        return flags;
    }

    public static void applyServerSymptoms(ServerPlayer player, float severity) {
        LivingEntity living = player;
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance moveSpeed = living.getAttribute(Attributes.MOVEMENT_SPEED);

        float mentalStrength = MutationManager.getValue(player, MutationStat.MENTAL_STRENGTH);
        float withdrawalResilience = MutationManager.getValue(player, MutationStat.WITHDRAWAL_RESILIENCE);
        float healthStability = MutationManager.getValue(player, MutationStat.HEALTH_STABILITY);
        float mentalSeverity = severity * Math.max(0.0F, 1.0F - mentalStrength);
        float physicalSeverity = severity * Math.max(0.0F, 1.0F - withdrawalResilience);

        if (moveSpeed != null) {
            moveSpeed.removeModifier(FATIGUE_SPEED_ID);
        }

        if (physicalSeverity >= SymptomThresholds.FRAGILITY && maxHealth != null) {
            float healthPoints = (float) Math.floor(2.0 + AddictionMath.mapRange(physicalSeverity, 0.35F, 1.0F, 0.0F, 6.0F));
            float hpDecrease = (healthPoints / 2.0F) * Math.max(0.0F, 1.0F - healthStability);
            if (hpDecrease > 0.0F) {
                DrugEffectRuntimeManager.addEffect(player, EffectType.HP_DECREASE, hpDecrease, 45);
            }
        }

        if (physicalSeverity >= SymptomThresholds.FATIGUE && moveSpeed != null) {
            double amount = -AddictionMath.mapRange(physicalSeverity, 0.30F, 1.0F, 0.05F, 0.20F);
            moveSpeed.addPermanentModifier(new AttributeModifier(FATIGUE_SPEED_ID, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }
    }

    public static AddictionClientSnapshotPayload buildSnapshot(ServerPlayer player,
                                                               float globalSeverity,
                                                               boolean inSafeZone) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugId dominantDrugId = AddictionManager.getDominantDrugId(player);
        DrugCategory dominantCategory = AddictionManager.getDominantCategory(player);
        TemporaryRecoveryEffects effects = stats.temporaryEffects;
        long now = player.level().getGameTime();

        int insomniaRemaining = (int) Math.max(0L, stats.sleepBlockedUntil - now);
        float mentalStrength = MutationManager.getValue(player, MutationStat.MENTAL_STRENGTH);
        float withdrawalResilience = MutationManager.getValue(player, MutationStat.WITHDRAWAL_RESILIENCE);
        float mentalSeverity = globalSeverity * Math.max(0.0F, 1.0F - mentalStrength);
        float physicalSeverity = globalSeverity * Math.max(0.0F, 1.0F - withdrawalResilience);
        int flags = buildFlags(mentalSeverity, physicalSeverity);
        flags |= BadTripManager.symptomFlags(stats);
        if (effects.hasSleepBonus(now)) {
            flags &= ~SymptomFlags.INSOMNIA;
        }

        int recoveryFlags = 0;
        if (inSafeZone) recoveryFlags |= AddictionClientSnapshotPayload.RECOVERY_SAFE_ZONE;
        if (effects.hasDiaryCalm(now)) recoveryFlags |= AddictionClientSnapshotPayload.RECOVERY_DIARY;
        if (effects.hasCalmingMixture(now)) recoveryFlags |= AddictionClientSnapshotPayload.RECOVERY_CALMING_MIXTURE;
        if (effects.hasHeadphones(now)) recoveryFlags |= AddictionClientSnapshotPayload.RECOVERY_HEADPHONES;
        if (effects.hasSleepBonus(now)) recoveryFlags |= AddictionClientSnapshotPayload.RECOVERY_SLEEP_BONUS;

        int overdoseTicksRemaining = Math.max(0, stats.overdoseDeathTimer);

        return new AddictionClientSnapshotPayload(
                globalSeverity,
                stats.stressLevel,
                dominantDrugId != null ? dominantDrugId.name() : "",
                dominantCategory.name(),
                flags,
                insomniaRemaining,
                recoveryFlags,
                overdoseTicksRemaining
        );
    }

    public static void sync(ServerPlayer player, float globalSeverity, boolean inSafeZone) {
        AddictionClientSnapshotPayload payload = buildSnapshot(player, globalSeverity, inSafeZone);
        PacketDistributor.sendToPlayer(player, payload);
    }
}
