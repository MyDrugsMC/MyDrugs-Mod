package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class MachineOrientation {
    private MachineOrientation() {
    }

    public static Direction front(BlockState state) {
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            return state.getValue(HorizontalDirectionalBlock.FACING);
        }
        if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    public static Direction toWorld(BlockState state, MachineLocalSide localSide) {
        return toWorld(front(state), localSide);
    }

    public static Direction toWorld(Direction front, MachineLocalSide localSide) {
        return switch (localSide) {
            case FRONT -> front;
            case BACK -> front.getOpposite();
            case TOP -> Direction.UP;
            case BOTTOM -> Direction.DOWN;
            // LEFT/RIGHT are from a player standing in front of the machine looking at its face.
            case LEFT -> front.getClockWise();
            case RIGHT -> front.getCounterClockWise();
        };
    }

    public static MachineLocalSide fromWorld(BlockState state, Direction worldSide) {
        Direction front = front(state);
        if (worldSide == Direction.UP) {
            return MachineLocalSide.TOP;
        }
        if (worldSide == Direction.DOWN) {
            return MachineLocalSide.BOTTOM;
        }
        if (worldSide == front) {
            return MachineLocalSide.FRONT;
        }
        if (worldSide == front.getOpposite()) {
            return MachineLocalSide.BACK;
        }
        if (worldSide == front.getClockWise()) {
            return MachineLocalSide.LEFT;
        }
        return MachineLocalSide.RIGHT;
    }

    public static Component displayName(MachineLocalSide localSide) {
        return Component.translatable("screen.mydrugs.machine_transfer.local_side." + localSide.getSerializedName());
    }
}
