package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.blaze3d.audio.Library;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.addiction.network.HeadphonesStatePayload;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public final class CustomMusicPlayer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final CustomMusicPlayer INSTANCE = new CustomMusicPlayer();

    private final Object lock = new Object();
    private final AtomicInteger playGeneration = new AtomicInteger();

    private volatile AudioStreamHandle currentHandle;
    private volatile MusicTrack currentTrack;
    private volatile MusicPlaybackState state = MusicPlaybackState.STOPPED;
    private volatile Component status = Component.empty();
    private long startedAtMs;
    private long pausedAtMs;
    private long accumulatedPausedMs;
    private volatile float volume = 1.0F;
    private volatile boolean repeat;
    private volatile boolean shuffle;
    private volatile boolean serverWantsPlayback;
    private volatile boolean hasSeenServerLibraryVersion;
    private volatile int lastServerLibraryVersion;

    private CustomMusicPlayer() {
    }

    public static CustomMusicPlayer get() {
        return INSTANCE;
    }

    public void applyHeadphonesState(HeadphonesStatePayload payload) {
        boolean nextRequested = hasSeenServerLibraryVersion && payload.libraryVersion() != lastServerLibraryVersion;
        this.serverWantsPlayback = payload.playing();
        this.lastServerLibraryVersion = payload.libraryVersion();
        this.hasSeenServerLibraryVersion = true;
        if (payload.volume() >= 0.0F) {
            setVolume(payload.volume());
        }
        if (nextRequested) {
            next();
            return;
        }
        if (!payload.playing()) {
            pause();
            return;
        }
        if (payload.trackId() != null && !payload.trackId().isBlank()) {
            MusicLibrary.get().find(payload.trackId()).ifPresent(this::play);
        } else if (state == MusicPlaybackState.STOPPED || currentTrack == null) {
            MusicLibrary.get().firstPlayable().ifPresentOrElse(this::play, () -> {
                status = Component.translatable("screen.mydrugs.music.empty");
                state = MusicPlaybackState.STOPPED;
            });
        } else {
            resume();
        }
    }

    public void play(MusicTrack track) {
        if (track == null) {
            return;
        }
        if (!track.isPlayable()) {
            status = Component.translatable("screen.mydrugs.music.missing_file");
            state = MusicPlaybackState.ERROR;
            return;
        }
        if (!track.isOgg()) {
            status = Component.translatable("screen.mydrugs.music.unsupported");
            state = MusicPlaybackState.ERROR;
            return;
        }
        if (track.sourceType != MusicTrack.SourceType.BUILT_IN) {
            Path path = Path.of(track.localPath);
            if (!Files.isRegularFile(path)) {
                status = Component.translatable("screen.mydrugs.music.missing_file");
                state = MusicPlaybackState.ERROR;
                return;
            }
        }

        // Bump the generation BEFORE stopping so any in-flight createHandle for the previous
        // play() call sees a mismatched generation and disposes its handle.
        final int gen = playGeneration.incrementAndGet();
        stopCurrentInternal();

        final ChannelAccess access;
        final AudioStream stream;
        try {
            access = resolveChannelAccess();
            stream = AudioDecoder.openTrack(track);
        } catch (Exception ex) {
            LOGGER.error("Failed to open audio stream for {}", track.title, ex);
            status = Component.translatable("screen.mydrugs.music.failed");
            state = MusicPlaybackState.ERROR;
            return;
        }

        synchronized (lock) {
            currentTrack = track;
            state = MusicPlaybackState.PLAYING;
            status = Component.translatable("screen.mydrugs.music.now_playing");
            startedAtMs = System.currentTimeMillis();
            pausedAtMs = 0L;
            accumulatedPausedMs = 0L;
        }

        CompletableFuture<ChannelAccess.ChannelHandle> future = access.createHandle(Library.Pool.STREAMING);
        future.thenAcceptAsync(handle -> {
            if (handle == null) {
                LOGGER.warn("No streaming channel available");
                closeStreamQuietly(stream);
                if (gen == playGeneration.get()) {
                    status = Component.translatable("screen.mydrugs.music.failed");
                    state = MusicPlaybackState.ERROR;
                }
                return;
            }
            // If a newer play() superseded us, stop this channel immediately so scheduleTick releases it.
            if (gen != playGeneration.get()) {
                handle.execute(channel -> {
                    if (channel != null) {
                        channel.stop();
                    }
                });
                closeStreamQuietly(stream);
                return;
            }
            AudioStreamHandle wrapped = new AudioStreamHandle(handle);
            synchronized (lock) {
                if (gen != playGeneration.get()) {
                    handle.execute(channel -> {
                        if (channel != null) {
                            channel.stop();
                        }
                    });
                    closeStreamQuietly(stream);
                    return;
                }
                currentHandle = wrapped;
            }
            float effective = effectiveVolume(track);
            handle.execute(channel -> {
                if (channel == null) {
                    return;
                }
                try {
                    boolean shouldStartPaused = state == MusicPlaybackState.PAUSED;
                    channel.disableAttenuation();
                    channel.setRelative(true);
                    channel.setVolume(shouldStartPaused ? 0.0F : effective);
                    channel.attachBufferStream(stream);
                    channel.play();
                    if (shouldStartPaused) {
                        channel.pause();
                    }
                } catch (Exception ex) {
                    LOGGER.error("Failed to start audio stream", ex);
                    channel.stop();
                    if (gen == playGeneration.get()) {
                        status = Component.translatable("screen.mydrugs.music.failed");
                        state = MusicPlaybackState.ERROR;
                    }
                }
            });
            MusicLibrary.get().markPlayed(track);
        }, runnable -> Minecraft.getInstance().execute(runnable)).exceptionally(ex -> {
            LOGGER.error("createHandle failed", ex);
            closeStreamQuietly(stream);
            if (gen == playGeneration.get()) {
                status = Component.translatable("screen.mydrugs.music.failed");
                state = MusicPlaybackState.ERROR;
            }
            return null;
        });
    }

    public void pause() {
        synchronized (lock) {
            if (state != MusicPlaybackState.PLAYING) {
                return;
            }
            if (currentHandle != null) {
                currentHandle.pause();
            }
            pausedAtMs = System.currentTimeMillis();
            state = MusicPlaybackState.PAUSED;
        }
    }

    public void resume() {
        MusicTrack toPlay = null;
        synchronized (lock) {
            if (state == MusicPlaybackState.PAUSED) {
                if (currentHandle != null && currentTrack != null) {
                    currentHandle.resume(effectiveVolume(currentTrack));
                }
                if (pausedAtMs > 0L) {
                    accumulatedPausedMs += System.currentTimeMillis() - pausedAtMs;
                    pausedAtMs = 0L;
                }
                state = MusicPlaybackState.PLAYING;
                return;
            }
            if (state == MusicPlaybackState.STOPPED && currentTrack != null) {
                toPlay = currentTrack;
            }
        }
        if (toPlay != null) {
            play(toPlay);
        }
    }

    public void toggle() {
        if (state == MusicPlaybackState.PLAYING) {
            pause();
        } else if (state == MusicPlaybackState.PAUSED) {
            resume();
        } else {
            MusicLibrary.get().firstPlayable().ifPresent(this::play);
        }
    }

    /**
     * Stops playback and clears local state. Safe to call from any thread.
     */
    public void stop() {
        // Bump generation to disqualify any in-flight createHandle completions
        playGeneration.incrementAndGet();
        stopCurrentInternal();
        state = MusicPlaybackState.STOPPED;
        startedAtMs = 0L;
        pausedAtMs = 0L;
        accumulatedPausedMs = 0L;
    }

    private void stopCurrentInternal() {
        AudioStreamHandle toStop;
        synchronized (lock) {
            toStop = currentHandle;
            currentHandle = null;
        }
        if (toStop != null) {
            toStop.stop();
        }
    }

    public void next() {
        MusicLibrary.get().nextAfter(currentTrack, false).ifPresent(this::play);
    }

    public void previous() {
        MusicLibrary.get().nextAfter(currentTrack, true).ifPresent(this::play);
    }

    public void setVolume(float volume) {
        this.volume = clampVolume(volume);
        AudioStreamHandle h = currentHandle;
        MusicTrack t = currentTrack;
        if (h != null && t != null) {
            h.setVolume(state == MusicPlaybackState.PAUSED ? 0.0F : effectiveVolume(t));
        }
    }

    public void setRepeat(boolean repeat) {
        this.repeat = repeat;
    }

    public boolean shuffle() {
        return shuffle;
    }

    public void setShuffle(boolean shuffle) {
        this.shuffle = shuffle;
    }

    public void tick() {
        AudioStreamHandle h = currentHandle;
        MusicTrack t = currentTrack;
        if (h != null && state == MusicPlaybackState.PAUSED) {
            h.pause();
            return;
        }
        if (h != null && state == MusicPlaybackState.PLAYING && h.isStopped()) {
            // Track ended
            if (repeat && t != null) {
                play(t);
            } else if (shuffle) {
                MusicLibrary.get().random(t).ifPresent(this::play);
            } else {
                next();
            }
        }
        if (serverWantsPlayback && state == MusicPlaybackState.STOPPED) {
            MusicLibrary.get().firstPlayable().ifPresent(this::play);
        }
        if (h != null && t != null && state == MusicPlaybackState.PLAYING) {
            h.setVolume(effectiveVolume(t));
        }
    }

    public void clear() {
        serverWantsPlayback = false;
        hasSeenServerLibraryVersion = false;
        currentTrack = null;
        stop();
    }

    public boolean isPlaying() {
        return state == MusicPlaybackState.PLAYING;
    }

    public MusicPlaybackState state() {
        return state;
    }

    public @Nullable MusicTrack currentTrack() {
        return currentTrack;
    }

    public long progressMs() {
        if (startedAtMs <= 0L) {
            return 0L;
        }
        long now = state == MusicPlaybackState.PAUSED && pausedAtMs > 0L ? pausedAtMs : System.currentTimeMillis();
        return Math.max(0L, now - startedAtMs - accumulatedPausedMs);
    }

    public float volume() {
        return volume;
    }

    public boolean repeat() {
        return repeat;
    }

    public Component status() {
        return status;
    }

    public int lastServerLibraryVersion() {
        return lastServerLibraryVersion;
    }

    private float effectiveVolume(MusicTrack track) {
        Minecraft mc = Minecraft.getInstance();
        float optionsVolume = mc.options.getFinalSoundSourceVolume(SoundSource.RECORDS);
        return clampVolume(volume * Math.max(0.0F, track.volume) * optionsVolume);
    }

    private static float clampVolume(float value) {
        return Math.max(0.0F, Math.min(1.0F, value));
    }

    private static void closeStreamQuietly(AudioStream stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (Exception ignored) {
            }
        }
    }

    static ChannelAccess resolveChannelAccess() throws ReflectiveOperationException {
        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        Field soundEngineField = SoundManager.class.getDeclaredField("soundEngine");
        soundEngineField.setAccessible(true);
        SoundEngine soundEngine = (SoundEngine) soundEngineField.get(soundManager);
        Field channelAccessField = SoundEngine.class.getDeclaredField("channelAccess");
        channelAccessField.setAccessible(true);
        return (ChannelAccess) channelAccessField.get(soundEngine);
    }
}
