package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.GrindingBowlBlockEntity;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipes;

public class GrindingBowlBlock extends Block implements EntityBlock {
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(3, 0, 3, 13, 2, 13),   // base
            Block.box(2, 0, 2, 14, 4, 14)    // outer bowl bounds
    );

    public GrindingBowlBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GrindingBowlBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof GrindingBowlBlockEntity bowl)) {
            return InteractionResult.PASS;
        }

        if (hand == InteractionHand.OFF_HAND) return InteractionResult.PASS;

        if (level.isClientSide()) return InteractionResult.SUCCESS;

        // Grinding tool click
        if (stack.is(ModItems.GRINDING_TOOL.get())) {
            if (!bowl.canGrind()) {
                return InteractionResult.PASS;
            }

            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (bowl.grindOnce()) {
                level.playSound(null, pos, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.8F, 1.1F);
                return InteractionResult.CONSUME;
            }

            return InteractionResult.PASS;
        }


        // Insert input item
        if (bowl.isEmpty() && GrindingRecipes.get(stack, (ServerLevel) level) != null) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }

            if (bowl.insertOne(stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 0.9F);
                return InteractionResult.CONSUME;
            }
        }

        if (stack.isEmpty()) {
            return useWithoutItem(state, level, pos, player, hitResult);
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof GrindingBowlBlockEntity bowl) || bowl.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack out = bowl.removeStack();
        if (!player.addItem(out)) {
            Containers.dropItemStack(level, player.getX(), player.getY(), player.getZ(), out);
        }

        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.1F);
        return InteractionResult.CONSUME;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
//
//    @Override
//    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
//        return 1.0F;
//    }
//
//    @Override
//    protected boolean propagatesSkylightDown(BlockState state) {
//        return true;
//    }
//
//    @Override
//    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
//        return Shapes.empty();
//    }
//
//    @Override
//    protected VoxelShape getOcclusionShape(BlockState state) {
//        return Shapes.empty();
//    }
//
//    @Override
//    protected int getLightBlock(BlockState state) {
//        return 0;
//    }
//
//    @Override
//    protected boolean useShapeForLightOcclusion(BlockState state) {
//        return false;
//    }
}