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
}