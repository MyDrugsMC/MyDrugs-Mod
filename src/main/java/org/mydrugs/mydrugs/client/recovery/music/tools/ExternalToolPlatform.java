package org.mydrugs.mydrugs.client.recovery.music.tools;

import java.util.Locale;

/**
 * OS/architecture combinations for which the pinned manifest can carry a download.
 *
 * <p>Any platform not represented here (for example Linux ARM64 or macOS x64) intentionally has
 * no in-GUI download: those players install the tools themselves and point the mod at them.
 */
public enum ExternalToolPlatform {
    WINDOWS_X64("windows-x64", true),
    LINUX_X64("linux-x64", false),
    MACOS_ARM64("macos-arm64", false),
    /** Current OS/arch has no pinned download; only system/manual install is offered. */
    UNSUPPORTED("unsupported", false);

    private final String id;
    private final boolean windows;

    ExternalToolPlatform(String id, boolean windows) {
        this.id = id;
        this.windows = windows;
    }

    public String id() {
        return id;
    }

    public boolean windows() {
        return windows;
    }

    /** Manifest key suffix, e.g. {@code WINDOWS_X64}. */
    public String manifestKey() {
        return name();
    }

    public boolean supportsDownload() {
        return this != UNSUPPORTED;
    }

    /** Detects the current platform, or {@link #UNSUPPORTED} when no pinned download can apply. */
    public static ExternalToolPlatform current() {
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String arch = System.getProperty("os.arch", "").toLowerCase(Locale.ROOT);
        boolean x64 = arch.equals("x86_64") || arch.equals("amd64");
        boolean arm64 = arch.equals("aarch64") || arch.equals("arm64");

        if (os.contains("win") && x64) {
            return WINDOWS_X64;
        }
        if (os.contains("linux") && x64) {
            return LINUX_X64;
        }
        if ((os.contains("mac") || os.contains("darwin")) && arm64) {
            return MACOS_ARM64;
        }
        return UNSUPPORTED;
    }

    public static boolean isCurrentOsWindows() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    }
}
