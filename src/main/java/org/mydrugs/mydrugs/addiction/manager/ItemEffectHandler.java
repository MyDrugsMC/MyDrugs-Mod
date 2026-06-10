package org.mydrugs.mydrugs.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.explain.AddictionRecoveryFeedback;
import org.mydrugs.mydrugs.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.recovery.PlayerRecoveryEnvironmentCache;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;
import org.mydrugs.mydrugs.recovery.RecoverySessionAction;
import org.mydrugs.mydrugs.recovery.RecoverySessionManager;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.addiction.manager.state.SymptomManager;
import org.mydrugs.mydrugs.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.core.drug.integration.IntegrationService;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.recovery.SanctuaryModule;

public final class ItemEffectHandler {
    private static final long HEADPHONES_REFRESH_TICKS = 5L;

    private ItemEffectHandler() {
    }

    public static void applyDiary(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        boolean diaryDesk = hasModule(room, SanctuaryModule.DIARY_DESK);

        stats.temporaryEffectsView().applyDiary(
                now + (20L * (diaryDesk ? 120L : 90L)),
                now + (20L * (diaryDesk ? 90L : 60L))
        );
        StressManager.reduce(stats, AddictionConstants.RELIEF_DIARY);
        if (diaryDesk) {
            StressManager.reduce(stats, 0.015F);
        }
        RecoveryProgressManager.onProductiveAction(player, ActionKind.DIARY_WRITTEN, diaryDesk ? 1.15F : 1.0F);
        RecoverySessionManager.onReflectionAction(player, RecoverySessionAction.DIARY);
        IntegrationService.onReflectionAction(player);
        syncClientHud(player);
    }

