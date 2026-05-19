package org.mydrugs.mydrugs.client.recovery.music;

public final class MusicTrack {
    public String id = "";
    public String title = "";
    public String artist = "";
    public String album = "";
    public long durationMs;
    public boolean liked;
    public SourceType sourceType = SourceType.LOCAL_FILE;
    public String localPath = "";
    public String originalSource = "";
    public long addedAt;
    public long lastPlayedAt;
    public int playCount;
    public float volume = 1.0F;
    public String coverPath = "";

    public enum SourceType {
        LOCAL_FILE,
        DIRECT_URL,
        BOOKMARK,
        BUILT_IN
    }

    public boolean isPlayable() {
        if (sourceType == SourceType.BOOKMARK) {
            return false;
        }
        if (sourceType == SourceType.BUILT_IN) {
            return localPath != null && !localPath.isBlank();
        }
        return localPath != null && !localPath.isBlank();
    }

    public boolean isOgg() {
        if (sourceType == SourceType.BUILT_IN) {
            return localPath != null && localPath.toLowerCase(java.util.Locale.ROOT).endsWith(".ogg");
        }
        String path = localPath == null || localPath.isBlank() ? originalSource : localPath;
        return path != null && path.toLowerCase(java.util.Locale.ROOT).endsWith(".ogg");
    }

    public String displayArtist() {
        if (artist != null && !artist.isBlank()) {
            return artist;
        }
        return switch (sourceType) {
            case BUILT_IN -> "Minecraft";
            case BOOKMARK -> "bookmark";
            case DIRECT_URL -> "url";
            case LOCAL_FILE -> "local";
        };
    }
}
