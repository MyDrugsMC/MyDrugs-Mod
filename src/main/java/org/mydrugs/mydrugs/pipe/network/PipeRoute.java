package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public record PipeRoute(PipeEndpoint source, PipeEndpoint target, List<BlockPos> path) {
    public PipeRoute {
        path = List.copyOf(path);
    }

    public boolean isLoadedPath(ServerLevel level, PipeNetwork network) {
        for (BlockPos pos : this.path) {
            if (!level.isLoaded(pos) || !network.isLoadedPipe(pos)) {
                return false;
            }
        }
        return true;
    }
}
