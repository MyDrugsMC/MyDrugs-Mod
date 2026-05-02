package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.SieveBlockEntity;
import org.mydrugs.mydrugs.menu.AromaticExtractorMenu;
import org.mydrugs.mydrugs.menu.BTXFractionationTowerMenu;
import org.mydrugs.mydrugs.menu.BiochemicalReactorMenu;
import org.mydrugs.mydrugs.menu.CatalyticReformerMenu;
import org.mydrugs.mydrugs.menu.CentrifugeMenu;
import org.mydrugs.mydrugs.menu.DistillerMenu;
import org.mydrugs.mydrugs.menu.ElectrolyzerMenu;
import org.mydrugs.mydrugs.menu.FluidFiltererMenu;
import org.mydrugs.mydrugs.menu.GasifierMenu;
import org.mydrugs.mydrugs.menu.GrowthChamberMenu;
import org.mydrugs.mydrugs.menu.SteamCrackerMenu;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public final class MachineTransferSpecs {
    private static final Set<MachineLocalSide> INPUT_DEFAULT = EnumSet.of(MachineLocalSide.LEFT);
    private static final Set<MachineLocalSide> SECONDARY_INPUT_DEFAULT = EnumSet.of(MachineLocalSide.BACK);
    private static final Set<MachineLocalSide> OUTPUT_DEFAULT = EnumSet.of(MachineLocalSide.RIGHT);
    private static final Set<MachineLocalSide> TOP_INPUT_DEFAULT = EnumSet.of(MachineLocalSide.TOP);
    private static final Set<MachineLocalSide> BOTTOM_OUTPUT_DEFAULT = EnumSet.of(MachineLocalSide.BOTTOM);

    private MachineTransferSpecs() {
    }

    public static MachineTransferSpec get(BlockEntity blockEntity) {
        return get(blockEntity.getType());
    }

    public static MachineTransferSpec get(BlockEntityType<?> type) {
        if (type == ModBlockEntities.ADVANCED_FURNACE.get()) {
            return spec(
                    itemIn("input_a", 0, AdvancedFurnaceBlockEntity.INPUT_A_SLOT, INPUT_DEFAULT),
                    itemIn("input_b", 1, AdvancedFurnaceBlockEntity.INPUT_B_SLOT, SECONDARY_INPUT_DEFAULT),
                    itemIn("fuel", 2, AdvancedFurnaceBlockEntity.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    itemOut("output_a", 3, AdvancedFurnaceBlockEntity.OUTPUT_A_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_b", 4, AdvancedFurnaceBlockEntity.OUTPUT_B_SLOT, OUTPUT_DEFAULT),
                    itemIn("output_fluid_container", 5, AdvancedFurnaceBlockEntity.OUTPUT_FLUID_CONTAINER_SLOT, SECONDARY_INPUT_DEFAULT),
                    fluidOut("fluid_output", 6, 0, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.DISTILLER.get()) {
            return spec(
                    itemIn("input_container", 0, DistillerMenu.INPUT_CONTAINER_SLOT, INPUT_DEFAULT),
                    itemOut("output_a_container", 1, DistillerMenu.OUTPUT_A_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_b_container", 2, DistillerMenu.OUTPUT_B_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    fluidIn("fluid_input", 3, 0, INPUT_DEFAULT),
                    fluidOut("fluid_output_a", 4, 1, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_b", 5, 2, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.SIEVE.get()) {
            return spec(
                    itemIn("input", 0, SieveBlockEntity.SLOT_INPUT, INPUT_DEFAULT),
                    itemOut("result", 1, SieveBlockEntity.SLOT_RESULT, OUTPUT_DEFAULT),
                    itemOut("bonus", 2, SieveBlockEntity.SLOT_BONUS, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.FLUID_FILTERER.get()) {
            return spec(
                    itemIn("input_container", 0, FluidFiltererMenu.INPUT_CONTAINER_SLOT, INPUT_DEFAULT),
                    itemOut("output_container", 1, FluidFiltererMenu.OUTPUT_A_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemIn("filter", 2, FluidFiltererMenu.FILTER_SLOT, TOP_INPUT_DEFAULT),
                    itemOut("residue", 3, FluidFiltererMenu.RESIDUE_SLOT, BOTTOM_OUTPUT_DEFAULT),
                    fluidIn("fluid_input", 4, 0, INPUT_DEFAULT),
                    fluidOut("fluid_output", 5, 1, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.CENTRIFUGE.get()) {
            return spec(
                    itemIn("input_container", 0, CentrifugeMenu.INPUT_CONTAINER_SLOT, INPUT_DEFAULT),
                    itemOut("output_a_container", 1, CentrifugeMenu.OUTPUT_A_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_b_container", 2, CentrifugeMenu.OUTPUT_B_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemIn("fuel", 3, CentrifugeMenu.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("fluid_input", 4, 0, INPUT_DEFAULT),
                    fluidOut("fluid_output_a", 5, 1, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_b", 6, 2, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.BTX_FRACTIONATION_TOWER.get()) {
            return spec(
                    itemIn("input_container", 0, BTXFractionationTowerMenu.INPUT_CONTAINER_SLOT, INPUT_DEFAULT),
                    itemOut("benzene_container", 1, BTXFractionationTowerMenu.BENZENE_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("toluene_container", 2, BTXFractionationTowerMenu.TOLUENE_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("xylene_container", 3, BTXFractionationTowerMenu.XYLENE_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemIn("fuel", 4, BTXFractionationTowerMenu.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("fluid_input", 5, 0, INPUT_DEFAULT),
                    fluidOut("benzene_output", 6, 1, OUTPUT_DEFAULT),
                    fluidOut("toluene_output", 7, 2, OUTPUT_DEFAULT),
                    fluidOut("xylene_output", 8, 3, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.AROMATIC_EXTRACTOR.get()) {
            return spec(
                    itemIn("input_container", 0, AromaticExtractorMenu.INPUT_CONTAINER_SLOT, INPUT_DEFAULT),
                    itemIn("catalyst_container", 1, AromaticExtractorMenu.CATALYST_CONTAINER_SLOT, SECONDARY_INPUT_DEFAULT),
                    itemOut("output_a_container", 2, AromaticExtractorMenu.OUTPUT_A_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_b_container", 3, AromaticExtractorMenu.OUTPUT_B_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemIn("fuel", 4, AromaticExtractorMenu.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("fluid_input", 5, 0, INPUT_DEFAULT),
                    fluidIn("fluid_catalyst", 6, 1, SECONDARY_INPUT_DEFAULT),
                    fluidOut("fluid_output_a", 7, 2, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_b", 8, 3, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.ELECTROLYZER.get()) {
            return spec(
                    itemIn("input_container", 0, ElectrolyzerMenu.INPUT_CONTAINER_SLOT, INPUT_DEFAULT),
                    itemOut("output_1_container", 1, ElectrolyzerMenu.OUTPUT_1_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_2_container", 2, ElectrolyzerMenu.OUTPUT_2_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_3_container", 3, ElectrolyzerMenu.OUTPUT_3_CONTAINER_SLOT, OUTPUT_DEFAULT),
                    itemIn("fuel", 4, ElectrolyzerMenu.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("fluid_input", 5, 0, INPUT_DEFAULT),
                    fluidOut("fluid_output_1", 6, 1, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_2", 7, 2, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_3", 8, 3, OUTPUT_DEFAULT),
                    gasOut("gas_output_1", 9, 0, OUTPUT_DEFAULT),
                    gasOut("gas_output_2", 10, 1, OUTPUT_DEFAULT),
                    gasOut("gas_output_3", 11, 2, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.GROWTH_CHAMBER.get()) {
            return spec(
                    itemIn("plant_input", 0, GrowthChamberMenu.INPUT_SLOT, INPUT_DEFAULT),
                    itemIn("biomass", 1, GrowthChamberMenu.BIOMASS_SLOT, SECONDARY_INPUT_DEFAULT),
                    itemOut("middle_output", 2, GrowthChamberMenu.MIDDLE_SLOT, OUTPUT_DEFAULT),
                    itemOut("final_output", 3, GrowthChamberMenu.FINAL_SLOT, OUTPUT_DEFAULT),
                    itemIn("water_container", 4, GrowthChamberMenu.WATER_INPUT_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("water_input", 5, 0, INPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.BIOCHEMICAL_REACTOR.get()) {
            return spec(
                    itemIn("ergot_input", 0, 0, INPUT_DEFAULT),
                    itemIn("tryptophan_input", 1, 1, SECONDARY_INPUT_DEFAULT),
                    itemIn("charcoal_input", 2, 2, TOP_INPUT_DEFAULT),
                    itemIn("output_container", 3, BiochemicalReactorMenu.OUTPUT_CONTAINER_SLOT, SECONDARY_INPUT_DEFAULT),
                    fluidOut("fluid_output", 4, 0, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.GASIFIER.get()) {
            return spec(
                    itemIn("input", 0, GasifierMenu.INPUT_SLOT, INPUT_DEFAULT),
                    itemIn("fuel", 1, GasifierMenu.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    itemIn("gas_export_container", 2, GasifierMenu.EXPORT_SLOT, SECONDARY_INPUT_DEFAULT),
                    gasOut("gas_output", 3, 0, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.CHEMICAL_REACTOR.get()) {
            return spec(
                    itemIn("fuel", 0, ChemicalReactorBlockEntity.SLOT_FUEL, TOP_INPUT_DEFAULT),
                    itemIn("primary_gas_container", 1, ChemicalReactorBlockEntity.SLOT_PRIMARY_GAS_TRANSFER, INPUT_DEFAULT),
                    itemIn("secondary_transfer", 2, ChemicalReactorBlockEntity.SLOT_SECONDARY_TRANSFER, SECONDARY_INPUT_DEFAULT),
                    itemBoth("output_container", 3, ChemicalReactorBlockEntity.SLOT_OUTPUT_TRANSFER, OUTPUT_DEFAULT),
                    fluidIn("secondary_fluid_input", 5, 0, INPUT_DEFAULT),
                    fluidOut("fluid_output", 6, 1, OUTPUT_DEFAULT),
                    gasIn("primary_gas_input", 7, 0, INPUT_DEFAULT),
                    gasIn("secondary_gas_input", 8, 1, SECONDARY_INPUT_DEFAULT),
                    gasOut("gas_output", 9, 2, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.ADVANCED_MIXING_VAT_BE.get()) {
            return spec(
                    itemIn("item_input_1", 0, AdvancedMixingVatBlockEntity.SLOT_RECIPE_0, INPUT_DEFAULT),
                    itemIn("item_input_2", 1, AdvancedMixingVatBlockEntity.SLOT_RECIPE_1, INPUT_DEFAULT),
                    itemIn("item_input_3", 2, AdvancedMixingVatBlockEntity.SLOT_RECIPE_2, SECONDARY_INPUT_DEFAULT),
                    itemIn("item_input_4", 3, AdvancedMixingVatBlockEntity.SLOT_RECIPE_3, SECONDARY_INPUT_DEFAULT),
                    itemIn("fluid_input_a_container", 4, AdvancedMixingVatBlockEntity.SLOT_TANK_INPUT_A, INPUT_DEFAULT),
                    itemIn("fluid_input_b_container", 5, AdvancedMixingVatBlockEntity.SLOT_TANK_INPUT_B, SECONDARY_INPUT_DEFAULT),
                    itemIn("fluid_input_c_container", 6, AdvancedMixingVatBlockEntity.SLOT_TANK_INPUT_C, SECONDARY_INPUT_DEFAULT),
                    itemIn("fluid_output_container", 7, AdvancedMixingVatBlockEntity.SLOT_TANK_OUTPUT, OUTPUT_DEFAULT),
                    itemIn("gas_transfer_container", 8, AdvancedMixingVatBlockEntity.SLOT_GAS_TRANSFER, SECONDARY_INPUT_DEFAULT),
                    fluidIn("fluid_input_a", 9, 0, INPUT_DEFAULT),
                    fluidIn("fluid_input_b", 10, 1, SECONDARY_INPUT_DEFAULT),
                    fluidIn("fluid_input_c", 11, 2, SECONDARY_INPUT_DEFAULT),
                    fluidOut("fluid_output", 12, 3, OUTPUT_DEFAULT),
                    gasIn("gas_input", 13, 0, INPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.CATALYTIC_REFORMER.get()) {
            return spec(
                    itemIn("input_1_container", 0, CatalyticReformerMenu.INPUT_1_TRANSFER_SLOT, INPUT_DEFAULT),
                    itemIn("input_2_container", 1, CatalyticReformerMenu.INPUT_2_TRANSFER_SLOT, SECONDARY_INPUT_DEFAULT),
                    itemOut("output_1_container", 2, CatalyticReformerMenu.OUTPUT_1_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_2_container", 3, CatalyticReformerMenu.OUTPUT_2_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemOut("output_3_container", 4, CatalyticReformerMenu.OUTPUT_3_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemIn("catalyst", 5, CatalyticReformerMenu.CATALYST_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("fluid_input_1", 6, 0, INPUT_DEFAULT),
                    fluidIn("fluid_input_2", 7, 1, SECONDARY_INPUT_DEFAULT),
                    fluidOut("fluid_output_1", 8, 2, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_2", 9, 3, OUTPUT_DEFAULT),
                    fluidOut("fluid_output_3", 10, 4, OUTPUT_DEFAULT),
                    gasIn("gas_input_1", 11, 0, INPUT_DEFAULT),
                    gasIn("gas_input_2", 12, 1, SECONDARY_INPUT_DEFAULT),
                    gasOut("gas_output_1", 13, 2, OUTPUT_DEFAULT),
                    gasOut("gas_output_2", 14, 3, OUTPUT_DEFAULT),
                    gasOut("gas_output_3", 15, 4, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.STEAM_CRACKER.get()) {
            return spec(
                    itemIn("steam_input_container", 0, SteamCrackerMenu.INPUT_TRANSFER_SLOT, INPUT_DEFAULT),
                    itemOut("steam_output_1_container", 1, SteamCrackerMenu.OUTPUT_1_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemOut("steam_output_2_container", 2, SteamCrackerMenu.OUTPUT_2_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemOut("steam_output_3_container", 3, SteamCrackerMenu.OUTPUT_3_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemOut("steam_output_4_container", 4, SteamCrackerMenu.OUTPUT_4_TRANSFER_SLOT, OUTPUT_DEFAULT),
                    itemIn("fuel", 5, SteamCrackerMenu.FUEL_SLOT, TOP_INPUT_DEFAULT),
                    fluidIn("steam_fluid_input", 6, 0, INPUT_DEFAULT),
                    gasIn("steam_gas_input", 7, 0, INPUT_DEFAULT),
                    fluidOut("steam_fluid_output_1", 8, 1, OUTPUT_DEFAULT),
                    gasOut("steam_gas_output_1", 9, 1, OUTPUT_DEFAULT),
                    fluidOut("steam_fluid_output_2", 10, 2, OUTPUT_DEFAULT),
                    gasOut("steam_gas_output_2", 11, 2, OUTPUT_DEFAULT),
                    fluidOut("steam_fluid_output_3", 12, 3, OUTPUT_DEFAULT),
                    gasOut("steam_gas_output_3", 13, 3, OUTPUT_DEFAULT),
                    fluidOut("steam_fluid_output_4", 14, 4, OUTPUT_DEFAULT),
                    gasOut("steam_gas_output_4", 15, 4, OUTPUT_DEFAULT)
            );
        }
        if (type == ModBlockEntities.GAS_TANK.get() || type == ModBlockEntities.GAS_PUMP.get()) {
            return spec(gasBoth("gas_buffer", 0, 0, EnumSet.of(MachineLocalSide.LEFT, MachineLocalSide.RIGHT)));
        }

        // These machines need exact tank/slot audits before exposing automation safely.
        return MachineTransferSpec.EMPTY;
    }

    private static MachineTransferSpec spec(MachineTransferPortSpec... ports) {
        return new MachineTransferSpec(List.of(ports));
    }

    private static MachineTransferPortSpec itemIn(String path, int order, int slot, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.ITEM, MachineTransferAccess.INPUT_ONLY, List.of(slot), List.of(), List.of(), defaults, order);
    }

    private static MachineTransferPortSpec itemOut(String path, int order, int slot, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.ITEM, MachineTransferAccess.OUTPUT_ONLY, List.of(slot), List.of(), List.of(), defaults, order);
    }

    private static MachineTransferPortSpec itemBoth(String path, int order, int slot, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.ITEM, MachineTransferAccess.BIDIRECTIONAL, List.of(slot), List.of(), List.of(), defaults, order);
    }

    private static MachineTransferPortSpec fluidIn(String path, int order, int tank, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.FLUID, MachineTransferAccess.INPUT_ONLY, List.of(), List.of(tank), List.of(), defaults, order);
    }

    private static MachineTransferPortSpec fluidOut(String path, int order, int tank, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.FLUID, MachineTransferAccess.OUTPUT_ONLY, List.of(), List.of(tank), List.of(), defaults, order);
    }

    private static MachineTransferPortSpec gasIn(String path, int order, int tank, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.GAS, MachineTransferAccess.INPUT_ONLY, List.of(), List.of(), List.of(tank), defaults, order);
    }

    private static MachineTransferPortSpec gasOut(String path, int order, int tank, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.GAS, MachineTransferAccess.OUTPUT_ONLY, List.of(), List.of(), List.of(tank), defaults, order);
    }

    private static MachineTransferPortSpec gasBoth(String path, int order, int tank, Set<MachineLocalSide> defaults) {
        return port(path, MachineTransferResourceKind.GAS, MachineTransferAccess.BIDIRECTIONAL, List.of(), List.of(), List.of(tank), defaults, order);
    }

    private static MachineTransferPortSpec port(
            String path,
            MachineTransferResourceKind kind,
            MachineTransferAccess access,
            List<Integer> itemSlots,
            List<Integer> fluidTanks,
            List<Integer> gasTanks,
            Set<MachineLocalSide> defaults,
            int order
    ) {
        return new MachineTransferPortSpec(
                MachineTransferPortId.of(MyDrugs.MODID, path),
                kind,
                access,
                "machine_transfer_port.mydrugs." + path,
                itemSlots,
                fluidTanks,
                gasTanks,
                defaults,
                true,
                order
        );
    }
}
