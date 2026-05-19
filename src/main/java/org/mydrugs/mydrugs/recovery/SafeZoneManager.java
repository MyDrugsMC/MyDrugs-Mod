package org.mydrugs.mydrugs.recovery;

import net.minecraft.server.level.ServerPlayer;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class SafeZoneManager {
    private static final Map<ServerPlayer, SafeZoneCache> SAFE_ZONE_CACHE = new WeakHashMap<>();

    private SafeZoneManager() {
    }

    public static boolean isInSafeZone(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        SafeZoneCache cached = SAFE_ZONE_CACHE.get(player);
        if (cached != null && cached.gameTime == gameTime) {
            return cached.inSafeZone;
        }

        Optional<RecoveryRoomReport> report = RecoveryRoomManager.getBestRoom(player);
        boolean inSafeZone = report.filter(RecoveryRoomManager::isValidRecoveryRoom).isPresent();
        SAFE_ZONE_CACHE.put(player, new SafeZoneCache(gameTime, inSafeZone));
        return inSafeZone;
    }

    private record SafeZoneCache(long gameTime, boolean inSafeZone) {
    }
}
