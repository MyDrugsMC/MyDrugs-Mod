package org.mydrugs.mydrugs.dimension.inner.v7.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerV7Constants;
import org.mydrugs.mydrugs.dimension.inner.v7.InnerV7Terrain;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class InnerV7ChunkGenerator extends ChunkGenerator {
    public static final MapCodec<InnerV7ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(InnerV7ChunkGenerator::getBiomeSource)
    ).apply(instance, InnerV7ChunkGenerator::new));

    public InnerV7ChunkGenerator(BiomeSource biomeSource) {
        super(biomeSource);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public void applyCarvers(
            WorldGenRegion level,
            long seed,
            RandomState randomState,
            BiomeManager biomeManager,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
    }

    @Override
    public void buildSurface(
            WorldGenRegion level,
            StructureManager structureManager,
            RandomState randomState,
            ChunkAccess chunk
    ) {
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(
            Blender blender,
            RandomState randomState,
            StructureManager structureManager,
            ChunkAccess chunk
    ) {
        ChunkPos chunkPos = chunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();
        if (!InnerV7Terrain.chunkMayHaveLand(minX, minZ)) {
            return CompletableFuture.completedFuture(chunk);
        }

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (int localX = 0; localX < 16; localX++) {
            int worldX = minX + localX;
            for (int localZ = 0; localZ < 16; localZ++) {
                int worldZ = minZ + localZ;
                InnerV7Terrain.Sample sample = InnerV7Terrain.sample(worldX, worldZ);
                if (!sample.land()) {
                    continue;
                }
                for (int y = sample.bottomY(); y <= sample.topY(); y++) {
                    if (y < chunk.getMinY() || y >= chunk.getMinY() + chunk.getHeight()) {
                        continue;
                    }
                    if (InnerV7Terrain.caveAir(sample, worldX, y, worldZ)) {
                        continue;
                    }
                    chunk.setBlockState(mutable.set(worldX, y, worldZ), InnerV7Terrain.stateFor(sample, y), 2);
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSpawnHeight(LevelHeightAccessor level) {
        return InnerV7Constants.BASE_Y + 8;
    }

    @Override
    public int getGenDepth() {
        return InnerV7Constants.GEN_DEPTH;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return InnerV7Constants.MIN_Y;
    }

    @Override
    public int getBaseHeight(
            int x,
            int z,
            Heightmap.Types type,
            LevelHeightAccessor level,
            RandomState randomState
    ) {
        InnerV7Terrain.Sample sample = InnerV7Terrain.sample(x, z);
        return sample.land() ? sample.topY() + 1 : level.getMinY();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor level, RandomState randomState) {
        BlockState[] states = new BlockState[level.getHeight()];
        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }

        InnerV7Terrain.Sample sample = InnerV7Terrain.sample(x, z);
        if (sample.land()) {
            for (int y = sample.bottomY(); y <= sample.topY(); y++) {
                if (y < level.getMinY() || y >= level.getMinY() + level.getHeight()) {
                    continue;
                }
                if (!InnerV7Terrain.caveAir(sample, x, y, z)) {
                    states[y - level.getMinY()] = InnerV7Terrain.stateFor(sample, y);
                }
            }
        }
        return new NoiseColumn(level.getMinY(), states);
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
    }

    @Override
    public void addDebugScreenInfo(List<String> info, RandomState randomState, BlockPos pos) {
        InnerV7Terrain.Sample sample = InnerV7Terrain.sample(pos.getX(), pos.getZ());
        info.add("Inner V7 " + InnerV7Constants.VERSION
                + " land=" + sample.land()
                + " drug=" + sample.drugId().serializedName()
                + " top=" + sample.topY());
    }
}
