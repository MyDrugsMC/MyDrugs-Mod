package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;

public final class PipeNetworkDiagnostics {
    private static final long REPORT_INTERVAL_TICKS = 20L * 60L;

    private static long lastReportTick = -1L;
    private static long networksTicked;
    private static long endpointsScanned;
    private static long candidatesConsidered;
    private static long transfersAttempted;
    private static long transfersSucceeded;
    private static long rebuilds;

    private PipeNetworkDiagnostics() {
    }

    public static void networkTicked(PipeResourceKind kind, int endpoints) {
        if (!enabled()) {
            return;
        }

        networksTicked++;
        endpointsScanned += endpoints;
    }

    public static void candidateConsidered() {
        if (enabled()) {
            candidatesConsidered++;
        }
    }

    public static void transferAttempted() {
        if (enabled()) {
            transfersAttempted++;
        }
    }

    public static void transferSucceeded() {
        if (enabled()) {
            transfersSucceeded++;
        }
    }

    public static void rebuild() {
        if (enabled()) {
            rebuilds++;
        }
    }

    public static void reportIfDue(ServerLevel level) {
        if (!enabled()) {
            reset(level.getGameTime());
            return;
        }

        long now = level.getGameTime();
        if (lastReportTick < 0L) {
            lastReportTick = now;
            return;
        }
        if (now - lastReportTick < REPORT_INTERVAL_TICKS) {
            return;
        }

        MyDrugs.getLOGGER().info(
                "Pipe diagnostics/minute: networksTicked={}, endpointsScanned={}, candidatesConsidered={}, transfersAttempted={}, transfersSucceeded={}, rebuilds={}",
                networksTicked,
                endpointsScanned,
                candidatesConsidered,
                transfersAttempted,
                transfersSucceeded,
                rebuilds
        );
        reset(now);
    }

    private static boolean enabled() {
        return Config.SERVER.enablePipeDebugCounters.get();
    }

    private static void reset(long tick) {
        lastReportTick = tick;
        networksTicked = 0L;
        endpointsScanned = 0L;
        candidatesConsidered = 0L;
        transfersAttempted = 0L;
        transfersSucceeded = 0L;
        rebuilds = 0L;
    }
}
