package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class PsyMixerMultiblock {
    public static final int SLOT_COUNT = 6;
    public static final int SLOT_BASE = 0;
    public static final int SLOT_MATERIAL = 1;
    public static final int SLOT_CATALYST = 2;
    public static final int SLOT_STABILIZER = 3;
    public static final int SLOT_VESSEL = 4;
    public static final int SLOT_OUTPUT = 5;

    private static final List<Slot> TEMPLATE = buildTemplate();

    private PsyMixerMultiblock() {
    }

    public static List<Slot> template() {
        return TEMPLATE;
    }

    public static @Nullable Match validate(LevelAccessor level, BlockPos bowlPos) {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            if (matchesRotation(level, bowlPos, facing)) {
                List<PlacedSlot> placed = new ArrayList<>(TEMPLATE.size());
                for (Slot slot : TEMPLATE) {
                    BlockPos pos = bowlPos.offset(rotate(slot.localOffset, facing));
                    placed.add(new PlacedSlot(slot, pos));
                }
                return new Match(facing, placed);
            }
        }
        return null;
    }

    private static boolean matchesRotation(LevelAccessor level, BlockPos bowlPos, Direction facing) {
        for (Slot slot : TEMPLATE) {
            BlockPos pos = bowlPos.offset(rotate(slot.localOffset, facing));
            BlockState state = level.getBlockState(pos);
            if (slot.expected == null) {
                if (!state.isAir()) {
                    return false;
                }
            } else {
                if (!state.is(slot.expected.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BlockPos rotate(BlockPos local, Direction facing) {
        int x = local.getX();
        int y = local.getY();
        int z = local.getZ();
        return switch (facing) {
            case NORTH -> new BlockPos(x, y, z);
            case SOUTH -> new BlockPos(-x, y, -z);
            case EAST -> new BlockPos(-z, y, x);
            case WEST -> new BlockPos(z, y, -x);
            default -> new BlockPos(x, y, z);
        };
    }

    private static List<Slot> buildTemplate() {
        List<Slot> slots = new ArrayList<>();
        // Layer Y=0
        slots.add(new Slot(new BlockPos(-1, 0, 0), () -> ModBlocks.WOVEN_VINE_FRAME.get()));
        slots.add(new Slot(new BlockPos(0, 0, 0), () -> ModBlocks.PAINTED_CLAY_BOWL.get()));
        slots.add(new Slot(new BlockPos(1, 0, 0), () -> ModBlocks.WOVEN_VINE_FRAME.get()));
        slots.add(new Slot(new BlockPos(-1, 0, 1), () -> ModBlocks.RITUAL_BARK.get()));
        slots.add(new Slot(new BlockPos(0, 0, 1), () -> ModBlocks.CHARCOAL_GLYPH_BLOCK.get()));
        slots.add(new Slot(new BlockPos(1, 0, 1), () -> ModBlocks.RITUAL_BARK.get()));
        // Layer Y=1
        slots.add(new Slot(new BlockPos(-1, 1, 0), () -> ModBlocks.HANGING_VINE_BUNDLE.get()));
        slots.add(new Slot(new BlockPos(0, 1, 0), null));
        slots.add(new Slot(new BlockPos(1, 1, 0), () -> ModBlocks.HANGING_VINE_BUNDLE.get()));
        slots.add(new Slot(new BlockPos(-1, 1, 1), () -> ModBlocks.MYCELIAL_PADDING.get()));
        slots.add(new Slot(new BlockPos(0, 1, 1), () -> ModBlocks.WOVEN_VINE_FRAME.get()));
        slots.add(new Slot(new BlockPos(1, 1, 1), () -> ModBlocks.MYCELIAL_PADDING.get()));
        return List.copyOf(slots);
    }

    public record Slot(BlockPos localOffset, @Nullable java.util.function.Supplier<Block> expected) {
    }

    public record PlacedSlot(Slot slot, BlockPos worldPos) {
    }

    public record Match(Direction facing, List<PlacedSlot> placed) {
    }
}
