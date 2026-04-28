package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;

import java.util.List;

public record PipeRoute(PipeEndpoint source, PipeEndpoint target, List<BlockPos> path) {
    public PipeRoute {
        path = List.copyOf(path);
    }

    public boolean isLoadedPath(PipeNetwork network) {
        return this.path.stream().allMatch(network::isLoadedPipe);
    }
}
