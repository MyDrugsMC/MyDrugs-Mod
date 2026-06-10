package org.mydrugs.mydrugs.client.recovery.music;

import net.minecraft.network.chat.Component;

public final class TrackImportJob {
    public enum Stage {
        IDLE,
        CHECKING_TOOL,
        DOWNLOADING,
        CONVERTING,
        IMPORTING,
        DONE,
        FAILED
    }

    private volatile Stage stage = Stage.IDLE;
    private volatile boolean done;
    private volatile boolean success;
    private volatile Component message = Component.translatable("screen.mydrugs.music.importing");
    private volatile int current;
    private volatile int total;
    private volatile String technicalReason = "";

    public boolean done() {
        return done;
    }

    public Component message() {
        return message;
    }

    public Stage stage() {
        return stage;
    }

    public boolean success() {
        return success;
    }

    public int current() {
        return current;
    }

    public int total() {
        return total;
    }

    public String technicalReason() {
        return technicalReason;
    }

    void update(Stage stage, Component message) {
        this.stage = stage;
        this.message = message;
    }

    void progress(Stage stage, int current, int total, Component message) {
        this.stage = stage;
        this.current = Math.max(0, current);
        this.total = Math.max(0, total);
        this.message = message;
    }

    void complete(boolean success, Component message) {
        this.message = message;
        this.success = success;
        this.stage = success ? Stage.DONE : Stage.FAILED;
        this.done = true;
    }

    void fail(Component message, String technicalReason) {
        this.technicalReason = technicalReason == null ? "" : technicalReason;
        complete(false, message);
    }
}
