package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.dimension.InnerDimensionSavedData;
import org.mydrugs.mydrugs.dimension.InnerDimensions;

import java.util.UUID;

public record InnerIslandContext(
        InnerDimensionSavedData.IslandState island,
        UUID owner,
        BlockPos centerPosition,
        boolean playerIsOwner,
        boolean playerInsideOwnIsland
) {
    public static @Nullable InnerIslandContext resolve(
            @Nullable ServerPlayer player,
            @Nullable InnerDimensionSavedData data
    ) {
        if (player == null
                || data == null
                || !player.level().dimension().equals(InnerDimensions.INNER_LEVEL)) {
            return null;
        }
        return resolveForInnerPosition(data, player.getUUID(), player.blockPosition());
    }

    static @Nullable InnerIslandContext resolveForInnerPosition(
            @Nullable InnerDimensionSavedData data,
            @Nullable UUID playerId,
            @Nullable BlockPos playerPos
    ) {
        if (data == null || playerId == null || playerPos == null) {
            return null;
        }
        int centerX = InnerTerrain.slotCenter(playerPos.getX());
        int centerZ = InnerTerrain.slotCenter(playerPos.getZ());
        InnerDimensionSavedData.IslandState island = data.findIslandBySlot(centerX, centerZ);
        if (island == null || island.owner() == null) {
            return null;
        }
        boolean playerIsOwner = island.owner().equals(playerId);
        return new InnerIslandContext(
                island,
                island.owner(),
                new BlockPos(centerX, InnerDimensionConstants.BASE_Y, centerZ),
                playerIsOwner,
                playerIsOwner
                        && island.centerX() == centerX
                        && island.centerZ() == centerZ
        );
    }

    public boolean contains(BlockPos pos) {
        return pos != null
                && InnerTerrain.slotCenter(pos.getX()) == island.centerX()
                && InnerTerrain.slotCenter(pos.getZ()) == island.centerZ();
    }

    public boolean allowsOwnerInteractionAt(BlockPos pos) {
        return playerInsideOwnIsland && contains(pos);
    }

    public int centerX() {
        return centerPosition.getX();
    }

    public int centerZ() {
        return centerPosition.getZ();
    }
}
