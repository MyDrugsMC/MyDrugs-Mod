package org.mydrugs.mydrugs.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;
import org.mydrugs.mydrugs.recovery.RecoveryRoomReport;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.addiction.manager.state.SymptomManager;
import org.mydrugs.mydrugs.addiction.network.HeadphonesStatePayload;
import org.mydrugs.mydrugs.items.ModItems;

public final class ItemEffectHandler {
    private static final long HEADPHONES_REFRESH_TICKS = 5L;

    private ItemEffectHandler() {
    }

    public static void applyDiary(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();

        stats.temporaryEffects.diaryCalmUntil = now + (20L * 90L);
        stats.temporaryEffects.thoughtSuppressionUntil = now + (20L * 60L);
        StressManager.reduce(stats, AddictionConstants.RELIEF_DIARY);
        syncClientHud(player);
    }

    public static void applyHeadphones(ServerPlayer player, int durationTicks) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        stats.temporaryEffects.headphonesUntil = Math.max(
                stats.temporaryEffects.headphonesUntil,
                player.level().getGameTime() + durationTicks
        );
        syncClientHud(player);
    }

    public static boolean toggleHeadphones(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            stats.temporaryEffects.headphonesEnabled = false;
            stats.temporaryEffects.headphonesUntil = 0L;
            syncHeadphones(player);
            syncClientHud(player);
            return false;
        }

        stats.temporaryEffects.headphonesEnabled = !stats.temporaryEffects.headphonesEnabled;

        if (stats.temporaryEffects.headphonesEnabled) {
            stats.temporaryEffects.headphonesUntil = player.level().getGameTime() + HEADPHONES_REFRESH_TICKS;
        } else {
            stats.temporaryEffects.headphonesUntil = 0L;
        }

        syncHeadphones(player);
        syncClientHud(player);
        return stats.temporaryEffects.headphonesEnabled;
    }

    public static boolean cycleHeadphonesTrack(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            stats.temporaryEffects.headphonesEnabled = false;
            stats.temporaryEffects.headphonesUntil = 0L;
            syncHeadphones(player);
            syncClientHud(player);
            return false;
        }

        stats.temporaryEffects.headphonesTrackNonce++;
        syncHeadphones(player);
        return true;
    }

    public static boolean setHeadphonesPlaying(ServerPlayer player, boolean playing) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            stats.temporaryEffects.headphonesEnabled = false;
            stats.temporaryEffects.headphonesUntil = 0L;
            syncHeadphones(player);
            syncClientHud(player);
            return false;
        }

        stats.temporaryEffects.headphonesEnabled = playing;
        stats.temporaryEffects.headphonesUntil = playing
                ? player.level().getGameTime() + HEADPHONES_REFRESH_TICKS
                : 0L;
        syncHeadphones(player);
        syncClientHud(player);
        return playing;
    }

    public static void tickHeadphones(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        if (!hasItem(player.getInventory(), ModItems.HEADPHONES.get())) {
            if (stats.temporaryEffects.headphonesEnabled || stats.temporaryEffects.headphonesUntil > 0L) {
                stats.temporaryEffects.headphonesEnabled = false;
                stats.temporaryEffects.headphonesUntil = 0L;
                syncHeadphones(player);
                syncClientHud(player);
            }
            return;
        }

        if (stats.temporaryEffects.headphonesEnabled) {
            stats.temporaryEffects.headphonesUntil = player.level().getGameTime() + HEADPHONES_REFRESH_TICKS;
        }
    }
    private static void syncHeadphones(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        PacketDistributor.sendToPlayer(
                player,
                new HeadphonesStatePayload(
                        stats.temporaryEffects.headphonesEnabled,
                        "",
                        stats.temporaryEffects.headphonesTrackNonce,
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
        StressManager.reduce(stats, AddictionConstants.RELIEF_HERBAL_TEA * itemMultiplier);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 6.0F * itemMultiplier);
        }

        stats.temporaryEffects.sleepBonusUntil = player.level().getGameTime() + Math.round(20L * 120L * itemMultiplier);
        syncClientHud(player);
    }

    public static void applyCalmingMixture(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoveryRoomReport room = RecoveryRoomManager.getBestRoom(player).orElse(null);
        float itemMultiplier = recoveryItemMultiplier(room);
        StressManager.reduce(stats, AddictionConstants.RELIEF_CALMING_MIXTURE * itemMultiplier);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 10.0F * itemMultiplier);
        }

        stats.temporaryEffects.calmingMixtureUntil = player.level().getGameTime() + Math.round(20L * 60L * itemMultiplier);
        stats.temporaryEffects.sleepBonusUntil = player.level().getGameTime() + Math.round(20L * 180L * itemMultiplier);
        syncClientHud(player);
    }

    public static void applySleepingAid(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());

        stats.sleepBlockedUntil = 0L;
        stats.temporaryEffects.sleepBonusUntil = player.level().getGameTime() + 20L * 240L;
        StressManager.reduce(stats, AddictionConstants.RELIEF_SLEEPING_AID);

        stats.reduceWithdrawalInCategory(DrugCategory.SEDATIVE, 4.0F);
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
            case NONE, FRAGILE_ROOM -> 1.03F;
            case RESTING_ROOM -> 1.06F;
            case SAFE_ROOM -> 1.10F;
            case SANCTUARY -> 1.15F;
        };
    }
}
