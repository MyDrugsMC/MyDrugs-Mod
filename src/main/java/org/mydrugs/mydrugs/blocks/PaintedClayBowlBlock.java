package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerTier;
import org.mydrugs.mydrugs.events.PsyMixerActivationHandler;
import org.mydrugs.mydrugs.items.ModItemTags;
import org.mydrugs.mydrugs.items.ModItems;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class PaintedClayBowlBlock extends Block {
    private static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 5.0D, 14.0D);

    public PaintedClayBowlBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            boolean sent = PsyBlueprintPreviewService.sendForPsychedelicInsight(
                    serverPlayer,
                    level,
                    pos,
                    player.getDirection().getOpposite()
            );
            return sent ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(ModItems.PSY_RECEPTACLE.get())) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer) {
                return PsyMixerActivationHandler.activate((net.minecraft.server.level.ServerLevel) level, pos, serverPlayer, PsyMixerTier.DORMANT)
                        ? InteractionResult.CONSUME
                        : InteractionResult.PASS;
            }
        }
        if (stack.is(ModItemTags.PSY_MIXER_AWAKENING_CORES)) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer serverPlayer
                    && PsyMixerActivationHandler.activate((net.minecraft.server.level.ServerLevel) level, pos, serverPlayer, PsyMixerTier.AWAKENED)) {
                if (!serverPlayer.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return useWithoutItem(state, level, pos, player, hit);
    }
}
