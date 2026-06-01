package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;
import org.mydrugs.mydrugs.Config;

public final class PipeTransferTicker {
    private PipeTransferTicker() {
    }

    public static void tick(ServerLevel level, PipeNetworkManager manager) {
        int interval = transferIntervalTicks();
        if (level.getGameTime() % interval != 0L) {
            return;
        }
        ItemPipeNetworkLogic.tick(level, manager, interval);
        FluidPipeNetworkLogic.tick(level, manager, interval);
        GasPipeNetworkLogic.tick(level, manager, interval);
    }

    private static int transferIntervalTicks() {
        try {
            return Math.max(1, Config.SERVER.pipeTickInterval.get());
        } catch (Throwable ignored) {
            return 4;
        }
    }
}
