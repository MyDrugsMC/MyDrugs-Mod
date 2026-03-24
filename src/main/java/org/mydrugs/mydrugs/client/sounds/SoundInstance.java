package org.mydrugs.mydrugs.client.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

public class SoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private boolean stoppedFlag = false;
    private float ticksLeft;

    public SoundInstance(SoundEvent event, Player player, int durationTicks) {
        super(event, SoundSource.NEUTRAL, RandomSource.create());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.pitch = 1.0F;
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.ticksLeft = durationTicks;
    }

    @Override
    public void tick() {
        if (ticksLeft <= 0) {
            return;
        }

        ticksLeft--;

        if (ticksLeft <= 0 || player.isRemoved() || !player.isAlive()) {
            this.stop();
            this.stoppedFlag = true;
            return;
        }

        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
    }


    public boolean isStoppedFlag() {
        return stoppedFlag;
    }
}