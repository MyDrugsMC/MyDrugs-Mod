package org.mydrugs.mydrugs.recovery.music;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.network.PayloadRateLimiter;
import org.mydrugs.mydrugs.network.ServerMusicRequestTrackPayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackChunkPayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackCompletePayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackInfoPayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackUnavailablePayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadChunkPayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadFinishPayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadResultPayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadStartPayload;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class ServerMusicLibrary {
    public static final boolean SHARED_UPLOAD_ENABLED = true;
    public static final int MAX_FILE_SIZE = 25 * 1024 * 1024;
    public static final int MAX_DURATION_MS = 20 * 60 * 1000;
    private static final long DOWNLOAD_AUTH_TTL_MS = 2 * 60_000L;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(r -> daemon(r, "MyDrugs server music IO"));
    private static final ScheduledExecutorService DOWNLOADS =
            Executors.newSingleThreadScheduledExecutor(r -> daemon(r, "MyDrugs server music downloads"));
    private static final Map<UUID, UploadSession> UPLOADS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<String, Long>> DOWNLOAD_AUTH = new ConcurrentHashMap<>();

    private ServerMusicLibrary() {
    }

    public static void handleUploadStart(Player rawPlayer, ServerMusicUploadStartPayload payload) {
        if (!(rawPlayer instanceof ServerPlayer player)) return;
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.SERVER_MUSIC_UPLOAD_START)) {
            uploadResult(player, payload.uploadId(), false, "screen.mydrugs.disc_scriber.failed.rate_limited", "", "");
            return;
        }
        if (!SHARED_UPLOAD_ENABLED) {
            uploadResult(player, payload.uploadId(), false, "screen.mydrugs.disc_scriber.upload_disabled", "", "");
            return;
        }
        if (!validId(payload.uploadId(), 36) || !validHash(payload.sha256())
                || payload.fileSize() <= 0 || payload.fileSize() > MAX_FILE_SIZE
                || payload.durationMs() < 0 || payload.durationMs() > MAX_DURATION_MS
                || UPLOADS.containsKey(player.getUUID())) {
            uploadResult(player, payload.uploadId(), false, "screen.mydrugs.disc_scriber.server_upload_failed", "", "");
            return;
        }
        Path temp = uploadsDir(player).resolve(payload.uploadId() + ".part");
        UploadSession session = new UploadSession(player.getUUID(), payload, temp);
        UPLOADS.put(player.getUUID(), session);
        IO.execute(() -> {
            try {
                Files.createDirectories(temp.getParent());
                Files.deleteIfExists(temp);
                session.output = Files.newOutputStream(temp);
            } catch (IOException ex) {
                failSession(player, session, "screen.mydrugs.disc_scriber.server_upload_failed");
            }
        });
    }

    public static void handleUploadChunk(Player rawPlayer, ServerMusicUploadChunkPayload payload) {
        if (!(rawPlayer instanceof ServerPlayer player)) return;
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.SERVER_MUSIC_UPLOAD_CHUNK)) return;
        UploadSession session = UPLOADS.get(player.getUUID());
        if (session == null || !session.start.uploadId().equals(payload.uploadId())
                || payload.chunkIndex() != session.nextChunk || payload.data().length == 0
                || session.received + payload.data().length > session.start.fileSize()) {
            if (session != null) failSession(player, session, "screen.mydrugs.disc_scriber.upload_interrupted");
            return;
        }
        session.nextChunk++;
        session.received += payload.data().length;
        byte[] copy = payload.data().clone();
        IO.execute(() -> {
            try {
                if (session.output == null) throw new IOException("upload not initialized");
                session.output.write(copy);
            } catch (IOException ex) {
                failSession(player, session, "screen.mydrugs.disc_scriber.upload_interrupted");
            }
        });
    }

    public static void handleUploadFinish(Player rawPlayer, ServerMusicUploadFinishPayload payload) {
        if (!(rawPlayer instanceof ServerPlayer player)) return;
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.SERVER_MUSIC_UPLOAD_FINISH)) return;
        UploadSession session = UPLOADS.get(player.getUUID());
        if (session == null || !session.start.uploadId().equals(payload.uploadId())) {
            uploadResult(player, payload.uploadId(), false, "screen.mydrugs.disc_scriber.upload_interrupted", "", "");
            return;
        }
        IO.execute(() -> finishUpload(player, session));
    }

    private static void finishUpload(ServerPlayer player, UploadSession session) {
        try {
            close(session);
            if (session.received != session.start.fileSize() || Files.size(session.temp) != session.start.fileSize()) {
                throw new IOException("size mismatch");
            }
            String actualHash = sha256(session.temp);
            if (!actualHash.equalsIgnoreCase(session.start.sha256())
                    || !OggMetadataReader.isVorbis(session.temp)) {
                throw new IOException("verification failed");
            }
            long duration = OggMetadataReader.readDurationMs(session.temp);
            if (duration <= 0L || duration > MAX_DURATION_MS) {
                throw new IOException("duration invalid");
            }
            Path target = tracksDir(player).resolve(actualHash + ".ogg");
            Files.createDirectories(target.getParent());
            Files.move(session.temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            String serverTrackId = actualHash;
            appendMetadata(player, new TrackMetadata(
                    serverTrackId, actualHash, clean(session.start.title(), 96), clean(session.start.artist(), 96),
                    (int) duration, session.start.fileSize(), player.getUUID().toString(),
                    player.getGameProfile().name(), Instant.now().toEpochMilli()
            ));
            UPLOADS.remove(player.getUUID(), session);
            uploadResult(player, session.start.uploadId(), true,
                    "screen.mydrugs.disc_scriber.server_track_ready", serverTrackId, actualHash);
        } catch (Exception ex) {
            MyDrugs.getLOGGER().warn("Server music upload verification failed for player {}", player.getUUID());
            failSession(player, session, "screen.mydrugs.disc_scriber.server_upload_failed");
        }
    }

    public static boolean hasTrack(ServerPlayer player, String serverTrackId, String audioHash) {
        return validHash(audioHash) && audioHash.equalsIgnoreCase(serverTrackId)
                && Files.isRegularFile(tracksDir(player).resolve(audioHash + ".ogg"));
    }

    public static void authorizeDownload(ServerPlayer player, String audioHash) {
        if (!validHash(audioHash)) return;
        DOWNLOAD_AUTH.computeIfAbsent(player.getUUID(), ignored -> new ConcurrentHashMap<>())
                .put(audioHash, System.currentTimeMillis() + DOWNLOAD_AUTH_TTL_MS);
    }

    public static void handleTrackRequest(Player rawPlayer, ServerMusicRequestTrackPayload payload) {
        if (!(rawPlayer instanceof ServerPlayer player)) return;
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.SERVER_MUSIC_DOWNLOAD_REQUEST)
                || !isAuthorized(player, payload.audioHash())
                || !hasTrack(player, payload.serverTrackId(), payload.audioHash())) {
            PacketDistributor.sendToPlayer(player, new ServerMusicTrackUnavailablePayload(
                    payload.serverTrackId(), payload.audioHash(), "message.mydrugs.music.shared.unavailable"));
            return;
        }
        Path file = tracksDir(player).resolve(payload.audioHash() + ".ogg");
        IO.execute(() -> {
            try {
                byte[] bytes = Files.readAllBytes(file);
                int chunks = (bytes.length + ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE - 1)
                        / ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE;
                player.level().getServer().execute(() -> PacketDistributor.sendToPlayer(player,
                        new ServerMusicTrackInfoPayload(payload.serverTrackId(), payload.audioHash(), bytes.length, chunks)));
                for (int i = 0; i < chunks; i++) {
                    int index = i;
                    int from = i * ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE;
                    int to = Math.min(bytes.length, from + ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE);
                    byte[] chunk = java.util.Arrays.copyOfRange(bytes, from, to);
                    DOWNLOADS.schedule(() -> player.level().getServer().execute(() -> PacketDistributor.sendToPlayer(player,
                            new ServerMusicTrackChunkPayload(payload.audioHash(), index, chunk))), i * 8L, TimeUnit.MILLISECONDS);
                }
                DOWNLOADS.schedule(() -> player.level().getServer().execute(() -> PacketDistributor.sendToPlayer(player,
                        new ServerMusicTrackCompletePayload(payload.serverTrackId(), payload.audioHash()))),
                        chunks * 8L + 10L, TimeUnit.MILLISECONDS);
            } catch (IOException ex) {
                player.level().getServer().execute(() -> PacketDistributor.sendToPlayer(player,
                        new ServerMusicTrackUnavailablePayload(payload.serverTrackId(), payload.audioHash(),
                                "message.mydrugs.music.shared.download_failed")));
            }
        });
    }

    private static boolean isAuthorized(ServerPlayer player, String hash) {
        Long expiry = DOWNLOAD_AUTH.getOrDefault(player.getUUID(), Map.of()).get(hash);
        return expiry != null && expiry >= System.currentTimeMillis();
    }

    private static Path root(ServerPlayer player) {
        return player.level().getServer().getWorldPath(LevelResource.ROOT).resolve("mydrugs_server_music");
    }
    private static Path tracksDir(ServerPlayer player) { return root(player).resolve("tracks"); }
    private static Path uploadsDir(ServerPlayer player) { return root(player).resolve("uploads"); }
    private static Path metadataFile(ServerPlayer player) { return root(player).resolve("library.json"); }

    private static void appendMetadata(ServerPlayer player, TrackMetadata metadata) throws IOException {
        Path file = metadataFile(player);
        Files.createDirectories(file.getParent());
        List<TrackMetadata> entries = new ArrayList<>();
        if (Files.isRegularFile(file)) {
            TrackMetadata[] existing = GSON.fromJson(Files.readString(file, StandardCharsets.UTF_8), TrackMetadata[].class);
            if (existing != null) entries.addAll(List.of(existing));
        }
        entries.removeIf(entry -> entry.audioHash.equals(metadata.audioHash));
        entries.add(metadata);
        Files.writeString(file, GSON.toJson(entries), StandardCharsets.UTF_8);
    }

    private static void failSession(ServerPlayer player, UploadSession session, String key) {
        UPLOADS.remove(player.getUUID(), session);
        IO.execute(() -> {
            close(session);
            try { Files.deleteIfExists(session.temp); } catch (IOException ignored) {}
        });
        uploadResult(player, session.start.uploadId(), false, key, "", "");
    }

    private static void uploadResult(ServerPlayer player, String uploadId, boolean success,
                                     String key, String serverTrackId, String hash) {
        player.level().getServer().execute(() -> PacketDistributor.sendToPlayer(player,
                new ServerMusicUploadResultPayload(uploadId, success, key, serverTrackId, hash)));
    }

    private static void close(UploadSession session) {
        try {
            if (session.output != null) session.output.close();
        } catch (IOException ignored) {
        }
        session.output = null;
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) digest.update(buffer, 0, read);
            }
        }
        return java.util.HexFormat.of().formatHex(digest.digest());
    }

    private static boolean validHash(String value) {
        return value != null && value.matches("[0-9a-fA-F]{64}");
    }
    private static boolean validId(String value, int max) {
        return value != null && !value.isBlank() && value.length() <= max
                && value.matches("[a-zA-Z0-9_-]+");
    }
    private static String clean(String value, int max) {
        String result = value == null ? "" : value.trim();
        return result.length() <= max ? result : result.substring(0, max);
    }
    private static Thread daemon(Runnable runnable, String name) {
        Thread thread = new Thread(runnable, name);
        thread.setDaemon(true);
        return thread;
    }

    private static final class UploadSession {
        final UUID playerId;
        final ServerMusicUploadStartPayload start;
        final Path temp;
        volatile OutputStream output;
        volatile int nextChunk;
        volatile int received;
        UploadSession(UUID playerId, ServerMusicUploadStartPayload start, Path temp) {
            this.playerId = playerId;
            this.start = start;
            this.temp = temp;
        }
    }

    private record TrackMetadata(
            String serverTrackId, String audioHash, String title, String artist, int durationMs,
            int fileSize, String uploaderUuid, String uploaderName, long createdAt
    ) {
    }
}
