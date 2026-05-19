package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.client.sounds.ChannelAccess;

/**
 * Wraps a {@link ChannelAccess.ChannelHandle} so all interactions go through
 * {@link ChannelAccess.ChannelHandle#execute(java.util.function.Consumer)} and are therefore
 * scheduled on the sound engine executor.
 *
 * <p>We deliberately do <strong>not</strong> call {@link ChannelAccess.ChannelHandle#release()}
 * ourselves: that method is not thread-safe (it nulls {@code handle.channel} while
 * {@link ChannelAccess#scheduleTick()} may be iterating, and it does not remove the handle
 * from the channels set). Instead we stop the channel and let
 * {@link ChannelAccess#scheduleTick()} release it once {@code channel.stopped()} is true.
 */
public final class AudioStreamHandle {
    private final ChannelAccess.ChannelHandle handle;
    private volatile boolean disposed;

    public AudioStreamHandle(ChannelAccess.ChannelHandle handle) {
        this.handle = handle;
    }

    public boolean isStopped() {
        return disposed || handle == null || handle.isStopped();
    }

    public boolean isValid() {
        return !disposed && handle != null;
    }

    public void pause() {
        if (isValid()) {
            handle.execute(channel -> {
                if (channel != null) {
                    channel.setVolume(0.0F);
                    channel.pause();
                }
            });
        }
    }

    public void resume(float volume) {
        if (isValid()) {
            handle.execute(channel -> {
                if (channel != null) {
                    channel.setVolume(volume);
                    channel.unpause();
                }
            });
        }
    }

    public void stop() {
        if (disposed || handle == null) {
            return;
        }
        disposed = true;
        handle.execute(channel -> {
            if (channel != null) {
                channel.stop();
            }
        });
    }

    public void setVolume(float volume) {
        if (isValid()) {
            handle.execute(channel -> {
                if (channel != null) {
                    channel.setVolume(volume);
                }
            });
        }
    }

    ChannelAccess.ChannelHandle raw() {
        return handle;
    }
}
