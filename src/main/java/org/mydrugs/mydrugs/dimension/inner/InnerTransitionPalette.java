package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.Map;
import java.util.function.Supplier;

final class InnerTransitionPalette {
    private static final Map<Pair, Hybrid> HYBRIDS = Map.ofEntries(
            Map.entry(Pair.of(DrugId.COFFEE, DrugId.TOBACCO), new Hybrid(
                    () -> Blocks.CRACKED_STONE_BRICKS.defaultBlockState(),
                    () -> Blocks.ROOTED_DIRT.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.BITTER_SPROUT.get()
            )),
            Map.entry(Pair.of(DrugId.TOBACCO, DrugId.WEED), new Hybrid(
                    () -> Blocks.MOSSY_COBBLESTONE.defaultBlockState(),
                    () -> Blocks.MOSS_BLOCK.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.CALMING_BUSH.get()
            )),
            Map.entry(Pair.of(DrugId.WEED, DrugId.HASH), new Hybrid(
                    () -> Blocks.CALCITE.defaultBlockState(),
                    () -> Blocks.MOSS_BLOCK.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.CRYSTAL_SHRUB.get()
            )),
            Map.entry(Pair.of(DrugId.HASH, DrugId.ALCOHOL), new Hybrid(
                    () -> Blocks.SMOOTH_BASALT.defaultBlockState(),
                    () -> Blocks.MUD.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.MEMORY_LOTUS.get()
            )),
            Map.entry(Pair.of(DrugId.ALCOHOL, DrugId.COCAINE), new Hybrid(
                    () -> Blocks.REDSTONE_BLOCK.defaultBlockState(),
                    () -> Blocks.MUD.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.REDLINE_BRAMBLE.get()
            )),
            Map.entry(Pair.of(DrugId.COCAINE, DrugId.LSD), new Hybrid(
                    () -> Blocks.SEA_LANTERN.defaultBlockState(),
                    () -> Blocks.SMOOTH_QUARTZ.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.REDLINE_SPARK_BLOOM.get()
            )),
            Map.entry(Pair.of(DrugId.LSD, DrugId.METH), new Hybrid(
                    () -> Blocks.MAGMA_BLOCK.defaultBlockState(),
                    () -> Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.PRISM_LOTUS.get()
            )),
            Map.entry(Pair.of(DrugId.METH, DrugId.MUSHROOMS), new Hybrid(
                    () -> Blocks.MUSHROOM_STEM.defaultBlockState(),
                    () -> Blocks.BASALT.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.SPORE_BLOOM.get()
            )),
            Map.entry(Pair.of(DrugId.MUSHROOMS, DrugId.COFFEE), new Hybrid(
                    () -> Blocks.ROOTED_DIRT.defaultBlockState(),
                    () -> Blocks.MYCELIUM.defaultBlockState(),
                    () -> ModInnerDimensionBlocks.LUCID_CLOVER.get()
            ))
    );

    private InnerTransitionPalette() {
    }

    static DrugId chooseFeatureDrug(InnerTerrain.Sample sample, long hash) {
        if (!sample.transitionZone() || sample.primaryDrug() == sample.secondaryDrug()) {
            return sample.primaryDrug();
        }
        double roll = ((hash >>> 13) & 1023L) / 1024.0D;
        double secondaryChance = InnerNoise.clamp01(sample.secondaryWeight());
        return roll < secondaryChance ? sample.secondaryDrug() : sample.primaryDrug();
    }

    static boolean hasExplicitHybrid(DrugId first, DrugId second) {
        return HYBRIDS.containsKey(Pair.of(first, second));
    }

    static int explicitHybridCountForTest() {
        return HYBRIDS.size();
    }

    static BlockState surfaceAccent(InnerTerrain.Sample sample) {
        Hybrid hybrid = hybrid(sample);
        return hybrid == null ? sample.profile().accentBlock() : hybrid.surfaceAccent().get();
    }

    static BlockState shoreAccent(InnerTerrain.Sample sample) {
        Hybrid hybrid = hybrid(sample);
        return hybrid == null ? sample.profile().accentBlock() : hybrid.shoreAccent().get();
    }

    static BlockState floraAccent(InnerTerrain.Sample sample) {
        Hybrid hybrid = hybrid(sample);
        return hybrid == null ? sample.profile().flora().any(0L) : hybrid.flora().get().defaultBlockState();
    }

    static BlockState pathAccent(InnerTerrain.Sample sample) {
        Pair pair = Pair.of(sample.primaryDrug(), sample.secondaryDrug());
        if (pair.equals(Pair.of(DrugId.COFFEE, DrugId.TOBACCO))) {
            return Blocks.CRACKED_STONE_BRICKS.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.TOBACCO, DrugId.WEED))) {
            return Blocks.MOSSY_COBBLESTONE.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.WEED, DrugId.HASH))) {
            return Blocks.CALCITE.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.HASH, DrugId.ALCOHOL))) {
            return Blocks.SMOOTH_BASALT.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.ALCOHOL, DrugId.COCAINE))) {
            return Blocks.WHITE_CONCRETE.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.COCAINE, DrugId.LSD))) {
            return Blocks.PRISMARINE.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.LSD, DrugId.METH))) {
            return Blocks.POLISHED_BLACKSTONE.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.METH, DrugId.MUSHROOMS))) {
            return Blocks.MYCELIUM.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.MUSHROOMS, DrugId.COFFEE))) {
            return Blocks.ROOTED_DIRT.defaultBlockState();
        }
        return sample.profile().pathBlock();
    }

    static BlockState glowAccent(InnerTerrain.Sample sample) {
        Pair pair = Pair.of(sample.primaryDrug(), sample.secondaryDrug());
        if (pair.equals(Pair.of(DrugId.COFFEE, DrugId.TOBACCO))) {
            return Blocks.LANTERN.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.TOBACCO, DrugId.WEED))) {
            return Blocks.SHROOMLIGHT.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.WEED, DrugId.HASH))) {
            return Blocks.AMETHYST_BLOCK.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.HASH, DrugId.ALCOHOL))) {
            return Blocks.SEA_LANTERN.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.ALCOHOL, DrugId.COCAINE))) {
            return Blocks.REDSTONE_BLOCK.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.COCAINE, DrugId.LSD))) {
            return Blocks.SEA_LANTERN.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.LSD, DrugId.METH))) {
            return Blocks.MAGMA_BLOCK.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.METH, DrugId.MUSHROOMS))) {
            return Blocks.SHROOMLIGHT.defaultBlockState();
        }
        if (pair.equals(Pair.of(DrugId.MUSHROOMS, DrugId.COFFEE))) {
            return Blocks.GLOWSTONE.defaultBlockState();
        }
        return sample.profile().accentBlock();
    }

    private static Hybrid hybrid(InnerTerrain.Sample sample) {
        if (!sample.transitionZone()) {
            return null;
        }
        return HYBRIDS.get(Pair.of(sample.primaryDrug(), sample.secondaryDrug()));
    }

    private record Pair(DrugId first, DrugId second) {
        private static Pair of(DrugId first, DrugId second) {
            return first.networkId() <= second.networkId() ? new Pair(first, second) : new Pair(second, first);
        }
    }

    private record Hybrid(
            Supplier<BlockState> surfaceAccent,
            Supplier<BlockState> shoreAccent,
            Supplier<? extends Block> flora
    ) {
    }
}
