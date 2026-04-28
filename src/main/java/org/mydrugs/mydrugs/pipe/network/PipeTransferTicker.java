package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;

public final class PipeTransferTicker {
    private PipeTransferTicker() {
    }

    public static void tick(ServerLevel level, PipeNetworkManager manager) {
        ItemPipeNetworkLogic.tick(level, manager);
        FluidPipeNetworkLogic.tick(level, manager);
        GasPipeNetworkLogic.tick(level, manager);
    }
}
