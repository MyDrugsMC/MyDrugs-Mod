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
    public static Path download(String url) {
        Path ytDlp;
        try {
            ytDlp = AudioLibraries.requireYtDlp();
        } catch (IOException e) {
            MyDrugs.getLOGGER().info("yt-dlp not available; online import is disabled: {}", e.getMessage());
            return null;
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

            runCommand(command);

            if (Files.exists(expectedOutput)) {
                return expectedOutput;
            }
            return findDownloadedFile(dlFolder, id);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            MyDrugs.getLOGGER().warn("Download interrupted for {}: {}", url, e.getMessage());
            return null;
        } catch (Exception e) {
            MyDrugs.getLOGGER().warn("Could not download {}: {}", url, e.getMessage());
            return null;
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

    private static void runCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();
        List<String> output = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (output.size() < 256) {
                    output.add(line);
                }
                MyDrugs.getLOGGER().info("[yt-dlp] {}", line);
            }
        }

        boolean finished = process.waitFor(PROCESS_TIMEOUT_MINUTES, TimeUnit.MINUTES);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("yt-dlp timed out");
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new IOException("yt-dlp failed with exit code " + exitCode + "\n"
                    + String.join("\n", output));
        }
    }
}
