package org.mydrugs.mydrugs.dimension;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionSystem;
import org.mydrugs.mydrugs.entity.InnerDemonSpawnManager;

/**
 * Boundary for Resonator access to the Inner Dimension.
 */
public final class InnerDimensionService {
    private InnerDimensionService() {
    }

    public enum OpenStatus {
        READY,
        MISSING_INTEGRATION,
        MISSING_DREAM_ALIGNMENT,
        UNAVAILABLE
    }

    public static boolean canOpen(ServerPlayer player) {
        return openStatus(player) == OpenStatus.READY;
    }

    public static OpenStatus openStatus(ServerPlayer player) {
        if (player == null || player.level().getServer() == null) {
            return OpenStatus.UNAVAILABLE;
        }
        if (player.getData(ModAttachments.PLAYER_INTEGRATION.get()).unlockedCount() <= 0) {
            return OpenStatus.MISSING_INTEGRATION;
        }
        if (!hasDreamAlignment(player)) {
            return OpenStatus.MISSING_DREAM_ALIGNMENT;
        }
        return innerLevel(player.level().getServer()) == null ? OpenStatus.UNAVAILABLE : OpenStatus.READY;
    }

    public static boolean open(ServerPlayer player, BlockPos resonatorPos) {
        if (player == null) {
            return false;
        }
        OpenStatus status = openStatus(player);
        if (status != OpenStatus.READY) {
            String message = status == OpenStatus.MISSING_DREAM_ALIGNMENT
                    ? "message.mydrugs.inner_dimension.requires_dream_alignment"
                    : "message.mydrugs.inner_dimension.requires_integration";
            player.displayClientMessage(
                    Component.translatable(message)
                            .withStyle(ChatFormatting.DARK_PURPLE),
                    false
            );
            return false;
        }

        ServerLevel innerLevel = innerLevel(player.level().getServer());
        if (innerLevel == null) {
            player.displayClientMessage(
                    Component.translatable("message.mydrugs.inner_dimension.unavailable")
                            .withStyle(ChatFormatting.DARK_PURPLE),
                    false
            );
            return false;
        }

        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        InnerDimensionSystem.ensureOwnerReady(innerLevel, island);
        BlockPos spawn = InnerDimensionSystem.safeSpawnPos(innerLevel, island);
        // B3: prime the sparse symbolic-encounter system so the player gets a grace period before
        // any atmosphere-danger-driven encounter; per-tick weighting then happens in PlayerTickEvents.
        InnerDemonSpawnManager.primeInnerAmbient(player);

        player.teleport(new TeleportTransition(
                innerLevel,
                new Vec3(spawn.getX() + 0.5D, spawn.getY(), spawn.getZ() + 0.5D),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                TeleportTransition.DO_NOTHING
        ));
        IntegrationDiary.firstDimensionEntry(player);
        return true;
    }

    /**
     * Called by the Resonator right after a successful integration.
     */
    public static void onIntegration(ServerPlayer player, DrugId drugId) {
        if (player == null || drugId == null) {
            return;
        }
        ServerLevel innerLevel = innerLevel(player.level().getServer());
        if (innerLevel == null) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
        if (InnerDimensionSystem.onIntegration(innerLevel, island, drugId)) {
            IntegrationDiary.dimensionExpanded(player, drugId);
        }
    }

    public static boolean markDreamCoordinate(ServerPlayer player, BlockPos resonatorPos) {
        if (player == null) {
            return false;
        }
        player.getData(ModAttachments.PLAYER_INTEGRATION.get()).markDreamAligned();
        ServerLevel innerLevel = innerLevel(player.level().getServer());
        if (innerLevel != null) {
            InnerDimensionSavedData.get(innerLevel).markDreamAligned(player.getUUID(), resonatorPos);
        }
        return true;
    }

    public static boolean hasDreamAlignment(ServerPlayer player) {
        return player != null && player.getData(ModAttachments.PLAYER_INTEGRATION.get()).isDreamAligned();
    }

    /** Returns the player to the overworld spawn when they fall out of the continent. */
    public static void returnToOverworld(ServerPlayer player) {
        if (player == null) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getLevelData().getRespawnData().pos();

        player.teleport(new TeleportTransition(
                overworld,
                new Vec3(spawn.getX() + 0.5, spawn.getY() + 1, spawn.getZ() + 0.5),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                TeleportTransition.DO_NOTHING
        ));
    }

    public static boolean isInInnerDimension(ServerPlayer player) {
        return player != null
                && player.level().dimension().equals(InnerDimensions.INNER_LEVEL);
    }

    @Nullable
    private static ServerLevel innerLevel(@Nullable MinecraftServer server) {
        return server == null ? null : server.getLevel(InnerDimensions.INNER_LEVEL);
    }
}
