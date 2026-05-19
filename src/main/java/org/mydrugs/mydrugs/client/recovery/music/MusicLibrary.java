package org.mydrugs.mydrugs.client.recovery.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.network.chat.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import net.minecraft.resources.ResourceLocation;

public final class MusicLibrary {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final MusicLibrary INSTANCE = new MusicLibrary();

    private final List<MusicTrack> tracks = new ArrayList<>();
    private boolean loaded;
    private int version;
    private Component lastStatus = Component.empty();

    private MusicLibrary() {
    }

    public static MusicLibrary get() {
        return INSTANCE;
    }

    public synchronized List<MusicTrack> tracks() {
        ensureLoaded();
        return List.copyOf(tracks);
    }

    public synchronized List<MusicTrack> sortedTracks(String search) {
        ensureLoaded();
        String needle = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        return tracks.stream()
                .filter(track -> needle.isEmpty()
                        || safe(track.title).toLowerCase(Locale.ROOT).contains(needle)
                        || safe(track.artist).toLowerCase(Locale.ROOT).contains(needle)
                        || safe(track.originalSource).toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator
                        .comparing((MusicTrack track) -> safe(track.title), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing((MusicTrack track) -> safe(track.artist), String.CASE_INSENSITIVE_ORDER)
                        .thenComparing((MusicTrack track) -> safe(track.id), String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public synchronized Optional<MusicTrack> find(String id) {
        ensureLoaded();
        return tracks.stream().filter(track -> safe(track.id).equals(id)).findFirst();
    }

    public synchronized Optional<MusicTrack> firstPlayable() {
        ensureLoaded();
        return sortedTracks("").stream().filter(MusicTrack::isPlayable).filter(MusicTrack::isOgg).findFirst();
    }

    public synchronized Optional<MusicTrack> nextAfter(MusicTrack current, boolean backwards) {
        ensureLoaded();
        List<MusicTrack> playable = sortedTracks("").stream()
                .filter(MusicTrack::isPlayable)
                .filter(MusicTrack::isOgg)
                .toList();
        if (playable.isEmpty()) {
            return Optional.empty();
        }
        if (current == null || current.id == null || current.id.isBlank()) {
            return Optional.of(playable.get(0));
        }
        int index = -1;
        for (int i = 0; i < playable.size(); i++) {
            if (current.id.equals(playable.get(i).id)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return Optional.of(playable.get(0));
        }
        int next = backwards
                ? (index - 1 + playable.size()) % playable.size()
                : (index + 1) % playable.size();
        return Optional.of(playable.get(next));
    }

    public synchronized ImportResult importFile(Path source) {
        ensureLoaded();
        try {
            if (source == null || !Files.isRegularFile(source)) {
                return fail("screen.mydrugs.music.missing_file");
            }
            String ext = extension(source);
            if (ext.isEmpty()) {
                return fail("screen.mydrugs.music.unsupported");
            }

            Files.createDirectories(MusicLibraryStorage.tracksDir());
            String sourceHash = sha256Hex(source);
            String id = sourceHash.substring(0, 32);

            Path target;
            String originalSource = source.toString();
            if ("ogg".equals(ext)) {
                target = MusicLibraryStorage.tracksDir().resolve(id + ".ogg");
                if (!Files.exists(target)) {
                    Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
                }
                // (duration filled in below)
            } else if (AudioConverter.needsConversion(ext)) {
                if (!AudioConverter.isFfmpegAvailable()) {
                    return fail("screen.mydrugs.music.ffmpeg_missing");
                }
                target = MusicLibraryStorage.tracksDir().resolve(id + ".ogg");
                if (!Files.exists(target)) {
                    Path tmp = MusicLibraryStorage.cacheDir().resolve(id + ".converting.ogg");
                    lastStatus = Component.translatable("screen.mydrugs.music.converting");
                    try {
                        AudioConverter.convertToOgg(source, tmp, 5);
                        Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException ex) {
                        try { Files.deleteIfExists(tmp); } catch (IOException ignored) {}
                        return fail("screen.mydrugs.music.conversion_failed");
                    }
                }
            } else {
                return fail("screen.mydrugs.music.unsupported");
            }

            Optional<MusicTrack> duplicate = find(id);
            if (duplicate.isPresent()) {
                return ok("screen.mydrugs.music.duplicate", duplicate.get());
            }

            MusicTrack track = new MusicTrack();
            track.id = id;
            track.title = filenameTitle(source);
            track.artist = "";
            track.durationMs = OggDuration.readMs(target);
            track.sourceType = MusicTrack.SourceType.LOCAL_FILE;
            track.localPath = target.toString();
            track.originalSource = originalSource;
            track.addedAt = Instant.now().toEpochMilli();
            tracks.add(track);
            save();
            return ok("ogg".equals(ext) ? "screen.mydrugs.music.done" : "screen.mydrugs.music.converted", track);
        } catch (IOException | RuntimeException ex) {
            return fail("screen.mydrugs.music.failed");
        }
    }

    public synchronized ImportResult importFolder(Path folder) {
        ensureLoaded();
        if (folder == null || !Files.isDirectory(folder)) {
            return fail("screen.mydrugs.music.missing_file");
        }
        int imported = 0;
        int failed = 0;
        try (var stream = Files.walk(folder, 1)) {
            for (Path path : stream.filter(Files::isRegularFile).toList()) {
                String ext = extension(path);
                if (!AudioConverter.isSupportedInput(ext)) {
                    continue;
                }
                ImportResult result = importFile(path);
                if (result.success()) {
                    imported++;
                } else {
                    failed++;
                }
            }
        } catch (IOException ex) {
            return fail("screen.mydrugs.music.failed");
        }
        if (imported > 0 && failed == 0) {
            return ok("screen.mydrugs.music.done", null);
        }
        if (imported > 0) {
            return ok("screen.mydrugs.music.done_with_errors", null);
        }
        return fail("screen.mydrugs.music.unsupported");
    }

    public synchronized Optional<MusicTrack> random(MusicTrack exclude) {
        ensureLoaded();
        List<MusicTrack> playable = sortedTracks("").stream()
                .filter(MusicTrack::isPlayable)
                .filter(MusicTrack::isOgg)
                .toList();
        if (playable.isEmpty()) {
            return Optional.empty();
        }
        if (playable.size() == 1) {
            return Optional.of(playable.get(0));
        }
        Random rng = ThreadLocalRandom.current();
        for (int attempt = 0; attempt < 8; attempt++) {
            MusicTrack candidate = playable.get(rng.nextInt(playable.size()));
            if (exclude == null || !candidate.id.equals(exclude.id)) {
                return Optional.of(candidate);
            }
        }
        return Optional.of(playable.get(rng.nextInt(playable.size())));
    }

    public synchronized ImportResult remove(String id) {
        ensureLoaded();
        Optional<MusicTrack> match = find(id);
        if (match.isEmpty()) {
            return fail("screen.mydrugs.music.failed");
        }
        MusicTrack track = match.get();
        tracks.removeIf(t -> t.id.equals(id));
        if (track.localPath != null && !track.localPath.isBlank()) {
            try {
                Files.deleteIfExists(Path.of(track.localPath));
            } catch (IOException ignored) {
            }
        }
        save();
        return ok("screen.mydrugs.music.removed", null);
    }

    public synchronized ImportResult renameTrack(String id, String title, String artist) {
        ensureLoaded();
        Optional<MusicTrack> match = find(id);
        if (match.isEmpty()) {
            return fail("screen.mydrugs.music.failed");
        }
        MusicTrack track = match.get();
        if (title != null && !title.isBlank()) {
            track.title = title.trim();
        }
        if (artist != null) {
            track.artist = artist.trim();
        }
        save();
        return ok("screen.mydrugs.music.done", track);
    }

    public synchronized ImportResult addDirectUrl(String rawUrl) {
        ensureLoaded();
        if (isMusicService(rawUrl)) {
            return addBookmark(rawUrl);
        }
        try {
            URI uri = URI.create(rawUrl.trim());
            String ext = extension(Path.of(uri.getPath()));
            if (!AudioConverter.isSupportedInput(ext)) {
                return fail("screen.mydrugs.music.unsupported");
            }
            if (AudioConverter.needsConversion(ext) && !AudioConverter.isFfmpegAvailable()) {
                return fail("screen.mydrugs.music.ffmpeg_missing");
            }

            Files.createDirectories(MusicLibraryStorage.tracksDir());
            Files.createDirectories(MusicLibraryStorage.cacheDir());

            String provisional = sha256Hex(rawUrl).substring(0, 32);
            Path downloaded = MusicLibraryStorage.cacheDir().resolve(provisional + "." + ext);
            URL url = uri.toURL();
            try (var in = url.openStream()) {
                Files.copy(in, downloaded, StandardCopyOption.REPLACE_EXISTING);
            }

            String hash = sha256Hex(downloaded);
            String id = hash.substring(0, 32);
            Path target = MusicLibraryStorage.tracksDir().resolve(id + ".ogg");

            if ("ogg".equals(ext)) {
                if (!Files.exists(target)) {
                    Files.move(downloaded, target, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    Files.deleteIfExists(downloaded);
                }
            } else {
                lastStatus = Component.translatable("screen.mydrugs.music.converting");
                try {
                    AudioConverter.convertToOgg(downloaded, target, 5);
                } catch (IOException ex) {
                    Files.deleteIfExists(downloaded);
                    return fail("screen.mydrugs.music.conversion_failed");
                }
                Files.deleteIfExists(downloaded);
            }

            MusicTrack track = new MusicTrack();
            track.id = id;
            track.title = filenameTitle(Path.of(uri.getPath()));
            track.sourceType = MusicTrack.SourceType.DIRECT_URL;
            track.localPath = target.toString();
            track.originalSource = rawUrl;
            track.durationMs = OggDuration.readMs(target);
            track.addedAt = Instant.now().toEpochMilli();
            tracks.removeIf(existing -> existing.id.equals(track.id));
            tracks.add(track);
            save();
            return ok("ogg".equals(ext) ? "screen.mydrugs.music.done" : "screen.mydrugs.music.converted", track);
        } catch (Exception ex) {
            return fail("screen.mydrugs.music.failed");
        }
    }

    public synchronized ImportResult addBookmark(String source) {
        ensureLoaded();
        if (source == null || source.isBlank()) {
            return fail("screen.mydrugs.music.failed");
        }
        MusicTrack track = new MusicTrack();
        track.id = sha256Hex(source).substring(0, 32);
        track.title = sourceTitle(source);
        track.sourceType = MusicTrack.SourceType.BOOKMARK;
        track.originalSource = source;
        track.addedAt = Instant.now().toEpochMilli();
        tracks.removeIf(existing -> existing.id.equals(track.id));
        tracks.add(track);
        save();
        return ok(isMusicService(source) ? "screen.mydrugs.music.bookmark_only" : "screen.mydrugs.music.done", track);
    }

    public synchronized void toggleLike(String id) {
        ensureLoaded();
        find(id).ifPresent(track -> {
            track.liked = !track.liked;
            save();
        });
    }

    public synchronized void markPlayed(MusicTrack track) {
        ensureLoaded();
        if (track == null) {
            return;
        }
        find(track.id).ifPresent(stored -> {
            stored.lastPlayedAt = Instant.now().toEpochMilli();
            stored.playCount++;
            save();
        });
    }

    public synchronized int version() {
        ensureLoaded();
        return version;
    }

    public synchronized Component lastStatus() {
        return lastStatus;
    }

    private synchronized void ensureLoaded() {
        if (loaded) {
            return;
        }
        loaded = true;
        try {
            Files.createDirectories(MusicLibraryStorage.tracksDir());
            Files.createDirectories(MusicLibraryStorage.cacheDir());
            Files.createDirectories(MusicLibraryStorage.artDir());
            Path path = MusicLibraryStorage.libraryJson();
            if (Files.isRegularFile(path)) {
                try (Reader reader = Files.newBufferedReader(path)) {
                    LibraryFile file = GSON.fromJson(reader, LibraryFile.class);
                    if (file != null && file.tracks != null) {
                        tracks.clear();
                        tracks.addAll(file.tracks);
                        version = file.version;
                    }
                }
            }
            backfillDurations();
            mergeBuiltins();
        } catch (IOException | RuntimeException ignored) {
            lastStatus = Component.translatable("screen.mydrugs.music.failed");
        }
    }

    /**
     * Force a re-scan of built-in MC music. Safe to call after resource reload.
     */
    public synchronized void refreshBuiltins() {
        ensureLoaded();
        mergeBuiltins();
    }

    private void backfillDurations() {
        List<MusicTrack> needs = new ArrayList<>();
        for (MusicTrack track : tracks) {
            if (track.durationMs > 0L) continue;
            if (track.sourceType == MusicTrack.SourceType.LOCAL_FILE
                    || track.sourceType == MusicTrack.SourceType.DIRECT_URL) {
                if (track.localPath != null && !track.localPath.isBlank()) {
                    needs.add(track);
                }
            }
        }
        if (needs.isEmpty()) {
            return;
        }
        Thread thread = new Thread(() -> {
            boolean dirty = false;
            for (MusicTrack track : needs) {
                long ms = OggDuration.readMs(Path.of(track.localPath));
                if (ms > 0L) {
                    synchronized (MusicLibrary.this) {
                        Optional<MusicTrack> stored = find(track.id);
                        if (stored.isPresent() && stored.get().durationMs <= 0L) {
                            stored.get().durationMs = ms;
                            dirty = true;
                        }
                    }
                }
            }
            if (dirty) {
                synchronized (MusicLibrary.this) {
                    save();
                }
            }
        }, "MyDrugs music duration backfill");
        thread.setDaemon(true);
        thread.start();
    }

    private void mergeBuiltins() {
        List<MusicTrack> builtins;
        try {
            builtins = BuiltinMusicProvider.discover();
        } catch (Throwable t) {
            return;
        }
        if (builtins.isEmpty()) {
            return;
        }
        boolean dirty = false;
        List<MusicTrack> needsDuration = new ArrayList<>();
        for (MusicTrack discovered : builtins) {
            Optional<MusicTrack> existing = find(discovered.id);
            if (existing.isEmpty()) {
                tracks.add(discovered);
                dirty = true;
                needsDuration.add(discovered);
            } else {
                MusicTrack stored = existing.get();
                if (stored.sourceType != MusicTrack.SourceType.BUILT_IN) {
                    continue;
                }
                if (!discovered.localPath.equals(stored.localPath)) {
                    stored.localPath = discovered.localPath;
                    dirty = true;
                }
                if (stored.title == null || stored.title.isBlank()) {
                    stored.title = discovered.title;
                    dirty = true;
                }
                if (stored.album == null || stored.album.isBlank()) {
                    stored.album = discovered.album;
                    dirty = true;
                }
                if (stored.durationMs <= 0L) {
                    needsDuration.add(stored);
                }
            }
        }
        if (dirty) {
            save();
        }
        if (!needsDuration.isEmpty()) {
            probeBuiltinDurationsAsync(needsDuration);
        }
    }

    private void probeBuiltinDurationsAsync(List<MusicTrack> targets) {
        Thread thread = new Thread(() -> {
            boolean anyUpdated = false;
            for (MusicTrack track : targets) {
                long ms = probeBuiltinDuration(track);
                if (ms > 0L) {
                    synchronized (MusicLibrary.this) {
                        Optional<MusicTrack> stored = find(track.id);
                        if (stored.isPresent() && stored.get().durationMs <= 0L) {
                            stored.get().durationMs = ms;
                            anyUpdated = true;
                        }
                    }
                }
            }
            if (anyUpdated) {
                synchronized (MusicLibrary.this) {
                    save();
                }
            }
        }, "MyDrugs builtin music duration probe");
        thread.setDaemon(true);
        thread.start();
    }

    private static long probeBuiltinDuration(MusicTrack track) {
        try {
            ResourceLocation loc = ResourceLocation.parse(track.localPath);
            try (java.io.InputStream stream = BuiltinMusicProvider.open(loc)) {
                if (stream == null) {
                    return 0L;
                }
                return OggDuration.readMs(stream);
            }
        } catch (Exception ex) {
            return 0L;
        }
    }

    private synchronized void save() {
        try {
            Files.createDirectories(MusicLibraryStorage.root());
            LibraryFile file = new LibraryFile();
            file.version = ++version;
            file.tracks = new ArrayList<>(tracks);
            try (Writer writer = Files.newBufferedWriter(MusicLibraryStorage.libraryJson())) {
                GSON.toJson(file, writer);
            }
        } catch (IOException ignored) {
            lastStatus = Component.translatable("screen.mydrugs.music.failed");
        }
    }

    private ImportResult ok(String key, MusicTrack track) {
        lastStatus = Component.translatable(key);
        return new ImportResult(true, lastStatus, track);
    }

    private ImportResult fail(String key) {
        lastStatus = Component.translatable(key);
        return new ImportResult(false, lastStatus, null);
    }

    private static String extension(Path path) {
        String name = path == null || path.getFileName() == null ? "" : path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static String filenameTitle(Path path) {
        String name = path.getFileName() == null ? path.toString() : path.getFileName().toString();
        int dot = name.lastIndexOf('.');
        return dot < 0 ? name : name.substring(0, dot);
    }

    private static String sourceTitle(String source) {
        try {
            URI uri = URI.create(source);
            String host = uri.getHost();
            return host == null || host.isBlank() ? source : host;
        } catch (RuntimeException ignored) {
            return source;
        }
    }

    private static boolean isMusicService(String source) {
        String lower = safe(source).toLowerCase(Locale.ROOT);
        return lower.contains("youtube.com")
                || lower.contains("youtu.be")
                || lower.contains("spotify.com")
                || lower.contains("deezer.com");
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String sha256Hex(Path path) throws IOException {
        try (InputStream in = Files.newInputStream(path)) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) >= 0) {
                digest.update(buffer, 0, read);
            }
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return toHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            builder.append(Character.forDigit((value >> 4) & 0xF, 16));
            builder.append(Character.forDigit(value & 0xF, 16));
        }
        return builder.toString();
    }

    private static final class LibraryFile {
        int version;
        List<MusicTrack> tracks = new ArrayList<>();
    }

    public record ImportResult(boolean success, Component message, MusicTrack track) {
    }
}
