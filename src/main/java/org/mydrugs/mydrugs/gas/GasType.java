package org.mydrugs.mydrugs.gas;

import net.minecraft.resources.ResourceLocation;

public record GasType(
        ResourceLocation id,
        int tint,
        boolean toxic,
        boolean flammable
) {
    public String name() {
        return id.getPath();
    }
}