package org.mydrugs.mydrugs.effects.addiction.manager.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;

public final class SafeZoneManager {
    private static final int RADIUS = AddictionConstants.SAFE_ZONE_RADIUS;

    private SafeZoneManager() {}

    public static boolean isInSafeZone(ServerPlayer player) {
        BlockPos origin = player.blockPosition();

        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-RADIUS, -RADIUS, -RADIUS), origin.offset(RADIUS, RADIUS, RADIUS))) {
            if (player.level().getBlockState(pos).is(ModBlocks.RECOVERY_ANCHOR.get())) {
                return true;
            }
        }

        return false;
    }
}