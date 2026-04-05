package org.mydrugs.mydrugs.gas;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModGasCapabilities {
    public static final BlockCapability<IGasHandler, Direction> BLOCK =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "gas"),
                    IGasHandler.class
            );

    private ModGasCapabilities() {
    }
}