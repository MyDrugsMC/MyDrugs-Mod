package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.registry.ModVillagerProfessions;

public final class TherapistHandler {
    private TherapistHandler() {
    }

    public static boolean isTherapist(Villager villager) {
        return villager.getVillagerData().profession().value() == ModVillagerProfessions.THERAPIST.get();
    }

    public static boolean tryUseTherapist(ServerPlayer player, Villager villager) {
        if (!isTherapist(villager)) return false;

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long day = player.level().getDayTime() / 24000L;

        if (stats.lastTherapyDay == day) {
            player.displayClientMessage(Component.literal("You already had therapy today."), true);
            return false;
        }

        stats.lastTherapyDay = day;

        for (DrugCategory category : DrugCategory.values()) {
            stats.get(category).baseWithdrawalMeter = Math.max(0.0F, stats.get(category).baseWithdrawalMeter - 12.0F);
            stats.get(category).addictionValue = Math.max(0.0F, stats.get(category).addictionValue - 6.0F);
        }

        StressManager.reduce(stats, 0.15F);
        ResilienceManager.onTherapy(stats);

        player.displayClientMessage(Component.literal("You feel slightly more grounded."), true);
        return true;
    }
}