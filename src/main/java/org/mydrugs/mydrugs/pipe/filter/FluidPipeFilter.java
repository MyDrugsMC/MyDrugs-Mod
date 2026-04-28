package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

public final class FluidPipeFilter {
    private FluidPipeFilter() {
    }

    public static boolean allows(PipeFilterConfig config, FluidResource resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }

        return config.allows(BuiltInRegistries.FLUID.getKey(resource.getFluid()));
    }
}
