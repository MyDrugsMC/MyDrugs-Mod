package org.mydrugs.mydrugs.recovery;

import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.npc.Villager;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.state.ResilienceManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;
import org.mydrugs.mydrugs.diary.DiaryEntry;
import org.mydrugs.mydrugs.diary.DiaryEntryType;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.worldgen.ModVillagerProfessions;

public final class TherapistHandler {
    private static final int DESK_SEARCH_RADIUS = 6;

    private TherapistHandler() {
    }

    public static boolean isTherapist(Villager villager) {
        return villager.getVillagerData().profession().value() == ModVillagerProfessions.THERAPIST.get();
    }

    public static boolean hasUsedTherapyToday(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        return stats.lastTherapyDay == therapyDay(player);
    }

    public static boolean tryUseTherapist(ServerPlayer player, Villager villager) {
        if (!isTherapist(villager)) return false;

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long day = therapyDay(player);

        if (stats.lastTherapyDay == day) {
            player.displayClientMessage(Component.translatable("message.mydrugs.therapy.already_today"), true);
            return false;
        }

        stats.lastTherapyDay = day;
        boolean deskBoost = hasNearbyTherapistDesk(villager);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 12.0F);
            stats.reduceAddictionInCategory(category, 6.0F);
        }

        StressManager.reduce(stats, AddictionConstants.RELIEF_THERAPIST);
        ResilienceManager.onTherapy(stats);
        if (deskBoost) {
            StressManager.reduce(stats, 0.015F);
        }

        player.displayClientMessage(Component.translatable("message.mydrugs.therapy.success"), true);
        if (deskBoost) {
            player.displayClientMessage(Component.translatable("message.mydrugs.therapy.desk.boost"), true);
        }
        AdvancementEventHooks.recoveryAction(player, "therapy");
        RecoveryProgressManager.onProductiveAction(player, ActionKind.THERAPY_SESSION, deskBoost ? 1.15F : 1.0F);
        RecoverySessionManager.onReflectionAction(player, RecoverySessionAction.THERAPY);
        appendTherapyDiaryEntry(player);
        return true;
    }

    private static long therapyDay(ServerPlayer player) {
        return player.level().getDayTime() / 24000L;
    }

    private static boolean hasNearbyTherapistDesk(Villager villager) {
        BlockPos center = villager.blockPosition();
        for (BlockPos mutable : BlockPos.betweenClosed(
                center.offset(-DESK_SEARCH_RADIUS, -2, -DESK_SEARCH_RADIUS),
                center.offset(DESK_SEARCH_RADIUS, 2, DESK_SEARCH_RADIUS)
        )) {
            BlockPos pos = mutable.immutable();
            if (pos.closerThan(center, DESK_SEARCH_RADIUS + 0.5D)
                    && villager.level().getBlockState(pos).is(ModBlocks.THERAPIST_DESK.get())) {
                return true;
            }
        }
        return false;
    }

    private static void appendTherapyDiaryEntry(ServerPlayer player) {
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        long gameTime = player.level().getGameTime();
        long day = PlayerDiaryAttachment.currentDay(gameTime);
        for (DiaryEntry entry : diary.getEntries()) {
            if (entry.day() == day && "therapy".equals(entry.sourceKey())) {
                return;
            }
        }

        String content = PlayerDiaryAttachment.sanitizeCustomContent(
                "Therapy helped name the pressure instead of obeying it."
        );
        if (content == null) {
            return;
        }
        diary.append(new DiaryEntry(day, gameTime, DiaryEntryType.AUTO, content, "therapy", ""));
    }
}
