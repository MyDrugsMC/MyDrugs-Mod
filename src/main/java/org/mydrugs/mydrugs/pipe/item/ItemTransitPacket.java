package org.mydrugs.mydrugs.pipe.item;

import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.mydrugs.mydrugs.pipe.network.PipeEndpoint;

import java.util.List;

public record ItemTransitPacket(
        ItemResource resource,
        int amount,
        PipeEndpoint source,
        PipeEndpoint target,
        List<BlockPos> path,
        int pathIndex,
        int progressTicks
) {
    public ItemTransitPacket {
        path = List.copyOf(path);
    }

    public ItemTransitPacket advance(int newProgressTicks, int newPathIndex) {
        return new ItemTransitPacket(
                this.resource,
                this.amount,
                this.source,
                this.target,
                this.path,
                newPathIndex,
                newProgressTicks
        );
    }
}
