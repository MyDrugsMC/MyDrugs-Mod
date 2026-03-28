package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.DryerBlockEntity;

public final class DryerBlock extends BaseEntityBlock implements EntityBlock {
    private static final VoxelShape SHAPE = box(0.0, 0.0, 0.0, 16.0, 11.2, 16.0);

    public DryerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return ModBlockTypes.DRYER_CODEC.get();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryerBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide()
                ? null
                : createTickerHelper(type, ModBlockEntities.DRYER.get(), DryerBlockEntity::serverTick);
    }

    private static int slotFromHit(BlockHitResult hit, BlockPos pos) {
        double localX = hit.getLocation().x - pos.getX();
        double localZ = hit.getLocation().z - pos.getZ();

        int col = localX < 0.5 ? 0 : 1;
        int row = localZ < 0.5 ? 0 : 1;
        return row * 2 + col;
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
        if (stack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!(level.getBlockEntity(pos) instanceof DryerBlockEntity dryer)) {
            return InteractionResult.PASS;
        }

        int slot = slotFromHit(hitResult, pos);
        if (!dryer.canInsert(slot, stack)) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            ItemStack toInsert = stack.copy();
            if (dryer.insert(slot, toInsert) && !player.getAbilities().instabuild) {
                player.setItemInHand(hand, ItemStack.EMPTY);
            }
        }

        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof DryerBlockEntity dryer)) {
            return InteractionResult.PASS;
        }

        int slot = slotFromHit(hitResult, pos);
        ItemStack extracted = dryer.getStack(slot);
        if (extracted.isEmpty()) {
            return InteractionResult.PASS;
        }

        if (!level.isClientSide()) {
            ItemStack stack = dryer.extract(slot);
            if (!player.addItem(stack)) {
                player.drop(stack, false);
            }
        }

        return InteractionResult.SUCCESS;
    }
}