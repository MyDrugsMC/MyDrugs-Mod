package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Nullable;

public enum MachineLocalSide implements StringRepresentable {
    FRONT("front", 0),
    BACK("back", 1),
    LEFT("left", 2),
    RIGHT("right", 3),
    TOP("top", 4),
    BOTTOM("bottom", 5);

    private final String serializedName;
    private final int networkId;

    MachineLocalSide(String serializedName, int networkId) {
        this.serializedName = serializedName;
        this.networkId = networkId;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public int networkId() {
        return this.networkId;
    }

    public static @Nullable MachineLocalSide bySerializedName(String name) {
        if (name == null) {
            return null;
        }

        for (MachineLocalSide side : values()) {
            if (side.serializedName.equals(name) || side.name().equalsIgnoreCase(name)) {
                return side;
            }
        }
        return null;
    }
}
