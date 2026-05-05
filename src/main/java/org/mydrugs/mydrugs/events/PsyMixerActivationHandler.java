package org.mydrugs.mydrugs.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerPartBlockEntity;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class PsyMixerActivationHandler {
    private PsyMixerActivationHandler() {
    }

    private static final String[] ACTIVATION_MESSAGES = {
            "message.mydrugs.psy_mixer.activation.1",
            "message.mydrugs.psy_mixer.activation.2",
            "message.mydrugs.psy_mixer.activation.3",
            "message.mydrugs.psy_mixer.activation.4",
            "message.mydrugs.psy_mixer.activation.5"
    };
    private static final String[] INVALID_MESSAGES = {
            "message.mydrugs.psy_mixer.invalid.1",
            "message.mydrugs.psy_mixer.invalid.2",
            "message.mydrugs.psy_mixer.invalid.3",
            "message.mydrugs.psy_mixer.invalid.4"
    };

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof ThrownEnderpearl pearl)) return;
        if (!(event.getRayTraceResult() instanceof BlockHitResult hit)) return;
        if (!(pearl.level() instanceof ServerLevel level)) return;

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        if (!state.is(ModBlocks.PAINTED_CLAY_BOWL.get())) return;

        // Cancel pearl teleport / damage
        event.setCanceled(true);

        ServerPlayer thrower = (pearl.getOwner() instanceof ServerPlayer sp) ? sp : null;

        PsyMixerMultiblock.Match match = PsyMixerMultiblock.validate(level, pos);
        if (match == null) {
            sendRandomMessage(thrower, level, INVALID_MESSAGES);
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 0.6F, 0.4F);
            pearl.discard();
            return;
        }

        // Save original states
        List<FormedPsyMixerCoreBlockEntity.SavedSlot> saved = new ArrayList<>();
        for (PsyMixerMultiblock.PlacedSlot placed : match.placed()) {
            BlockState original = level.getBlockState(placed.worldPos());
            saved.add(new FormedPsyMixerCoreBlockEntity.SavedSlot(placed.worldPos().immutable(), original));
        }

        // Replace blocks: bowl -> core, others -> part (skip the air slot)
        // First place core on bowl pos
        level.setBlock(pos, ModBlocks.FORMED_PSY_MIXER_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof FormedPsyMixerCoreBlockEntity core) {
            core.initFromActivation(match.facing(), saved);
        }

        for (PsyMixerMultiblock.PlacedSlot placed : match.placed()) {
            if (placed.worldPos().equals(pos)) continue;
            if (placed.slot().expected() == null) continue; // air slot
            level.setBlock(placed.worldPos(), ModBlocks.FORMED_PSY_MIXER_PART.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(placed.worldPos()) instanceof FormedPsyMixerPartBlockEntity part) {
                part.setCorePos(pos);
            }
        }

        // Send the required activation message
        if (thrower != null) {
            thrower.displayClientMessage(Component.translatable("message.mydrugs.psy_mixer.awaken"), false);
            sendRandomMessage(thrower, level, ACTIVATION_MESSAGES);
        }

        // Sound + particles
        level.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.0F, 0.7F);
        for (int i = 0; i < 60; i++) {
            double angle = i * Math.PI * 2.0 / 60.0;
            double rx = Math.cos(angle) * 1.4;
            double rz = Math.sin(angle) * 1.4;
            level.sendParticles(
                    new net.minecraft.core.particles.DustParticleOptions(0xC04A8A, 1.5F),
                    pos.getX() + 0.5 + rx, pos.getY() + 0.6, pos.getZ() + 0.5 + rz,
                    1, 0.0, 0.5, 0.0, 0.0
            );
        }

        pearl.discard();
    }

    private static void sendRandomMessage(ServerPlayer player, Level level, String[] keys) {
        if (player == null) return;
        RandomSource random = level.getRandom();
        String key = keys[random.nextInt(keys.length)];
        player.displayClientMessage(Component.translatable(key), false);
    }
}
