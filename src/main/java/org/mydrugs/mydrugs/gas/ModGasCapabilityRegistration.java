package org.mydrugs.mydrugs.gas;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModGasCapabilityRegistration {
    private ModGasCapabilityRegistration() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.GAS_TANK.get(),
                (be, side) -> be.getGasHandler(side)
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.GAS_PUMP.get(),
                (be, side) -> be.getGasHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.CHEMICAL_REACTOR.get(),
                (blockEntity, side) -> blockEntity.getGasHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.CHEMICAL_REACTOR.get(),
                (blockEntity, side) -> blockEntity.getFluidResourceHandler(side)
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.CHEMICAL_REACTOR.get(),
                (blockEntity, side) -> blockEntity.getGasHandler(side)
        );
    }
}