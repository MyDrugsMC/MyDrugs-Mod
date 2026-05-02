package org.mydrugs.mydrugs.effects.addiction.manager.recovery;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.worldgen.ModVillagerProfessions;

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
            player.displayClientMessage(Component.translatable("message.mydrugs.therapy.already_today"), true);
            return false;
        }

        stats.lastTherapyDay = day;

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 12.0F);
            stats.reduceAddictionInCategory(category, 6.0F);
        }

        StressManager.reduce(stats, AddictionConstants.RELIEF_THERAPIST);
        ResilienceManager.onTherapy(stats);

        player.displayClientMessage(Component.translatable("message.mydrugs.therapy.success"), true);
        AdvancementEventHooks.recoveryAction(player, "therapy");
        return true;
    }
}
