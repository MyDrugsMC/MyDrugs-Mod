package org.mydrugs.mydrugs.client.sounds;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/**
 * A player-local looping ambient bed whose volume can be steered from outside each tick. Used by
 * {@link org.mydrugs.mydrugs.client.InnerSoundscapeController} for the per-region drones: the
 * controller sets a target volume every client tick and the instance eases toward it, producing the
 * crossfade between region beds. When the target reaches zero the instance smoothly fades out and
 * then reports itself stopped so the controller can drop it.
 */
public final class InnerLoopSoundInstance extends AbstractTickableSoundInstance {
    private float targetVolume;
    private boolean fadingOut;

    public InnerLoopSoundInstance(SoundEvent event, float initialTarget) {
        super(event, SoundSource.AMBIENT, RandomSource.create());
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.pitch = 1.0F;
        this.targetVolume = Mth.clamp(initialTarget, 0.0F, 1.0F);
        // Keep the bed centred on the player rather than relying on world attenuation.
        this.relative = true;
        this.attenuation = Attenuation.NONE;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
    }

    public void setTargetVolume(float target) {
        this.targetVolume = Mth.clamp(target, 0.0F, 1.0F);
        if (this.targetVolume > 0.0F) {
            this.fadingOut = false;
        }
    }

    /** Request a graceful fade-out; the instance stops once silent. */
    public void fadeOut() {
        this.fadingOut = true;
        this.targetVolume = 0.0F;
    }

    @Override
    public void tick() {
        float goal = fadingOut ? 0.0F : targetVolume;
        // ~0.05 ease per tick gives a roughly 1s crossfade, matching the visual region blend feel.
        this.volume += (goal - this.volume) * 0.05F;
        if (this.volume < 0.002F && goal <= 0.0F) {
            this.volume = 0.0F;
            stop();
        }
    }
}
