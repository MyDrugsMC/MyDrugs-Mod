package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerPartBlockEntity;

public final class FormedPsyMixerPartBlock extends BaseEntityBlock {
    public static final MapCodec<FormedPsyMixerPartBlock> CODEC = simpleCodec(FormedPsyMixerPartBlock::new);

    public FormedPsyMixerPartBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FormedPsyMixerPartBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return routeToCore(level, pos, player);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return routeToCore(level, pos, player);
    }

    private static InteractionResult routeToCore(Level level, BlockPos pos, Player player) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof FormedPsyMixerPartBlockEntity part)) {
            return InteractionResult.PASS;
        }
        BlockPos corePos = part.getCorePos();
        if (corePos == null) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity core) {
            serverPlayer.openMenu(core, buf -> buf.writeBlockPos(corePos));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof FormedPsyMixerPartBlockEntity part) {
            BlockPos corePos = part.getCorePos();
            if (corePos != null && level.getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity core) {
                core.onPartBroken(level, pos);
            } else {
                level.removeBlock(pos, false);
            }
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }
}
