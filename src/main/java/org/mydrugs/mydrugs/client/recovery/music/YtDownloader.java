package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.client.Minecraft;
import org.mydrugs.mydrugs.MyDrugs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class YtDownloader {

    private static final String AUDIO_FORMAT = "vorbis";
    private static final String AUDIO_EXTENSION = "ogg";
    private static final String AUDIO_QUALITY = "5";

    private static boolean loaded = false;

    private static Path libDir() {
        return Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("mydrugs")
                .resolve("libs");
    }

    private static Path ytDlpPath() {
        String os = currentOs();

        String fileName;

        if (os.contains("win")) {
            fileName = "yt-dlp.exe";
        } else if (os.contains("mac")) {
            fileName = "yt-dlp_macos";
        } else {
            fileName = "yt-dlp";
        }

        return libDir().resolve(fileName);
    }

    private static String currentOs() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT);
    }

    private static synchronized void ensureLoaded() throws IOException {
        if (loaded) return;

        Files.createDirectories(libDir());
        loaded = true;
    }

    public static void checkYtDlpInstallation() throws IOException, InterruptedException {
        ensureLoaded();

        Path binaryPath = ytDlpPath();
        String os = currentOs();

        if (Files.exists(binaryPath)) {
            if (!os.contains("win") && !Files.isExecutable(binaryPath)) {
                makeExecutable(binaryPath);
            }
            return;
        }

        String url;

        if (os.contains("win")) {
            url = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe";
        } else if (os.contains("mac")) {
            url = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos";
        } else {
            url = "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp";
        }

        MyDrugs.getLOGGER().info("Downloading yt-dlp from {}", url);

        Path tempFile = binaryPath.resolveSibling(binaryPath.getFileName() + ".tmp");

        try (InputStream in = URI.create(url).toURL().openStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        Files.move(tempFile, binaryPath, StandardCopyOption.REPLACE_EXISTING);

        if (!os.contains("win")) {
            makeExecutable(binaryPath);
        }

        MyDrugs.getLOGGER().info("yt-dlp installed at {}", binaryPath.toAbsolutePath());
    }

    private static void makeExecutable(Path file) throws IOException, InterruptedException {
        runCommand(List.of(
                "chmod",
                "+x",
                file.toAbsolutePath().toString()
        ));
    }

    public static Path download(String url) {
        try {
            checkYtDlpInstallation();

            Path root = MusicLibraryStorage.root();
            Path dlFolder = root.resolve("downloads");
            Files.createDirectories(dlFolder);

            String id = UUID.randomUUID().toString();

            Path outputTemplate = dlFolder.resolve(id + ".%(ext)s");
            Path expectedOutput = dlFolder.resolve(id + "." + AUDIO_EXTENSION);

            List<String> command = List.of(
                    ytDlpPath().toAbsolutePath().toString(),
                    "--ffmpeg-location", AudioLibraries.ffmpegLocationForYtDlp(),
                    "--no-playlist",
                    "-x",
                    "--audio-format", AUDIO_FORMAT,
                    "--audio-quality", AUDIO_QUALITY,
                    "-o", outputTemplate.toString(),
                    url
            );

            runCommand(command);

            if (Files.exists(expectedOutput)) {
                return expectedOutput;
            }

            Path discovered = findDownloadedFile(dlFolder, id);

            if (discovered != null) {
                return discovered;
            }

            MyDrugs.getLOGGER().warn("yt-dlp finished but no output file was found for {}", url);
            return null;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            MyDrugs.getLOGGER().warn("Download interrupted for {} : {}", url, e.getMessage());
            return null;

        } catch (Exception e) {
            MyDrugs.getLOGGER().warn("Could not download {} : {}", url, e.getMessage());
            return null;
        }
    }

    private static Path findDownloadedFile(Path folder, String id) throws IOException {
        if (!Files.exists(folder)) return null;

        try (var stream = Files.list(folder)) {
            return stream
                    .filter(path -> {
                        String name = path.getFileName().toString();
                        return name.startsWith(id + ".");
                    })
                    .findFirst()
                    .orElse(null);
        }
    }

    private static List<String> runCommand(List<String> command) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);

        Process process = pb.start();

        List<String> output = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
        )) {
            String line;

            while ((line = reader.readLine()) != null) {
                output.add(line);
                MyDrugs.getLOGGER().info("[yt-dlp] {}", line);
            }
        }

        int exitCode = process.waitFor();

        if (exitCode != 0) {
            String joinedOutput = String.join("\n", output);

            throw new IOException(
                    "Command failed with exit code "
                            + exitCode
                            + ": "
                            + String.join(" ", command)
                            + "\n"
                            + joinedOutput
            );
        }

        return output;
    }
}