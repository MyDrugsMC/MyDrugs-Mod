package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.FluidPumpBlockEntity;
import org.mydrugs.mydrugs.items.ModItems;

public class FluidPumpBlock extends Block implements EntityBlock {
    public static final EnumProperty<FluidPumpLoggedFluid> LOGGED_FLUID = EnumProperty.create("logged_fluid", FluidPumpLoggedFluid.class);
    public static final BooleanProperty CRANK = BooleanProperty.create("crank");

    public FluidPumpBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(LOGGED_FLUID, FluidPumpLoggedFluid.EMPTY)
                .setValue(CRANK, false));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidPumpLoggedFluid logged = FluidPumpLoggedFluid.fromFluid(context.getLevel().getFluidState(context.getClickedPos()).getType());
        return this.defaultBlockState().setValue(LOGGED_FLUID, logged);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LOGGED_FLUID, CRANK);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        FluidPumpLoggedFluid logged = state.getValue(LOGGED_FLUID);
        return logged == FluidPumpLoggedFluid.EMPTY ? super.getFluidState(state) : logged.sourceFluid().defaultFluidState();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluidPumpBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(ModItems.HAND_CRANK.get()) && !state.getValue(CRANK)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(CRANK, true), 3);
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!state.getValue(CRANK)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel && level.getBlockEntity(pos) instanceof FluidPumpBlockEntity pump) {
            pump.pumpOnce(serverLevel, Direction.UP, 50);
        }
        return InteractionResult.SUCCESS;
    }

}
