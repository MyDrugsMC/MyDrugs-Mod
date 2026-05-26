package org.mydrugs.mydrugs.client.recovery.music.tools;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Safe, explicit download of a pinned external tool.
 *
 * <p>Safety guarantees, all required by the project's hard constraints:
 * <ul>
 *   <li>only ever called from an explicit player click — never automatically;</li>
 *   <li>downloads to a temp file inside the mod-owned tools directory;</li>
 *   <li>bounds the response size and uses connect/read timeouts;</li>
 *   <li>verifies the SHA-256 of the downloaded bytes <em>before</em> any extraction or use;</li>
 *   <li>extracts only into the mod-owned directory, never overwriting files elsewhere;</li>
 *   <li>quarantines/deletes anything that fails verification;</li>
 *   <li>never executes the temp file and never touches PATH.</li>
 * </ul>
 */
public final class ExternalToolDownloader {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Hard ceiling on a downloaded artifact; FFmpeg zips are well under this. */
    private static final long MAX_DOWNLOAD_BYTES = 256L * 1024L * 1024L;
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration REQUEST_TIMEOUT = Duration.ofMinutes(10);

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(CONNECT_TIMEOUT)
            .build();

    /** Receives download progress; {@code total} is -1 when the server does not report a length. */
    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(long bytesRead, long total);
    }

    /** Outcome of a download attempt. */
    public record Outcome(ExternalToolStatus status, String messageKey) {
    }

    private ExternalToolDownloader() {
    }

    /**
     * Downloads, verifies and installs {@code entry}'s tool into {@code targetExecutable}.
     *
     * @param targetExecutable final mod-owned path the verified executable is installed to
     * @param workDir          mod-owned scratch directory for temp + quarantine files
     */
    public static Outcome download(ExternalToolManifest.Entry entry, ExternalTool tool,
                                   Path targetExecutable, Path workDir, ProgressListener listener) {
        Path temp = workDir.resolve(tool.id() + ".download.tmp");
        try {
            Files.createDirectories(workDir);
            Files.createDirectories(targetExecutable.getParent());

            String actualSha = downloadToTemp(entry.downloadUrl(), temp, listener);

            if (!actualSha.equalsIgnoreCase(entry.sha256())) {
                quarantine(temp, workDir, tool);
                LOGGER.warn("SHA-256 mismatch for {}: expected {}, got {}",
                        tool.id(), entry.sha256(), actualSha);
                return new Outcome(ExternalToolStatus.VERIFICATION_FAILED,
                        "message.mydrugs.external_tool.verification_failed");
            }

            // Verified bytes only past this point.
            if (entry.archiveType() == ExternalToolManifest.ArchiveType.ZIP) {
                extractExecutable(temp, targetExecutable,
                        tool.executableName(ExternalToolPlatform.isCurrentOsWindows()), workDir);
            } else {
                Files.move(temp, targetExecutable, StandardCopyOption.REPLACE_EXISTING);
            }
            makeExecutable(targetExecutable);

            return new Outcome(ExternalToolStatus.DOWNLOADED_VERIFIED,
                    "message.mydrugs.external_tool.downloaded_verified");
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            LOGGER.warn("Download failed for {}: {}", tool.id(), e.getMessage());
            deleteQuietly(temp);
            // Network/IO failures are non-fatal: the feature simply stays disabled.
            return new Outcome(ExternalToolStatus.NOT_FOUND,
                    "message.mydrugs.external_tool.download_failed");
        }
    }

    private static String downloadToTemp(String url, Path temp, ProgressListener listener)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .timeout(REQUEST_TIMEOUT)
                .header("User-Agent", "MyDrugs-Mod")
                .GET()
                .build();

        HttpResponse<InputStream> response = HTTP.send(request, HttpResponse.BodyHandlers.ofInputStream());
        int status = response.statusCode();
        if (status < 200 || status >= 300) {
            try (InputStream ignored = response.body()) {
                throw new IOException("HTTP " + status + " for " + url);
            }
        }

        long total = response.headers().firstValueAsLong("Content-Length").orElse(-1L);
        if (total > MAX_DOWNLOAD_BYTES) {
            try (InputStream ignored = response.body()) {
                throw new IOException("Declared size exceeds limit: " + total);
            }
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (Exception e) {
            throw new IOException("SHA-256 unavailable", e);
        }

        long read = 0L;
        byte[] buffer = new byte[64 * 1024];
        try (InputStream in = response.body();
             OutputStream out = Files.newOutputStream(temp)) {
            int n;
            while ((n = in.read(buffer)) != -1) {
                read += n;
                if (read > MAX_DOWNLOAD_BYTES) {
                    throw new IOException("Download exceeded size limit");
                }
                out.write(buffer, 0, n);
                digest.update(buffer, 0, n);
                if (listener != null) {
                    listener.onProgress(read, total);
                }
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void extractExecutable(Path zip, Path target, String wantedSimpleName, Path workDir)
            throws IOException {
        Path tempOut = workDir.resolve(target.getFileName() + ".extract.tmp");
        try (ZipInputStream zin = new ZipInputStream(Files.newInputStream(zip))) {
            ZipEntry zipEntry;
            while ((zipEntry = zin.getNextEntry()) != null) {
                if (zipEntry.isDirectory()) {
                    continue;
                }
                String name = zipEntry.getName().replace('\\', '/');
                String simple = name.substring(name.lastIndexOf('/') + 1);
                if (simple.equalsIgnoreCase(wantedSimpleName)) {
                    Files.copy(zin, tempOut, StandardCopyOption.REPLACE_EXISTING);
                    if (Files.size(tempOut) <= 0L) {
                        throw new IOException("Extracted entry is empty: " + wantedSimpleName);
                    }
                    Files.move(tempOut, target, StandardCopyOption.REPLACE_EXISTING);
                    return;
                }
            }
        } finally {
            deleteQuietly(tempOut);
            deleteQuietly(zip);
        }
        throw new IOException("Executable " + wantedSimpleName + " not found inside archive");
    }

    private static void makeExecutable(Path file) {
        if (ExternalToolPlatform.isCurrentOsWindows()) {
            return;
        }
        try {
            Set<PosixFilePermission> perms = EnumSet.of(
                    PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE,
                    PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_READ,
                    PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_READ,
                    PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(file, perms);
        } catch (Exception e) {
            LOGGER.warn("Could not mark {} executable: {}", file, e.getMessage());
        }
    }

    private static void quarantine(Path temp, Path workDir, ExternalTool tool) {
        try {
            if (Files.exists(temp)) {
                Path quarantine = workDir.resolve(tool.id() + ".quarantine."
                        + System.currentTimeMillis() + ".bin");
                Files.move(temp, quarantine, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.warn("Quarantined unverified download: {}", quarantine);
            }
        } catch (IOException e) {
            deleteQuietly(temp);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
        }
    }
}
