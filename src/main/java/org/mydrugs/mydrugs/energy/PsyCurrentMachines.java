package org.mydrugs.mydrugs.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;

/** Helper for machines drawing Psy Current from their upgrade buffer each tick. */
public final class PsyCurrentMachines {
    /** Tick gate between consuming-aura pulses. ~1.5 seconds between blips. */
    private static final int CONSUMING_AURA_INTERVAL = 30;
    /** Tick gate between starved sputters. */
    private static final int STARVED_AURA_INTERVAL = 80;
    private static final int CONSUMING_DUST_COLOR = 0x8E7CFF;
    private static final int FULL_DUST_COLOR = 0xCFC7FF;

    private PsyCurrentMachines() {
    }

    public static boolean tryUseCurrentTick(BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        if (!attachment.hasEnergyUpgrade()) {
            return false;
        }
        return tryUseCurrentTick(blockEntity, attachment);
    }

    public static boolean tryUseAutomationCurrentTick(BlockEntity blockEntity) {
        MachineEnergyAttachment attachment = MachineEnergyAttachments.get(blockEntity);
        if (!attachment.hasAutomationUpgrade()) {
            return false;
        }
        return tryUseCurrentTick(blockEntity, attachment);
    }

    private static boolean tryUseCurrentTick(BlockEntity blockEntity, MachineEnergyAttachment attachment) {
        int amount = PsyCurrentConstants.DEFAULT_MACHINE_CURRENT_PER_TICK;
        if (attachment.storage().extract(amount, true) < amount) {
            emitStateAura(blockEntity, attachment, AuraState.STARVED);
            return false;
        }

        attachment.storage().extract(amount, false);
        AdvancementEventHooks.psychotropePoweredMachine(blockEntity);
        sync(blockEntity);
        emitStateAura(blockEntity, attachment, AuraState.CONSUMING);
        return true;
    }

    /**
     * Subtle ambient cue that communicates a powered machine's current state:
     * actively consuming -> faint purple dust above the block on a rhythmic tick;
     * starved -> infrequent smoke sputter so the player can tell the machine is asking
     * for power without spamming the chat or a HUD overlay. Server-emitted so vanilla
     * particle settings already apply.
     */
    private static void emitStateAura(BlockEntity blockEntity, MachineEnergyAttachment attachment, AuraState state) {
        Level level = blockEntity.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        long now = serverLevel.getGameTime();
        BlockPos pos = blockEntity.getBlockPos();
        // Offset the tick gate per-position so a wall of machines does not blink in unison.
        int posSalt = Math.floorMod(pos.getX() * 31 + pos.getZ() * 17 + pos.getY(), 17);
        long gated = now + posSalt;
        switch (state) {
            case CONSUMING -> {
                if (gated % CONSUMING_AURA_INTERVAL != 0) {
                    return;
                }
                boolean full = attachment.storage().receive(1, true) <= 0;
                int color = full ? FULL_DUST_COLOR : CONSUMING_DUST_COLOR;
                serverLevel.sendParticles(
                        new DustParticleOptions(color, full ? 1.2F : 0.9F),
                        pos.getX() + 0.5D,
                        pos.getY() + 1.05D,
                        pos.getZ() + 0.5D,
                        full ? 2 : 1,
                        0.12D, 0.05D, 0.12D, 0.0D);
            }
            case STARVED -> {
                if (gated % STARVED_AURA_INTERVAL != 0) {
                    return;
                }
                serverLevel.sendParticles(
                        ParticleTypes.SMOKE,
                        pos.getX() + 0.5D,
                        pos.getY() + 1.05D,
                        pos.getZ() + 0.5D,
                        1,
                        0.08D, 0.02D, 0.08D, 0.0D);
            }
        }
    }

    private enum AuraState {
        CONSUMING,
        STARVED
    }

    public static void sync(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            BlockState state = blockEntity.getBlockState();
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }
}
