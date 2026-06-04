package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Optional;
import java.util.UUID;

public record PlayerEnvironmentSnapshot(
        long gameTime,
        UUID playerId,
        ResourceKey<Level> dimension,
        BlockPos playerPos,
        int chunkX,
        int chunkZ,
        RecoveryRoomReport recoveryRoom,
        boolean inSafeZone,
        int companionCount,
        boolean hasEdibleFood,
        boolean hasDiary,
        boolean hasHeadphones
) {
    public Optional<RecoveryRoomReport> recoveryRoomOptional() {
        return Optional.ofNullable(recoveryRoom);
    }
}
