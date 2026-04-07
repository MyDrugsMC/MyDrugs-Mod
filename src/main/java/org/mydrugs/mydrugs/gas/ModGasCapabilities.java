package org.mydrugs.mydrugs.gas;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModGasCapabilities {
    public static final BlockCapability<IGasHandler, Direction> BLOCK =
            BlockCapability.createSided(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "gas_block"),
                    IGasHandler.class
            );

    public static final ItemCapability<IGasHandler, Void> ITEM =
            ItemCapability.createVoid(
                    ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "gas_item"),
                    IGasHandler.class
            );

    private ModGasCapabilities() {
    }
}