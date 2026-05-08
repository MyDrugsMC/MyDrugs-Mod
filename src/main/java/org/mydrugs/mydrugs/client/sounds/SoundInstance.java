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
    private int fadeTicksLeft;
    private int fadeDurationTicks;

    public SoundInstance(SoundEvent event, Player player, int durationTicks) {
        this(event, player, durationTicks, 0, 0);
    }

    public SoundInstance(SoundEvent event, Player player, int durationTicks, int fadeTicksLeft, int fadeDurationTicks) {
        super(event, SoundSource.PLAYERS, RandomSource.create());

        this.player = player;
        this.ticksLeft = durationTicks;
        this.fadeTicksLeft = Math.max(0, Math.min(fadeTicksLeft, durationTicks));
        this.fadeDurationTicks = Math.max(0, fadeDurationTicks);

        this.looping = true;
        this.delay = 0;
        this.volume = fadeTicksLeft > 0 && fadeDurationTicks > 0
                ? Math.max(0.0F, Math.min(1.0F, fadeTicksLeft / (float) fadeDurationTicks))
                : 1.0F;
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
        if (ticksLeft <= 0 && fadeTicksLeft <= 0 && fadeDurationTicks > 0) {
            fadeTicksLeft = fadeDurationTicks;
            ticksLeft = fadeDurationTicks;
        }
        if (fadeTicksLeft > 0) {
            fadeTicksLeft--;
            int duration = Math.max(1, fadeDurationTicks);
            this.volume = Math.max(0.0F, Math.min(1.0F, fadeTicksLeft / (float) duration));
        }

        if (ticksLeft <= 0 && fadeTicksLeft <= 0) {
            stopNow();
        }
    }

    public void refreshDuration(int durationTicks) {
        refreshDuration(durationTicks, 0, 0);
    }

    public void refreshDuration(int durationTicks, int fadeTicksLeft, int fadeDurationTicks) {
        this.ticksLeft = Math.max(this.ticksLeft, durationTicks);
        if (fadeTicksLeft > 0 && fadeDurationTicks > 0) {
            this.fadeTicksLeft = Math.min(fadeTicksLeft, this.ticksLeft);
            this.fadeDurationTicks = fadeDurationTicks;
        } else if (this.fadeTicksLeft <= 0) {
            this.volume = 1.0F;
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
