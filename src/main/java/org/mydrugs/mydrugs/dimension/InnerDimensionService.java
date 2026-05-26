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

/**
 * Phase G: the Inner Dimension boundary.
 *
 * The Resonator calls {@link #canOpen}/{@link #open}/{@link #onIntegration}, which manipulate the
 * persistent island state ({@link InnerDimensionSavedData}) and place the next ring of terrain
 * via {@link InnerDimensionGenerator}.
 */
public final class InnerDimensionService {
    private static final double SPAWN_X = 0.5D;
    private static final double SPAWN_Y = InnerDimensions.ISLAND_Y + 1.0D;
    private static final double SPAWN_Z = 0.5D;

    private InnerDimensionService() {
    }

    public static boolean canOpen(ServerPlayer player) {
        return player != null && player.getData(ModAttachments.PLAYER_INTEGRATION.get()).unlockedCount() > 0;
    }

    public static boolean open(ServerPlayer player, BlockPos resonatorPos) {
        if (player == null) {
            return false;
        }
        if (!canOpen(player)) {
            player.displayClientMessage(
                    Component.translatable("message.mydrugs.inner_dimension.requires_integration")
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

        InnerDimensionGenerator.ensureInitialIsland(innerLevel);

        player.teleport(new TeleportTransition(
                innerLevel,
                new Vec3(SPAWN_X, SPAWN_Y, SPAWN_Z),
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
        if (InnerDimensionGenerator.onIntegration(innerLevel, drugId)) {
            IntegrationDiary.dimensionExpanded(player, drugId);
        }
    }

    public static boolean markDreamCoordinate(ServerPlayer player, BlockPos resonatorPos) {
        // Reserved for the Dream Alignment ritual; concrete behavior lives in the Resonator
        // (Phase E). The dimension itself has no per-resonator coordinate yet.
        return player != null;
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
