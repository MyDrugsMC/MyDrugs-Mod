package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record InnerFloraPalette(
        List<Supplier<? extends Block>> groundCover,
        List<Supplier<? extends Block>> flowers,
        List<Supplier<? extends Block>> shrubs,
        List<Supplier<? extends Block>> reeds,
        List<Supplier<? extends Block>> glow,
        List<Supplier<? extends Block>> danger
) {
    public InnerFloraPalette {
        groundCover = List.copyOf(groundCover);
        flowers = List.copyOf(flowers);
        shrubs = List.copyOf(shrubs);
        reeds = List.copyOf(reeds);
        glow = List.copyOf(glow);
        danger = List.copyOf(danger);
    }

    public boolean hasAny() {
        return !groundCover.isEmpty()
                || !flowers.isEmpty()
                || !shrubs.isEmpty()
                || !reeds.isEmpty()
                || !glow.isEmpty()
                || !danger.isEmpty();
    }

    public int categoryCount() {
        int count = 0;
        count += groundCover.isEmpty() ? 0 : 1;
        count += flowers.isEmpty() ? 0 : 1;
        count += shrubs.isEmpty() ? 0 : 1;
        count += reeds.isEmpty() ? 0 : 1;
        count += glow.isEmpty() ? 0 : 1;
        count += danger.isEmpty() ? 0 : 1;
        return count;
    }

    public List<Supplier<? extends Block>> allSuppliers() {
        List<Supplier<? extends Block>> result = new ArrayList<>();
        result.addAll(groundCover);
        result.addAll(flowers);
        result.addAll(shrubs);
        result.addAll(reeds);
        result.addAll(glow);
        result.addAll(danger);
        return List.copyOf(result);
    }

    public BlockState groundCover(long hash) {
        return pick(groundCover, hash);
    }

    public BlockState flower(long hash) {
        return pick(flowers.isEmpty() ? groundCover : flowers, hash);
    }

    public BlockState shrub(long hash) {
        return pick(shrubs.isEmpty() ? groundCover : shrubs, hash);
    }

    public BlockState reed(long hash) {
        return pick(reeds.isEmpty() ? groundCover : reeds, hash);
    }

    public BlockState glow(long hash) {
        return pick(glow.isEmpty() ? flowers : glow, hash);
    }

    public BlockState danger(long hash) {
        return pick(danger.isEmpty() ? shrubs : danger, hash);
    }

    public BlockState any(long hash) {
        List<Supplier<? extends Block>> all = allSuppliers();
        return pick(all.isEmpty() ? List.of(() -> ModInnerDimensionBlocks.BREATH_GRASS.get()) : all, hash);
    }

    private static BlockState pick(List<Supplier<? extends Block>> blocks, long hash) {
        if (blocks.isEmpty()) {
            return ModInnerDimensionBlocks.BREATH_GRASS.get().defaultBlockState();
        }
        int index = Math.floorMod((int) hash, blocks.size());
        return blocks.get(index).get().defaultBlockState();
    }
}
