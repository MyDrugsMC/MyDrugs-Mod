package org.mydrugs.mydrugs.dimension;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.function.Supplier;

/**
 * Programmatic builder for the Inner Dimension island (Phase G.4 + G.5 + G.6).
 *
 * The dimension itself is a void; this class places blocks for the initial blob and, on each
 * integration, materializes the next radius band (annulus) and its tier-specific content. All
 * "structures" (currently the loot-chest markers placed at certain tiers) are gated through
 * {@link InnerDimensionSavedData} so they are placed once and never regenerated.
 */
public final class InnerDimensionGenerator {
    private InnerDimensionGenerator() {
    }

    /** Builds the initial barren disc if it has not been built before. Idempotent. */
    public static void ensureInitialIsland(ServerLevel innerLevel) {
        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        if (data.initialIslandBuilt()) {
            return;
        }
        fillDisc(innerLevel, 0, 0, InnerDimensionSavedData.INITIAL_RADIUS,
                Blocks.STONE.defaultBlockState(), null);
        // A small visible marker at the spawn point so the player can orient on arrival.
        innerLevel.setBlock(new BlockPos(0, InnerDimensions.ISLAND_Y, 0),
                Blocks.AMETHYST_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        data.markInitialIslandBuilt();
    }

    public static void ensureInitialIsland(ServerLevel innerLevel, InnerDimensionSavedData.IslandState island) {
        if (island == null || island.owner() == null || island.initialIslandBuilt()) {
            return;
        }
        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        fillDisc(innerLevel, island.centerX(), island.centerZ(), InnerDimensionSavedData.INITIAL_RADIUS,
                Blocks.STONE.defaultBlockState(), null);
        innerLevel.setBlock(new BlockPos(island.centerX(), InnerDimensions.ISLAND_Y, island.centerZ()),
                Blocks.AMETHYST_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
        data.markInitialIslandBuilt(island.owner());
    }

    /**
     * Expands the island for a freshly integrated drug. Places the new annulus and that tier's
     * distinguishing feature (and, where the design calls for it, a once-only loot chest).
     *
     * Returns true when work was performed (the integration was new to this dimension).
     */
    public static boolean onIntegration(ServerLevel innerLevel, DrugId drugId) {
        if (drugId == null) {
            return false;
        }
        ensureInitialIsland(innerLevel);

        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        if (data.integratedDrugs().contains(drugId)) {
            return false;
        }

        int previousRadius = data.currentRadius();
        int newRadius = InnerDimensionSavedData.radiusAfterIntegration(data.integratedCount(), drugId);
        if (newRadius <= previousRadius) {
            return data.recordIntegration(drugId, previousRadius);
        }

        BlockState floor = tierFloor(drugId);
        fillAnnulus(innerLevel, 0, 0, previousRadius, newRadius, floor, null);
        placeTierFeature(innerLevel, drugId, previousRadius, newRadius, data);

        data.recordIntegration(drugId, newRadius);
        return true;
    }

    public static boolean onIntegration(ServerLevel innerLevel, InnerDimensionSavedData.IslandState island, DrugId drugId) {
        if (drugId == null || island == null || island.owner() == null) {
            return false;
        }
        ensureInitialIsland(innerLevel, island);

        InnerDimensionSavedData data = InnerDimensionSavedData.get(innerLevel);
        if (island.integratedDrugs().contains(drugId)) {
            return false;
        }

        int previousRadius = island.currentRadius();
        int newRadius = InnerDimensionSavedData.radiusAfterIntegration(island.integratedCount(), drugId);
        if (newRadius <= previousRadius) {
            return data.recordIntegration(island.owner(), drugId, previousRadius);
        }

        BlockState floor = tierFloor(drugId);
        fillAnnulus(innerLevel, island.centerX(), island.centerZ(), previousRadius, newRadius, floor, null);
        placeTierFeature(innerLevel, drugId, previousRadius, newRadius, data, island);

        data.recordIntegration(island.owner(), drugId, newRadius);
        return true;
    }

    private static void placeTierFeature(
            ServerLevel level,
            DrugId drugId,
            int previousRadius,
            int newRadius,
            InnerDimensionSavedData data
    ) {
        // A single deterministic anchor block at the outer edge of the new ring on +X axis.
        int featureX = (previousRadius + newRadius) / 2;
        BlockPos featurePos = new BlockPos(featureX, InnerDimensions.ISLAND_Y + 1, 0);

        switch (drugId) {
            case COFFEE -> setBlockIfReplaceable(level, featurePos, Blocks.GRASS_BLOCK.defaultBlockState());
            case TOBACCO -> setBlockIfReplaceable(level, featurePos, Blocks.IRON_ORE.defaultBlockState());
            case WEED -> placeSmallTree(level, featurePos);
            case HASH -> setBlockIfReplaceable(level, featurePos, Blocks.AMETHYST_CLUSTER.defaultBlockState());
            case ALCOHOL -> placeStructureWithChest(level, drugId, featurePos, data,
                    ModItems.OVERDOSE_ANTIDOTE);
            case COCAINE -> setBlockIfReplaceable(level, featurePos, Blocks.QUARTZ_BLOCK.defaultBlockState());
            case LSD -> setBlockIfReplaceable(level, featurePos, Blocks.SCULK.defaultBlockState());
            case METH -> setBlockIfReplaceable(level, featurePos, Blocks.GOLD_ORE.defaultBlockState());
            case MUSHROOMS -> placeStructureWithChest(level, drugId, featurePos, data,
                    ModItems.PRIME_INTEGRATION_CORE);
            default -> {
            }
        }
    }

    private static void placeTierFeature(
            ServerLevel level,
            DrugId drugId,
            int previousRadius,
            int newRadius,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island
    ) {
        int featureX = island.centerX() + (previousRadius + newRadius) / 2;
        BlockPos featurePos = new BlockPos(featureX, InnerDimensions.ISLAND_Y + 1, island.centerZ());

        switch (drugId) {
            case COFFEE -> setBlockIfReplaceable(level, featurePos, Blocks.GRASS_BLOCK.defaultBlockState());
            case TOBACCO -> setBlockIfReplaceable(level, featurePos, Blocks.IRON_ORE.defaultBlockState());
            case WEED -> placeSmallTree(level, featurePos);
            case HASH -> setBlockIfReplaceable(level, featurePos, Blocks.AMETHYST_CLUSTER.defaultBlockState());
            case ALCOHOL -> placeStructureWithChest(level, drugId, featurePos, data, island,
                    ModItems.OVERDOSE_ANTIDOTE);
            case COCAINE -> setBlockIfReplaceable(level, featurePos, Blocks.QUARTZ_BLOCK.defaultBlockState());
            case LSD -> setBlockIfReplaceable(level, featurePos, Blocks.SCULK.defaultBlockState());
            case METH -> setBlockIfReplaceable(level, featurePos, Blocks.GOLD_ORE.defaultBlockState());
            case MUSHROOMS -> placeStructureWithChest(level, drugId, featurePos, data, island,
                    ModItems.PRIME_INTEGRATION_CORE);
            default -> {
            }
        }
    }

    private static BlockState tierFloor(DrugId drugId) {
        return switch (drugId) {
            case COFFEE -> Blocks.DIRT.defaultBlockState();
            case TOBACCO -> Blocks.STONE.defaultBlockState();
            case WEED -> Blocks.GRASS_BLOCK.defaultBlockState();
            case HASH -> Blocks.CALCITE.defaultBlockState();
            case ALCOHOL -> Blocks.DEEPSLATE.defaultBlockState();
            case COCAINE -> Blocks.SMOOTH_QUARTZ.defaultBlockState();
            case LSD -> Blocks.SCULK.defaultBlockState();
            case METH -> Blocks.BLACKSTONE.defaultBlockState();
            case MUSHROOMS -> Blocks.MYCELIUM.defaultBlockState();
            default -> Blocks.STONE.defaultBlockState();
        };
    }

    /**
     * Places a structure (currently a single chest with one item) at {@code anchor} unless it was
     * already placed for this drug tier. Phase G.6: never regenerate, never refill.
     */
    private static void placeStructureWithChest(
            ServerLevel level,
            DrugId drugId,
            BlockPos anchor,
            InnerDimensionSavedData data,
            Supplier<? extends net.minecraft.world.level.ItemLike> lootItemSupplier
    ) {
        String structureId = "tier_" + drugId.serializedName() + "@" + anchor.getX() + "," + anchor.getY() + "," + anchor.getZ();
        if (!data.markStructureGenerated(structureId)) {
            return; // already placed; do not regenerate, do not refill.
        }

        setBlockIfReplaceable(level, anchor, Blocks.CHEST.defaultBlockState());
        BlockEntity be = level.getBlockEntity(anchor);
        if (be instanceof Container container) {
            container.setItem(0, new ItemStack(lootItemSupplier.get()));
            be.setChanged();
        }
    }

    private static void placeStructureWithChest(
            ServerLevel level,
            DrugId drugId,
            BlockPos anchor,
            InnerDimensionSavedData data,
            InnerDimensionSavedData.IslandState island,
            Supplier<? extends net.minecraft.world.level.ItemLike> lootItemSupplier
    ) {
        String structureId = "player_" + island.owner() + ":tier_" + drugId.serializedName()
                + "@" + anchor.getX() + "," + anchor.getY() + "," + anchor.getZ();
        if (!data.markStructureGenerated(island.owner(), structureId)) {
            return;
        }

        setBlockIfReplaceable(level, anchor, Blocks.CHEST.defaultBlockState());
        BlockEntity be = level.getBlockEntity(anchor);
        if (be instanceof Container container) {
            container.setItem(0, new ItemStack(lootItemSupplier.get()));
            be.setChanged();
        }
    }

    private static void placeSmallTree(ServerLevel level, BlockPos base) {
        for (int dy = 0; dy < 3; dy++) {
            setBlockIfReplaceable(level, base.above(dy), Blocks.OAK_LOG.defaultBlockState());
        }
        setBlockIfReplaceable(level, base.above(3), Blocks.OAK_LEAVES.defaultBlockState());
    }

    private static void fillDisc(ServerLevel level, int cx, int cz, int radius, BlockState block, BlockState aboveBlock) {
        int r2 = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz <= r2) {
                    BlockPos pos = new BlockPos(cx + dx, InnerDimensions.ISLAND_Y, cz + dz);
                    level.setBlock(pos, block, Block.UPDATE_ALL);
                    if (aboveBlock != null) {
                        level.setBlock(pos.above(), aboveBlock, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static void fillAnnulus(
            ServerLevel level, int cx, int cz, int innerRadius, int outerRadius,
            BlockState block, BlockState aboveBlock
    ) {
        int innerSq = innerRadius * innerRadius;
        int outerSq = outerRadius * outerRadius;
        for (int dx = -outerRadius; dx <= outerRadius; dx++) {
            for (int dz = -outerRadius; dz <= outerRadius; dz++) {
                int dsq = dx * dx + dz * dz;
                if (dsq > innerSq && dsq <= outerSq) {
                    BlockPos pos = new BlockPos(cx + dx, InnerDimensions.ISLAND_Y, cz + dz);
                    level.setBlock(pos, block, Block.UPDATE_ALL);
                    if (aboveBlock != null) {
                        level.setBlock(pos.above(), aboveBlock, Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    private static void setBlockIfReplaceable(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState current = level.getBlockState(pos);
        if (current.canBeReplaced() || current.isAir()) {
            level.setBlock(pos, state, Block.UPDATE_ALL);
        }
    }
}
