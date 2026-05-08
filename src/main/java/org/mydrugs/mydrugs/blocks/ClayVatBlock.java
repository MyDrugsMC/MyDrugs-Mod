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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.entity.ClayVatBlockEntity;

public class ClayVatBlock extends BaseEntityBlock {
    public static final MapCodec<ClayVatBlock> CODEC = simpleCodec(ClayVatBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(1, 0, 1, 15, 3, 15),   // base
            Block.box(1, 3, 1, 15, 16, 3),   // north wall
            Block.box(1, 3, 13, 15, 16, 15), // south wall
            Block.box(1, 3, 3, 3, 16, 13),   // west wall
            Block.box(13, 3, 3, 15, 16, 13)  // east wall
    );

    public ClayVatBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
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
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ClayVatBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof ClayVatBlockEntity vat)) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (vat.tryFillCoffeeCup(player, hand, stack)) {
            return InteractionResult.SUCCESS;
        }

        if (vat.tryBrewCoffee(player, stack)) {
            return InteractionResult.SUCCESS;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var fluidHandler = access.getCapability(Capabilities.Fluid.ITEM);

        // Only fluid containers interact with this block
        if (fluidHandler == null) {
            return InteractionResult.PASS;
        }

        // First try filling the held item from the vat
        if (vat.tryExtractFluidToHeld(player, hand, stack)) {
            return InteractionResult.SUCCESS;
        }

        // Then try emptying the held item into the vat
        if (vat.tryInsertFluidFromHeld(player, hand, stack)) {
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return InteractionResult.PASS;
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }
}
