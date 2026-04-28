package org.mydrugs.mydrugs.pipe.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.blockentity.PipeBlockEntity;
import org.mydrugs.mydrugs.pipe.network.PipeNetworkDirtyReason;
import org.mydrugs.mydrugs.pipe.network.PipeNetworkManager;

public class PipeBlock extends BaseEntityBlock {
    private static final int SHAPE_KEY_BITS_PER_SIDE = 2;
    private static final int SHAPE_CACHE_SIZE = 1 << (Direction.values().length * SHAPE_KEY_BITS_PER_SIDE);

    private static final VoxelShape CORE_SHAPE = Block.box(5, 5, 5, 11, 11, 11);
    private static final VoxelShape[] ARM_SHAPES = new VoxelShape[Direction.values().length];
    private static final VoxelShape[] SHAPE_CACHE = new VoxelShape[SHAPE_CACHE_SIZE];

    private static final MapCodec<PipeBlock> CODEC = simpleCodec(properties -> new PipeBlock(
            properties,
            PipeResourceKind.ITEM,
            PipeTier.BASIC
    ));

    static {
        ARM_SHAPES[Direction.NORTH.ordinal()] = Block.box(5, 5, 0, 11, 11, 5);
        ARM_SHAPES[Direction.SOUTH.ordinal()] = Block.box(5, 5, 11, 11, 11, 16);
        ARM_SHAPES[Direction.WEST.ordinal()] = Block.box(0, 5, 5, 5, 11, 11);
        ARM_SHAPES[Direction.EAST.ordinal()] = Block.box(11, 5, 5, 16, 11, 11);
        ARM_SHAPES[Direction.DOWN.ordinal()] = Block.box(5, 0, 5, 11, 5, 11);
        ARM_SHAPES[Direction.UP.ordinal()] = Block.box(5, 11, 5, 11, 16, 11);

        SHAPE_CACHE[0] = CORE_SHAPE;
    }

    private final PipeResourceKind kind;
    private final PipeTier tier;

    public PipeBlock(Properties properties, PipeResourceKind kind, PipeTier tier) {
        super(properties);
        this.kind = kind;
        this.tier = tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        PipeNetworkManager.markDirty(level, pos, this.kind, PipeNetworkDirtyReason.PIPE_PLACED);
    }

    @Override
    protected void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            @Nullable Orientation orientation,
            boolean movedByPiston
    ) {
        PipeNetworkManager.markDirty(level, pos, this.kind, PipeNetworkDirtyReason.NEIGHBOR_CHANGED);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        PipeNetworkManager.markDirty(level, pos, this.kind, PipeNetworkDirtyReason.PIPE_REMOVED);
        for (Direction direction : Direction.values()) {
            PipeNetworkManager.markDirty(level, pos.relative(direction), this.kind, PipeNetworkDirtyReason.PIPE_REMOVED);
        }
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForKey(this.getShapeKey(level, pos));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return this.getShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    private int getShapeKey(BlockGetter level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PipeBlockEntity pipe)) {
            return 0;
        }

        // Side mode lives in the block entity to avoid 4^6 blockstate variants per pipe block.
        int key = 0;
        for (Direction direction : Direction.values()) {
            PipeConnectionMode mode = pipe.getSideConfig(direction).mode();
            key |= mode.ordinal() << (direction.ordinal() * SHAPE_KEY_BITS_PER_SIDE);
        }
        return key;
    }

    private static VoxelShape shapeForKey(int key) {
        VoxelShape cached = SHAPE_CACHE[key];
        if (cached != null) {
            return cached;
        }

        VoxelShape shape = CORE_SHAPE;
        for (Direction direction : Direction.values()) {
            int ordinal = (key >> (direction.ordinal() * SHAPE_KEY_BITS_PER_SIDE)) & 3;
            if (ordinal != PipeConnectionMode.DISABLED.ordinal()) {
                shape = Shapes.or(shape, ARM_SHAPES[direction.ordinal()]);
            }
        }

        SHAPE_CACHE[key] = shape;
        return shape;
    }

    public PipeResourceKind kind() {
        return this.kind;
    }

    public PipeTier tier() {
        return this.tier;
    }
}
