package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.network.ServerMusicRequestTrackPayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackChunkPayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackCompletePayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackInfoPayload;
import org.mydrugs.mydrugs.network.ServerMusicTrackUnavailablePayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadChunkPayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadFinishPayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadResultPayload;
import org.mydrugs.mydrugs.network.ServerMusicUploadStartPayload;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class SharedMusicTransferClient {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Queue<Runnable> OUTBOUND = new ConcurrentLinkedQueue<>();
    private static final Map<String, DownloadSession> DOWNLOADS = new ConcurrentHashMap<>();
    private static final ExecutorService IO = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "MyDrugs shared music client IO");
        thread.setDaemon(true);
        return thread;
    });

    private SharedMusicTransferClient() {
    }

    public static void upload(MusicTrack track) {
        if (track == null || track.localPath == null || track.localPath.isBlank()) return;
        String uploadId = UUID.randomUUID().toString();
        Path file;
        try {
            file = Path.of(track.localPath);
        } catch (RuntimeException ex) {
            notifyUploadFailure(uploadId, "screen.mydrugs.disc_scriber.missing_local_file");
            return;
        }
        IO.execute(() -> {
            try {
                long size = Files.size(file);
                if (size <= 0 || size > Integer.MAX_VALUE) {
                    notifyUploadFailure(uploadId, "screen.mydrugs.disc_scriber.file_too_large");
                    return;
                }
                String hash = sha256(file);
                int totalChunks = (int) ((size + ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE - 1)
                        / ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE);
                OUTBOUND.add(() -> ClientPacketDistributor.sendToServer(new ServerMusicUploadStartPayload(
                        uploadId, track.id, safe(track.title), safe(track.artist),
                        (int) Math.min(Integer.MAX_VALUE, track.durationMs), (int) size, hash
                )));
                try (var input = Files.newInputStream(file)) {
                    byte[] buffer = new byte[ServerMusicUploadChunkPayload.MAX_CHUNK_SIZE];
                    int read;
                    int index = 0;
                    while ((read = input.read(buffer)) >= 0) {
                        if (read == 0) continue;
                        byte[] chunk = Arrays.copyOf(buffer, read);
                        int chunkIndex = index++;
                        OUTBOUND.add(() -> {
                            notifyUploadProgress(chunkIndex + 1, totalChunks);
                            ClientPacketDistributor.sendToServer(
                                    new ServerMusicUploadChunkPayload(uploadId, chunkIndex, chunk));
                        });
                    }
                }
                OUTBOUND.add(() -> ClientPacketDistributor.sendToServer(new ServerMusicUploadFinishPayload(uploadId)));
            } catch (Exception ex) {
                LOGGER.warn("Could not prepare shared music upload");
                notifyUploadFailure(uploadId, "screen.mydrugs.disc_scriber.server_upload_failed");
            }
        });
    }

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        for (int i = 0; i < 2; i++) {
            Runnable action = OUTBOUND.poll();
            if (action == null) break;
            action.run();
        }
    }

    public static void handleUploadResult(ServerMusicUploadResultPayload payload) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.screen instanceof DiscScriberScreen screen) {
                screen.handleUploadResult(payload);
            }
        });
    }

    public static void requestTrack(String serverTrackId, String audioHash) {
        ClientPacketDistributor.sendToServer(new ServerMusicRequestTrackPayload(serverTrackId, audioHash));
    }

    public static Path cachedTrack(String audioHash) {
        return MusicLibraryStorage.serverCacheDir().resolve(audioHash + ".ogg");
    }

    public static boolean hasVerifiedCache(String audioHash) {
        Path file = cachedTrack(audioHash);
        if (!Files.isRegularFile(file)) return false;
        try {
            return sha256(file).equalsIgnoreCase(audioHash);
        } catch (Exception ex) {
            return false;
        }
    }

    public static void handleInfo(ServerMusicTrackInfoPayload payload) {
        IO.execute(() -> {
            try {
                Files.createDirectories(MusicLibraryStorage.serverCacheDir());
                Path part = cachedTrack(payload.audioHash()).resolveSibling(payload.audioHash() + ".ogg.part");
                Files.deleteIfExists(part);
                DownloadSession session = new DownloadSession(payload.serverTrackId(), payload.audioHash(),
                        payload.fileSize(), payload.chunkCount(), part, Files.newOutputStream(part));
                DOWNLOADS.put(payload.audioHash(), session);
            } catch (IOException ex) {
                failDownload(payload.audioHash(), "message.mydrugs.music.shared.download_failed");
            }
        });
    }

    public static void handleChunk(ServerMusicTrackChunkPayload payload) {
        IO.execute(() -> {
            DownloadSession session = DOWNLOADS.get(payload.audioHash());
            if (session == null || payload.chunkIndex() != session.nextChunk
                    || session.written + payload.data().length > session.fileSize) {
                failDownload(payload.audioHash(), "message.mydrugs.music.shared.download_failed");
                return;
            }
            try {
                session.output.write(payload.data());
                session.nextChunk++;
                session.written += payload.data().length;
            } catch (IOException ex) {
                failDownload(payload.audioHash(), "message.mydrugs.music.shared.download_failed");
            }
        });
    }

    public static void handleComplete(ServerMusicTrackCompletePayload payload) {
        IO.execute(() -> {
            DownloadSession session = DOWNLOADS.remove(payload.audioHash());
            if (session == null) return;
            try {
                session.output.close();
                if (session.written != session.fileSize || session.nextChunk != session.chunkCount
                        || !sha256(session.part).equalsIgnoreCase(payload.audioHash())) {
                    Files.deleteIfExists(session.part);
                    notifyPlayer("message.mydrugs.music.shared.verify_failed");
                    return;
                }
                Path target = cachedTrack(payload.audioHash());
                Files.move(session.part, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                Minecraft.getInstance().execute(() ->
                        CustomDiscPlaybackController.onSharedTrackReady(payload.audioHash(), target));
            } catch (Exception ex) {
                failDownload(payload.audioHash(), "message.mydrugs.music.shared.download_failed");
            }
        });
    }

    public static void handleUnavailable(ServerMusicTrackUnavailablePayload payload) {
        IO.execute(() -> failDownload(payload.audioHash(), payload.messageKey()));
    }

    private static void failDownload(String hash, String messageKey) {
        DownloadSession session = DOWNLOADS.remove(hash);
        if (session != null) {
            try { session.output.close(); } catch (IOException ignored) {}
            try { Files.deleteIfExists(session.part); } catch (IOException ignored) {}
        }
        notifyPlayer(messageKey);
    }

    private static void notifyUploadFailure(String uploadId, String messageKey) {
        Minecraft.getInstance().execute(() -> handleUploadResult(
                new ServerMusicUploadResultPayload(uploadId, false, messageKey, "", "")));
    }

    private static void notifyUploadProgress(int current, int total) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen instanceof DiscScriberScreen screen) {
            screen.handleUploadProgress(current, total);
        }
    }

    private static void notifyPlayer(String messageKey) {
        Minecraft.getInstance().execute(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null) mc.player.displayClientMessage(Component.translatable(messageKey), true);
        });
    }

    private static String sha256(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (var input = Files.newInputStream(file)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) if (read > 0) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static final class DownloadSession {
        final String serverTrackId;
        final String audioHash;
        final int fileSize;
        final int chunkCount;
        final Path part;
        final OutputStream output;
        int nextChunk;
        int written;
        DownloadSession(String serverTrackId, String audioHash, int fileSize, int chunkCount,
                        Path part, OutputStream output) {
            this.serverTrackId = serverTrackId;
            this.audioHash = audioHash;
            this.fileSize = fileSize;
            this.chunkCount = chunkCount;
            this.part = part;
            this.output = output;
        }
    }
}
