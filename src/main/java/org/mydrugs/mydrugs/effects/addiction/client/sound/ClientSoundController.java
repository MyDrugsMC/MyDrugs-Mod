package org.mydrugs.mydrugs.effects.addiction.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.sounds.SoundSource;
import org.mydrugs.mydrugs.sounds.ModSounds;
import org.mydrugs.mydrugs.effects.addiction.client.AddictionClientState;
import org.mydrugs.mydrugs.effects.addiction.client.HeartbeatPulse;
import org.mydrugs.mydrugs.effects.addiction.config.SymptomFlags;

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
            mc.player.playNotifySound(ModSounds.SINGLE_HEARTBEAT.get(), SoundSource.PLAYERS, 0.5F, 1.0F);
            nextHeartbeatAt = gameTime + Math.max(8L, 40L - (long) (AddictionClientState.stressLevel * 25L));
        }

        if (AddictionClientState.has(SymptomFlags.INTRUSIVE_THOUGHTS) && gameTime >= nextThoughtAt) {
            if (RANDOM.nextFloat() < 0.50F) {
                mc.player.playNotifySound(ModSounds.INTRUSIVE_WHISPER.get(), SoundSource.PLAYERS, 0.35F, 1.0F);
            }
            nextThoughtAt = gameTime + 20L * (20L + RANDOM.nextInt(40));
        }

        if (AddictionClientState.has(SymptomFlags.HALLUCINATION) && RANDOM.nextFloat() < 0.008F) {
            mc.player.playNotifySound(ModSounds.HALLUCINATION_CUE.get(), SoundSource.PLAYERS, 0.30F, 1.0F);
        }
    }
}