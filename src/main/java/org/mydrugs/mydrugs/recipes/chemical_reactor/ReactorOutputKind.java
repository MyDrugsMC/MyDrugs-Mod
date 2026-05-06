package org.mydrugs.mydrugs.recipes.chemical_reactor;

import net.minecraft.util.StringRepresentable;

public enum ReactorOutputKind implements StringRepresentable {
    GAS("gas"),
    FLUID("fluid");

    private final String serializedName;

    ReactorOutputKind(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }

    public static ReactorOutputKind bySerializedName(String name) {
        for (ReactorOutputKind kind : values()) {
            if (kind.serializedName.equals(name)) {
                return kind;
            }
        }
        throw new IllegalArgumentException("Unknown reactor output kind: " + name);
    }
}
