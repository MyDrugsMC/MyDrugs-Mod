package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.util.StringRepresentable;

public enum MachineLocalSide implements StringRepresentable {
    FRONT("front"),
    BACK("back"),
    LEFT("left"),
    RIGHT("right"),
    TOP("top"),
    BOTTOM("bottom");

    private final String serializedName;

    MachineLocalSide(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
