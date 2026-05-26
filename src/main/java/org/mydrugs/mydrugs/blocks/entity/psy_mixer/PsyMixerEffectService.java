package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.addiction.manager.state.StressManager;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.sounds.ModSounds;

public final class PsyMixerEffectService {
    private PsyMixerEffectService() {
    }

    public static void spawnAmbientThirdEyePetal(ServerLevel level, BlockPos origin) {
        Block petal = ModBlocks.THIRD_EYE_PETAL.get();
        BlockState petalState = petal.defaultBlockState();
        for (int attempt = 0; attempt < 5; attempt++) {
            int dx = level.random.nextInt(11) - 5;
            int dz = level.random.nextInt(11) - 5;
            int dy = level.random.nextInt(3) - 1;
            BlockPos candidate = origin.offset(dx, dy, dz);
            if (!level.getBlockState(candidate).isAir()) {
                continue;
            }
            if (!petalState.canSurvive(level, candidate)) {
                continue;
            }
            level.setBlock(candidate, petalState, Block.UPDATE_ALL);
            return;
        }
    }

    public static void spawnRunningParticles(ServerLevel level, BlockPos pos, int qualityScore) {
        int particleCount = 1 + Math.max(0, qualityScore / 2);
        level.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                particleCount, 0.2, 0.1, 0.2, 0.01
        );
    }

    public static void playActionVoice(ServerLevel level, BlockPos pos, PsyMixerRitualAction action) {
        SoundEvent voice = ModSounds.psyMixerVoice(action);
        if (voice != null) {
            level.playSound(null, pos, voice, SoundSource.BLOCKS, 0.75F, 1.0F);
        }
    }

    public static void spawnCompletionBurst(
            ServerLevel level,
            BlockPos pos,
            PsyMixerRitualQuality quality
    ) {
        level.playSound(null, pos,
                quality == PsyMixerRitualQuality.MASTERWORK
                        ? SoundEvents.BEACON_POWER_SELECT
                        : SoundEvents.AMETHYST_CLUSTER_BREAK,
                SoundSource.BLOCKS,
                quality == PsyMixerRitualQuality.MASTERWORK ? 0.9F : 0.65F,
                quality == PsyMixerRitualQuality.CRUDE ? 0.65F : 1.25F);
        int count = quality == PsyMixerRitualQuality.MASTERWORK ? 72 : 42;
        for (int i = 0; i < count; i++) {
            level.sendParticles(
                    quality == PsyMixerRitualQuality.CRUDE
                            ? new DustParticleOptions(0xAA3355, 1.35F)
                            : ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 2.1, pos.getZ() + 0.5,
                    1, 0.5, 0.45, 0.5, 0.09
            );
        }
    }

    public static void applyFailureSeverity(ServerPlayer player, float severity) {
        if (severity <= 0.0F) {
            return;
        }

        float clamped = Math.min(2.0F, severity);
        StressManager.addStress(player, clamped * AddictionConstants.STRESS_PSY_MIXER_FAILURE_SPIKE_SCALE);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CONFUSION, 0.15F + clamped * 0.25F, 20 * 8);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CUSTOM_NAUSEA, 0.10F + clamped * 0.20F, 20 * 6);
        if (clamped >= 1.0F) {
            DrugEffectRuntimeManager.addEffect(player, EffectType.CAMERA_SWAY, 0.10F + clamped * 0.10F, 20 * 5);
        }
    }
}
