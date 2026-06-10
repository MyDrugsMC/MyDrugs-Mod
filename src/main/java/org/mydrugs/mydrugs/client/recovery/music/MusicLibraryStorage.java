package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.client.Minecraft;

import java.nio.file.Path;

public final class MusicLibraryStorage {
    private MusicLibraryStorage() {
    }

    public static Path root() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("mydrugs").resolve("music");
    }

    public static Path libraryJson() {
        return root().resolve("library.json");
    }

    public static Path tracksDir() {
        return root().resolve("tracks");
    }

    public static Path cacheDir() {
        return root().resolve("cache");
    }

    public static Path artDir() {
        return root().resolve("art");
    }

    public static Path serverCacheDir() {
        return root().resolve("server_cache");
    }

    public static Path bookmarksJson() {
        return root().resolve("bookmarks.json");
    }
}
