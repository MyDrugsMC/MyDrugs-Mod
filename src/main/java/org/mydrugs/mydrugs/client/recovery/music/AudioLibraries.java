package org.mydrugs.mydrugs.client.recovery.music;

import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalTool;
import org.mydrugs.mydrugs.client.recovery.music.tools.ExternalToolManager;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Thin, no-download facade over {@link ExternalToolManager} for the audio features.
 *
 * <p>This class never downloads anything. Tools are resolved from user config / system PATH /
 * common locations / a previously-verified mod-managed copy. When a tool is missing the dependent
 * feature simply reports as disabled — the mod stays playable. Downloads happen only through the
 * disclaimer screen, on an explicit player click, with SHA-256 verification.
 */
public final class AudioLibraries {
    private AudioLibraries() {
    }

    private static Optional<Path> resolve(ExternalTool tool) {
        ExternalToolManager.Resolution resolution = ExternalToolManager.get().resolve(tool);
        return resolution.usable() ? Optional.of(resolution.path()) : Optional.empty();
    }

    public static boolean isFfmpegAvailable() {
        return resolve(ExternalTool.FFMPEG).isPresent();
    }

    public static boolean isYtDlpAvailable() {
        return resolve(ExternalTool.YT_DLP).isPresent();
    }

    public static Optional<Path> ffmpegPath() {
        return resolve(ExternalTool.FFMPEG);
    }

    public static Optional<Path> ffprobePath() {
        return resolve(ExternalTool.FFPROBE);
    }

    public static Optional<Path> ytDlpPath() {
        return resolve(ExternalTool.YT_DLP);
    }

    /** Resolved ffmpeg path, or an {@link IOException} when ffmpeg is unavailable. */
    public static Path requireFfmpeg() throws IOException {
        return ffmpegPath().orElseThrow(() ->
                new IOException("FFmpeg is not available; configure or download it in the MyDrugs notice screen"));
    }

    /** Resolved yt-dlp path, or an {@link IOException} when yt-dlp is unavailable. */
    public static Path requireYtDlp() throws IOException {
        return ytDlpPath().orElseThrow(() ->
                new IOException("yt-dlp is not available; configure or download it in the MyDrugs notice screen"));
    }

    /** Directory holding ffmpeg, for {@code yt-dlp --ffmpeg-location}, when ffmpeg is available. */
    public static Optional<String> ffmpegDirectoryForYtDlp() {
        return ffmpegPath()
                .map(Path::toAbsolutePath)
                .map(Path::getParent)
                .map(Path::toString);
    }
}
