package org.mydrugs.mydrugs.client.recovery.music;

import com.mojang.logging.LogUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.tinyfd.TinyFileDialogs;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thin wrapper around LWJGL's {@link TinyFileDialogs} that opens native OS pickers on a
 * background thread (calling them from the render thread blocks the game and can deadlock
 * on some window managers).
 */
public final class NativeFileDialog {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String[] AUDIO_FILTERS = {
            "*.ogg", "*.mp3", "*.m4a", "*.aac", "*.wav", "*.flac", "*.opus", "*.wma"
    };

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "MyDrugs native-file-dialog");
        thread.setDaemon(true);
        return thread;
    });

    private static final AtomicLong INFLIGHT = new AtomicLong();

    private NativeFileDialog() {
    }

    /**
     * Returns true if a dialog is currently open. Used by callers to debounce repeated clicks.
     */
    public static boolean isBusy() {
        return INFLIGHT.get() > 0L;
    }

    public static CompletableFuture<Path> openFile(String title, Path defaultPath, boolean multiSelect) {
        return submit(() -> {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                PointerBuffer filters = stack.mallocPointer(AUDIO_FILTERS.length);
                for (String pattern : AUDIO_FILTERS) {
                    filters.put(stack.UTF8(pattern));
                }
                filters.flip();
                String result = TinyFileDialogs.tinyfd_openFileDialog(
                        title,
                        defaultPath == null ? "" : defaultPath.toString(),
                        filters,
                        "Audio files (ogg, mp3, m4a, wav, flac, opus, wma)",
                        multiSelect
                );
                if (result == null || result.isBlank()) {
                    return null;
                }
                // If multiSelect, results are joined with '|' — return the first.
                int sep = result.indexOf('|');
                String first = sep >= 0 ? result.substring(0, sep) : result;
                return Path.of(first);
            }
        });
    }

    public static CompletableFuture<Path> selectFolder(String title, Path defaultPath) {
        return submit(() -> {
            String result = TinyFileDialogs.tinyfd_selectFolderDialog(
                    title,
                    defaultPath == null ? "" : defaultPath.toString()
            );
            if (result == null || result.isBlank()) {
                return null;
            }
            return Path.of(result);
        });
    }

    private static CompletableFuture<Path> submit(DialogTask task) {
        INFLIGHT.incrementAndGet();
        CompletableFuture<Path> future = new CompletableFuture<>();
        EXECUTOR.submit(() -> {
            try {
                future.complete(task.run());
            } catch (Throwable t) {
                LOGGER.warn("Native file dialog failed", t);
                future.complete(null);
            } finally {
                INFLIGHT.decrementAndGet();
            }
        });
        return future;
    }

    @FunctionalInterface
    private interface DialogTask {
        Path run() throws Exception;
    }

    /**
     * Cheap UTF-8 byte length helper for callers that build filter strings dynamically.
     */
    public static int utf8Length(String value) {
        return value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
    }
}
