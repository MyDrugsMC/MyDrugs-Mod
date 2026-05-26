package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.mydrugs.mydrugs.Config;

public final class StreetCookFumeHazard {
    public static final int TELEGRAPH_TICKS = 20;
    public static final double VENT_BASE_CHANCE_PER_SECOND = 0.06;
    public static final double RADIUS = 3.5;
    public static final int POISON_DURATION_TICKS = 60;
    public static final int NAUSEA_DURATION_TICKS = 100;
    public static final int FIRE_IGNITE_TICKS = 60;
    public static final double FIRE_CHANCE_PER_ADJACENT = 0.2;
    public static final ResourceLocation CRUDE_METH_SLURRY_ID =
            ResourceLocation.fromNamespaceAndPath("mydrugs", "crude_meth_slurry");

    private StreetCookFumeHazard() {
    }

    public static boolean recipeProducesCrudeMethSlurry(org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe recipe) {
        return recipe.resultFluid()
                .map(rf -> CRUDE_METH_SLURRY_ID.equals(rf.fluid()))
                .orElse(false);
    }

    public static boolean isEnabled() {
        return Config.SERVER.enableCookHazards.get() && Config.SERVER.cookHazardIntensity.get() > 0.0D;
    }

    public static double intensity() {
        return Math.max(0.0D, Config.SERVER.cookHazardIntensity.get());
    }

    public static void telegraph(ServerLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 1.0D;
        double cz = pos.getZ() + 0.5D;
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy, cz, 12, 0.3D, 0.4D, 0.3D, 0.02D);
        level.sendParticles(ParticleTypes.ASH, cx, cy + 0.2D, cz, 6, 0.4D, 0.2D, 0.4D, 0.0D);
        level.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.4F);
    }

    public static void vent(ServerLevel level, BlockPos pos) {
        if (!isEnabled()) return;
        double cx = pos.getX() + 0.5D;
        double cy = pos.getY() + 1.0D;
        double cz = pos.getZ() + 0.5D;
        double scaledRadius = RADIUS;
        AABB area = new AABB(
                cx - scaledRadius, cy - 1.0D, cz - scaledRadius,
                cx + scaledRadius, cy + 2.0D, cz + scaledRadius
        );
        int poisonAmp = intensity() >= 1.0D ? 0 : 0;
        int nauseaAmp = 0;
        int poisonTicks = (int) Math.round(POISON_DURATION_TICKS * Math.min(1.0D, intensity()));
        int nauseaTicks = (int) Math.round(NAUSEA_DURATION_TICKS * Math.min(1.0D, intensity()));
        if (poisonTicks > 0) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, area)) {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, poisonTicks, poisonAmp));
                entity.addEffect(new MobEffectInstance(MobEffects.NAUSEA, nauseaTicks, nauseaAmp));
            }
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, cx, cy + 0.5D, cz, 40, scaledRadius * 0.4D, 0.6D, scaledRadius * 0.4D, 0.03D);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.9F, 0.8F);

        double fireChance = Math.min(1.0D, FIRE_CHANCE_PER_ADJACENT * intensity());
        for (var dir : net.minecraft.core.Direction.values()) {
            if (dir.getStepY() < 0) continue;
            BlockPos adj = pos.relative(dir);
            BlockState above = level.getBlockState(adj.above());
            if (!above.isAir() && !above.canBeReplaced()) continue;
            if (level.random.nextDouble() < fireChance && level.getBlockState(adj).ignitedByLava()) {
                level.setBlockAndUpdate(adj.above(), Blocks.FIRE.defaultBlockState());
            }
        }
    }
}
