package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;

public record PipeEndpoint(
        PipeEndpointType type,
        BlockPos pipePos,
        Direction pipeSide,
        BlockPos targetPos,
        Direction targetSide,
        @Nullable PipeFilterConfig filter
) {
    public static PipeEndpoint input(BlockPos pipePos, Direction pipeSide, @Nullable PipeFilterConfig filter) {
        return new PipeEndpoint(
                PipeEndpointType.INPUT,
                pipePos,
                pipeSide,
                pipePos.relative(pipeSide),
                pipeSide.getOpposite(),
                filter
        );
    }

    public static PipeEndpoint output(BlockPos pipePos, Direction pipeSide, @Nullable PipeFilterConfig filter) {
        return new PipeEndpoint(
                PipeEndpointType.OUTPUT,
                pipePos,
                pipeSide,
                pipePos.relative(pipeSide),
                pipeSide.getOpposite(),
                filter
        );
    }
}
