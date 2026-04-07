package org.mydrugs.mydrugs.machine.transfer;

import java.util.Arrays;

public final class LockedTransferSlots {
    private final TransferMode[] modes;

    public LockedTransferSlots(int size) {
        this.modes = new TransferMode[size];
        Arrays.fill(this.modes, TransferMode.NONE);
    }

    public TransferMode get(int index) {
        return this.modes[index];
    }

    public void set(int index, TransferMode mode) {
        this.modes[index] = mode == null ? TransferMode.NONE : mode;
    }

    public void reset(int index) {
        this.modes[index] = TransferMode.NONE;
    }

    public void resetAll() {
        Arrays.fill(this.modes, TransferMode.NONE);
    }
}