package org.mydrugs.mydrugs.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.mydrugs.mydrugs.items.ModItems;

public final class BitterNutBushBlock extends VegetationBlock {
    public static final MapCodec<BitterNutBushBlock> CODEC = simpleCodec(BitterNutBushBlock::new);
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final int MAX_AGE = 3;

    private static final VoxelShape SHAPE_SAPLING = Block.column(10.0, 0.0, 8.0);
    private static final VoxelShape SHAPE_GROWING = Block.column(14.0, 0.0, 14.0);
    private static final VoxelShape SHAPE_RIPE = Block.column(16.0, 0.0, 16.0);

    public BitterNutBushBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(BlockTags.DIRT) || state.is(Blocks.FARMLAND) || state.is(Blocks.GRASS_BLOCK);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        int age = state.getValue(AGE);
        if (age <= 0) return SHAPE_SAPLING;
        if (age >= MAX_AGE) return SHAPE_RIPE;
        return SHAPE_GROWING;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE && random.nextInt(5) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier applier, boolean canStuckEntity) {
        if (!(entity instanceof LivingEntity)) return;
        if (entity.getType() == EntityType.FOX || entity.getType() == EntityType.BEE) return;
        entity.makeStuckInBlock(state, new Vec3(0.8F, 0.75D, 0.8F));
        if (level instanceof ServerLevel serverLevel && state.getValue(AGE) > 0) {
            Vec3 motion = entity.oldPosition().subtract(entity.position());
            if (motion.horizontalDistanceSqr() > 0.0D) {
                double dx = Math.abs(motion.x);
                double dz = Math.abs(motion.z);
                if (dx >= 0.003D || dz >= 0.003D) {
                    entity.hurtServer(serverLevel, level.damageSources().sweetBerryBush(), 1.0F);
                }
            }
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        return harvest(state, level, pos);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (state.getValue(AGE) >= MAX_AGE) {
            return harvest(state, level, pos);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    private InteractionResult harvest(BlockState state, Level level, BlockPos pos) {
        int age = state.getValue(AGE);
        if (age < MAX_AGE) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel) {
            int count = 1 + serverLevel.random.nextInt(2);
            popResource(serverLevel, pos, new ItemStack(ModItems.BITTER_NUT.get(), count));
            serverLevel.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 0.8F + serverLevel.random.nextFloat() * 0.4F);
            serverLevel.setBlock(pos, state.setValue(AGE, 0), Block.UPDATE_ALL);
        }
        return InteractionResult.SUCCESS;
    }
}