    public static void applyHeadphones(ServerPlayer player, int durationTicks) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        stats.temporaryEffectsView().extendHeadphonesUntil(player.level().getGameTime() + durationTicks);
        syncClientHud(player);
    }

    public static boolean toggleHeadphones(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            stats.temporaryEffectsView().disableHeadphones();
            syncHeadphones(player);
            syncClientHud(player);
            return false;
        }

        boolean enabled = stats.temporaryEffectsView().toggleHeadphones(
                player.level().getGameTime() + HEADPHONES_REFRESH_TICKS
        );

        syncHeadphones(player);
        syncClientHud(player);
        if (enabled) {
            if (!RecoverySessionManager.onGroundingAction(player, RecoverySessionAction.HEADPHONES)) {
                AddictionRecoveryFeedback.sendHeadphones(player);
            }
        }
        return enabled;
    }

    public static boolean cycleHeadphonesTrack(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            stats.temporaryEffectsView().disableHeadphones();
            syncHeadphones(player);
            syncClientHud(player);
            return false;
        }

        stats.temporaryEffectsView().advanceHeadphonesTrack();
        syncHeadphones(player);
        return true;
    }

    public static boolean setHeadphonesPlaying(ServerPlayer player, boolean playing) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            stats.temporaryEffectsView().disableHeadphones();
            syncHeadphones(player);
            syncClientHud(player);
            return false;
        }

        stats.temporaryEffectsView().setHeadphonesPlaying(
                playing,
                player.level().getGameTime() + HEADPHONES_REFRESH_TICKS
        );
        syncHeadphones(player);
        syncClientHud(player);
        if (playing) {
            if (!RecoverySessionManager.onGroundingAction(player, RecoverySessionAction.HEADPHONES)) {
                AddictionRecoveryFeedback.sendHeadphones(player);
            }
        }
        return playing;
    }

    public static void tickHeadphones(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!PlayerRecoveryEnvironmentCache.snapshot(player).hasHeadphones()) {
            if (stats.temporaryEffectsView().headphonesEnabled()
                    || stats.temporaryEffectsView().headphonesUntil() > 0L) {
                stats.temporaryEffectsView().disableHeadphones();
                syncHeadphones(player);
                syncClientHud(player);
            }
            return;
        }

        if (stats.temporaryEffectsView().headphonesEnabled()) {
            stats.temporaryEffectsView().extendHeadphonesUntil(
                    player.level().getGameTime() + HEADPHONES_REFRESH_TICKS
            );
        }
    }
    private static void syncHeadphones(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        PacketDistributor.sendToPlayer(
                player,
                new HeadphonesStatePayload(
                        stats.temporaryEffectsView().headphonesEnabled(),
                        "",
                        stats.temporaryEffectsView().headphonesTrackNonce(),
                        -1.0F,
                        false,
                        false
                )
        );
    }

    private static boolean hasItem(Inventory inventory, Item item) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    public static void applyHerbalTea(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        float itemMultiplier = recoveryItemMultiplier(room);
        boolean teaKitchen = hasModule(room, SanctuaryModule.TEA_KITCHEN);
        if (teaKitchen) {
            itemMultiplier *= 1.10F;
        }
        StressManager.reduce(stats, AddictionConstants.RELIEF_HERBAL_TEA * itemMultiplier * 1.35F);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 10.0F * itemMultiplier);
        }

        stats.temporaryEffectsView().setSleepBonusUntil(
                player.level().getGameTime() + Math.round(20L * 120L * itemMultiplier)
        );
        if (teaKitchen) {
            long preparedUntil = player.level().getGameTime() + 20L * 240L;
            stats.temporaryEffectsView().extendPreparedTeaUntil(preparedUntil);
        }
        RecoveryProgressManager.onProductiveAction(player, ActionKind.HERBAL_TEA, itemMultiplier);
        RecoverySessionAction sessionAction = teaKitchen
                ? RecoverySessionAction.PREPARED_TEA
                : RecoverySessionAction.TEA;
        if (teaKitchen && !RecoverySessionManager.onGroundingAction(player, sessionAction)) {
            RecoverySessionManager.sendContextualMessage(
                    player,
                    RecoverySessionAction.PREPARED_TEA.messageKey(),
                    20L * 60L
            );
        } else if (!teaKitchen) {
            RecoverySessionManager.onGroundingAction(player, sessionAction);
        }
        syncClientHud(player);
    }

    public static void applyCalmingMixture(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        float itemMultiplier = recoveryItemMultiplier(room);
        boolean teaKitchen = hasModule(room, SanctuaryModule.TEA_KITCHEN);
        StressManager.reduce(stats, AddictionConstants.RELIEF_CALMING_MIXTURE * itemMultiplier * 2.5F);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 30.0F * itemMultiplier);
        }

        stats.temporaryEffectsView().setCalmingMixtureUntil(
                player.level().getGameTime() + Math.round(20L * 60L * itemMultiplier)
        );
        stats.temporaryEffectsView().setSleepBonusUntil(
                player.level().getGameTime() + Math.round(20L * 180L * itemMultiplier)
        );
        RecoveryProgressManager.onProductiveAction(player, ActionKind.CALMING_MIXTURE, itemMultiplier);
        RecoverySessionAction sessionAction = teaKitchen
                ? RecoverySessionAction.PREPARED_TEA
                : RecoverySessionAction.TEA;
        if (teaKitchen && !RecoverySessionManager.onGroundingAction(player, sessionAction)) {
            RecoverySessionManager.sendContextualMessage(
                    player,
                    RecoverySessionAction.PREPARED_TEA.messageKey(),
                    20L * 60L
            );
        } else if (!teaKitchen) {
            RecoverySessionManager.onGroundingAction(player, sessionAction);
        }
        syncClientHud(player);
    }

    public static void applySleepingAid(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        stats.sleepBlockedUntil = 0L;
        stats.temporaryEffectsView().setSleepBonusUntil(player.level().getGameTime() + 20L * 240L);
        StressManager.reduce(stats, AddictionConstants.RELIEF_SLEEPING_AID);

        stats.reduceWithdrawalInCategory(DrugCategory.SEDATIVE, 8.0F);
        RecoveryProgressManager.onProductiveAction(player, ActionKind.SLEEPING_AID, 1.0F);
        syncClientHud(player);
    }

    private static void syncClientHud(ServerPlayer player) {
        SymptomManager.sync(player, AddictionManager.getGlobalSeverity(player), SafeZoneManager.isInSafeZone(player));
    }

    private static float recoveryItemMultiplier(RecoveryRoomReport room) {
        if (!RecoveryRoomManager.isValidRecoveryRoom(room)) {
            return 1.0F;
        }
        return switch (room.tier()) {
            case NONE, FRAGILE_ROOM -> 1.0F;
            case RESTING_ROOM -> 1.15F;
            case SAFE_ROOM -> 1.45F;
            case SANCTUARY -> 1.85F;
        };
    }

    private static boolean hasModule(RecoveryRoomReport room, SanctuaryModule module) {
        return RecoveryRoomManager.isValidRecoveryRoom(room) && room.hasModule(module);
    }
}
