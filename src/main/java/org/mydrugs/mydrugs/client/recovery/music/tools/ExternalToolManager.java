package org.mydrugs.mydrugs.client.recovery.music.tools;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Resolves the optional external tools and orchestrates their explicit, verified download.
 *
 * <p>Resolution order (first match wins), per the project's hard constraints:
 * <ol>
 *   <li>a path the player explicitly configured;</li>
 *   <li>the system {@code PATH};</li>
 *   <li>common OS install locations;</li>
 *   <li>a mod-managed copy, which can only exist after an explicit SHA-256-verified download;</li>
 *   <li>otherwise the tool is unavailable and dependent features stay disabled.</li>
 * </ol>
 *
 * <p>System/user tools are always preferred over mod-managed downloads. Nothing here downloads or
 * executes a binary on its own; {@link #download} runs only from an explicit player action.
 */
public final class ExternalToolManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final ExternalToolManager INSTANCE = new ExternalToolManager();

    private final Map<ExternalTool, Resolution> cache = new EnumMap<>(ExternalTool.class);

    private ExternalToolManager() {
    }

    public static ExternalToolManager get() {
        return INSTANCE;
    }

    /** Resolved tool: a status and, when usable, the executable path. */
    public record Resolution(ExternalTool tool, ExternalToolStatus status, Path path) {
        public boolean usable() {
            return status.usable() && path != null;
        }
    }

    /** Mod-owned directory the downloaded tools live in. Never added to PATH. */
    public Path toolsDir() {
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("mydrugs").resolve("tools")
                .resolve(ExternalToolPlatform.current().id());
    }

    /** Drops cached resolutions; call after a download or a configuration change. */
    public synchronized void invalidate() {
        cache.clear();
    }

    /** Resolves a tool, using a cached result when available. */
    public synchronized Resolution resolve(ExternalTool tool) {
        return cache.computeIfAbsent(tool, this::resolveUncached);
    }

    private Resolution resolveUncached(ExternalTool tool) {
        MyDrugsClientConfig config = MyDrugsClientConfig.get();
        if (config.isToolDisabled(tool)) {
            return new Resolution(tool, ExternalToolStatus.DISABLED, null);
        }

        Resolution firstUnusableCandidate = null;

        // 1. Explicit user-configured path.
        String configured = config.getToolPath(tool);
        if (!configured.isBlank()) {
            Path path = Path.of(configured);
            if (isUsableFile(path)) {
                Resolution resolution = validateCandidate(tool, ExternalToolStatus.USER_CONFIGURED, path);
                if (resolution.usable()) {
                    return resolution;
                }
                firstUnusableCandidate = resolution;
            }
        }

        boolean windows = ExternalToolPlatform.isCurrentOsWindows();
        String exe = tool.executableName(windows);

        // 2. System PATH.
        Path onPath = findOnPath(exe);
        if (onPath != null) {
            Resolution resolution = validateCandidate(tool, ExternalToolStatus.FOUND_ON_SYSTEM, onPath);
            if (resolution.usable()) {
                return resolution;
            }
            firstUnusableCandidate = firstUnusableCandidate == null ? resolution : firstUnusableCandidate;
        }

        // 3. Common OS install locations.
        Path common = findInCommonLocations(exe, windows);
        if (common != null) {
            Resolution resolution = validateCandidate(tool, ExternalToolStatus.FOUND_ON_SYSTEM, common);
            if (resolution.usable()) {
                return resolution;
            }
            firstUnusableCandidate = firstUnusableCandidate == null ? resolution : firstUnusableCandidate;
        }

        // 4. Mod-managed copy (only exists after an explicit verified download).
        Path managed = toolsDir().resolve(exe);
        if (isUsableFile(managed)) {
            Resolution resolution = validateCandidate(tool, ExternalToolStatus.DOWNLOADED_VERIFIED, managed);
            if (resolution.usable()) {
                return resolution;
            }
            firstUnusableCandidate = firstUnusableCandidate == null ? resolution : firstUnusableCandidate;
        }

        // 5. Unavailable: the dependent feature stays disabled.
        if (firstUnusableCandidate != null) {
            return firstUnusableCandidate;
        }
        return new Resolution(tool, ExternalToolStatus.NOT_FOUND, null);
    }

    private static Resolution validateCandidate(ExternalTool tool, ExternalToolStatus status, Path path) {
        if (tool == ExternalTool.FFMPEG && !supportsLibvorbis(path)) {
            return new Resolution(tool, ExternalToolStatus.MISSING_CODEC, path);
        }
        return new Resolution(tool, status, path);
    }

    private static boolean isUsableFile(Path path) {
        return path != null && Files.isRegularFile(path) && Files.isExecutable(path);
    }

    private static Path findOnPath(String executable) {
        String pathEnv = System.getenv("PATH");
        if (pathEnv == null || pathEnv.isBlank()) {
            return null;
        }
        for (String dir : pathEnv.split(File.pathSeparator)) {
            if (dir.isBlank()) {
                continue;
            }
            try {
                Path candidate = Path.of(dir.trim()).resolve(executable);
                if (isUsableFile(candidate)) {
                    return candidate;
                }
            } catch (Exception ignored) {
                // Skip malformed PATH segments.
            }
        }
        return null;
    }

    private static Path findInCommonLocations(String executable, boolean windows) {
        List<Path> roots = new ArrayList<>();
        if (windows) {
            roots.add(Path.of("C:\\ffmpeg\\bin"));
            roots.add(Path.of("C:\\Program Files\\ffmpeg\\bin"));
            roots.add(Path.of("C:\\Program Files\\yt-dlp"));
        } else {
            roots.add(Path.of("/opt/homebrew/bin"));
            roots.add(Path.of("/usr/local/bin"));
            roots.add(Path.of("/usr/bin"));
            roots.add(Path.of("/snap/bin"));
            String home = System.getProperty("user.home");
            if (home != null && !home.isBlank()) {
                roots.add(Path.of(home, ".local", "bin"));
            }
        }
        for (Path root : roots) {
            Path candidate = root.resolve(executable);
            if (isUsableFile(candidate)) {
                return candidate;
            }
        }
        return null;
    }

    private static boolean supportsLibvorbis(Path executable) {
        Process process = null;
        try {
            ProcessBuilder builder = new ProcessBuilder(
                    executable.toAbsolutePath().toString(),
                    "-hide_banner",
                    "-v",
                    "error",
                    "-h",
                    "encoder=libvorbis"
            );
            builder.redirectErrorStream(true);
            process = builder.start();
            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            String output = new String(process.getInputStream().readAllBytes()).toLowerCase(Locale.ROOT);
            return process.exitValue() == 0 && output.contains("libvorbis");
        } catch (IOException e) {
            LOGGER.warn("Could not probe ffmpeg Vorbis support at {}: {}", executable, e.getMessage());
            return false;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * Whether an in-GUI download can be offered for this tool on the current platform: a pinned,
     * SHA-256-verified manifest entry must exist.
     */
    public boolean canDownload(ExternalTool tool) {
        return ExternalToolManifest.get()
                .isDownloadPinned(tool, ExternalToolPlatform.current());
    }

    /**
     * Explicitly downloads and verifies a tool. Must only be called from a player action.
     * Network/verification failures are non-fatal and reported via the returned outcome.
     */
    public ExternalToolDownloader.Outcome download(ExternalTool tool,
                                                   ExternalToolDownloader.ProgressListener listener) {
        ExternalToolPlatform platform = ExternalToolPlatform.current();
        ExternalToolManifest.Entry entry = ExternalToolManifest.get().downloadEntry(tool, platform);
        if (entry == null) {
            return new ExternalToolDownloader.Outcome(ExternalToolStatus.DISABLED,
                    "message.mydrugs.external_tool.download_unavailable");
        }

        Path target = toolsDir().resolve(tool.executableName(platform.windows()));
        ExternalToolDownloader.Outcome outcome =
                ExternalToolDownloader.download(entry, tool, target, toolsDir(), listener);
        invalidate();
        LOGGER.info("Download of {} finished with status {}", tool.id(), outcome.status());
        return outcome;
    }
}
