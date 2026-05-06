package org.mydrugs.mydrugs.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.SniffingStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;
import org.mydrugs.mydrugs.items.ModItems;

public final class CocainePowderPileBlock extends Block {
    public enum Stage implements StringRepresentable {
        PILE,
        RAIL;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final EnumProperty<Stage> STAGE = EnumProperty.create("stage", Stage.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private static final VoxelShape PILE_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 2.0D, 13.0D);
    private static final VoxelShape RAIL_SHAPE_NS = Block.box(7.0D, 0.0D, 2.0D, 9.0D, 1.0D, 14.0D);
    private static final VoxelShape RAIL_SHAPE_EW = Block.box(2.0D, 0.0D, 7.0D, 14.0D, 1.0D, 9.0D);

    public CocainePowderPileBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(STAGE, Stage.PILE)
                        .setValue(FACING, Direction.NORTH)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE, FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(STAGE) == Stage.PILE) {
            return PILE_SHAPE;
        }
        Direction facing = state.getValue(FACING);
        return facing.getAxis() == Direction.Axis.Z ? RAIL_SHAPE_NS : RAIL_SHAPE_EW;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);
        return belowState.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (!canSurvive(state, level, pos)) {
            scheduledTickAccess.scheduleTick(pos, this, 1);
        }
        return state;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (canSurvive(state, level, pos)) {
            return;
        }
        if (state.getValue(STAGE) == Stage.PILE) {
            popResource(level, pos, new ItemStack(ModItems.COCAINE_POWDER.get()));
        }
        level.removeBlock(pos, false);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        // PILE + cupboard_piece -> RAIL
        if (state.getValue(STAGE) == Stage.PILE && stack.is(ModItems.CUPBOARD_PIECE.get())) {
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            Direction railFacing = player.getDirection();
            BlockState newState = state
                    .setValue(STAGE, Stage.RAIL)
                    .setValue(FACING, railFacing);
            level.setBlock(pos, newState, Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.WOOL_PLACE, SoundSource.BLOCKS, 0.4F, 1.6F);
            if (level instanceof ServerLevel sl) {
                sl.sendParticles(
                        ParticleTypes.WHITE_ASH,
                        pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                        8, 0.25, 0.05, 0.25, 0.0
                );
            }
            // Spec: do not consume the cupboard piece by default.
            return InteractionResult.SUCCESS;
        }
        // RAIL + anything held -> still consume the rail (let players use it without emptying their hand)
        if (state.getValue(STAGE) == Stage.RAIL) {
            return useWithoutItem(state, level, pos, player, hit);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (state.getValue(STAGE) != Stage.RAIL) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        DrugModel model = DrugRegistry.getDrug(DrugId.COCAINE);
        if (model == null) {
            return InteractionResult.PASS;
        }

        // TODO: Replace instant consumption with a client/server snorting animation.
        // The server should only consume the rail once the animation completes.
        startSnortingAnimation(serverPlayer, pos);

        MyDrugs.DRUG_USE_SERVICE.consume(serverPlayer, model, new SniffingStrategy(), DrugUseSource.ITEM);
        level.removeBlock(pos, false);

        level.playSound(null, pos, SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 0.5F, 1.6F);
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(
                    ParticleTypes.WHITE_ASH,
                    pos.getX() + 0.5, pos.getY() + 0.1, pos.getZ() + 0.5,
                    10, 0.2, 0.05, 0.2, 0.0
            );
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Hook reserved for a future client/server snorting animation.
     * For now, this is intentionally a no-op so consumption is instant.
     */
    private static void startSnortingAnimation(ServerPlayer player, BlockPos pos) {
        // TODO: send a client payload to play a snorting animation; defer rail removal until completion.
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel serverLevel
                && !player.getAbilities().instabuild
                && state.getValue(STAGE) == Stage.PILE) {
            popResource(serverLevel, pos, new ItemStack(ModItems.COCAINE_POWDER.get()));
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
