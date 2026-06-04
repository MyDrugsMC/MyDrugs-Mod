package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 7.4 — scars heal over time (server authority). A reconciled region's scar blocks slowly
 * convert toward calm states across sessions: the {@link ModInnerDimensionBlocks#REDLINE_THORN}
 * thorns placed in {@code COCAINE}/{@code METH} scar cells retract (and some become a calming fern)
 * as the owner's integration count rises.
 *
 * <p>Why a dedicated healer: the overlay decorator ({@link InnerDecorator#decorateRegionAwakening})
 * already <em>places fewer</em> thorns as the region heals, but its {@code safeSet} can never
 * <em>remove</em> a thorn that is already in the world. This pass closes that gap by explicitly
 * retracting standing thorns to match the region's current healing level.
 *
 * <p>Constraints honoured:
 * <ul>
 *   <li><b>Server-side / persisted:</b> the healing target is driven by {@code integratedCount} in
 *       {@link InnerDimensionSavedData.IslandState}, which persists across sessions; the retraction
 *       is written to the world, so progress survives relog.</li>
 *   <li><b>Slow cadence &amp; budget:</b> spreads one 5x5 chunk sweep over roughly
 *       {@link #HEAL_INTERVAL} ticks and changes at most {@link #MAX_BLOCKS_PER_SWEEP} blocks per
 *       present player per rolling sweep, so the sample work does not land in one tick.</li>
 *   <li><b>Loaded chunks only:</b> heals only around players actually in the dimension; never
 *       force-loads.</li>
 *   <li><b>Idempotent:</b> a retracted thorn is gone, so re-running is a no-op; the keep/retract
 *       decision is a deterministic function of position and healing level.</li>
 * </ul>
 *
 * <p>Deferred (noted, not silently expanded): warming the scar <em>terrain</em> itself
 * (magma/blackstone &rarr; stone) would need the scar surface palette and a per-region progress
 * field; this pass deliberately limits itself to the unambiguous thorn marker.
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class InnerScarHealer {
    private static final int HEAL_INTERVAL = 200;       // ~10s between passes
    private static final int MAX_BLOCKS_PER_SWEEP = 24; // per present player per 5x5 sweep
    private static final int CHUNK_RADIUS = 2;          // around each player
    private static final int STRIDE = 3;                // match the decorator's column stride
    private static final int HEAL_SWEEP_WIDTH = CHUNK_RADIUS * 2 + 1;
    private static final int CHUNKS_PER_HEAL_SWEEP = HEAL_SWEEP_WIDTH * HEAL_SWEEP_WIDTH;
    private static final int HEAL_STEP_INTERVAL = Math.max(1, HEAL_INTERVAL / CHUNKS_PER_HEAL_SWEEP);
    private static final Map<UUID, Integer> SWEEP_WRITE_BUDGETS = new HashMap<>();

    private static int timer;
    private static int chunkCursor;

    private InnerScarHealer() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        if (++timer < HEAL_STEP_INTERVAL) {
            return;
        }
        timer = 0;

        int chunkIndex = chunkCursor;
        chunkCursor = (chunkCursor + 1) % CHUNKS_PER_HEAL_SWEEP;
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        for (ServerPlayer player : level.players()) {
            InnerDimensionSavedData.IslandState island = data.getOrCreateIsland(player.getUUID());
            if (island.integratedCount() <= 0) {
                continue;
            }
            int budget = remainingSweepBudget(player.getUUID(), chunkIndex);
            if (budget <= 0) {
                continue;
            }
            SWEEP_WRITE_BUDGETS.put(
                    player.getUUID(),
                    healNextChunk(level, island, player.blockPosition(), chunkIndex, budget)
            );
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        timer = 0;
        chunkCursor = 0;
        SWEEP_WRITE_BUDGETS.clear();
    }

    private static int healNextChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos around,
            int chunkIndex,
            int budget
    ) {
        // Higher integration -> stronger retraction. Mirrors the decorator's `order` model.
        double order = InnerNoise.clamp01(island.integratedCount() / 9.0D);
        double retractChance = 0.6D * order;
        if (retractChance <= 0.0D) {
            return budget;
        }
        int centerX = island.centerX();
        int centerZ = island.centerZ();
        int playerChunkX = around.getX() >> 4;
        int playerChunkZ = around.getZ() >> 4;
        int dx = chunkDxForIndex(chunkIndex);
        int dz = chunkDzForIndex(chunkIndex);
        int chunkX = playerChunkX + dx;
        int chunkZ = playerChunkZ + dz;
        if (!level.hasChunk(chunkX, chunkZ)) {
            return budget;
        }
        InnerTerrain.beginCachePass();
        try {
            return healChunk(level, centerX, centerZ, chunkX, chunkZ, retractChance, budget);
        } finally {
            InnerTerrain.endCachePass();
        }
    }

    private static int healChunk(
            ServerLevel level,
            int centerX,
            int centerZ,
            int chunkX,
            int chunkZ,
            double retractChance,
            int budget
    ) {
        int minX = chunkX << 4;
        int minZ = chunkZ << 4;
        for (int localZ = 1; localZ < 16 && budget > 0; localZ += STRIDE) {
            for (int localX = 1; localX < 16 && budget > 0; localX += STRIDE) {
                int x = minX + localX;
                int z = minZ + localZ;
                int slotCenterX = InnerTerrain.slotCenter(x);
                int slotCenterZ = InnerTerrain.slotCenter(z);
                if (slotCenterX != centerX || slotCenterZ != centerZ) {
                    continue; // only heal this island's own slot
                }
                InnerTerrain.Sample sample = InnerTerrain.sample(centerX, centerZ, x, z);
                if (!sample.scar() || (sample.drugId() != DrugId.COCAINE && sample.drugId() != DrugId.METH)) {
                    continue;
                }
                BlockPos pos = new BlockPos(x, sample.topY() + 1, z);
                BlockState current = level.getBlockState(pos);
                if (!current.is(ModInnerDimensionBlocks.REDLINE_THORN.get())) {
                    continue; // nothing to retract here (idempotent)
                }
                int roll = thornRetractionRoll(centerX, centerZ, x, z, sample.drugId());
                if (!shouldRetractThorn(roll, retractChance)) {
                    continue; // this thorn persists at the current healing level
                }
                // A fraction of retracted cells turn to calming growth; the rest clear to air.
                BlockState replacement = (roll & 3) == 0
                        ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                        : Blocks.AIR.defaultBlockState();
                if (replacement.canSurvive(level, pos) || replacement.isAir()) {
                    level.setBlock(pos, replacement, InnerDimensionConstants.UPDATE_FLAGS);
                    budget--;
                }
            }
        }
        return budget;
    }

    private static int chunkDxForIndex(int chunkIndex) {
        return Math.floorMod(chunkIndex, CHUNKS_PER_HEAL_SWEEP) % HEAL_SWEEP_WIDTH - CHUNK_RADIUS;
    }

    private static int chunkDzForIndex(int chunkIndex) {
        return Math.floorMod(chunkIndex, CHUNKS_PER_HEAL_SWEEP) / HEAL_SWEEP_WIDTH - CHUNK_RADIUS;
    }

    private static int remainingSweepBudget(UUID playerId, int chunkIndex) {
        if (chunkIndex == 0 || !SWEEP_WRITE_BUDGETS.containsKey(playerId)) {
            SWEEP_WRITE_BUDGETS.put(playerId, MAX_BLOCKS_PER_SWEEP);
        }
        return SWEEP_WRITE_BUDGETS.get(playerId);
    }

    private static int thornRetractionRoll(int centerX, int centerZ, int x, int z, DrugId drugId) {
        long hash = InnerNoise.mix64(InnerTerrain.seedForSlot(centerX, centerZ)
                + x * 73428767L + z * 912931L + drugId.networkId());
        return (int) (hash & 1023L);
    }

    private static boolean shouldRetractThorn(int roll, double retractChance) {
        return roll < InnerNoise.clamp01(retractChance) * 1024.0D;
    }

    static int healStepIntervalForTest() {
        return HEAL_STEP_INTERVAL;
    }

    static int chunksPerHealSweepForTest() {
        return CHUNKS_PER_HEAL_SWEEP;
    }

    static int chunkDxForIndexForTest(int chunkIndex) {
        return chunkDxForIndex(chunkIndex);
    }

    static int chunkDzForIndexForTest(int chunkIndex) {
        return chunkDzForIndex(chunkIndex);
    }

    static int thornRetractionRollForTest(int centerX, int centerZ, int x, int z, DrugId drugId) {
        return thornRetractionRoll(centerX, centerZ, x, z, drugId);
    }

    static boolean shouldRetractThornForTest(int roll, double retractChance) {
        return shouldRetractThorn(roll, retractChance);
    }

    static int maxBlocksPerSweepForTest() {
        return MAX_BLOCKS_PER_SWEEP;
    }
}
