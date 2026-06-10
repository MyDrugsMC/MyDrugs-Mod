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
        EXECUTOR.submit(() -> {
            job.update(TrackImportJob.Stage.IMPORTING, Component.translatable("screen.mydrugs.music.importing"));
            MusicLibrary.ImportResult result = MusicLibrary.get().importFile(path);
            job.complete(result.success(), result.message());
        });
        return job;
    }

    public static TrackImportJob importFolder(Path path) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> {
            job.update(TrackImportJob.Stage.IMPORTING, Component.translatable("screen.mydrugs.music.importing"));
            MusicLibrary.ImportResult result = MusicLibrary.get().importFolder(path);
            job.complete(result.success(), result.message());
        });
        return job;
    }

    public static TrackImportJob importYoutubeAudio(String url) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> {
            job.update(TrackImportJob.Stage.CHECKING_TOOL,
                    Component.translatable("screen.mydrugs.music.import_stage.checking_tool"));
            YtDownloader.DownloadResult download = YtDownloader.download(url);
            if (!download.success()) {
                job.fail(Component.translatable(download.messageKey()), download.reason().name());
                return;
            }
            job.update(TrackImportJob.Stage.IMPORTING,
                    Component.translatable("screen.mydrugs.music.import_stage.importing"));
            MusicLibrary.ImportResult result = MusicLibrary.get().importFile(download.path());
            job.complete(result.success(), result.message());
        });
        return job;
    }

    public static TrackImportJob addDirectUrl(String url) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> {
            job.update(TrackImportJob.Stage.DOWNLOADING,
                    Component.translatable("screen.mydrugs.music.import_stage.downloading"));
            MusicLibrary.ImportResult result = MusicLibrary.get().addDirectUrl(url);
            job.complete(result.success(), result.message());
        });
        return job;
    }

    public static TrackImportJob addBookmark(String url) {
        TrackImportJob job = new TrackImportJob();
        EXECUTOR.submit(() -> {
            job.update(TrackImportJob.Stage.IMPORTING,
                    Component.translatable("screen.mydrugs.music.import_stage.importing"));
            MusicLibrary.ImportResult result = MusicLibrary.get().addBookmark(url);
            job.complete(result.success(), result.message());
        });
        return job;
    }
}
