package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class MachineTransferAttachment implements ValueIOSerializable {
    private final MachineTransferConfig config = new MachineTransferConfig();
    private boolean installed;
    private boolean defaultsSeeded;

    public boolean installed() {
        return this.installed;
    }

    public void setInstalled(boolean installed) {
        this.installed = installed;
    }

    public MachineTransferConfig config() {
        return this.config;
    }

    public boolean defaultsSeeded() {
        return this.defaultsSeeded;
    }

    public void setDefaultsSeeded(boolean defaultsSeeded) {
        this.defaultsSeeded = defaultsSeeded;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("installed", this.installed);
        output.putBoolean("defaults_seeded", this.defaultsSeeded);
        this.config.serialize(output.child("config"));
    }

    @Override
    public void deserialize(ValueInput input) {
        this.installed = input.getBooleanOr("installed", false);
        this.defaultsSeeded = input.getBooleanOr("defaults_seeded", false);
        this.config.deserialize(input.childOrEmpty("config"));
    }
}
