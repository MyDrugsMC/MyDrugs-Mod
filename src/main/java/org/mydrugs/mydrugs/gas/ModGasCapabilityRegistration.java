package org.mydrugs.mydrugs.gas;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferResourceKind;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferResourceHandlers;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModGasCapabilityRegistration {
    private ModGasCapabilityRegistration() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.GAS_TANK.get(),
                (be, side) -> MachineTransferResourceHandlers.restrictedGas(be, side, be.getGasHandler(side))
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.GAS_PUMP.get(),
                (be, side) -> MachineTransferResourceHandlers.restrictedGas(be, side, be.getGasHandler(side))
        );


        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.CHEMICAL_REACTOR.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.restricted(blockEntity, MachineTransferResourceKind.FLUID, side, blockEntity.getFluidResourceHandler(side))
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.CHEMICAL_REACTOR.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.restrictedGas(blockEntity, side, blockEntity.getGasHandler(side))
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.ADVANCED_MIXING_VAT_BE.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.restricted(blockEntity, MachineTransferResourceKind.FLUID, side, blockEntity.getFluidCapability(side))
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.ADVANCED_MIXING_VAT_BE.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.restrictedGas(blockEntity, side, blockEntity.getGasCapability(side))
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.GASIFIER.get(),
                (be, side) -> MachineTransferResourceHandlers.restrictedGas(be, side, be.getGasHandler(side))
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.ELECTROLYZER.get(),
                (be, side) -> MachineTransferResourceHandlers.restrictedGas(be, side, be.getGasHandler(side))
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.CATALYTIC_REFORMER.get(),
                (be, side) -> MachineTransferResourceHandlers.restrictedGas(be, side, be.getGasHandler(side))
        );

        event.registerItem(
                ModGasCapabilities.ITEM,
                (stack, itemAccess) -> new GasTankItemHandler(stack),
                ModItems.GAS_TANK_ITEM.get()
        );
    }
}
