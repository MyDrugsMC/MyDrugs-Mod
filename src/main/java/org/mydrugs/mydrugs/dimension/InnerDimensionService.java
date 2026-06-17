package org.mydrugs.mydrugs.dimension;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.IntegrationDiary;
import org.mydrugs.mydrugs.dimension.inner.InnerProgressionMilestones;
import org.mydrugs.mydrugs.dimension.inner.InnerDimensionSystem;
import org.mydrugs.mydrugs.entity.InnerDemonSpawnManager;
import org.mydrugs.mydrugs.network.InnerGrowthWavePayload;
import org.mydrugs.mydrugs.network.InnerSkyStatePayload;

import java.util.List;

/**
 * Boundary for Resonator access to the Inner Dimension.
 */
public final class InnerDimensionService {
    private static final int[][] ADJACENT_OFFSETS = {
            {1, 0},
            {-1, 0},
            {0, 1},
            {0, -1},
            {1, 1},
            {1, -1},
            {-1, 1},
            {-1, -1},
            {2, 0},
            {-2, 0},
            {0, 2},
            {0, -2}
    };
    private static final int SAFE_SEARCH_RADIUS = 8;

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
        if (player == null) {
            return OpenStatus.UNAVAILABLE;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return resolveOpenStatus(false, 0, false, false);
        }
        var integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        int integratedCount = integration.unlockedCount();
        boolean dreamAligned = integration.isDreamAligned();
        if (integratedCount <= 0 || !dreamAligned) {
            return resolveOpenStatus(true, integratedCount, dreamAligned, false);
        }
        return resolveOpenStatus(
                true,
                integratedCount,
                true,
                innerLevel(server) != null
        );
    }

    public static boolean open(ServerPlayer player, BlockPos resonatorPos) {
        if (player == null) {
            return false;
        }
        OpenStatus status = openStatus(player);
        switch (status) {
            case READY -> {
            }
            case MISSING_INTEGRATION, MISSING_DREAM_ALIGNMENT, UNAVAILABLE -> {
                player.displayClientMessage(
                        Component.translatable(openFailureMessageKey(status))
                                .withStyle(ChatFormatting.DARK_PURPLE),
                        false
                );
                return false;
            }
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
        data.markDreamAligned(player.getUUID(), resonatorPos, player.level().dimension().location().toString());
        InnerDimensionSystem.ensureOwnerReady(innerLevel, island);
        BlockPos spawn = InnerDimensionSystem.safeSpawnPos(innerLevel, island);
        // Prime the sparse symbolic-encounter system so the player gets a grace period before
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
        InnerProgressionMilestones.entered(player);
        syncSky(player, island);
        return true;
    }

    static OpenStatus resolveOpenStatus(
            boolean hasServer,
            int integratedCount,
            boolean dreamAligned,
            boolean innerLevelAvailable
    ) {
        if (!hasServer) {
            return OpenStatus.UNAVAILABLE;
        }
        if (integratedCount <= 0) {
            return OpenStatus.MISSING_INTEGRATION;
        }
        if (!dreamAligned) {
            return OpenStatus.MISSING_DREAM_ALIGNMENT;
        }
        return innerLevelAvailable ? OpenStatus.READY : OpenStatus.UNAVAILABLE;
    }

    static @Nullable String openFailureMessageKey(OpenStatus status) {
        return switch (status) {
            case READY -> null;
            case MISSING_INTEGRATION -> "message.mydrugs.inner_dimension.requires_integration";
            case MISSING_DREAM_ALIGNMENT -> "message.mydrugs.inner_dimension.requires_dream_alignment";
            case UNAVAILABLE -> "message.mydrugs.inner_dimension.unavailable";
        };
    }

    /**
     * Pushes the player's integrated-drug set to the client so the custom sky (constellations +
     * core beacon brightness) reflects current healing progress. Cheap, fire-and-forget; safe to
     * call on entry and after each integration.
     */
    public static void syncSky(ServerPlayer player, InnerDimensionSavedData.IslandState island) {
        if (player == null || island == null) {
            return;
        }
        List<Integer> ids = island.integratedDrugNetworkIds();
        PacketDistributor.sendToPlayer(player, new InnerSkyStatePayload(ids));
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
        // Phase 8: if the player is standing in their dimension at integration time, pace the new
        // region in as a visible outward wave and fire the client growth flourish; otherwise the
        // awakening enqueues silently as before.
        boolean liveWave = isInInnerDimension(player);
        if (InnerDimensionSystem.onIntegration(innerLevel, island, drugId, liveWave)) {
            IntegrationDiary.dimensionExpanded(player, drugId);
            if (liveWave) {
                PacketDistributor.sendToPlayer(player, new InnerGrowthWavePayload(drugId.networkId()));
            }
        }
        // Reflect the (possibly newly) integrated set on the client sky even if the system reported
        // no structural expansion — the constellation/beacon state still needs to be current.
        syncSky(player, island);
    }

    public static boolean markDreamCoordinate(ServerPlayer player, BlockPos resonatorPos) {
        if (player == null) {
            return false;
        }
        player.getData(ModAttachments.PLAYER_INTEGRATION.get()).markDreamAligned();
        ServerLevel innerLevel = innerLevel(player.level().getServer());
        if (innerLevel != null) {
            InnerDimensionSavedData.get(innerLevel).markDreamAligned(
                    player.getUUID(),
                    resonatorPos,
                    player.level().dimension().location().toString()
            );
        }
        return true;
    }

    public static boolean hasDreamAlignment(ServerPlayer player) {
        return player != null && player.getData(ModAttachments.PLAYER_INTEGRATION.get()).isDreamAligned();
    }

    /** Returns the player near their entry Resonator, then respawn point, then world spawn. */
    public static void returnToOverworld(ServerPlayer player) {
        if (player == null) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        if (server == null) {
            return;
        }
        ReturnTarget target = resolveReturnTarget(player, server);

        player.teleport(new TeleportTransition(
                target.level(),
                new Vec3(target.feet().getX() + 0.5, target.feet().getY(), target.feet().getZ() + 0.5),
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                TeleportTransition.DO_NOTHING
        ));
    }

    private static ReturnTarget resolveReturnTarget(ServerPlayer player, MinecraftServer server) {
        ReturnTarget dream = dreamCoordinateTarget(player, server);
        if (dream != null) {
            return dream;
        }
        MyDrugs.getLOGGER().debug("Inner Dimension return: no dream coordinate for player {}, falling back to respawn", player.getName().getString());
        ReturnTarget respawn = respawnTarget(player, server);
        if (respawn != null) {
            return respawn;
        }
        MyDrugs.getLOGGER().debug("Inner Dimension return: no valid respawn for player {}, falling back to world spawn", player.getName().getString());
        ServerLevel overworld = server.overworld();
        BlockPos spawn = overworld.getLevelData().getRespawnData().pos();
        BlockPos safeSpawn = safeNearOrNull(overworld, spawn, false);
        if (safeSpawn != null) {
            return new ReturnTarget(overworld, safeSpawn);
        }
        return new ReturnTarget(overworld, unsafeSurfaceLastResort(overworld, spawn, "world_spawn"));
    }

    @Nullable
    private static ReturnTarget dreamCoordinateTarget(ServerPlayer player, MinecraftServer server) {
        ServerLevel innerLevel = innerLevel(server);
        if (innerLevel == null) {
            return null;
        }
        InnerDimensionSavedData.IslandState island = InnerDimensionSavedData.get(innerLevel).island(player.getUUID());
        if (island == null || !island.hasDreamCoordinate()) {
            return null;
        }
        ResourceKey<Level> dimension = parseDimension(island.dreamDimension());
        if (dimension == null) {
            MyDrugs.getLOGGER().debug("Inner Dimension return: dream dimension '{}' for player {} could not be parsed", island.dreamDimension(), player.getName().getString());
            return null;
        }
        if (dimension.equals(InnerDimensions.INNER_LEVEL)) {
            MyDrugs.getLOGGER().debug("Inner Dimension return: dream dimension for player {} is the Inner Dimension itself, rejecting", player.getName().getString());
            return null;
        }
        ServerLevel targetLevel = server.getLevel(dimension);
        if (targetLevel == null) {
            return null;
        }
        BlockPos resonator = new BlockPos(island.dreamX(), island.dreamY(), island.dreamZ());
        BlockPos safe = safeNearOrNull(targetLevel, resonator, true);
        if (safe == null) {
            MyDrugs.getLOGGER().debug("Inner Dimension return: dream coordinate for player {} has no safe nearby position, falling back", player.getName().getString());
            return null;
        }
        return new ReturnTarget(targetLevel, safe);
    }

    @Nullable
    private static ReturnTarget respawnTarget(ServerPlayer player, MinecraftServer server) {
        ServerPlayer.RespawnConfig respawnConfig = player.getRespawnConfig();
        if (respawnConfig == null) {
            return null;
        }
        ServerLevel respawnLevel = server.getLevel(ServerPlayer.RespawnConfig.getDimensionOrDefault(respawnConfig));
        if (respawnLevel == null) {
            return null;
        }
        if (respawnLevel.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            MyDrugs.getLOGGER().debug("Inner Dimension return: respawn point for player {} is the Inner Dimension itself, rejecting", player.getName().getString());
            return null;
        }
        BlockPos respawn = respawnConfig.respawnData().pos();
        BlockPos safe = safeNearOrNull(respawnLevel, respawn, false);
        if (safe == null) {
            MyDrugs.getLOGGER().debug("Inner Dimension return: respawn point for player {} has no safe nearby position, falling back", player.getName().getString());
            return null;
        }
        return new ReturnTarget(respawnLevel, safe);
    }

    @Nullable
    private static ResourceKey<Level> parseDimension(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id == null || id.isBlank()
                ? Level.OVERWORLD.location().toString()
                : id);
        if (location == null) {
            return null;
        }
        return ResourceKey.create(Registries.DIMENSION, location);
    }

    @Nullable
    private static BlockPos safeNearOrNull(ServerLevel level, BlockPos origin, boolean adjacent) {
        ChunkPos originChunk = new ChunkPos(origin);
        level.getChunk(originChunk.x, originChunk.z);
        if (!adjacent) {
            BlockPos safe = safeInColumn(level, origin.getX(), origin.getZ(), origin.getY());
            if (safe != null) {
                return safe;
            }
        }
        for (int[] offset : ADJACENT_OFFSETS) {
            BlockPos safe = safeInColumn(level, origin.getX() + offset[0], origin.getZ() + offset[1], origin.getY());
            if (safe != null) {
                return safe;
            }
        }
        for (int radius = adjacent ? 2 : 1; radius <= SAFE_SEARCH_RADIUS; radius++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dx = -radius; dx <= radius; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    BlockPos safe = safeInColumn(level, origin.getX() + dx, origin.getZ() + dz, origin.getY());
                    if (safe != null) {
                        return safe;
                    }
                }
            }
        }
        MyDrugs.getLOGGER().debug("Inner Dimension return: no safe position found near ({},{},{})", origin.getX(), origin.getY(), origin.getZ());
        return null;
    }

    private static BlockPos unsafeSurfaceLastResort(ServerLevel level, BlockPos origin, String source) {
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        int clampedY = Math.clamp(surfaceY, level.getMinY() + 1, level.getMaxY() - 2);
        BlockPos lastResort = new BlockPos(origin.getX(), clampedY, origin.getZ());
        BlockPos sameColumnSafe = safeInColumn(level, origin.getX(), origin.getZ(), clampedY);
        if (sameColumnSafe != null) {
            return sameColumnSafe;
        }
        MyDrugs.getLOGGER().debug(
                "Inner Dimension return: using unsafe absolute last resort {} from {} after all safe candidates failed",
                lastResort,
                source
        );
        return lastResort;
    }

    @Nullable
    private static BlockPos safeInColumn(ServerLevel level, int x, int z, int preferredY) {
        int minY = level.getMinY() + 1;
        int maxY = level.getMaxY() - 2;
        int start = Math.clamp(preferredY, minY, maxY);
        for (int dy = 0; dy <= 8; dy++) {
            BlockPos up = new BlockPos(x, Math.min(maxY, start + dy), z);
            if (isSafeStandingPos(level, up)) {
                return up;
            }
            BlockPos down = new BlockPos(x, Math.max(minY, start - dy), z);
            if (isSafeStandingPos(level, down)) {
                return down;
            }
        }
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos surface = new BlockPos(x, Math.clamp(surfaceY, minY, maxY), z);
        return isSafeStandingPos(level, surface) ? surface : null;
    }

    private static boolean isSafeStandingPos(ServerLevel level, BlockPos feet) {
        if (feet.getY() <= level.getMinY() || feet.getY() + 1 >= level.getMaxY()) {
            return false;
        }
        BlockPos belowPos = feet.below();
        BlockPos headPos = feet.above();
        BlockState below = level.getBlockState(belowPos);
        BlockState feetState = level.getBlockState(feet);
        BlockState headState = level.getBlockState(headPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP)
                && feetState.getCollisionShape(level, feet).isEmpty()
                && headState.getCollisionShape(level, headPos).isEmpty()
                && feetState.getFluidState().isEmpty()
                && headState.getFluidState().isEmpty()
                && !isLava(feetState.getFluidState())
                && !isLava(headState.getFluidState());
    }

    private static boolean isLava(FluidState state) {
        return state.is(Fluids.LAVA) || state.is(Fluids.FLOWING_LAVA);
    }

    public static boolean isInInnerDimension(ServerPlayer player) {
        return player != null
                && player.level().dimension().equals(InnerDimensions.INNER_LEVEL);
    }

    @Nullable
    private static ServerLevel innerLevel(@Nullable MinecraftServer server) {
        return server == null ? null : server.getLevel(InnerDimensions.INNER_LEVEL);
    }

    private record ReturnTarget(ServerLevel level, BlockPos feet) {
    }
}
