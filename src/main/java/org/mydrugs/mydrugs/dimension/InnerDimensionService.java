package org.mydrugs.mydrugs.dimension;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerDimensionV7;

/**
 * Phase G: the Inner Dimension boundary.
 *
 * The Resonator calls {@link #canOpen}/{@link #open}/{@link #onIntegration}, which manipulate the
 * persistent owner state ({@link InnerDimensionSavedData}) and enqueue V7 owner overlays.
 */
public final class InnerDimensionService {
    private static final double SPAWN_Y = InnerDimensions.ISLAND_Y + 1.0D;

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
        InnerDimensionV7.ensureOwnerReady(innerLevel, island);
        // TODO: Feed player recovery/stress state into sparse Thought Echo and Craving Wisp hooks.

        player.teleport(new TeleportTransition(
                innerLevel,
                new Vec3(island.centerX() + 0.5D, SPAWN_Y, island.centerZ() + 0.5D),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                TeleportTransition.DO_NOTHING
        ));
        IntegrationDiary.firstDimensionEntry(player);
        return true;
    }

    /**
     * Called by the Resonator right after a successful integration. Records the drug on the
     * dimension's saved data and expands the island by that drug's §3.3 ring.
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
        if (InnerDimensionV7.onIntegration(innerLevel, island, drugId)) {
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

    /** Returns the player to the overworld spawn — invoked when they fall off the island. */
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
