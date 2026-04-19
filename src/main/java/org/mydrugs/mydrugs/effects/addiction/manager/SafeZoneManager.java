package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.blocks.ModBlocks;

public final class SafeZoneManager {
    private static final int RADIUS = 8;

    private SafeZoneManager() {
    }

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