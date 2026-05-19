package org.mydrugs.mydrugs.recovery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.recovery.RecoveryRoomManager;

public final class RecoveryAnchorBlock extends Block {
    public RecoveryAnchorBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof ServerPlayer player) {
            RecoveryRoomManager.onAnchorPlaced(player, pos);
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            RecoveryRoomManager.inspectAnchor(serverPlayer, pos, player.isShiftKeyDown());
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, @Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, block, orientation, movedByPiston);
        if (!level.isClientSide()) {
            RecoveryRoomManager.invalidate(level, pos);
        }
    }
}
