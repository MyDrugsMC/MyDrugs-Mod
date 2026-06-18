package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;
import org.mydrugs.mydrugs.entity.InnerDemonEntity;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Server-authoritative manual scar restoration. Players establish calming growth inside a bounded
 * scar cell; restored cells persist, repel nearby demons, and are healed gradually in loaded chunks.
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
    private static final int SCAR_CELL_SIZE = 16;
    private static final int RESTORATION_THRESHOLD = 4;
    // Per-pass probability that an eligible thorn retracts as a restored cell heals.
    private static final double HEAL_THORN_RETRACT_CHANCE = 0.85D;
    // One in four retracted thorns becomes calming growth; the rest clear to air (roll & MASK == 0).
    private static final int THORN_TO_FERN_MASK = 3;
    // When a cell finishes restoring, nearby demons are pushed to despawn within this radius/delay.
    private static final double RESTORED_CELL_DEMON_CLEAR_RADIUS = 18.0D;
    private static final int RESTORED_CELL_DEMON_DESPAWN_TICKS = 20 * 3;
    // Cooldowns (ticks) for the restrained scar feedback messages.
    private static final int PROGRESS_MESSAGE_COOLDOWN_TICKS = 5;
    private static final int NOT_SCARRED_HINT_COOLDOWN_TICKS = 20 * 4;
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
            InnerIslandContext context = InnerIslandContext.resolve(player, data);
            if (context == null || !context.playerInsideOwnIsland()) {
                continue;
            }
            InnerDimensionSavedData.IslandState island = context.island();
            if (island.restoredScarMarkers().isEmpty()) {
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
    public static void onBlockPlaced(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(InnerDimensions.INNER_LEVEL)
                || !isRestorativeBlock(event.getPlacedBlock())) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerIslandContext context = InnerIslandContext.resolve(player, data);
        if (!canOwnerRestoreAt(context, event.getPos())) {
            return;
        }
        InnerDimensionSavedData.IslandState island = context.island();
        if (!scarSample(island, event.getPos()).scar()) {
            return;
        }
        evaluateRestoration(level, data, island, player, event.getPos());
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getLevel() instanceof ServerLevel level)
                || !level.dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return;
        }
        ItemStack held = player.getItemInHand(event.getHand());
        if (!held.is(ModItems.CALMING_SPORES.get())) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(level);
        InnerIslandContext context = InnerIslandContext.resolve(player, data);
        if (!canOwnerRestoreAt(context, event.getPos())) {
            return;
        }
        InnerDimensionSavedData.IslandState island = context.island();
        if (!scarSample(island, event.getPos()).scar()) {
            // Restrained, cooldown-gated guidance: the player is trying to restore on their own
            // island but this ground is not scarred, so nothing is consumed and we say why.
            InnerMessageCooldowns.actionBar(
                    player,
                    "scar:not_scarred",
                    NOT_SCARRED_HINT_COOLDOWN_TICKS,
                    Component.translatable("message.mydrugs.inner_scar.not_scarred")
                            .withStyle(ChatFormatting.GRAY)
            );
            return;
        }
        seedCalmingFlora(level, island, event.getPos());
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        evaluateRestoration(level, data, island, player, event.getPos());
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
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
            return healChunk(level, island, chunkX, chunkZ, HEAL_THORN_RETRACT_CHANCE, budget);
        } finally {
            InnerTerrain.endCachePass();
        }
    }

    private static int healChunk(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
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
                if (slotCenterX != island.centerX() || slotCenterZ != island.centerZ()) {
                    continue; // only heal this island's own slot
                }
                InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), x, z);
                if (!sample.scar() || (sample.drugId() != DrugId.COCAINE && sample.drugId() != DrugId.METH)) {
                    continue;
                }
                if (!isRestoredAt(island, new BlockPos(x, sample.topY(), z))) {
                    continue;
                }
                BlockPos pos = new BlockPos(x, sample.topY() + 1, z);
                BlockState current = level.getBlockState(pos);
                if (!current.is(ModInnerDimensionBlocks.REDLINE_THORN.get())) {
                    continue; // nothing to retract here (idempotent)
                }
                int roll = thornRetractionRoll(island.centerX(), island.centerZ(), x, z, sample.drugId());
                if (!shouldRetractThorn(roll, retractChance)) {
                    continue; // this thorn persists at the current healing level
                }
                // A fraction of retracted cells turn to calming growth; the rest clear to air.
                BlockState replacement = (roll & THORN_TO_FERN_MASK) == 0
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

    public static String scarMarkerFor(InnerDimensionSavedData.IslandState island, BlockPos pos) {
        int cellX = Math.floorDiv(pos.getX() - island.centerX(), SCAR_CELL_SIZE);
        int cellZ = Math.floorDiv(pos.getZ() - island.centerZ(), SCAR_CELL_SIZE);
        return "scar_cell:" + cellX + ":" + cellZ;
    }

    public static boolean isRestoredAt(InnerDimensionSavedData.IslandState island, BlockPos pos) {
        return island != null && island.isScarRestored(scarMarkerFor(island, pos));
    }

    private static void evaluateRestoration(
            ServerLevel level,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            ServerPlayer player,
            BlockPos pos
    ) {
        String marker = scarMarkerFor(island, pos);
        if (island.isScarRestored(marker)) {
            return;
        }
        int restoredBlocks = countRestorativeBlocks(level, island, pos);
        InnerMessageCooldowns.actionBar(
                player,
                "scar:progress:" + marker,
                PROGRESS_MESSAGE_COOLDOWN_TICKS,
                Component.translatable(
                        "message.mydrugs.inner_scar.progress",
                        Math.min(RESTORATION_THRESHOLD, restoredBlocks),
                        RESTORATION_THRESHOLD
                ).withStyle(ChatFormatting.GREEN)
        );
        if (restoredBlocks < RESTORATION_THRESHOLD || !data.markScarRestored(island.owner(), marker)) {
            return;
        }

        calmCell(level, island, pos);
        for (InnerDemonEntity demon : level.getEntitiesOfClass(
                InnerDemonEntity.class,
                new AABB(pos).inflate(RESTORED_CELL_DEMON_CLEAR_RADIUS)
        )) {
            demon.beginNaturalDespawn(RESTORED_CELL_DEMON_DESPAWN_TICKS);
        }
        ItemStack reward = new ItemStack(ModItems.CALMING_RESIN.get());
        if (!player.addItem(reward)) {
            player.drop(reward, false);
        }
        player.sendSystemMessage(Component.translatable(
                "message.mydrugs.inner_scar.restored"
        ).withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
        InnerProgressionMilestones.scarHealed(player);
    }

    private static int countRestorativeBlocks(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos pos
    ) {
        int cellX = Math.floorDiv(pos.getX() - island.centerX(), SCAR_CELL_SIZE);
        int cellZ = Math.floorDiv(pos.getZ() - island.centerZ(), SCAR_CELL_SIZE);
        int minX = island.centerX() + cellX * SCAR_CELL_SIZE;
        int minZ = island.centerZ() + cellZ * SCAR_CELL_SIZE;
        int count = 0;
        for (int x = minX; x < minX + SCAR_CELL_SIZE; x++) {
            for (int z = minZ; z < minZ + SCAR_CELL_SIZE; z++) {
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    continue;
                }
                InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), x, z);
                if (isRestorativeBlock(level.getBlockState(new BlockPos(x, sample.topY() + 1, z)))) {
                    count++;
                }
            }
        }
        return count;
    }

    private static void seedCalmingFlora(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos origin
    ) {
        int[][] offsets = {{0, 0}, {2, 0}, {-2, 0}, {0, 2}, {0, -2}};
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        for (int[] offset : offsets) {
            int x = origin.getX() + offset[0];
            int z = origin.getZ() + offset[1];
            if (!level.hasChunk(x >> 4, z >> 4)) {
                continue;
            }
            InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), x, z);
            BlockPos top = new BlockPos(x, sample.topY() + 1, z);
            InnerPlacement.safeSet(level, top,
                    ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState(), false, count);
        }
    }

    private static void calmCell(
            ServerLevel level,
            InnerDimensionSavedData.IslandState island,
            BlockPos pos
    ) {
        int cellX = Math.floorDiv(pos.getX() - island.centerX(), SCAR_CELL_SIZE);
        int cellZ = Math.floorDiv(pos.getZ() - island.centerZ(), SCAR_CELL_SIZE);
        int minX = island.centerX() + cellX * SCAR_CELL_SIZE;
        int minZ = island.centerZ() + cellZ * SCAR_CELL_SIZE;
        InnerPlacement.MutablePlacementCount count = new InnerPlacement.MutablePlacementCount();
        for (int x = minX + 1; x < minX + SCAR_CELL_SIZE; x += 3) {
            for (int z = minZ + 1; z < minZ + SCAR_CELL_SIZE; z += 3) {
                if (!level.hasChunk(x >> 4, z >> 4)) {
                    continue;
                }
                InnerTerrain.Sample sample = InnerTerrain.sample(island.centerX(), island.centerZ(), x, z);
                BlockPos plantPos = new BlockPos(x, sample.topY() + 1, z);
                if (level.getBlockState(plantPos).is(ModInnerDimensionBlocks.REDLINE_THORN.get())) {
                    level.setBlock(plantPos, Blocks.AIR.defaultBlockState(), InnerDimensionConstants.UPDATE_FLAGS);
                }
                InnerPlacement.safeSet(level, plantPos,
                        ((x + z) & 1) == 0
                                ? ModInnerDimensionBlocks.CALMING_FERN.get().defaultBlockState()
                                : ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState(),
                        false,
                        count);
            }
        }
    }

    private static InnerTerrain.Sample scarSample(
            InnerDimensionSavedData.IslandState island,
            BlockPos pos
    ) {
        return InnerTerrain.sample(island.centerX(), island.centerZ(), pos.getX(), pos.getZ());
    }

    private static boolean canOwnerRestoreAt(InnerIslandContext context, BlockPos pos) {
        return context != null && context.allowsOwnerInteractionAt(pos);
    }

    static boolean canOwnerRestoreAtForTest(
            InnerDimensionSavedData data,
            UUID playerId,
            BlockPos playerPos,
            BlockPos interactionPos
    ) {
        return canOwnerRestoreAt(
                InnerIslandContext.resolveForInnerPosition(data, playerId, playerPos),
                interactionPos
        );
    }

    private static boolean isRestorativeBlock(BlockState state) {
        return state.is(ModInnerDimensionBlocks.CALMING_FERN.get())
                || state.is(ModInnerDimensionBlocks.CALMING_BUSH.get())
                || state.is(ModInnerDimensionBlocks.MOSS_BREATH_CARPET.get())
                || state.is(ModInnerDimensionBlocks.BREATH_GRASS.get())
                || state.is(ModInnerDimensionBlocks.CALMING_ECHO_NODE.get())
                || state.is(Blocks.LANTERN)
                || state.is(Blocks.SOUL_LANTERN);
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
