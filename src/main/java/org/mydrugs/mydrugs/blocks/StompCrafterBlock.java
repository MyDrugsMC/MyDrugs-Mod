package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.StompCrafterBlockEntity;

public class StompCrafterBlock extends BaseEntityBlock {
    public StompCrafterBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(StompCrafterBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new StompCrafterBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack,
                                          BlockState state,
                                          Level level,
                                          BlockPos pos,
                                          Player player,
                                          InteractionHand hand,
                                          BlockHitResult hitResult) {
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof StompCrafterBlockEntity crafter)) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }


        // Client only predicts success and stops here.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }


        // Client and server both evaluate the same condition.
        if (!crafter.canAcceptInsertion((ServerLevel) level, stack)) {
            return InteractionResult.FAIL;
        }

        // Server performs the real mutation.
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        crafter.insertAcceptedItem(stack.copyWithCount(1));

        level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.8F, 1.1F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, double fallDistance) {
        super.fallOn(level, state, pos, entity, fallDistance);

        if (level.isClientSide()) {
            return;
        }

        if (!(entity instanceof Player player)) {
            return;
        }

        if (fallDistance < 1.5D) {
            return;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof StompCrafterBlockEntity crafter && level instanceof ServerLevel serverLevel) {
            crafter.addProgressFromFall(serverLevel, fallDistance, player);
        }
    }
}
