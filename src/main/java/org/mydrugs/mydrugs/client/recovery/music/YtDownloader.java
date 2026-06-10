package org.mydrugs.mydrugs.client.recovery.music;

import org.mydrugs.mydrugs.MyDrugs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * Optional online-media import via yt-dlp.
 *
 * <p>yt-dlp is NEVER auto-installed: it is resolved through {@link AudioLibraries} (user config /
 * system PATH / common locations / a previously-verified mod-managed copy). When yt-dlp is not
 * available the feature is simply disabled and {@link #download} returns {@code null} without
 * throwing. The process is launched with a {@link ProcessBuilder} argument array — never a shell
 * string — and is bounded by a timeout.
 */
public final class YtDownloader {
    public enum FailureReason {
        NONE,
        YT_DLP_MISSING,
        FFMPEG_MISSING,
        TIMEOUT,
        EXIT_CODE,
        OUTPUT_MISSING,
        INVALID_URL,
        IO_ERROR
    }

    public record DownloadResult(boolean success, Path path, String messageKey, FailureReason reason) {
        private static DownloadResult failure(String key, FailureReason reason) {
            return new DownloadResult(false, null, key, reason);
        }
    }

    private static final Pattern URL_PATTERN = Pattern.compile("https?://\\S+", Pattern.CASE_INSENSITIVE);

    private static final String AUDIO_FORMAT = "vorbis";
    private static final String AUDIO_EXTENSION = "ogg";
    private static final String AUDIO_QUALITY = "5";
    private static final long PROCESS_TIMEOUT_MINUTES = 15L;

    private YtDownloader() {
    }

    /** True when the online-media import feature can run. */
    public static boolean isAvailable() {
        return AudioLibraries.isYtDlpAvailable();
    }

    /**
     * Downloads audio from {@code url} using yt-dlp. Returns the downloaded file, or {@code null}
     * when yt-dlp is unavailable or the download fails — callers must handle {@code null} as a
     * disabled/failed feature rather than an error.
     */
    public static DownloadResult download(String url) {
        if (url == null || url.isBlank()) {
            return DownloadResult.failure("screen.mydrugs.music.import_error.invalid_url", FailureReason.INVALID_URL);
        }
        Path ytDlp;
        try {
            ytDlp = AudioLibraries.requireYtDlp();
        } catch (IOException e) {
            MyDrugs.getLOGGER().info("yt-dlp is unavailable; online import is disabled");
            return DownloadResult.failure("screen.mydrugs.music.import_error.ytdlp_missing", FailureReason.YT_DLP_MISSING);
        }
        if (AudioLibraries.ffmpegDirectoryForYtDlp().isEmpty()) {
            return DownloadResult.failure("screen.mydrugs.music.import_error.ffmpeg_missing", FailureReason.FFMPEG_MISSING);
        }

        try {
            Path dlFolder = MusicLibraryStorage.root().resolve("downloads");
            Files.createDirectories(dlFolder);

            String id = UUID.randomUUID().toString();
            Path outputTemplate = dlFolder.resolve(id + ".%(ext)s");
            Path expectedOutput = dlFolder.resolve(id + "." + AUDIO_EXTENSION);

            List<String> command = new ArrayList<>(List.of(
                    ytDlp.toAbsolutePath().toString(),
                    "--no-playlist",
                    "-x",
                    "--audio-format", AUDIO_FORMAT,
                    "--audio-quality", AUDIO_QUALITY,
                    "-o", outputTemplate.toString()
            ));
            // Pass ffmpeg only when it resolved; yt-dlp needs it for the Vorbis conversion.
            Optional<String> ffmpegDir = AudioLibraries.ffmpegDirectoryForYtDlp();
            ffmpegDir.ifPresent(dir -> {
                command.add("--ffmpeg-location");
                command.add(dir);
            });
            command.add(url);

            CommandResult commandResult = runCommand(command);
            if (commandResult.timedOut()) {
                return DownloadResult.failure("screen.mydrugs.music.import_error.timeout", FailureReason.TIMEOUT);
            }
            if (commandResult.exitCode() != 0) {
                MyDrugs.getLOGGER().warn("yt-dlp failed with exit code {}", commandResult.exitCode());
                return DownloadResult.failure("screen.mydrugs.music.import_error.download_failed", FailureReason.EXIT_CODE);
            }

            if (Files.exists(expectedOutput)) {
                return new DownloadResult(true, expectedOutput, "screen.mydrugs.music.done", FailureReason.NONE);
            }
            Path found = findDownloadedFile(dlFolder, id);
            return found == null
                    ? DownloadResult.failure("screen.mydrugs.music.import_error.output_missing", FailureReason.OUTPUT_MISSING)
                    : new DownloadResult(true, found, "screen.mydrugs.music.done", FailureReason.NONE);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            MyDrugs.getLOGGER().warn("yt-dlp download was interrupted");
            return DownloadResult.failure("screen.mydrugs.music.import_error.download_failed", FailureReason.IO_ERROR);
        } catch (Exception e) {
            MyDrugs.getLOGGER().warn("yt-dlp download failed: {}", redactExternalToolOutput(e.getMessage()));
            return DownloadResult.failure("screen.mydrugs.music.import_error.download_failed", FailureReason.IO_ERROR);
        }
    }

    private static Path findDownloadedFile(Path folder, String id) throws IOException {
        if (!Files.exists(folder)) {
            return null;
        }
        try (var stream = Files.list(folder)) {
            return stream
                    .filter(path -> path.getFileName().toString().startsWith(id + "."))
                    .findFirst()
                    .orElse(null);
        }
    }

    private static CommandResult runCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        MyDrugs.getLOGGER().info("yt-dlp started");
        Process process = pb.start();
        Thread outputReader = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    MyDrugs.getLOGGER().debug("[yt-dlp] {}", redactExternalToolOutput(line));
                }
            } catch (IOException ignored) {
            }
        }, "MyDrugs yt-dlp output");
        outputReader.setDaemon(true);
        outputReader.start();

        boolean finished = process.waitFor(PROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            MyDrugs.getLOGGER().warn("yt-dlp timed out");
            return new CommandResult(-1, true);
        }

        int exitCode = process.exitValue();
        if (exitCode == 0) {
            MyDrugs.getLOGGER().info("yt-dlp completed");
        }
        return new CommandResult(exitCode, false);
    }

    public static String redactExternalToolOutput(String line) {
        if (line == null || line.isBlank()) {
            return "";
        }
        return URL_PATTERN.matcher(line).replaceAll("[redacted-url]");
    }

    private record CommandResult(int exitCode, boolean timedOut) {
    }
}
