package org.mydrugs.mydrugs.client.effects.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.sounds.ModSounds;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.client.effects.HeartbeatPulse;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;

public final class ClientSoundController {
    private static final java.util.Random RANDOM = new java.util.Random();
    private static long nextHeartbeatAt = 0L;
    private static long nextThoughtAt = 0L;

    private ClientSoundController() {
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        long gameTime = mc.level.getGameTime();

        if (AddictionClientState.has(SymptomFlags.STRESS) && gameTime >= nextHeartbeatAt) {
            HeartbeatPulse.triggerBeat();
            // Accessibility: visual pulse still fires (it follows enableCameraShake),
            // only the audible heartbeat is gated by enableHeartbeatSounds.
            if (Config.CLIENT.enableHeartbeatSounds.get()) {
                float volume = Config.CLIENT.suddenSoundCap(0.5F * Config.CLIENT.heartbeatVolume.get().floatValue());
                if (volume > 0.0F) {
                    mc.player.playNotifySound(ModSounds.SINGLE_HEARTBEAT.get(), SoundSource.PLAYERS, volume, 1.0F);
                }
            }
            nextHeartbeatAt = gameTime + Math.max(8L, 40L - (long) (AddictionClientState.stressLevel * 25L));
        }

        if (AddictionClientState.has(SymptomFlags.INTRUSIVE_THOUGHTS) && gameTime >= nextThoughtAt) {
            float voiceVolume = Config.CLIENT.suddenSoundCap(0.35F * Config.CLIENT.badTripVoiceVolume.get().floatValue());
            if (voiceVolume > 0.0F && RANDOM.nextFloat() < 0.50F) {
                mc.player.playNotifySound(ModSounds.INTRUSIVE_WHISPER.get(), SoundSource.PLAYERS, voiceVolume, 1.0F);
            }
            nextThoughtAt = gameTime + 20L * (20L + RANDOM.nextInt(40));
        }

        if (AddictionClientState.has(SymptomFlags.HALLUCINATION)
                && Config.CLIENT.enableHallucinations.get()
                && RANDOM.nextFloat() < 0.008F * Config.CLIENT.hallucinationIntensity.get().floatValue()) {
            float volume = Config.CLIENT.suddenSoundCap(0.30F * Config.CLIENT.hallucinationSoundVolume.get().floatValue());
            if (volume > 0.0F) {
                mc.player.playNotifySound(ModSounds.HALLUCINATION_CUE.get(), SoundSource.PLAYERS, volume, 1.0F);
            }
        }
    }
}
