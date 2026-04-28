package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public final class PipeNetworkScanner {
    private PipeNetworkScanner() {
    }

    public static PipeNetwork scan(ServerLevel level, PipeNetworkKey key, BlockPos seed, PipeResourceKind kind) {
        PipeNetwork network = new PipeNetwork(key, kind);
        ArrayDeque<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        open.add(seed);

        while (!open.isEmpty()) {
            BlockPos pos = open.removeFirst();
            if (!visited.add(pos) || !level.isLoaded(pos)) {
                continue;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof PipeBlockEntity pipe) || pipe.kind() != kind) {
                continue;
            }

            network.nodes().put(pos, new PipeNode(pos, pipe.copySideConfigs()));

            for (Direction direction : Direction.values()) {
                PipeConnectionMode mode = pipe.getSideConfig(direction).mode();
                if (mode == PipeConnectionMode.PIPE) {
                    BlockPos next = pos.relative(direction);
                    if (canConnectPipe(level, next, direction.getOpposite(), kind)) {
                        open.add(next);
                    }
                } else if (mode == PipeConnectionMode.INPUT && level.isLoaded(pos.relative(direction))) {
                    network.inputs().add(PipeEndpoint.input(pos, direction, pipe.getSideConfig(direction).filter()));
                } else if (mode == PipeConnectionMode.OUTPUT && level.isLoaded(pos.relative(direction))) {
                    network.outputs().add(PipeEndpoint.output(pos, direction, pipe.getSideConfig(direction).filter()));
                }
            }
        }

        network.rebuildRoutes(level);
        return network;
    }

    private static boolean canConnectPipe(ServerLevel level, BlockPos pos, Direction side, PipeResourceKind kind) {
        if (!level.isLoaded(pos)) {
            return false;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof PipeBlockEntity pipe
                && pipe.kind() == kind
                && pipe.getSideConfig(side).mode() == PipeConnectionMode.PIPE;
    }
}
