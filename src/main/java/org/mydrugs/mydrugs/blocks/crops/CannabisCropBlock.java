package org.mydrugs.mydrugs.blocks.crops;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class CannabisCropBlock extends CropBlock {
    public static final MapCodec<CannabisCropBlock> CODEC = simpleCodec(CannabisCropBlock::new);
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    public static final int TALL_START_AGE = 4;

    private static final VoxelShape[] LOWER_SHAPES = new VoxelShape[]{
            Block.box(2, 0, 2, 14, 2, 14),
            Block.box(2, 0, 2, 14, 4, 14),
            Block.box(2, 0, 2, 14, 6, 14),
            Block.box(2, 0, 2, 14, 8, 14),
            Block.box(2, 0, 2, 14, 16, 14),
            Block.box(2, 0, 2, 14, 16, 14),
            Block.box(2, 0, 2, 14, 16, 14),
            Block.box(2, 0, 2, 14, 16, 14)
    };
    private static final VoxelShape UPPER_SHAPE = Block.box(2, 0, 2, 14, 16, 14);

    public CannabisCropBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    public MapCodec<CannabisCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, HALF);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModCrops.CANNABIS_SEEDS.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public BlockState getStateForAge(int age) {
        return this.defaultBlockState()
                .setValue(AGE, age)
                .setValue(HALF, DoubleBlockHalf.LOWER);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER && !this.isMaxAge(state);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return;
        }

        int age = this.getAge(state);
        if (age == TALL_START_AGE - 1 && !canPlaceUpper(level, pos)) {
            return;
        }

        super.randomTick(state, level, pos, random);

        BlockState newState = level.getBlockState(pos);
        if (newState.is(this) && newState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            syncUpperHalf(level, pos, newState);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        BlockPos lowerPos = getLowerPos(pos, state);
        BlockState lowerState = level.getBlockState(lowerPos);

        if (!lowerState.is(this) || lowerState.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return false;
        }

        int age = lowerState.getValue(AGE);
        return age < this.getMaxAge()
                && (age < TALL_START_AGE - 1 || canPlaceUpper(level, lowerPos));
    }

    @Override
    public void growCrops(Level level, BlockPos pos, BlockState state) {
        BlockPos lowerPos = getLowerPos(pos, state);
        BlockState lowerState = level.getBlockState(lowerPos);

        if (!lowerState.is(this) || lowerState.getValue(HALF) != DoubleBlockHalf.LOWER) {
            return;
        }

        int currentAge = this.getAge(lowerState);
        int nextAge = Math.min(this.getMaxAge(), currentAge + Mth.nextInt(level.random, 2, 5));

        if (nextAge >= TALL_START_AGE && !canPlaceUpper(level, lowerPos)) {
            nextAge = TALL_START_AGE - 1;
        }

        BlockState newLower = this.getStateForAge(nextAge);
        level.setBlock(lowerPos, newLower, Block.UPDATE_ALL);

        if (level instanceof ServerLevel serverLevel) {
            syncUpperHalf(serverLevel, lowerPos, newLower);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this)
                    && below.getValue(HALF) == DoubleBlockHalf.LOWER
                    && below.getValue(AGE) >= TALL_START_AGE;
        }

        return super.canSurvive(state, level, pos);
    }

    @Override
    public BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess scheduledTickAccess,
            BlockPos pos,
            Direction direction,
            BlockPos neighborPos,
            BlockState neighborState,
            RandomSource random
    ) {
        DoubleBlockHalf half = state.getValue(HALF);

        if (half == DoubleBlockHalf.UPPER) {
            if (direction == Direction.DOWN) {
                if (neighborState.is(this)
                        && neighborState.getValue(HALF) == DoubleBlockHalf.LOWER
                        && neighborState.getValue(AGE) >= TALL_START_AGE) {
                    return state.setValue(AGE, neighborState.getValue(AGE));
                }

                return Blocks.AIR.defaultBlockState();
            }

            return state;
        }

        if (!state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }

        if (direction == Direction.UP
                && state.getValue(AGE) >= TALL_START_AGE
                && (!neighborState.is(this) || neighborState.getValue(HALF) != DoubleBlockHalf.UPPER)) {
            return Blocks.AIR.defaultBlockState();
        }

        return state;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide()) {
            if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
                BlockPos lowerPos = pos.below();
                BlockState lowerState = level.getBlockState(lowerPos);

                if (lowerState.is(this) && lowerState.getValue(HALF) == DoubleBlockHalf.LOWER) {
                    level.destroyBlock(lowerPos, !player.isCreative(), player);
                }
            } else if (state.getValue(AGE) >= TALL_START_AGE) {
                BlockPos upperPos = pos.above();
                BlockState upperState = level.getBlockState(upperPos);

                if (upperState.is(this) && upperState.getValue(HALF) == DoubleBlockHalf.UPPER) {
                    level.destroyBlock(upperPos, false, player);
                }
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            return UPPER_SHAPE;
        }

        return LOWER_SHAPES[state.getValue(AGE)];
    }

    private static boolean canPlaceUpper(LevelReader level, BlockPos lowerPos) {
        BlockState above = level.getBlockState(lowerPos.above());
        return above.isAir()
                || above.getBlock() instanceof CannabisCropBlock
                && above.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    private static BlockPos getLowerPos(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
    }

    private void syncUpperHalf(ServerLevel level, BlockPos lowerPos, BlockState lowerState) {
        int age = lowerState.getValue(AGE);
        BlockPos upperPos = lowerPos.above();

        if (age >= TALL_START_AGE) {
            BlockState upperState = this.defaultBlockState()
                    .setValue(AGE, age)
                    .setValue(HALF, DoubleBlockHalf.UPPER);

            level.setBlock(upperPos, upperState, Block.UPDATE_ALL);
        } else {
            BlockState above = level.getBlockState(upperPos);

            if (above.is(this) && above.getValue(HALF) == DoubleBlockHalf.UPPER) {
                level.removeBlock(upperPos, false);
            }
        }
    }
}
