package org.mydrugs.mydrugs.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.BottleFluidHandler;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferResourceKind;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferResourceHandlers;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ServerModEvents {
    private ServerModEvents() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, access) -> new BottleFluidHandler(access),
                ModItems.GLASS_BOTTLE.get()
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.PIPES.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.PIPES.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler(side)
        );

        event.registerBlockEntity(
                ModGasCapabilities.BLOCK,
                ModBlockEntities.PIPES.get(),
                (blockEntity, side) -> blockEntity.getGasHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.FLUID_PUMP.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ADVANCED_FURNACE.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.DISTILLER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.SIEVE.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.FLUID_FILTERER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.CENTRIFUGE.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.BTX_FRACTIONATION_TOWER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.AROMATIC_EXTRACTOR.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ELECTROLYZER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.GROWTH_CHAMBER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.BIOCHEMICAL_REACTOR.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.GASIFIER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.CATALYTIC_REFORMER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.STEAM_CRACKER.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.itemContainer(blockEntity, blockEntity, side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.CHEMICAL_REACTOR.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.restricted(
                        blockEntity,
                        MachineTransferResourceKind.ITEM,
                        side,
                        blockEntity.getItemHandler(side)
                )
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.ADVANCED_MIXING_VAT_BE.get(),
                (blockEntity, side) -> MachineTransferResourceHandlers.restricted(
                        blockEntity,
                        MachineTransferResourceKind.ITEM,
                        side,
                        blockEntity.getItemCapability(side)
                )
        );

        // Fluid capabilities for machines using StoredFluidTank
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.ADVANCED_FURNACE.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.DISTILLER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.FLUID_FILTERER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.CENTRIFUGE.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.BTX_FRACTIONATION_TOWER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.AROMATIC_EXTRACTOR.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.ELECTROLYZER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.GROWTH_CHAMBER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.BIOCHEMICAL_REACTOR.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.CATALYTIC_REFORMER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );
        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.STEAM_CRACKER.get(),
                (be, side) -> MachineTransferResourceHandlers.restricted(be, MachineTransferResourceKind.FLUID, side, be.getFluidHandler(side))
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.PSYCHOTROPE_COMPONENT.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Item.BLOCK,
                ModBlockEntities.EVAPORATION_TRAY.get(),
                (blockEntity, side) -> blockEntity.getItemHandler(side)
        );

        event.registerBlockEntity(
                Capabilities.Fluid.BLOCK,
                ModBlockEntities.PSYCHOTROPE_COMPONENT.get(),
                (blockEntity, side) -> blockEntity.getFluidHandler(side)
        );
    }
}
