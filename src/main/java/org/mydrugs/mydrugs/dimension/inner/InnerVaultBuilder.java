package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;

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
    private static final int MARGIN = 0;

    private InnerVaultBuilder() {
    }

    static void place(InnerBlockSink sink, InnerChunkSampleCache cache, int minX, int minZ) {
        if (!cache.anyLand()) {
            return;
        }
        for (InnerVaults.Vault vault : InnerVaults.vaultsTouchingChunk(
                cache.islandCenterX(), cache.islandCenterZ(), minX, minZ)) {
            renderVaultSlice(vault, cache, minX, minZ, sink);
        }
    }

    private static void renderVaultSlice(InnerVaults.Vault vault, InnerChunkSampleCache cache, int minX, int minZ, InnerBlockSink sink) {
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
                    memoryCellar(vault, profile, worldX, g, worldZ, dx, dz, hash, sink);
                } else {
                    echoShrine(vault, profile, worldX, g, worldZ, dx, dz, hash, sink);
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
            InnerBlockSink sink
    ) {
        int adx = Math.abs(dx);
        int adz = Math.abs(dz);
        // Four corner pillars, ruined to uneven heights.
        if (adx == 3 && adz == 3) {
            int height = 2 + (int) (hash & 3L);
            for (int dy = 1; dy <= height; dy++) {
                sink.setBlock(new BlockPos(x, g + dy, z), profile.subsurfaceBlock(), true);
            }
            if ((hash & 4L) == 0L) {
                sink.setBlock(new BlockPos(x, g + height + 1, z), profile.nodeState(), true);
            }
            return;
        }
        // Paved ring floor, weathered.
        if (adx <= 3 && adz <= 3 && (hash & 7L) != 0L) {
            sink.setBlock(new BlockPos(x, g, z), ((hash & 3L) == 0L
                    ? profile.accentBlock()
                    : profile.pathBlock()), true);
        }
        // Central pedestal: the chest (placed server-side) sits at g+2 on this plinth.
        if (dx == 0 && dz == 0) {
            sink.setBlock(new BlockPos(x, g + 1, z), profile.subsurfaceBlock(), true);
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
            InnerBlockSink sink
    ) {
        int adx = Math.abs(dx);
        int adz = Math.abs(dz);
        if (adx > 3 || adz > 3) {
            return;
        }
        if (adx == 3 || adz == 3) {
            // The pit rim: a broken lip of pavers.
            if ((hash & 3L) != 0L) {
                sink.setBlock(new BlockPos(x, g, z), profile.pathBlock(), true);
            }
            return;
        }
        // Stepped corner so the cellar can be walked into and out of.
        if (dx == 2 && dz == 2) {
            sink.setBlock(new BlockPos(x, g - 1, z), profile.subsurfaceBlock(), true);
            sink.setBlock(new BlockPos(x, g, z), Blocks.AIR.defaultBlockState(), true);
            sink.setBlock(new BlockPos(x, g + 1, z), Blocks.AIR.defaultBlockState(), true);
            return;
        }
        if (dx == 1 && dz == 2 || dx == 2 && dz == 1) {
            sink.setBlock(new BlockPos(x, g - 2, z), profile.subsurfaceBlock(), true);
            for (int dy = -1; dy <= 1; dy++) {
                sink.setBlock(new BlockPos(x, g + dy, z), Blocks.AIR.defaultBlockState(), true);
            }
            return;
        }
        // The sunken floor, three deep, with the chest nook at the centre (chest at g-2).
        sink.setBlock(new BlockPos(x, g - 3, z), ((hash & 7L) == 0L
                ? profile.accentBlock()
                : profile.subsurfaceBlock()), true);
        for (int dy = -2; dy <= 1; dy++) {
            if (dx == 0 && dz == 0 && dy == -2) {
                continue; // leave the chest cell untouched for the server pass
            }
            sink.setBlock(new BlockPos(x, g + dy, z), Blocks.AIR.defaultBlockState(), true);
        }
        // A single glow node tucked in one floor corner so the pit reads from above at night.
        if (dx == -2 && dz == -2) {
            sink.setBlock(new BlockPos(x, g - 3, z), profile.nodeState(), true);
        }
    }
}
