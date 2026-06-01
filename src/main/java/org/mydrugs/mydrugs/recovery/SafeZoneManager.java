package org.mydrugs.mydrugs.recovery;

import net.minecraft.server.level.ServerPlayer;

public final class SafeZoneManager {
    private SafeZoneManager() {
    }

    public static boolean isInSafeZone(ServerPlayer player) {
        return PlayerRecoveryEnvironmentCache.snapshot(player).inSafeZone();
    }
}
