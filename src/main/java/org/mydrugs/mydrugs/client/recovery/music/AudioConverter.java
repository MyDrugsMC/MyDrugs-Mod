package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Converts non-Ogg audio files to Minecraft-compatible Ogg Vorbis
 * using the managed ffmpeg downloaded by AudioLibraries.
 */
public final class AudioConverter {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final Set<String> CONVERTIBLE_EXTENSIONS = Set.of(
            "mp3", "m4a", "aac", "wav", "flac", "opus", "wma", "ogg"
    );

    public static final Set<String> NEEDS_CONVERSION = Set.of(
            "mp3", "m4a", "aac", "wav", "flac", "opus", "wma"
    );

    private AudioConverter() {
    }

    public static boolean needsConversion(String extension) {
        return extension != null && NEEDS_CONVERSION.contains(extension.toLowerCase(Locale.ROOT));
    }

    public static boolean isSupportedInput(String extension) {
        return extension != null && CONVERTIBLE_EXTENSIONS.contains(extension.toLowerCase(Locale.ROOT));
    }

    public static boolean isFfmpegAvailable() {
        return AudioLibraries.isFfmpegAvailable();
    }

    public static String ffmpegLocation() {
        return AudioLibraries.ffmpegPath()
                .map(p -> p.toAbsolutePath().toString())
                .orElse("");
    }

    /**
     * Convert {@code source} to an Ogg Vorbis file at {@code target}.
     * Blocks the calling thread, so call from your import/background thread.
     */
    public static void convertToOgg(Path source, Path target, int quality) throws IOException {
        Path ffmpeg = AudioLibraries.requireFfmpeg();

        Path parent = target.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        int clampedQuality = Math.max(0, Math.min(10, quality));

        List<String> args = Arrays.asList(
                ffmpeg.toAbsolutePath().toString(),
                "-y",
                "-loglevel", "error",
                "-i", source.toAbsolutePath().toString(),
                "-vn",
                "-map_metadata", "0",
                "-c:a", "libvorbis",
                "-q:a", Integer.toString(clampedQuality),
                target.toAbsolutePath().toString()
        );

        ProcessBuilder builder = new ProcessBuilder(args);
        builder.redirectErrorStream(true);

        Process process;

        try {
            process = builder.start();
        } catch (IOException ex) {
            throw new IOException("Failed to launch managed ffmpeg: " + ex.getMessage(), ex);
        }

        StringBuilder output = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (output.length() < 8192) {
                    output.append(line).append('\n');
                }
            }
        }

        boolean finished;

        try {
            finished = process.waitFor(10, TimeUnit.MINUTES);
        } catch (InterruptedException ie) {
            process.destroyForcibly();
            Thread.currentThread().interrupt();
            throw new IOException("ffmpeg interrupted", ie);
        }

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("ffmpeg timed out");
        }

        if (process.exitValue() != 0) {
            throw new IOException("ffmpeg exited with code " + process.exitValue() + ": " + output);
        }

        if (!Files.isRegularFile(target) || Files.size(target) <= 0L) {
            throw new IOException("ffmpeg did not produce output: " + target);
        }
    }
}