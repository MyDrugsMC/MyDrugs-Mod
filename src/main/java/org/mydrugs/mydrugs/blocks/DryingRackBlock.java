package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.DryingRackBlockEntity;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;

public final class DryingRackBlock extends BaseEntityBlock {
    public static final MapCodec<DryingRackBlock> CODEC = simpleCodec(DryingRackBlock::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DryingRackBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new DryingRackBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, ModBlockEntities.DRYING_RACK.get(), DryingRackBlockEntity::serverTick);
    }

    // Right-click with an item: insert into clicked quadrant
    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (heldStack.isEmpty()) {
            return InteractionResult.TRY_WITH_EMPTY_HAND;
        }

        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }

        int slot = getClickedSlot(state, pos, hit);
        if (!rack.canInsert(slot, heldStack)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack toInsert = heldStack.copy();
        rack.insert(slot, toInsert);

        if (!player.getAbilities().instabuild) {
            player.setItemInHand(hand, ItemStack.EMPTY);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    // Empty-hand right-click: extract from clicked quadrant
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof DryingRackBlockEntity rack)) {
            return InteractionResult.PASS;
        }

        int slot = getClickedSlot(state, pos, hit);
        if (rack.getStack(slot).isEmpty()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        ItemStack extracted = rack.extract(slot);
        if (!player.addItem(extracted)) {
            player.drop(extracted, false);
        }

        return InteractionResult.SUCCESS_SERVER;
    }

    private static int getClickedSlot(BlockState state, BlockPos pos, BlockHitResult hit) {
        double localX = hit.getLocation().x - pos.getX();
        double localZ = hit.getLocation().z - pos.getZ();

        // Convert world-space click into NORTH-facing rack local space.
        double x = localX;
        double z = localZ;

        Direction facing = state.getValue(FACING);
        switch (facing) {
            case NORTH -> {
            }
            case SOUTH -> {
                x = 1.0 - localX;
                z = 1.0 - localZ;
            }
            case WEST -> {
                x = localZ;
                z = 1.0 - localX;
            }
            case EAST -> {
                x = 1.0 - localZ;
                z = localX;
            }
            default -> {
            }
        }

        boolean right = x >= 0.5;
        boolean bottom = z >= 0.5;

        if (!right && !bottom) return 0;
        if (right && !bottom) return 1;
        if (!right) return 2;
        return 3;
    }
}