package org.mydrugs.mydrugs.gas;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.fluids.Hazard;

public record GasType(
        ResourceLocation id,
        int tint,
        boolean toxic,
        boolean flammable,
        Hazard hazard,
        boolean pipeTransportable,
        boolean tankStorable,
        int containmentTier
) {
    public String name() {
        return id.getPath();
    }
}
