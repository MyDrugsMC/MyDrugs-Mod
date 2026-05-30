package org.mydrugs.mydrugs.dimension.inner.v7;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.ArrayDeque;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerV7OverlayQueue {
    private static final Map<UUID, QueueState> QUEUES = new LinkedHashMap<>();
    private static final Map<UUID, InnerV7GenerationMetrics> LAST_METRICS = new LinkedHashMap<>();
    private static final Set<Block> GENERATED_REPLACEABLE = Set.of(
            Blocks.AIR,
            Blocks.CAVE_AIR,
            Blocks.VOID_AIR,
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.DIRT_PATH,
            Blocks.ROOTED_DIRT,
            Blocks.MOSS_BLOCK,
            Blocks.MYCELIUM,
            Blocks.TUFF,
            Blocks.STONE,
            Blocks.STONE_BRICKS,
            Blocks.CRACKED_STONE_BRICKS,
            Blocks.CALCITE,
            Blocks.AMETHYST_BLOCK,
            Blocks.SMOOTH_BASALT,
            Blocks.DEEPSLATE,
            Blocks.DEEPSLATE_TILES,
            Blocks.CRACKED_DEEPSLATE_TILES,
            Blocks.MUD,
            Blocks.SMOOTH_QUARTZ,
            Blocks.QUARTZ_BLOCK,
            Blocks.WHITE_CONCRETE,
            Blocks.REDSTONE_BLOCK,
            Blocks.PRISMARINE,
            Blocks.SCULK,
            Blocks.BLACKSTONE,
            Blocks.BASALT,
            Blocks.MAGMA_BLOCK,
            Blocks.MUSHROOM_STEM
    );

    private InnerV7OverlayQueue() {
    }

    public static InnerV7RegenerationJob enqueueFullRegeneration(InnerDimensionSavedData.IslandState island) {
        ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
        int centerChunkX = island.centerX() >> 4;
        int centerChunkZ = island.centerZ() >> 4;
        int radius = InnerV7Constants.FULL_REGENERATION_CHUNK_RADIUS;
        int radiusSq = radius * radius;
        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                if (dx * dx + dz * dz <= radiusSq) {
                    chunks.add(new ChunkPos(centerChunkX + dx, centerChunkZ + dz));
                }
            }
        }
        boolean replaced = QUEUES.put(island.owner(), new QueueState(island.owner(), chunks, 0, 0, 0, System.currentTimeMillis())) != null;
        return new InnerV7RegenerationJob(island.owner(), chunks.size(), replaced);
    }

    public static void enqueueEntryPatch(InnerDimensionSavedData.IslandState island) {
        ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
        int centerChunkX = island.centerX() >> 4;
        int centerChunkZ = island.centerZ() >> 4;
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                chunks.add(new ChunkPos(centerChunkX + dx, centerChunkZ + dz));
            }
        }
        QUEUES.compute(island.owner(), (owner, existing) -> existing == null
                ? new QueueState(owner, chunks, 0, 0, 0, System.currentTimeMillis())
                : existing.append(chunks));
    }

    public static void enqueueIntegrationPatch(InnerDimensionSavedData.IslandState island, DrugId drugId) {
        ArrayDeque<ChunkPos> chunks = new ArrayDeque<>();
        BlockPos landmark = InnerV7RegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId);
        ChunkPos landmarkChunk = new ChunkPos(landmark);
        for (int dz = -1; dz <= 1; dz++) {
            for (int dx = -1; dx <= 1; dx++) {
                chunks.add(new ChunkPos(landmarkChunk.x + dx, landmarkChunk.z + dz));
            }
        }
        chunks.add(new ChunkPos(island.centerX() >> 4, island.centerZ() >> 4));
        QUEUES.compute(island.owner(), (owner, existing) -> existing == null
                ? new QueueState(owner, chunks, 0, 0, 0, System.currentTimeMillis())
                : existing.append(chunks));
    }

    public static boolean cancel(UUID owner) {
        return QUEUES.remove(owner) != null;
    }

    public static String queueStatus(UUID owner) {
        QueueState state = QUEUES.get(owner);
        if (state == null) {
            InnerV7GenerationMetrics metrics = LAST_METRICS.getOrDefault(owner, InnerV7GenerationMetrics.EMPTY);
            return "Inner V7 queue idle. Last metrics: " + metrics.toDebugString();
        }
        return "Inner V7 queue owner=" + owner
                + " remaining=" + state.remaining()
                + ", processed=" + state.processedChunks
                + ", placed=" + state.placedBlocks
                + ", skipped=" + state.skippedBlocks;
    }

    public static String lastMetricsFor(UUID owner) {
        return LAST_METRICS.getOrDefault(owner, InnerV7GenerationMetrics.EMPTY).toDebugString();
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        if (QUEUES.isEmpty()) {
            return;
        }

        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        int chunksLeftThisTick = InnerV7Constants.OVERLAY_CHUNKS_PER_TICK;
        var iterator = QUEUES.entrySet().iterator();
        while (iterator.hasNext() && chunksLeftThisTick > 0) {
            Map.Entry<UUID, QueueState> entry = iterator.next();
            QueueState state = entry.getValue();
            InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(entry.getKey());
            while (chunksLeftThisTick > 0 && !state.chunks.isEmpty()) {
                ChunkPos chunkPos = state.chunks.removeFirst();
                PlacementCount count = placeChunk(level, island, chunkPos);
                state.processedChunks++;
                state.placedBlocks += count.placed();
                state.skippedBlocks += count.skipped();
                chunksLeftThisTick--;
                data.markV7Generated(island.owner(), terrainKey(chunkPos));
            }
            if (state.chunks.isEmpty()) {
                long elapsed = Math.max(0L, System.currentTimeMillis() - state.startedMillis);
                InnerV7GenerationMetrics metrics = new InnerV7GenerationMetrics(
                        state.processedChunks,
                        state.processedChunks,
                        state.placedBlocks,
                        state.skippedBlocks,
                        elapsed
                );
                LAST_METRICS.put(entry.getKey(), metrics);
                data.markV7MetricsSummary(entry.getKey(), metrics.toDebugString());
                iterator.remove();
            }
        }
    }

    private static String terrainKey(ChunkPos chunkPos) {
        return InnerV7Constants.KEY_PREFIX + "overlay:chunk:" + chunkPos.x + ":" + chunkPos.z;
    }

    private static PlacementCount placeChunk(ServerLevel level, InnerDimensionSavedData.IslandState island, ChunkPos chunkPos) {
        MutablePlacementCount count = new MutablePlacementCount();
        if (chunkPos.x == (island.centerX() >> 4) && chunkPos.z == (island.centerZ() >> 4)) {
            placeCenterSanctuary(level, island, count);
        }
        for (DrugId drugId : CuratedDrugChain.ORDER) {
            BlockPos landmark = InnerV7RegionMap.landmarkFor(island.centerX(), island.centerZ(), drugId);
            if ((landmark.getX() >> 4) == chunkPos.x && (landmark.getZ() >> 4) == chunkPos.z) {
                placeLandmark(level, island, drugId, landmark, island.integratedDrugs().contains(drugId), count);
            }
        }
        return count.freeze();
    }

    private static void placeCenterSanctuary(ServerLevel level, InnerDimensionSavedData.IslandState island, MutablePlacementCount count) {
        BlockPos center = surface(level, island.centerX(), island.centerZ());
        for (int dz = -4; dz <= 4; dz++) {
            for (int dx = -4; dx <= 4; dx++) {
                int dist = Math.abs(dx) + Math.abs(dz);
                BlockPos pos = surface(level, island.centerX() + dx, island.centerZ() + dz);
                BlockState state = dist <= 2 ? Blocks.SMOOTH_STONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState();
                safeSet(level, pos.below(), state, true, count);
            }
        }
        safeSet(level, center, ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState(), false, count);
        safeSet(level, center.north(3), Blocks.SEA_LANTERN.defaultBlockState(), false, count);
        safeSet(level, center.south(3), Blocks.SEA_LANTERN.defaultBlockState(), false, count);
        safeSet(level, center.east(3), Blocks.SEA_LANTERN.defaultBlockState(), false, count);
        safeSet(level, center.west(3), Blocks.SEA_LANTERN.defaultBlockState(), false, count);
    }

    private static void placeLandmark(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            DrugId drugId,
            BlockPos anchor,
            boolean unlocked,
            MutablePlacementCount count
    ) {
        BlockPos surface = surface(level, anchor.getX(), anchor.getZ());
        BlockState accent = accentFor(drugId);
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) == 3 && Math.abs(dz) == 3) {
                    continue;
                }
                safeSet(level, surface.offset(dx, -1, dz), unlocked ? accent : Blocks.DEEPSLATE_TILES.defaultBlockState(), true, count);
            }
        }
        for (int i = 0; i < 4; i++) {
            int sx = i < 2 ? -3 : 3;
            int sz = i % 2 == 0 ? -3 : 3;
            BlockPos pillar = surface.offset(sx, 0, sz);
            for (int y = 0; y < (unlocked ? 5 : 3); y++) {
                safeSet(level, pillar.above(y), accent, true, count);
            }
        }
        BlockState node = nodeFor(drugId, unlocked);
        safeSet(level, surface.above(unlocked ? 2 : 1), node, false, count);
        if (unlocked) {
            InnerDimensionSavedData.get(level).markV7StructurePlaced(
                    island.owner(),
                    InnerV7Constants.KEY_PREFIX + "landmark:" + drugId.serializedName()
            );
        }
    }

    private static BlockPos surface(ServerLevel level, int x, int z) {
        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
        if (y <= level.getMinY()) {
            y = InnerV7Constants.BASE_Y + 1;
        }
        return new BlockPos(x, y, z);
    }

    private static boolean safeSet(
            ServerLevel level,
            BlockPos pos,
            BlockState state,
            boolean allowTerrainReplace,
            MutablePlacementCount count
    ) {
        if (count.attempted++ >= InnerV7Constants.MAX_OVERLAY_BLOCKS_PER_CHUNK
                || pos.getY() < level.getMinY()
                || pos.getY() >= level.getMaxY()) {
            count.skipped++;
            return false;
        }
        if (level.getBlockEntity(pos) != null) {
            count.skipped++;
            return false;
        }
        if (requiresSupport(state) && !state.canSurvive(level, pos)) {
            count.skipped++;
            return false;
        }
        BlockState current = level.getBlockState(pos);
        boolean replaceable = current.isAir() || current.canBeReplaced() || !current.getFluidState().isEmpty();
        if (!replaceable && (!allowTerrainReplace || !GENERATED_REPLACEABLE.contains(current.getBlock()))) {
            count.skipped++;
            return false;
        }
        level.setBlock(pos, state, InnerV7Constants.UPDATE_FLAGS);
        count.placed++;
        return true;
    }

    private static boolean requiresSupport(BlockState state) {
        return state.is(Blocks.CANDLE)
                || state.is(Blocks.REDSTONE_TORCH)
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN)
                || state.is(Blocks.AMETHYST_CLUSTER)
                || state.is(ModInnerDimensionBlocks.BREATH_GRASS.get())
                || state.is(ModInnerDimensionBlocks.CALMING_FERN.get())
                || state.is(ModInnerDimensionBlocks.MEMORY_REEDS.get())
                || state.is(ModInnerDimensionBlocks.REDLINE_THORN.get())
                || state.is(ModInnerDimensionBlocks.MYCELIAL_ROOT.get());
    }

    private static BlockState accentFor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.BOOKSHELF.defaultBlockState();
            case TOBACCO -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
            case WEED -> Blocks.MOSS_BLOCK.defaultBlockState();
            case HASH -> Blocks.AMETHYST_BLOCK.defaultBlockState();
            case ALCOHOL -> Blocks.DEEPSLATE_TILES.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> Blocks.PRISMARINE.defaultBlockState();
            case METH -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> Blocks.MUSHROOM_STEM.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    private static BlockState nodeFor(DrugId drugId, boolean unlocked) {
        if (!unlocked) {
            return Blocks.IRON_BARS.defaultBlockState();
        }
        return switch (drugId) {
            case COFFEE -> ModInnerDimensionBlocks.LUCID_ECHO_NODE.get().defaultBlockState();
            case TOBACCO -> ModInnerDimensionBlocks.BITTER_ECHO_NODE.get().defaultBlockState();
            case WEED -> ModInnerDimensionBlocks.CALMING_ECHO_NODE.get().defaultBlockState();
            case HASH -> ModInnerDimensionBlocks.PRESSED_CALM_NODE.get().defaultBlockState();
            case ALCOHOL -> ModInnerDimensionBlocks.FERMENTED_MEMORY_NODE.get().defaultBlockState();
            case COCAINE -> ModInnerDimensionBlocks.REDLINE_CRYSTAL_NODE.get().defaultBlockState();
            case LSD -> ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get().defaultBlockState();
            case METH -> ModInnerDimensionBlocks.OVERDRIVE_SLAG.get().defaultBlockState();
            case MUSHROOMS -> ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get().defaultBlockState();
            default -> Blocks.SEA_LANTERN.defaultBlockState();
        };
    }

    private static final class QueueState {
        private final UUID owner;
        private final ArrayDeque<ChunkPos> chunks;
        private final long startedMillis;
        private int processedChunks;
        private int placedBlocks;
        private int skippedBlocks;

        private QueueState(
                UUID owner,
                ArrayDeque<ChunkPos> chunks,
                int processedChunks,
                int placedBlocks,
                int skippedBlocks,
                long startedMillis
        ) {
            this.owner = owner;
            this.chunks = chunks;
            this.processedChunks = processedChunks;
            this.placedBlocks = placedBlocks;
            this.skippedBlocks = skippedBlocks;
            this.startedMillis = startedMillis;
        }

        QueueState append(ArrayDeque<ChunkPos> extra) {
            chunks.addAll(extra);
            return this;
        }

        int remaining() {
            return chunks.size();
        }
    }

    private record PlacementCount(int placed, int skipped) {
    }

    private static final class MutablePlacementCount {
        private int attempted;
        private int placed;
        private int skipped;

        private PlacementCount freeze() {
            return new PlacementCount(placed, skipped);
        }
    }
}
