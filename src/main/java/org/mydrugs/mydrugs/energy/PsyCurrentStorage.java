package org.mydrugs.mydrugs.energy;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/** A bounded buffer of Psy Current. */
public final class PsyCurrentStorage {
    private int stored;
    private int capacity;

    public PsyCurrentStorage() {
        this(PsyCurrentConstants.DEFAULT_MACHINE_CAPACITY);
    }

    public PsyCurrentStorage(int capacity) {
        this.capacity = Math.max(0, capacity);
    }

    public int stored() {
        return this.stored;
    }

    public int capacity() {
        return this.capacity;
    }

    public int receive(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, this.capacity - this.stored);
        if (!simulate) {
            this.stored += accepted;
        }
        return accepted;
    }

    public int extract(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }
        int extracted = Math.min(amount, this.stored);
        if (!simulate) {
            this.stored -= extracted;
        }
        return extracted;
    }

    public boolean canReceive(int amount) {
        return amount > 0 && this.stored + amount <= this.capacity;
    }

    public void serialize(ValueOutput output) {
        output.putInt("stored", this.stored);
        output.putInt("capacity", this.capacity);
    }

    public void deserialize(ValueInput input) {
        this.capacity = Math.max(0, input.getIntOr("capacity", PsyCurrentConstants.DEFAULT_MACHINE_CAPACITY));
        this.stored = Math.clamp(input.getIntOr("stored", 0), 0, this.capacity);
    }
}
