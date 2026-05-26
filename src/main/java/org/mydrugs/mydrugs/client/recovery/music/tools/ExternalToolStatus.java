package org.mydrugs.mydrugs.client.recovery.music.tools;

/**
 * Resolution state of an optional external tool. Drives the disclaimer screen tool rows and
 * whether a feature that depends on the tool is enabled.
 */
public enum ExternalToolStatus {
    /** Not configured, not on PATH, not in common locations, and not downloaded. */
    NOT_FOUND("message.mydrugs.external_tool.not_found", false),
    /** Found via the system PATH or a common OS install location. */
    FOUND_ON_SYSTEM("message.mydrugs.external_tool.found_system", true),
    /** Resolved from a path the player explicitly configured. */
    USER_CONFIGURED("message.mydrugs.external_tool.user_configured", true),
    /** Downloaded through the mod and verified against the pinned SHA-256. */
    DOWNLOADED_VERIFIED("message.mydrugs.external_tool.downloaded_verified", true),
    /** Executable exists, but lacks the codec capability required by the mod. */
    MISSING_CODEC("message.mydrugs.external_tool.missing_codec", false),
    /** A downloaded copy failed SHA-256 verification and was quarantined. */
    VERIFICATION_FAILED("message.mydrugs.external_tool.verification_failed", false),
    /** The player disabled this tool, so dependent features stay off. */
    DISABLED("message.mydrugs.external_tool.disabled", false);

    private final String messageKey;
    private final boolean usable;

    ExternalToolStatus(String messageKey, boolean usable) {
        this.messageKey = messageKey;
        this.usable = usable;
    }

    public String messageKey() {
        return messageKey;
    }

    /** True when the resolved tool can actually be executed. */
    public boolean usable() {
        return usable;
    }
}
