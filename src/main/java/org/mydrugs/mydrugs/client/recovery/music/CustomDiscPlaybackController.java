package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.blaze3d.audio.Library;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.ChannelAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.network.PersonalDiscPlaybackPayload;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public final class CustomDiscPlaybackController {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final double MAX_RANGE = 64.0D;
    private static final Map<BlockPos, Session> SESSIONS = new HashMap<>();
    private static final Map<BlockPos, Long> GENERATIONS = new HashMap<>();
    private static final Set<String> MISSING_NOTICES = new HashSet<>();
    private static long nextGeneration;

    private CustomDiscPlaybackController() {
    }

    public static void handle(PersonalDiscPlaybackPayload payload, IPayloadContext context) {
        Minecraft.getInstance().execute(() -> {
            if (payload.action() == PersonalDiscPlaybackPayload.Action.STOP) {
                stop(payload.pos());
            } else {
                start(payload);
            }
        });
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            clear();
            return;
        }
        SESSIONS.entrySet().removeIf(entry -> {
            Session session = entry.getValue();
            if (session.isStopped()) {
                session.stop();
                return true;
            }
            session.updateVolume(mc);
            return false;
        });
    }

    public static void clear() {
        for (Session session : SESSIONS.values()) {
            session.stop();
        }
        SESSIONS.clear();
        GENERATIONS.clear();
        MISSING_NOTICES.clear();
    }

    private static void start(PersonalDiscPlaybackPayload payload) {
        BlockPos sessionPos = payload.pos().immutable();
        Session existing = SESSIONS.get(sessionPos);
        if (existing != null && existing.isSameTrack(payload.trackId()) && !existing.isStopped()) {
            existing.updateVolume(Minecraft.getInstance());
            return;
        }

        stop(sessionPos);
        Optional<MusicTrack> track = MusicLibrary.get().find(payload.trackId());
        if (track.isEmpty() || !track.get().isPlayable() || !track.get().isOgg()) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && MISSING_NOTICES.add(missingNoticeKey(payload.pos(), payload.trackId()))) {
                String title = payload.title() == null || payload.title().isBlank() ? payload.trackId() : payload.title();
                mc.player.displayClientMessage(Component.translatable("message.mydrugs.music.missing_local_track", title), true);
            }
            return;
        }

        AudioStream stream;
        ChannelAccess access;
        try {
            stream = AudioDecoder.openTrack(track.get());
            access = CustomMusicPlayer.resolveChannelAccess();
        } catch (Exception ex) {
            LOGGER.warn("Unable to open personal disc track {}", payload.trackId(), ex);
            return;
        }

        long generation = ++nextGeneration;
        GENERATIONS.put(sessionPos, generation);
        access.createHandle(Library.Pool.STREAMING).thenAcceptAsync(handle -> {
            if (!Objects.equals(GENERATIONS.get(sessionPos), generation)) {
                closeStreamQuietly(stream);
                return;
            }
            if (handle == null) {
                closeStreamQuietly(stream);
                return;
            }
            Session session = new Session(sessionPos, payload.trackId(), handle);
            SESSIONS.put(sessionPos, session);
            handle.execute(channel -> {
                if (channel == null) {
                    return;
                }
                try {
                    channel.disableAttenuation();
                    channel.setRelative(true);
                    channel.setVolume(0.0F);
                    channel.attachBufferStream(stream);
                    channel.play();
                } catch (Exception ex) {
                    LOGGER.warn("Unable to start personal disc stream at {}", payload.pos(), ex);
                    channel.stop();
                    session.stop();
                }
            });
            session.updateVolume(Minecraft.getInstance());
        }, runnable -> Minecraft.getInstance().execute(runnable));
    }

    private static void stop(BlockPos pos) {
        BlockPos sessionPos = pos.immutable();
        GENERATIONS.remove(sessionPos);
        MISSING_NOTICES.removeIf(key -> key.startsWith(sessionPos.asLong() + "|"));
        Session old = SESSIONS.remove(sessionPos);
        if (old != null) {
            old.stop();
        }
    }

    private static String missingNoticeKey(BlockPos pos, String trackId) {
        return pos.asLong() + "|" + (trackId == null ? "" : trackId);
    }

    private static void closeStreamQuietly(AudioStream stream) {
        try {
            stream.close();
        } catch (Exception ignored) {
        }
    }

    private static final class Session {
        private final BlockPos pos;
        private final String trackId;
        private final ChannelAccess.ChannelHandle handle;
        private boolean stopped;

        private Session(BlockPos pos, String trackId, ChannelAccess.ChannelHandle handle) {
            this.pos = pos.immutable();
            this.trackId = trackId == null ? "" : trackId;
            this.handle = handle;
        }

        private boolean isStopped() {
            return stopped || handle.isStopped();
        }

        private boolean isSameTrack(String otherTrackId) {
            return trackId.equals(otherTrackId == null ? "" : otherTrackId);
        }

        private void updateVolume(Minecraft mc) {
            if (stopped || mc.player == null) {
                return;
            }
            double distance = Math.sqrt(mc.player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
            float volume = distance >= MAX_RANGE ? 0.0F : (float) Math.max(0.0D, 1.0D - distance / MAX_RANGE);
            handle.execute(channel -> {
                if (channel != null) {
                    channel.setVolume(volume);
                }
            });
        }

        private void stop() {
            if (stopped) {
                return;
            }
            stopped = true;
            handle.execute(channel -> {
                if (channel != null) {
                    channel.stop();
                }
            });
        }
    }
}
