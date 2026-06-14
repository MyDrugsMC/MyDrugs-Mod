package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Structure blocks of the memory vaults (B1). Two silhouettes, both in the local region palette:
 *
 * <ul>
 *   <li><b>Echo shrine</b> — a surface ring of ruined pillars around a raised pedestal;</li>
 *   <li><b>Memory cellar</b> — a sunken square pit with a stepped corner, half reclaimed.</li>
 * </ul>
 *
 * <p>The loot chest itself is NOT placed here — chests need a live level for their loot-table
 * block entity, so the overlay queue places them server-side, marker-gated. This builder only
 * shapes the ruin (deterministically, in both passes), leaving the chest position untouched.
 */
final class InnerVaultBuilder {
    private InnerVaultBuilder() {
    }

    static void placeInitialVaults(ChunkAccess chunk, InnerChunkSampleCache cache) {
        ChunkPos chunkPos = chunk.getPos();
        placeVaults(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(), (pos, state) -> {
            if (pos.getY() < chunk.getMinY() || pos.getY() >= chunk.getMinY() + chunk.getHeight()) {
                return;
            }
            if (!chunkPos.equals(new ChunkPos(pos))) {
                return;
            }
            chunk.setBlockState(pos, state, 2);
        });
    }

    static void placeOverlayVaults(
            ServerLevel level,
            ChunkPos chunkPos,
            InnerChunkSampleCache cache,
            InnerPlacement.MutablePlacementCount count
    ) {
        placeVaults(cache, chunkPos.getMinBlockX(), chunkPos.getMinBlockZ(),
                (pos, state) -> InnerPlacement.safeSet(level, pos, state, true, count));
    }

    private static void placeVaults(InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        if (!cache.anyLand()) {
            return;
        }
        for (InnerVaults.Vault vault : InnerVaults.vaultsTouchingChunk(
                cache.islandCenterX(), cache.islandCenterZ(), minX, minZ)) {
            renderVaultSlice(vault, cache, minX, minZ, setter);
        }
    }

    private static void renderVaultSlice(InnerVaults.Vault vault, InnerChunkSampleCache cache, int minX, int minZ, BlockSetter setter) {
        // Ground level is taken at the vault centre so the whole ruin shares one floor; the
        // centre column may be in a neighbouring chunk — the pass cache makes this cheap.
        InnerTerrain.Sample centre = InnerTerrain.sample(cache.islandCenterX(), cache.islandCenterZ(), vault.x(), vault.z());
        int g = centre.topY();
        InnerTerrainProfile profile = InnerTerrainProfile.forDrug(vault.drug());
        for (int localZ = 0; localZ < 16; localZ++) {
            for (int localX = 0; localX < 16; localX++) {
                int worldX = minX + localX;
                int worldZ = minZ + localZ;
                int dx = worldX - vault.x();
                int dz = worldZ - vault.z();
                if (Math.abs(dx) > InnerVaults.RADIUS || Math.abs(dz) > InnerVaults.RADIUS) {
                    continue;
                }
                long hash = InnerNoise.mix64(vault.hash()
                        ^ (long) worldX * 0x9E37_79B9L
                        ^ (long) worldZ * 0x85EB_CA6BL);
                if (vault.cellar()) {
                    memoryCellar(vault, profile, worldX, g, worldZ, dx, dz, hash, setter);
                } else {
                    echoShrine(vault, profile, worldX, g, worldZ, dx, dz, hash, setter);
                }
            }
        }
    }

    private static void echoShrine(
            InnerVaults.Vault vault,
            InnerTerrainProfile profile,
            int x,
            int g,
            int z,
            int dx,
            int dz,
            long hash,
            BlockSetter setter
    ) {
        int adx = Math.abs(dx);
        int adz = Math.abs(dz);
        // Four corner pillars, ruined to uneven heights.
        if (adx == 3 && adz == 3) {
            int height = 2 + (int) (hash & 3L);
            for (int dy = 1; dy <= height; dy++) {
                setter.set(new BlockPos(x, g + dy, z), profile.subsurfaceBlock());
            }
            if ((hash & 4L) == 0L) {
                setter.set(new BlockPos(x, g + height + 1, z), profile.nodeState());
            }
            return;
        }
        // Paved ring floor, weathered.
        if (adx <= 3 && adz <= 3 && (hash & 7L) != 0L) {
            setter.set(new BlockPos(x, g, z), ((hash & 3L) == 0L
                    ? profile.accentBlock()
                    : profile.pathBlock()));
        }
        // Central pedestal: the chest (placed server-side) sits at g+2 on this plinth.
        if (dx == 0 && dz == 0) {
            setter.set(new BlockPos(x, g + 1, z), profile.subsurfaceBlock());
        }
    }

    private static void memoryCellar(
            InnerVaults.Vault vault,
            InnerTerrainProfile profile,
            int x,
            int g,
            int z,
            int dx,
            int dz,
            long hash,
            BlockSetter setter
    ) {
        int adx = Math.abs(dx);
        int adz = Math.abs(dz);
        if (adx > 3 || adz > 3) {
            return;
        }
        if (adx == 3 || adz == 3) {
            // The pit rim: a broken lip of pavers.
            if ((hash & 3L) != 0L) {
                setter.set(new BlockPos(x, g, z), profile.pathBlock());
            }
            return;
        }
        // Stepped corner so the cellar can be walked into and out of.
        if (dx == 2 && dz == 2) {
            setter.set(new BlockPos(x, g - 1, z), profile.subsurfaceBlock());
            setter.set(new BlockPos(x, g, z), Blocks.AIR.defaultBlockState());
            setter.set(new BlockPos(x, g + 1, z), Blocks.AIR.defaultBlockState());
            return;
        }
        if (dx == 1 && dz == 2 || dx == 2 && dz == 1) {
            setter.set(new BlockPos(x, g - 2, z), profile.subsurfaceBlock());
            for (int dy = -1; dy <= 1; dy++) {
                setter.set(new BlockPos(x, g + dy, z), Blocks.AIR.defaultBlockState());
            }
            return;
        }
        // The sunken floor, three deep, with the chest nook at the centre (chest at g-2).
        setter.set(new BlockPos(x, g - 3, z), ((hash & 7L) == 0L
                ? profile.accentBlock()
                : profile.subsurfaceBlock()));
        for (int dy = -2; dy <= 1; dy++) {
            if (dx == 0 && dz == 0 && dy == -2) {
                continue; // leave the chest cell untouched for the server pass
            }
            setter.set(new BlockPos(x, g + dy, z), Blocks.AIR.defaultBlockState());
        }
        // A single glow node tucked in one floor corner so the pit reads from above at night.
        if (dx == -2 && dz == -2) {
            setter.set(new BlockPos(x, g - 3, z), profile.nodeState());
        }
    }

    private interface BlockSetter {
        void set(BlockPos pos, BlockState state);
    }
}
