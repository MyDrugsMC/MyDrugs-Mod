package org.mydrugs.mydrugs.effects.addiction.manager.state;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomThresholds;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.TemporaryRecoveryEffects;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class SymptomManager {
    private static final ResourceLocation FRAGILITY_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "withdrawal_fragility");
    private static final ResourceLocation FATIGUE_SPEED_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "withdrawal_fatigue_speed");

    private SymptomManager() {
    }

    public static int buildFlags(float severity) {
        int flags = 0;

        if (severity >= SymptomThresholds.CONFUSION) flags |= SymptomFlags.CONFUSION;
        if (severity >= SymptomThresholds.FRAGILITY) flags |= SymptomFlags.FRAGILITY;
        if (severity >= SymptomThresholds.VISION) flags |= SymptomFlags.VISION;
        if (severity >= SymptomThresholds.HALLUCINATION) flags |= SymptomFlags.HALLUCINATION;
        if (severity >= SymptomThresholds.STRESS) flags |= SymptomFlags.STRESS;
        if (severity >= SymptomThresholds.DISSOCIATION) flags |= SymptomFlags.DISSOCIATION;
        if (severity >= SymptomThresholds.FATIGUE) flags |= SymptomFlags.FATIGUE;
        if (severity >= SymptomThresholds.INTRUSIVE_THOUGHTS) flags |= SymptomFlags.INTRUSIVE_THOUGHTS;
        if (severity >= SymptomThresholds.INSOMNIA) flags |= SymptomFlags.INSOMNIA;

        return flags;
    }

    public static void applyServerSymptoms(ServerPlayer player, float severity) {
        LivingEntity living = player;
        AttributeInstance maxHealth = living.getAttribute(Attributes.MAX_HEALTH);
        AttributeInstance moveSpeed = living.getAttribute(Attributes.MOVEMENT_SPEED);

        if (maxHealth != null) {
            maxHealth.removeModifier(FRAGILITY_ID);
        }

        if (moveSpeed != null) {
            moveSpeed.removeModifier(FATIGUE_SPEED_ID);
        }

        if (severity >= SymptomThresholds.FRAGILITY && maxHealth != null) {
            double penalty = -Math.floor(2.0 + AddictionMath.mapRange(severity, 0.35F, 1.0F, 0.0F, 6.0F));
            maxHealth.addPermanentModifier(new AttributeModifier(FRAGILITY_ID, penalty, AttributeModifier.Operation.ADD_VALUE));
            if (player.getHealth() > player.getMaxHealth()) {
                player.setHealth(player.getMaxHealth());
            }
        }

        if (severity >= SymptomThresholds.FATIGUE && moveSpeed != null) {
            double amount = -AddictionMath.mapRange(severity, 0.30F, 1.0F, 0.05F, 0.20F);
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
        int flags = buildFlags(globalSeverity);
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
