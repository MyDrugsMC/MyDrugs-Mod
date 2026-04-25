package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.SymptomManager;
import org.mydrugs.mydrugs.effects.addiction.network.HeadphonesStatePayload;
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
                        stats.temporaryEffects.headphonesTrackNonce
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
        StressManager.reduce(stats, AddictionConstants.RELIEF_HERBAL_TEA);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 6.0F);
        }

        stats.temporaryEffects.sleepBonusUntil = player.level().getGameTime() + 20L * 120L;
        syncClientHud(player);
    }

    public static void applyCalmingMixture(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        StressManager.reduce(stats, AddictionConstants.RELIEF_CALMING_MIXTURE);

        for (DrugCategory category : DrugCategory.values()) {
            stats.reduceWithdrawalInCategory(category, 10.0F);
        }

        stats.temporaryEffects.calmingMixtureUntil = player.level().getGameTime() + 20L * 60L;
        stats.temporaryEffects.sleepBonusUntil = player.level().getGameTime() + 20L * 180L;
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
}