package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.mydrugs.mydrugs.items.ModItems;

public class CoffeeCropBlock extends CropBlock {
    public static final MapCodec<CoffeeCropBlock> CODEC = simpleCodec(CoffeeCropBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final int REGROWTH_AGE = 5;
    private static final int TWO_BLOCK_AGE = 6;
    private static final VoxelShape[] LOWER_SHAPES = new VoxelShape[]{
            Block.box(4, 0, 4, 12, 4, 12),
            Block.box(3, 0, 3, 13, 6, 13),
            Block.box(2, 0, 2, 14, 8, 14),
            Block.box(2, 0, 2, 14, 10, 14),
            Block.box(1, 0, 1, 15, 12, 15),
            Block.box(1, 0, 1, 15, 15, 15),
            Block.box(1, 0, 1, 15, 16, 15),
            Block.box(1, 0, 1, 15, 16, 15)
    };
    private static final VoxelShape UPPER_SHAPE = Block.box(1, 0, 1, 15, 14, 15);

    public CoffeeCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<CoffeeCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModCrops.COFFEE_SEEDS.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.FARMLAND);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER && below.getValue(AGE) >= TWO_BLOCK_AGE;
        }

        BlockState below = level.getBlockState(pos.below());
        boolean farmland = mayPlaceOn(below, level, pos.below());
        if (!farmland || level.getRawBrightness(pos, 0) < 8) {
            return false;
        }
        int age = state.getValue(AGE);
        return age < TWO_BLOCK_AGE || level.getBlockState(pos.above()).isAir() || isUpperCoffee(level.getBlockState(pos.above()));
    }

    private boolean isUpperCoffee(BlockState state) {
        return state.is(this) && state.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, net.minecraft.core.Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return UPPER_SHAPE;
        }
        return LOWER_SHAPES[state.getValue(AGE)];
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return;
        }
        if (level.getRawBrightness(pos, 0) < 9) {
            return;
        }
        int age = state.getValue(AGE);
        if (age >= getMaxAge()) {
            ensureUpper(level, pos, state);
            return;
        }
        if (random.nextInt(4) == 0) {
            growToAge(level, pos, state, age + 1);
        }
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            pos = pos.below();
            state = level.getBlockState(pos);
            if (!state.is(this)) {
                return;
            }
        }
        growToAge(level, pos, state, Math.min(getMaxAge(), state.getValue(AGE) + getBonemealAgeIncrease(level)));
    }

    private void growToAge(Level level, BlockPos pos, BlockState state, int newAge) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return;
        }
        int clamped = Math.min(getMaxAge(), Math.max(0, newAge));
        level.setBlock(pos, state.setValue(AGE, clamped), Block.UPDATE_CLIENTS);
        if (clamped >= TWO_BLOCK_AGE) {
            ensureUpper(level, pos, state.setValue(AGE, clamped));
        } else if (isUpperCoffee(level.getBlockState(pos.above()))) {
            level.removeBlock(pos.above(), false);
        }
    }

    private void ensureUpper(Level level, BlockPos pos, BlockState lowerState) {
        BlockPos above = pos.above();
        BlockState upper = level.getBlockState(above);
        if (upper.isAir() || isUpperCoffee(upper)) {
            level.setBlock(above, lowerState.setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return tryHarvest(state, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return tryHarvest(state, level, pos, player);
    }

    private InteractionResult tryHarvest(BlockState clickedState, Level level, BlockPos clickedPos, Player player) {
        BlockPos lowerPos = clickedState.getValue(HALF) == DoubleBlockHalf.UPPER ? clickedPos.below() : clickedPos;
        BlockState lower = level.getBlockState(lowerPos);
        if (!lower.is(this) || lower.getValue(HALF) != DoubleBlockHalf.LOWER || lower.getValue(AGE) < getMaxAge()) {
            return InteractionResult.PASS;
        }

        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        popResource(level, lowerPos, new ItemStack(ModItems.COFFEE_CHERRIES.get(), 2 + level.random.nextInt(3)));
        if (level.random.nextFloat() < 0.55F) {
            popResource(level, lowerPos, new ItemStack(ModCrops.COFFEE_SEEDS.get(), 1));
        }
        level.playSound(null, lowerPos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + level.random.nextFloat() * 0.4F);
        growToAge(level, lowerPos, lower, REGROWTH_AGE);
        return InteractionResult.SUCCESS_SERVER;
    }
}
