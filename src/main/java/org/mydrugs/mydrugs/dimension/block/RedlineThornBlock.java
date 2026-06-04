package org.mydrugs.mydrugs.dimension.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;

public final class RedlineThornBlock extends SymbolicPlantBlock {
    public static final MapCodec<RedlineThornBlock> CODEC = simpleCodec(RedlineThornBlock::new);

    public RedlineThornBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends VegetationBlock> codec() {
        return CODEC;
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier applier, boolean canStuckEntity) {
        if (!(entity instanceof LivingEntity)) {
            return;
        }

        entity.makeStuckInBlock(state, new Vec3(0.86D, 0.92D, 0.86D));
        if (!(level instanceof ServerLevel serverLevel) || !entity.isSprinting() || entity.tickCount % 20 != 0) {
            return;
        }

        entity.hurtServer(serverLevel, level.damageSources().sweetBerryBush(), 1.0F);
        if (entity instanceof ServerPlayer player) {
            StressManager.addStress(player, 0.01F);
        }
    }
}
