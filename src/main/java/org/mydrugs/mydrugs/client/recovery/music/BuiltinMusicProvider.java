package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Scans Minecraft's loaded resources for Ogg files under {@code sounds/music/} and
 * {@code sounds/music_disc/} and exposes them as {@link MusicTrack}s with
 * {@link MusicTrack.SourceType#BUILT_IN}.
 *
 * <p>Built-in tracks store their resource location string in {@link MusicTrack#localPath}
 * (e.g. {@code "minecraft:sounds/music/game/calm1.ogg"}) so playback can resolve them through
 * the resource manager.
 */
public final class BuiltinMusicProvider {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String SCAN_PREFIX = "sounds";

    private BuiltinMusicProvider() {
    }

    public static List<MusicTrack> discover() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null) {
            return List.of();
        }
        ResourceManager rm = mc.getResourceManager();
        if (rm == null) {
            return List.of();
        }

        Map<ResourceLocation, Resource> all = new LinkedHashMap<>();
        try {
            Map<ResourceLocation, Resource> found = rm.listResources(SCAN_PREFIX, loc -> {
                if (!"minecraft".equals(loc.getNamespace())) {
                    return false;
                }
                String p = loc.getPath().toLowerCase(Locale.ROOT);
                if (!p.endsWith(".ogg")) return false;
                return p.startsWith("sounds/music/") || p.startsWith("sounds/music_disc/");
            });
            all.putAll(found);
        } catch (Exception ex) {
            LOGGER.debug("Failed to list builtin music: {}", ex.getMessage());
        }

        List<MusicTrack> tracks = new ArrayList<>(all.size());
        for (Map.Entry<ResourceLocation, Resource> entry : all.entrySet()) {
            ResourceLocation loc = entry.getKey();
            MusicTrack track = new MusicTrack();
            track.id = idFor(loc);
            track.title = prettyTitle(loc);
            track.artist = artistFor(loc);
            track.album = albumFor(loc);
            track.sourceType = MusicTrack.SourceType.BUILT_IN;
            track.localPath = loc.toString();
            track.originalSource = loc.toString();
            track.addedAt = Instant.now().toEpochMilli();
            // Duration probed lazily — opening every MC resource on startup would be heavy.
            tracks.add(track);
        }
        LOGGER.info("Discovered {} built-in music tracks", tracks.size());
        return tracks;
    }

    /**
     * Opens an Ogg stream for a built-in track via the resource manager. Returns null if the
     * resource is missing (e.g. asset index issue).
     */
    public static InputStream open(ResourceLocation location) {
        try {
            return Minecraft.getInstance().getResourceManager()
                    .getResource(location)
                    .map(resource -> {
                        try {
                            return resource.open();
                        } catch (Exception ex) {
                            LOGGER.warn("Failed to open built-in resource {}: {}", location, ex.getMessage());
                            return null;
                        }
                    })
                    .orElse(null);
        } catch (Exception ex) {
            LOGGER.warn("Failed to resolve built-in resource {}: {}", location, ex.getMessage());
            return null;
        }
    }

    public static String idFor(ResourceLocation loc) {
        return "mc:" + sha32(loc.toString());
    }

    private static String prettyTitle(ResourceLocation loc) {
        String path = loc.getPath();
        int slash = path.lastIndexOf('/');
        String name = slash < 0 ? path : path.substring(slash + 1);
        if (name.toLowerCase(Locale.ROOT).endsWith(".ogg")) {
            name = name.substring(0, name.length() - 4);
        }
        // Replace underscores with spaces, capitalize words
        StringBuilder out = new StringBuilder(name.length());
        boolean capNext = true;
        for (char c : name.toCharArray()) {
            if (c == '_' || c == '-') {
                out.append(' ');
                capNext = true;
            } else if (capNext && Character.isLetter(c)) {
                out.append(Character.toUpperCase(c));
                capNext = false;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String artistFor(ResourceLocation loc) {
        String path = loc.getPath().toLowerCase(Locale.ROOT);
        if (path.contains("music_disc/pigstep")) {
            return "Lena Raine";
        }
        if (path.contains("music_disc/otherside")
                || path.contains("music_disc/relic")
                || path.contains("music_disc/precipice")
                || path.contains("music/nether/")
                || path.contains("music/end/credits")) {
            return "Lena Raine";
        }
        if (path.contains("music_disc/5") || path.contains("music_disc/ward")) {
            return "Samuel Åberg";
        }
        if (path.contains("music_disc/creator")) {
            return "Lena Raine";
        }
        return "C418";
    }

    private static String albumFor(ResourceLocation loc) {
        String path = loc.getPath().toLowerCase(Locale.ROOT);
        if (path.startsWith("sounds/music_disc/")) {
            return "Music Discs";
        }
        if (path.contains("/music/menu/")) {
            return "Minecraft — Menu";
        }
        if (path.contains("/music/creative/")) {
            return "Minecraft — Creative";
        }
        if (path.contains("/music/nether/")) {
            return "Minecraft — Nether";
        }
        if (path.contains("/music/end/")) {
            return "Minecraft — The End";
        }
        if (path.contains("/music/under_water/") || path.contains("/music/game/water/")) {
            return "Minecraft — Underwater";
        }
        if (path.contains("/music/game/")) {
            return "Minecraft — Overworld";
        }
        return "Minecraft";
    }

    private static String sha32(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32);
            for (int i = 0; i < 16; i++) {
                sb.append(Character.forDigit((bytes[i] >> 4) & 0xF, 16));
                sb.append(Character.forDigit(bytes[i] & 0xF, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
