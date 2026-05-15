package org.mydrugs.mydrugs.mutation;

public final class InfectionState {
    private boolean active;
    private int ticks;
    private float severity;
    private int lastMessageStage;

    public boolean active() {
        return this.active;
    }

    public int ticks() {
        return this.ticks;
    }

    public float severity() {
        return this.severity;
    }

    public int lastMessageStage() {
        return this.lastMessageStage;
    }

    public void start(float severity) {
        this.active = true;
        this.ticks = Math.max(this.ticks, 1);
        this.severity = Math.clamp(Math.max(this.severity, severity), 0.10F, 1.0F);
        this.lastMessageStage = 0;
    }

    public void cure(float strength) {
        if (!this.active) {
            return;
        }
        this.severity = Math.max(0.0F, this.severity - Math.max(0.0F, strength));
        if (this.severity <= 0.0F) {
            clear();
        }
    }

    public void clear() {
        this.active = false;
        this.ticks = 0;
        this.severity = 0.0F;
        this.lastMessageStage = 0;
    }

    public int stage() {
        if (!this.active) {
            return 0;
        }
        if (this.ticks < 20 * 60 * 2) {
            return 1;
        }
        if (this.ticks < 20 * 60 * 5) {
            return 2;
        }
        if (this.ticks < 20 * 60 * 9) {
            return 3;
        }
        return 4;
    }

    public boolean tick() {
        if (!this.active) {
            return false;
        }
        this.ticks++;
        this.severity = Math.clamp(this.severity + 0.00002F, 0.10F, 1.0F);
        return true;
    }

    public boolean markMessageShown(int stage) {
        if (stage <= this.lastMessageStage) {
            return false;
        }
        this.lastMessageStage = stage;
        return true;
    }

    public void load(boolean active, int ticks, float severity, int lastMessageStage) {
        this.active = active;
        this.ticks = Math.max(0, ticks);
        this.severity = Math.clamp(severity, 0.0F, 1.0F);
        this.lastMessageStage = Math.max(0, lastMessageStage);
        if (!this.active) {
            clear();
        }
    }
}
