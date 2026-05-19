package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.client.sounds.AudioStream;
import net.minecraft.client.sounds.JOrbisAudioStream;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public final class AudioDecoder {
    private AudioDecoder() {
    }

    public static AudioStream openOgg(Path path) throws IOException {
        String lower = path.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!lower.endsWith(".ogg")) {
            throw new IOException("Unsupported format (expected .ogg): " + path.getFileName());
        }
        return new JOrbisAudioStream(Files.newInputStream(path));
    }

    /**
     * Open a track for playback. Routes built-in tracks through Minecraft's resource manager
     * and falls back to filesystem for everything else.
     */
    public static AudioStream openTrack(MusicTrack track) throws IOException {
        if (track == null) {
            throw new IOException("Track is null");
        }
        if (track.sourceType == MusicTrack.SourceType.BUILT_IN) {
            ResourceLocation loc = parseLocation(track.localPath);
            if (loc == null) {
                throw new IOException("Invalid built-in track location: " + track.localPath);
            }
            InputStream stream = BuiltinMusicProvider.open(loc);
            if (stream == null) {
                throw new IOException("Built-in resource not found: " + loc);
            }
            return new JOrbisAudioStream(stream);
        }
        return openOgg(Path.of(track.localPath));
    }

    private static ResourceLocation parseLocation(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return ResourceLocation.parse(value);
        } catch (Exception ignored) {
            return null;
        }
    }
}
