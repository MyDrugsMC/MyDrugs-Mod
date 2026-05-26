package org.mydrugs.mydrugs.client.recovery.music.tools;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;

/**
 * Immutable, pinned description of the optional external tools.
 *
 * <p>Version / download URL / SHA-256 are loaded from the classpath resource
 * {@code /mydrugs_external_tools.properties}, which ships with {@code <PLACEHOLDER>} values until a
 * maintainer fills in exact, verified entries. Documentation URLs (official website, release page,
 * license) and manual install commands are stable public links and are kept here as constants.
 *
 * <p>An in-GUI download is offered only when {@link #isDownloadPinned} is true, i.e. the version,
 * download URL and a syntactically valid SHA-256 are all present. There is no "latest" URL, no
 * nightly, and no self-update path anywhere in this manifest.
 */
public final class ExternalToolManifest {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String RESOURCE = "/mydrugs_external_tools.properties";

    private static final ExternalToolManifest INSTANCE = new ExternalToolManifest();

    private final Properties properties = new Properties();

    private ExternalToolManifest() {
        try (InputStream in = ExternalToolManifest.class.getResourceAsStream(RESOURCE)) {
            if (in != null) {
                properties.load(in);
            } else {
                LOGGER.warn("External tool manifest resource missing: {}", RESOURCE);
            }
        } catch (IOException e) {
            LOGGER.warn("Could not read external tool manifest: {}", e.getMessage());
        }
    }

    public static ExternalToolManifest get() {
        return INSTANCE;
    }

    /** A pinned, ready-to-download artifact for one tool on one platform. */
    public record Entry(ExternalTool tool, ExternalToolPlatform platform, String version,
                         String downloadUrl, String sha256, ArchiveType archiveType) {
    }

    public enum ArchiveType {
        /** A single executable file downloaded directly. */
        RAW,
        /** A .zip archive from which the executable entry is extracted. */
        ZIP;

        static ArchiveType parse(String value) {
            return value != null && value.trim().equalsIgnoreCase("zip") ? ZIP : RAW;
        }
    }

    private String value(String key) {
        String raw = properties.getProperty(key);
        return raw == null ? "" : raw.trim();
    }

    /** A value is a placeholder when it is blank or still contains the {@code <...>} marker. */
    private static boolean isPlaceholder(String value) {
        return value == null || value.isBlank() || value.contains("<") || value.contains(">");
    }

    private static boolean isValidSha256(String value) {
        return value != null && value.length() == 64 && value.chars()
                .allMatch(c -> (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'));
    }

    private static boolean isStableDownloadUrl(String value) {
        return value != null
                && value.startsWith("https://")
                && !value.startsWith("https://release-assets.githubusercontent.com/")
                && !value.contains("?");
    }

    public String version(ExternalTool tool) {
        return value(tool.manifestKey() + "_VERSION");
    }

    public String downloadUrl(ExternalTool tool, ExternalToolPlatform platform) {
        return value(tool.manifestKey() + "_URL_" + platform.manifestKey());
    }

    public String sha256(ExternalTool tool, ExternalToolPlatform platform) {
        return value(tool.manifestKey() + "_SHA256_" + platform.manifestKey());
    }

    public ArchiveType archiveType(ExternalTool tool, ExternalToolPlatform platform) {
        return ArchiveType.parse(value(tool.manifestKey() + "_ARCHIVE_" + platform.manifestKey()));
    }

    /**
     * True only when this tool can be downloaded in-GUI for the platform: the platform is one we
     * pin builds for, and the version, URL and a syntactically valid SHA-256 are all present.
     */
    public boolean isDownloadPinned(ExternalTool tool, ExternalToolPlatform platform) {
        if (platform == null || !platform.supportsDownload()) {
            return false;
        }
        String version = version(tool);
        String url = downloadUrl(tool, platform);
        String sha = sha256(tool, platform);
        return !isPlaceholder(version)
                && !isPlaceholder(url)
                && isStableDownloadUrl(url)
                && !isPlaceholder(sha)
                && isValidSha256(sha);
    }

    /** Resolved download entry, or {@code null} when not pinned for the platform. */
    public Entry downloadEntry(ExternalTool tool, ExternalToolPlatform platform) {
        if (!isDownloadPinned(tool, platform)) {
            return null;
        }
        return new Entry(tool, platform, version(tool), downloadUrl(tool, platform),
                sha256(tool, platform).toLowerCase(Locale.ROOT), archiveType(tool, platform));
    }

    public String displayName(ExternalTool tool) {
        return switch (tool) {
            case FFMPEG -> "FFmpeg";
            case FFPROBE -> "ffprobe (FFmpeg)";
            case YT_DLP -> "yt-dlp";
        };
    }

    /** Official project website (documentation / source), never a direct binary link. */
    public String officialWebsiteUrl(ExternalTool tool) {
        return switch (tool) {
            case FFMPEG, FFPROBE -> "https://ffmpeg.org/";
            case YT_DLP -> "https://github.com/yt-dlp/yt-dlp";
        };
    }

    /** Official release/download page a player can review before choosing to download. */
    public String releasePageUrl(ExternalTool tool) {
        return switch (tool) {
            case FFMPEG, FFPROBE -> "https://ffmpeg.org/download.html";
            case YT_DLP -> "https://github.com/yt-dlp/yt-dlp/releases";
        };
    }

    public String licenseUrl(ExternalTool tool) {
        return switch (tool) {
            case FFMPEG, FFPROBE -> "https://ffmpeg.org/legal.html";
            case YT_DLP -> "https://github.com/yt-dlp/yt-dlp/blob/master/LICENSE";
        };
    }

    /** A copyable manual-install command appropriate for the current platform. */
    public String manualInstallCommand(ExternalTool tool, ExternalToolPlatform platform) {
        return switch (tool) {
            case FFMPEG, FFPROBE -> switch (platform) {
                case WINDOWS_X64 -> "winget install Gyan.FFmpeg";
                case MACOS_ARM64 -> "brew install ffmpeg";
                default -> "sudo apt install ffmpeg";
            };
            case YT_DLP -> switch (platform) {
                case WINDOWS_X64 -> "winget install yt-dlp.yt-dlp";
                case MACOS_ARM64 -> "brew install yt-dlp";
                default -> "python3 -m pip install --user yt-dlp";
            };
        };
    }

    /** Human-readable note on where the executable ends up after a download. */
    public String extractionLayout(ExternalTool tool) {
        return tool == ExternalTool.FFPROBE
                ? "ffprobe is extracted from the FFmpeg archive into the mod-owned tools directory."
                : "Stored as a single executable in the mod-owned tools directory.";
    }
}
