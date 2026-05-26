package org.mydrugs.mydrugs.client.recovery.music.tools;

import java.util.Locale;

/**
 * Identity of an optional external command-line tool used by the music features.
 *
 * <p>None of these tools are required: the mod is fully playable without them. They are never
 * downloaded or executed silently — see {@link ExternalToolManager} for the resolution order and
 * {@link ExternalToolDownloader} for the explicit, SHA-256-verified download path.
 */
public enum ExternalTool {
    /** FFmpeg: optional audio conversion to Ogg Vorbis. */
    FFMPEG("ffmpeg", "ffmpeg.exe", "ffmpeg"),
    /** ffprobe: optional audio metadata reading. Ships inside FFmpeg distributions. */
    FFPROBE("ffprobe", "ffprobe.exe", "ffprobe"),
    /** yt-dlp: optional online media processing. */
    YT_DLP("yt-dlp", "yt-dlp.exe", "yt-dlp");

    private final String id;
    private final String windowsExecutable;
    private final String unixExecutable;

    ExternalTool(String id, String windowsExecutable, String unixExecutable) {
        this.id = id;
        this.windowsExecutable = windowsExecutable;
        this.unixExecutable = unixExecutable;
    }

    public String id() {
        return id;
    }

    /** Uppercase key fragment used in the pinned manifest properties file. */
    public String manifestKey() {
        return name();
    }

    public String executableName(boolean windows) {
        return windows ? windowsExecutable : unixExecutable;
    }

    public static ExternalTool byId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (ExternalTool tool : values()) {
            if (tool.id.equals(normalized)) {
                return tool;
            }
        }
        return null;
    }
}
