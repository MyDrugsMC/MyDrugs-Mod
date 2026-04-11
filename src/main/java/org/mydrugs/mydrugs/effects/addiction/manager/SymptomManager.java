package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomThresholds;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.network.AddictionClientSnapshotPayload;
import org.mydrugs.mydrugs.effects.addiction.util.AddictionMath;

public final class SymptomManager {
    private static final ResourceLocation FRAGILITY_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "withdrawal_fragility");
    private static final ResourceLocation FATIGUE_SPEED_ID = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "withdrawal_fatigue_speed");

    private SymptomManager() {}

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

    public static AddictionClientSnapshotPayload buildSnapshot(ServerPlayer player, float globalSeverity) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugCategory dominant = AddictionManager.getDominantCategory(player);
        int flags = buildFlags(globalSeverity);
        int insomniaRemaining = (int) Math.max(0L, stats.sleepBlockedUntil - player.level().getGameTime());

        return new AddictionClientSnapshotPayload(
                globalSeverity,
                stats.stressLevel,
                dominant.name(),
                flags,
                insomniaRemaining
        );
    }

    public static void sync(ServerPlayer player, float globalSeverity) {
        AddictionClientSnapshotPayload payload = buildSnapshot(player, globalSeverity);
        PacketDistributor.sendToPlayer(player, payload);
    }
}