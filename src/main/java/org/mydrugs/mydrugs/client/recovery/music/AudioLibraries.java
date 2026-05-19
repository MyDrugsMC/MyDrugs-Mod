package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Duration;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class AudioLibraries {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(30))
            .build();

    private static volatile boolean ready = false;

    private AudioLibraries() {
    }

    public static synchronized void ensureReady() throws IOException, InterruptedException {
        if (ready && Files.isRegularFile(ytDlpPathUnchecked())
                && Files.isRegularFile(ffmpegPathUnchecked())
                && Files.isRegularFile(ffprobePathUnchecked())) {
            return;
        }

        Platform platform = detectPlatform();
        Files.createDirectories(platformDir());

        installYtDlp(platform);
        installFfmpegAndFfprobe(platform);

        makeExecutableIfNeeded(ytDlpPathUnchecked());
        makeExecutableIfNeeded(ffmpegPathUnchecked());
        makeExecutableIfNeeded(ffprobePathUnchecked());

        verifyExecutable(ffmpegPathUnchecked(), "-version");
        verifyExecutable(ffprobePathUnchecked(), "-version");

        if (!ffmpegSupportsLibVorbis(ffmpegPathUnchecked())) {
            throw new IOException("Managed ffmpeg does not support libvorbis: " + ffmpegPathUnchecked());
        }

        ready = true;

        LOGGER.info("Audio libraries ready:");
        LOGGER.info("yt-dlp  = {}", ytDlpPathUnchecked().toAbsolutePath());
        LOGGER.info("ffmpeg  = {}", ffmpegPathUnchecked().toAbsolutePath());
        LOGGER.info("ffprobe = {}", ffprobePathUnchecked().toAbsolutePath());
    }

    public static Path ytDlpPath() throws IOException, InterruptedException {
        ensureReady();
        return ytDlpPathUnchecked();
    }

    public static Path ffmpegPath() throws IOException, InterruptedException {
        ensureReady();
        return ffmpegPathUnchecked();
    }

    public static Path ffprobePath() throws IOException, InterruptedException {
        ensureReady();
        return ffprobePathUnchecked();
    }

    /**
     * yt-dlp wants the folder containing ffmpeg and ffprobe for --ffmpeg-location.
     */
    public static Path ffmpegDirectory() throws IOException, InterruptedException {
        ensureReady();
        return platformDir();
    }

    public static String ffmpegLocationForYtDlp() throws IOException, InterruptedException {
        return ffmpegDirectory().toAbsolutePath().toString();
    }

    public static boolean isReady() {
        try {
            ensureReady();
            return true;
        } catch (Exception e) {
            LOGGER.warn("Audio libraries are not ready: {}", e.getMessage());
            return false;
        }
    }

    public static synchronized void forceReinstall() throws IOException, InterruptedException {
        ready = false;

        deleteIfExists(ytDlpPathUnchecked());
        deleteIfExists(ffmpegPathUnchecked());
        deleteIfExists(ffprobePathUnchecked());

        ensureReady();
    }

    private static Path libsRoot() {
        return Minecraft.getInstance()
                .gameDirectory
                .toPath()
                .resolve("mydrugs")
                .resolve("libs");
    }

    private static Path platformDir() throws IOException {
        return libsRoot().resolve(detectPlatform().id());
    }

    private static Path ytDlpPathUnchecked() throws IOException {
        return platformDir().resolve(isWindows() ? "yt-dlp.exe" : "yt-dlp");
    }

    private static Path ffmpegPathUnchecked() throws IOException {
        return platformDir().resolve(isWindows() ? "ffmpeg.exe" : "ffmpeg");
    }

    private static Path ffprobePathUnchecked() throws IOException {
        return platformDir().resolve(isWindows() ? "ffprobe.exe" : "ffprobe");
    }

    private static void installYtDlp(Platform platform) throws IOException, InterruptedException {
        Path target = ytDlpPathUnchecked();

        if (Files.isRegularFile(target) && Files.size(target) > 0L) {
            return;
        }

        LOGGER.info("Downloading yt-dlp for {}", platform.id());

        downloadBinary(platform.ytDlpUrl(), target);
        makeExecutableIfNeeded(target);
    }

    private static void installFfmpegAndFfprobe(Platform platform) throws IOException, InterruptedException {
        Path ffmpeg = ffmpegPathUnchecked();
        Path ffprobe = ffprobePathUnchecked();

        if (!Files.isRegularFile(ffmpeg) || Files.size(ffmpeg) <= 0L) {
            LOGGER.info("Downloading ffmpeg for {}", platform.id());
            downloadAndExtractSingleFromZip(
                    platform.ffmpegZipUrl(),
                    platform.ffmpegEntryName(),
                    ffmpeg
            );
            makeExecutableIfNeeded(ffmpeg);
        }

        if (!Files.isRegularFile(ffprobe) || Files.size(ffprobe) <= 0L) {
            LOGGER.info("Downloading ffprobe for {}", platform.id());
            downloadAndExtractSingleFromZip(
                    platform.ffprobeZipUrl(),
                    platform.ffprobeEntryName(),
                    ffprobe
            );
            makeExecutableIfNeeded(ffprobe);
        }
    }

    private static void downloadBinary(String url, Path target) throws IOException, InterruptedException {
        Files.createDirectories(target.getParent());

        Path temp = target.resolveSibling(target.getFileName() + ".tmp");

        try {
            downloadToFile(url, temp);
            Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            deleteIfExists(temp);
        }
    }

    private static void downloadAndExtractSingleFromZip(String url, String wantedFileName, Path target)
            throws IOException, InterruptedException {
        Files.createDirectories(target.getParent());

        Path zip = target.resolveSibling(target.getFileName() + ".zip.tmp");
        Path tempOutput = target.resolveSibling(target.getFileName() + ".tmp");

        try {
            downloadToFile(url, zip);
            extractSingleFileFromZip(zip, wantedFileName, tempOutput);

            if (!Files.isRegularFile(tempOutput) || Files.size(tempOutput) <= 0L) {
                throw new IOException("Extracted file is empty: " + wantedFileName);
            }

            Files.move(tempOutput, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            deleteIfExists(zip);
            deleteIfExists(tempOutput);
        }
    }

    private static void downloadToFile(String url, Path target) throws IOException, InterruptedException {
        LOGGER.info("Downloading {}", url);

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(Duration.ofMinutes(10))
                .header("User-Agent", "MyDrugs-Mod AudioLibraries")
                .GET()
                .build();

        HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());

        int status = response.statusCode();

        if (status < 200 || status >= 300) {
            try (InputStream ignored = response.body()) {
                throw new IOException("HTTP " + status + " while downloading " + url);
            }
        }

        try (InputStream body = response.body()) {
            Files.copy(body, target, StandardCopyOption.REPLACE_EXISTING);
        }

        if (!Files.isRegularFile(target) || Files.size(target) <= 0L) {
            throw new IOException("Downloaded file is empty: " + url);
        }
    }

    private static void extractSingleFileFromZip(Path zipFile, String wantedFileName, Path target) throws IOException {
        try (ZipInputStream zip = new ZipInputStream(Files.newInputStream(zipFile))) {
            ZipEntry entry;

            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String entryName = entry.getName().replace('\\', '/');
                String simpleName = entryName.substring(entryName.lastIndexOf('/') + 1);

                if (simpleName.equalsIgnoreCase(wantedFileName)) {
                    Files.copy(zip, target, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        }

        throw new IOException("Could not find " + wantedFileName + " inside " + zipFile);
    }

    private static void verifyExecutable(Path executable, String arg) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(
                executable.toAbsolutePath().toString(),
                arg
        ).redirectErrorStream(true).start();

        boolean finished = process.waitFor(15, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Timed out while verifying " + executable);
        }

        if (process.exitValue() != 0) {
            throw new IOException("Executable check failed for " + executable + " with code " + process.exitValue());
        }
    }

    private static boolean ffmpegSupportsLibVorbis(Path ffmpeg) {
        try {
            Process process = new ProcessBuilder(
                    ffmpeg.toAbsolutePath().toString(),
                    "-hide_banner",
                    "-encoders"
            ).redirectErrorStream(true).start();

            boolean found = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)
            )) {
                String line;

                while ((line = reader.readLine()) != null) {
                    if (line.toLowerCase(Locale.ROOT).contains("libvorbis")) {
                        found = true;
                    }
                }
            }

            boolean finished = process.waitFor(15, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            return process.exitValue() == 0 && found;

        } catch (Exception e) {
            LOGGER.warn("Could not check libvorbis support: {}", e.getMessage());
            return false;
        }
    }

    private static void makeExecutableIfNeeded(Path file) throws IOException, InterruptedException {
        if (isWindows()) {
            return;
        }

        try {
            Set<PosixFilePermission> permissions = EnumSet.of(
                    PosixFilePermission.OWNER_READ,
                    PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE,
                    PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE,
                    PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE
            );

            Files.setPosixFilePermissions(file, permissions);
            return;
        } catch (UnsupportedOperationException ignored) {
            // Fall back to chmod.
        }

        Process chmod = new ProcessBuilder(
                "chmod",
                "+x",
                file.toAbsolutePath().toString()
        ).redirectErrorStream(true).start();

        boolean finished = chmod.waitFor(10, TimeUnit.SECONDS);

        if (!finished) {
            chmod.destroyForcibly();
            throw new IOException("chmod timed out for " + file);
        }

        if (chmod.exitValue() != 0) {
            throw new IOException("chmod failed for " + file + " with code " + chmod.exitValue());
        }
    }

    private static Platform detectPlatform() throws IOException {
        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch").toLowerCase(Locale.ROOT);

        boolean x64 = arch.equals("x86_64") || arch.equals("amd64");
        boolean arm64 = arch.equals("aarch64") || arch.equals("arm64");

        if (os.contains("win")) {
            if (!x64) {
                throw new IOException("Unsupported Windows architecture: " + arch);
            }

            return new Platform(
                    "windows-x64",
                    "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp.exe",
                    "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffmpeg-6.1-win-64.zip",
                    "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffprobe-6.1-win-64.zip",
                    "ffmpeg.exe",
                    "ffprobe.exe"
            );
        }

        if (os.contains("mac") || os.contains("darwin")) {
            if (arm64) {
                return new Platform(
                        "macos-arm64",
                        "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos",
                        "https://www.osxexperts.net/ffmpeg6arm.zip",
                        "https://www.osxexperts.net/ffprobe6arm.zip",
                        "ffmpeg",
                        "ffprobe"
                );
            }

            if (x64) {
                return new Platform(
                        "macos-x64",
                        "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_macos",
                        "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffmpeg-6.1-osx-64.zip",
                        "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffprobe-6.1-osx-64.zip",
                        "ffmpeg",
                        "ffprobe"
                );
            }

            throw new IOException("Unsupported macOS architecture: " + arch);
        }

        if (os.contains("linux")) {
            if (arm64) {
                return new Platform(
                        "linux-arm64",
                        "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_aarch64",
                        "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffmpeg-6.1-linux-arm64.zip",
                        "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffprobe-6.1-linux-arm64.zip",
                        "ffmpeg",
                        "ffprobe"
                );
            }

            if (x64) {
                return new Platform(
                        "linux-x64",
                        "https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux",
                        "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffmpeg-6.1-linux-64.zip",
                        "https://github.com/ffbinaries/ffbinaries-prebuilt/releases/download/v6.1/ffprobe-6.1-linux-64.zip",
                        "ffmpeg",
                        "ffprobe"
                );
            }

            throw new IOException("Unsupported Linux architecture: " + arch);
        }

        throw new IOException("Unsupported OS: " + os + " / " + arch);
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
    }

    private static void deleteIfExists(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }

    private record Platform(
            String id,
            String ytDlpUrl,
            String ffmpegZipUrl,
            String ffprobeZipUrl,
            String ffmpegEntryName,
            String ffprobeEntryName
    ) {
    }
}