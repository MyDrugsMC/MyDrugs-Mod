package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;

import java.util.Map;
import java.util.WeakHashMap;

public final class SafeZoneManager {
    private static final int RADIUS = AddictionConstants.SAFE_ZONE_RADIUS;
    private static final Map<ServerPlayer, SafeZoneCache> SAFE_ZONE_CACHE = new WeakHashMap<>();

    private SafeZoneManager() {
    }

    public static boolean isInSafeZone(ServerPlayer player) {
        long gameTime = player.level().getGameTime();
        SafeZoneCache cached = SAFE_ZONE_CACHE.get(player);
        if (cached != null && cached.gameTime == gameTime) {
            return cached.inSafeZone;
        }

        BlockPos origin = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RADIUS, -RADIUS, -RADIUS), origin.offset(RADIUS, RADIUS, RADIUS))) {
            if (player.level().getBlockState(pos).is(ModBlocks.RECOVERY_ANCHOR.get())) {
                SAFE_ZONE_CACHE.put(player, new SafeZoneCache(gameTime, true));
                return true;
            }
        }

        SAFE_ZONE_CACHE.put(player, new SafeZoneCache(gameTime, false));
        return false;
    }

    private record SafeZoneCache(long gameTime, boolean inSafeZone) {
    }
}
