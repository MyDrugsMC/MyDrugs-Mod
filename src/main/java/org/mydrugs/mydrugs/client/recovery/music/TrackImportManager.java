package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class TrackImportManager {
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "MyDrugs music import");
        thread.setDaemon(true);
        return thread;
    });

    private TrackImportManager() {
    }

    public static TrackImportJob importFile(Path path) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> job.complete(MusicLibrary.get().importFile(path).message()));
        return job;
    }

    public static TrackImportJob importFolder(Path path) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> job.complete(MusicLibrary.get().importFolder(path).message()));
        return job;
    }

    public static TrackImportJob importYoutubeAudio(String url) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> {
            Path path = YtDownloader.download(url);
            if (path == null) job.complete(Component.translatable("screen.mydrugs.music.failed_yt_download"));
            job.complete(MusicLibrary.get().importFile(path).message());
        });
        return job;
    }

    public static TrackImportJob addDirectUrl(String url) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> job.complete(MusicLibrary.get().addDirectUrl(url).message()));
        return job;
    }

    public static TrackImportJob addBookmark(String url) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> job.complete(MusicLibrary.get().addBookmark(url).message()));
        return job;
    }
}
