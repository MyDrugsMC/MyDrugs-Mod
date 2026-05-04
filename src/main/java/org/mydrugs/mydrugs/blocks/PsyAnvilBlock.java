package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.PsyAnvilBlockEntity;

public final class PsyAnvilBlock extends BaseEntityBlock {
    public static final MapCodec<PsyAnvilBlock> CODEC = simpleCodec(PsyAnvilBlock::new);

    public PsyAnvilBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PsyAnvilBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PsyAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player instanceof ServerPlayer serverPlayer && PsyAnvilBlockEntity.isHammer(stack)) {
            return anvil.craftWithHammer(serverPlayer, stack) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        if (stack.isEmpty()) {
            return useWithoutItem(state, level, pos, player, hitResult);
        }
        return anvil.insertOne(stack, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PsyAnvilBlockEntity anvil)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        System.out.println("trying to take last...");
        return anvil.takeLast(player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof PsyAnvilBlockEntity anvil) {
            Containers.dropContents(level, pos, anvil);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

}
