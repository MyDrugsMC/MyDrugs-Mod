package org.mydrugs.mydrugs.client.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class SoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private boolean stoppedFlag = false;
    private int ticksLeft;

    public SoundInstance(SoundEvent event, Player player, int durationTicks) {
        super(event, SoundSource.PLAYERS, RandomSource.create());

        this.player = player;
        this.ticksLeft = durationTicks;

        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;

        // Keep the sound local to the player instead of relying on world attenuation.
        this.relative = true;
        this.attenuation = net.minecraft.client.resources.sounds.SoundInstance.Attenuation.NONE;

        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
    }

    @Override
    public void tick() {
        if (stoppedFlag) {
            return;
        }

        if (player.isRemoved() || !player.isAlive()) {
            stopNow();
            return;
        }

        if (ticksLeft > 0) {
            ticksLeft--;
        }

        if (ticksLeft <= 0) {
            stopNow();
        }
    }

    public void refreshDuration(int durationTicks) {
        if (durationTicks > this.ticksLeft) {
            this.ticksLeft = durationTicks;
        }
    }

    private void stopNow() {
        this.stop();
        this.stoppedFlag = true;
    }

    public boolean isStoppedFlag() {
        return stoppedFlag;
    }
}