package org.mydrugs.mydrugs.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mydrugs.mydrugs.datagen.VanillaRecipeSnapshotWriter.alt;
import static org.mydrugs.mydrugs.datagen.VanillaRecipeSnapshotWriter.key;

public class ModVanillaRecipeSnapshotProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;

    public ModVanillaRecipeSnapshotProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        VanillaRecipeSnapshotWriter writer =
                new VanillaRecipeSnapshotWriter(recipePathProvider, futures, cachedOutput);

        /*
         * Progression design:
         *
         * Tier 0: wood, clay, glass, basic hand tools
         * Tier 1: copper, seals, filters, vats, sieve, stomp crafter
         * Tier 2: iron/steel mechanical machines
         * Tier 3: pressure systems, chemistry machines, gas/fluid handling
         * Tier 4: petrochemistry, electrochemistry, psychotrope blocks
         *
         * New component items expected:
         * - insulated_wire
         * - control_circuit
         * - advanced_control_circuit
         * - electric_motor
         * - heating_coil
         * - condenser_coil
         * - electrode_pair
         * - catalyst_bed
         * - packed_column
         * - pipe_joint
         * - psychotrope_lens
         */

        // ---------------------------------------------------------------------
        // Storage / raw material compression
        // ---------------------------------------------------------------------

        writer.shaped("salt_block",
                new String[]{
                        "AAA",
                        "AAA",
                        "AAA"
                },
                key(
                        "A", "mydrugs:salt_powder"
                ),
                "mydrugs:salt_block",
                1
        );

        writer.shapeless("salt_from_salt_block",
                new Object[]{
                        "mydrugs:salt_block"
                },
                "mydrugs:salt_powder",
                9
        );

        writer.shaped("raw_platinum_block",
                new String[]{
                        "AAA",
                        "AAA",
                        "AAA"
                },
                key(
                        "A", "mydrugs:raw_platinum"
                ),
                "mydrugs:raw_platinum_block",
                1
        );

        writer.shapeless("raw_platinum_from_raw_platinum_block",
                new Object[]{
                        "mydrugs:raw_platinum_block"
                },
                "mydrugs:raw_platinum",
                9
        );

        writer.smelting("platinum_ingot_from_raw_platinum",
                "mydrugs:raw_platinum",
                "mydrugs:platinum_ingot",
                0.7F,
                200
        );

        writer.blasting("platinum_ingot_from_blasting_raw_platinum",
                "mydrugs:raw_platinum",
                "mydrugs:platinum_ingot",
                0.7F,
                100
        );

        writer.smelting("sulfur_from_sulfur_ore",
                "mydrugs:sulfur_ore",
                "mydrugs:sulfur_powder",
                0.5F,
                200
        );

        writer.blasting("sulfur_from_blasting_sulfur_ore",
                "mydrugs:sulfur_ore",
                "mydrugs:sulfur_powder",
                0.5F,
                100
        );

        writer.smelting("sulfur_from_deepslate_sulfur_ore",
                "mydrugs:deepslate_sulfur_ore",
                "mydrugs:sulfur_powder",
                0.7F,
                200
        );

        writer.blasting("sulfur_from_blasting_deepslate_sulfur_ore",
                "mydrugs:deepslate_sulfur_ore",
                "mydrugs:sulfur_powder",
                0.7F,
                100
        );

        writer.smelting("refractory_brick",
                "mydrugs:refractory_mix",
                "mydrugs:refractory_brick",
                0.7F,
                200
        );

        writer.blasting("refractory_brick_blasting",
                "mydrugs:refractory_mix",
                "mydrugs:refractory_brick",
                0.7F,
                100
        );

        // ---------------------------------------------------------------------
        // Basic wood, clay, glass, and hand components
        // ---------------------------------------------------------------------

        writer.shapeless("treated_planks",
                new Object[]{
                        "#minecraft:planks",
                        "mydrugs:resin"
                },
                "mydrugs:treated_planks",
                4
        );

        writer.shaped("wooden_frame",
                new String[]{
                        "A A",
                        " B ",
                        "A A"
                },
                key(
                        "A", "mydrugs:treated_planks",
                        "B", "minecraft:stick"
                ),
                "mydrugs:wooden_frame",
                1
        );

        writer.shaped("cupboard_piece",
                new String[]{
                        "AA",
                        "AA"
                },
                key(
                        "A", "minecraft:sugar_cane"
                ),
                "mydrugs:cupboard_piece",
                4
        );

        writer.shaped("clay_vat",
                new String[]{
                        "A A",
                        "A A",
                        "AAA"
                },
                key(
                        "A", "minecraft:clay_ball"
                ),
                "mydrugs:clay_vat",
                1
        );

        writer.shapeless("cup",
                new Object[]{
                        "minecraft:brick"
                },
                "mydrugs:cup",
                1
        );

        writer.shapeless("porous_clay",
                new Object[]{
                        "minecraft:clay_ball",
                        "minecraft:sand",
                        "minecraft:gravel"
                },
                "mydrugs:porous_clay",
                2
        );

        writer.smelting("porous_ceramic_from_porous_clay",
                "mydrugs:porous_clay",
                "mydrugs:porous_ceramic",
                0.1F,
                200
        );

        writer.shapeless("raw_thick_glass",
                new Object[]{
                        "minecraft:glass",
                        "minecraft:quartz",
                        "minecraft:quartz"
                },
                "mydrugs:raw_thick_glass",
                2
        );

        writer.smelting("thick_glass_from_raw_thick_glass",
                "mydrugs:raw_thick_glass",
                "mydrugs:thick_glass",
                0.1F,
                200
        );

        writer.shaped("glass_tube",
                new String[]{
                        "A A",
                        " B ",
                        "A A"
                },
                key(
                        "A", "minecraft:glass",
                        "B", "minecraft:copper_ingot"
                ),
                "mydrugs:glass_tube",
                2
        );

        writer.shaped("copper_tube",
                new String[]{
                        "AAA",
                        "AAA"
                },
                key(
                        "A", "mydrugs:copper_plate"
                ),
                "mydrugs:copper_tube",
                2
        );

        writer.shapeless("glass_bottle",
                new Object[]{
                        "minecraft:glass_bottle"
                },
                "mydrugs:glass_bottle",
                1
        );

        writer.shapeless("progression_guide",
                new Object[]{
                        "minecraft:book",
                        "#mydrugs:progression_guide_seed_sources"
                },
                "mydrugs:progression_guide",
                1
        );

        writer.shaped("grinding_bowl",
                new String[]{
                        "A A",
                        "AAA"
                },
                key(
                        "A", "minecraft:brick"
                ),
                "mydrugs:grinding_bowl",
                1
        );

        writer.shaped("grinding_tool",
                new String[]{
                        " A",
                        "B "
                },
                key(
                        "A", "minecraft:stick",
                        "B", "minecraft:stone"
                ),
                "mydrugs:grinding_tool",
                1
        );

        writer.shaped("stone_hammer",
                new String[]{
                        "AAA",
                        "ASA",
                        "  S"
                },
                key(
                        "A", alt("minecraft:cobblestone", "minecraft:stone"),
                        "S", "minecraft:stick"
                ),
                "mydrugs:stone_hammer",
                1
        );

        writer.shaped("syringe",
                new String[]{
                        " A ",
                        " B ",
                        " C "
                },
                key(
                        "A", "minecraft:iron_nugget",
                        "B", "minecraft:glass_bottle",
                        "C", "minecraft:copper_ingot"
                ),
                "mydrugs:syringe",
                1
        );

        writer.shaped("iron_hammer",
                new String[]{
                        "AAA",
                        "ASA",
                        "  S"
                },
                key(
                        "A", "minecraft:iron_ingot",
                        "S", "minecraft:stick"
                ),
                "mydrugs:iron_hammer",
                1
        );

        writer.shaped("steel_hammer",
                new String[]{
                        "AAA",
                        "ASA",
                        "  S"
                },
                key(
                        "A", "mydrugs:steel_ingot",
                        "S", "minecraft:stick"
                ),
                "mydrugs:steel_hammer",
                1
        );

        writer.shaped("psy_anvil",
                new String[]{
                        "IRI",
                        "CAC",
                        "SSS"
                },
                key(
                        "I", "minecraft:iron_ingot",
                        "R", "mydrugs:psy_receptacle",
                        "C", "minecraft:copper_ingot",
                        "A", "minecraft:ender_pearl",
                        "S", alt("minecraft:cobbled_deepslate", "minecraft:stone")
                ),
                "mydrugs:psy_anvil",
                1
        );

        // ---------------------------------------------------------------------
        // Mechanical parts
        // ---------------------------------------------------------------------

        writer.shaped("copper_strapping",
                new String[]{
                        "AAA",
                        "A A",
                        "AAA"
                },
                key(
                        "A", "mydrugs:copper_plate"
                ),
                "mydrugs:copper_strapping",
                8
        );

        writer.shaped("iron_axle",
                new String[]{
                        " A ",
                        " B ",
                        " A "
                },
                key(
                        "A", "mydrugs:heavy_iron",
                        "B", "minecraft:iron_ingot"
                ),
                "mydrugs:iron_axle",
                1
        );

        writer.shaped("hand_crank",
                new String[]{
                        " A ",
                        "ASA",
                        " A "
                },
                key(
                        "A", "minecraft:stick",
                        "S", "mydrugs:iron_axle"
                ),
                "mydrugs:hand_crank",
                1
        );

        writer.shaped("agitator",
                new String[]{
                        " A ",
                        "BCB",
                        " D "
                },
                key(
                        "A", "mydrugs:iron_axle",
                        "B", "mydrugs:steel_plate",
                        "C", "mydrugs:copper_plate",
                        "D", "minecraft:iron_ingot"
                ),
                "mydrugs:agitator",
                1
        );

        writer.shaped("stomp_plate",
                new String[]{
                        "AAA"
                },
                key(
                        "A", "mydrugs:heavy_iron"
                ),
                "mydrugs:stomp_plate",
                1
        );

        writer.shaped("stomp_plate_block",
                new String[]{
                        "AAA",
                        "ABA",
                        "AAA"
                },
                key(
                        "A", "mydrugs:stomp_plate",
                        "B", "mydrugs:mechanical_frame"
                ),
                "mydrugs:stomp_plate_block",
                1
        );

        writer.shaped("mechanical_frame",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:heavy_iron",
                        "B", "#minecraft:planks",
                        "C", "minecraft:stick",
                        "D", "minecraft:iron_ingot"
                ),
                "mydrugs:mechanical_frame",
                1
        );

        writer.shaped("reinforced_casing",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:heavy_iron_plate",
                        "B", "mydrugs:heavy_iron",
                        "C", "minecraft:iron_ingot",
                        "D", "mydrugs:mechanical_frame"
                ),
                "mydrugs:reinforced_casing",
                1
        );

        writer.shaped("stomp_crafter",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "mydrugs:heavy_iron",
                        "B", "mydrugs:hand_crank",
                        "C", "mydrugs:stomp_plate",
                        "D", "mydrugs:mechanical_frame",
                        "E", "mydrugs:treated_planks",
                        "F", "mydrugs:wooden_frame"
                ),
                "mydrugs:stomp_crafter",
                1
        );

        // ---------------------------------------------------------------------
        // Seals, filters, pressure parts
        // ---------------------------------------------------------------------

        writer.shaped("soft_seal",
                new String[]{
                        "AA",
                        "AA"
                },
                key(
                        "A", "minecraft:leather"
                ),
                "mydrugs:soft_seal",
                8
        );

        writer.shaped("tight_seal",
                new String[]{
                        "AA",
                        "AA"
                },
                key(
                        "A", "mydrugs:rubber"
                ),
                "mydrugs:tight_seal",
                8
        );

        writer.shaped("pressure_seal",
                new String[]{
                        " A ",
                        " B ",
                        " A "
                },
                key(
                        "A", "mydrugs:tight_seal",
                        "B", "mydrugs:copper_strapping"
                ),
                "mydrugs:pressure_seal",
                1
        );

        writer.shaped("membrane",
                new String[]{
                        " A ",
                        "BCB",
                        " B "
                },
                key(
                        "A", "minecraft:leather",
                        "B", "mydrugs:rubber",
                        "C", "minecraft:string"
                ),
                "mydrugs:membrane",
                1
        );

        writer.shapeless("cigarette_filter",
                new Object[]{
                        "mydrugs:cupboard_piece"
                },
                "mydrugs:cigarette_filter",
                4
        );

        writer.shapeless("opium_poppy_seeds",
                new Object[]{
                        "minecraft:poppy"
                },
                "mydrugs:opium_poppy_seeds",
                1
        );

        writer.shaped("fluid_filter",
                new String[]{
                        " A ",
                        "BCB",
                        " D "
                },
                key(
                        "A", "mydrugs:porous_ceramic",
                        "B", "minecraft:paper",
                        "C", "mydrugs:activated_coal",
                        "D", "minecraft:string"
                ),
                "mydrugs:fluid_filter",
                1
        );

        writer.shaped("filter_box",
                new String[]{
                        "AAA",
                        "BCB",
                        "AAA"
                },
                key(
                        "A", "minecraft:iron_ingot",
                        "B", "mydrugs:fluid_filter",
                        "C", "minecraft:glass"
                ),
                "mydrugs:filter_box",
                1
        );

        writer.shaped("valve",
                new String[]{
                        " A ",
                        "BCB",
                        " D "
                },
                key(
                        "A", "mydrugs:soft_seal",
                        "B", "mydrugs:copper_plate",
                        "C", "minecraft:iron_ingot",
                        "D", "mydrugs:copper_tube"
                ),
                "mydrugs:valve",
                1
        );

        writer.shaped("pump_head",
                new String[]{
                        " A ",
                        "BCB",
                        " D "
                },
                key(
                        "A", "mydrugs:valve",
                        "B", "mydrugs:copper_tube",
                        "C", "mydrugs:membrane",
                        "D", "mydrugs:pressure_seal"
                ),
                "mydrugs:pump_head",
                1
        );

        writer.shaped("injector_nozzle",
                new String[]{
                        " A ",
                        " B ",
                        " C "
                },
                key(
                        "A", "mydrugs:copper_tube",
                        "B", "mydrugs:valve",
                        "C", "mydrugs:pressure_seal"
                ),
                "mydrugs:injector_nozzle",
                1
        );

        writer.shaped("tank_wall",
                new String[]{
                        "AAA",
                        " B ",
                        "AAA"
                },
                key(
                        "A", "mydrugs:steel_plate",
                        "B", "mydrugs:pressure_seal"
                ),
                "mydrugs:tank_wall",
                1
        );

        writer.shaped("pressure_casing",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:pressure_seal",
                        "B", "mydrugs:thick_glass",
                        "C", "mydrugs:reinforced_casing",
                        "D", "mydrugs:tank_wall"
                ),
                "mydrugs:pressure_casing",
                1
        );

        writer.shaped("reaction_core",
                new String[]{
                        " A ",
                        "BCB",
                        " D "
                },
                key(
                        "A", "mydrugs:thick_glass",
                        "B", "mydrugs:glass_tube",
                        "C", "mydrugs:pressure_casing",
                        "D", "mydrugs:valve"
                ),
                "mydrugs:reaction_core",
                1
        );

        // ---------------------------------------------------------------------
        // Electrical / advanced industrial components
        // ---------------------------------------------------------------------


        writer.shaped("control_circuit",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:insulated_wire",
                        "B", "minecraft:quartz",
                        "C", "mydrugs:copper_plate",
                        "D", "minecraft:redstone"
                ),
                "mydrugs:control_circuit",
                1
        );

        writer.shaped("electric_motor",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "minecraft:iron_ingot",
                        "B", "mydrugs:insulated_wire",
                        "C", "mydrugs:copper_plate",
                        "D", "mydrugs:iron_axle"
                ),
                "mydrugs:electric_motor",
                1
        );

        writer.shaped("heating_coil",
                new String[]{
                        "ABA",
                        "BCB",
                        "ABA"
                },
                key(
                        "A", "mydrugs:refractory_brick",
                        "B", "mydrugs:copper_plate",
                        "C", "minecraft:redstone"
                ),
                "mydrugs:heating_coil",
                1
        );

        writer.shaped("condenser_coil",
                new String[]{
                        "ABA",
                        "C C",
                        "ABA"
                },
                key(
                        "A", "mydrugs:copper_tube",
                        "B", "mydrugs:thick_glass",
                        "C", "mydrugs:copper_plate"
                ),
                "mydrugs:condenser_coil",
                1
        );

        writer.shaped("electrode_pair",
                new String[]{
                        " A ",
                        "BCB",
                        " A "
                },
                key(
                        "A", "mydrugs:copper_tube",
                        "B", "mydrugs:platinum_ingot",
                        "C", "mydrugs:insulated_wire"
                ),
                "mydrugs:electrode_pair",
                1
        );

        writer.shaped("catalyst_bed",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:porous_ceramic",
                        "B", "mydrugs:platinum_ingot",
                        "C", "mydrugs:activated_coal",
                        "D", "mydrugs:steel_plate"
                ),
                "mydrugs:catalyst_bed",
                1
        );

        writer.shaped("packed_column",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:steel_plate",
                        "B", "mydrugs:copper_tube",
                        "C", "mydrugs:porous_ceramic",
                        "D", "mydrugs:thick_glass"
                ),
                "mydrugs:packed_column",
                1
        );

        writer.shaped("pipe_joint",
                new String[]{
                        " A ",
                        "ABA",
                        " A "
                },
                key(
                        "A", "mydrugs:soft_seal",
                        "B", "mydrugs:copper_plate"
                ),
                "mydrugs:pipe_joint",
                4
        );

        writer.shaped("pipe_wrench",
                new String[]{
                        " A ",
                        " BA",
                        "B  "
                },
                key(
                        "A", "minecraft:iron_ingot",
                        "B", "minecraft:stick"
                ),
                "mydrugs:pipe_wrench",
                1
        );

        writer.shaped("pipe_filter_upgrade",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:pipe_joint",
                        "B", "minecraft:redstone",
                        "C", "mydrugs:iron_mesh",
                        "D", "mydrugs:fluid_filter"
                ),
                "mydrugs:pipe_filter_upgrade",
                1
        );

        writer.shaped("machine_transfer_upgrade",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:pipe_joint",
                        "B", "mydrugs:copper_tube",
                        "C", "minecraft:redstone",
                        "D", "mydrugs:control_circuit"
                ),
                "mydrugs:machine_transfer_upgrade",
                1
        );

        writer.shaped("psychotrope_lens",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "minecraft:amethyst_shard",
                        "B", "minecraft:quartz",
                        "C", "mydrugs:thick_glass",
                        "D", "minecraft:diamond"
                ),
                "mydrugs:psychotrope_lens",
                1
        );

        // ---------------------------------------------------------------------
        // Primitive and early machines
        // ---------------------------------------------------------------------

        writer.shaped("mixing_vat",
                new String[]{
                        " A ",
                        "BCB",
                        " D "
                },
                key(
                        "A", "minecraft:stick",
                        "B", "mydrugs:copper_strapping",
                        "C", "mydrugs:clay_vat",
                        "D", "mydrugs:wooden_frame"
                ),
                "mydrugs:mixing_vat",
                1
        );

        writer.shaped("sieve",
                new String[]{
                        "A A",
                        "BCB",
                        " D "
                },
                key(
                        "A", "minecraft:copper_ingot",
                        "B", "minecraft:stick",
                        "C", "mydrugs:iron_mesh",
                        "D", "mydrugs:wooden_frame"
                ),
                "mydrugs:sieve",
                1
        );

        writer.shaped("evaporation_tray",
                new String[]{
                        "A A",
                        "BBB"
                },
                key(
                        "A", "mydrugs:copper_plate",
                        "B", "minecraft:brick"
                ),
                "mydrugs:evaporation_tray",
                1
        );

        writer.shaped("drying_rack",
                new String[]{
                        "AAA",
                        "BCB",
                        "AAA"
                },
                key(
                        "A", "minecraft:stick",
                        "B", "minecraft:string",
                        "C", "mydrugs:treated_planks"
                ),
                "mydrugs:drying_rack",
                1
        );

        writer.shaped("fluid_filterer",
                new String[]{
                        "ABA",
                        "CDC",
                        " E "
                },
                key(
                        "A", "minecraft:iron_ingot",
                        "B", "minecraft:glass",
                        "C", "mydrugs:glass_tube",
                        "D", "mydrugs:filter_box",
                        "E", "mydrugs:porous_ceramic"
                ),
                "mydrugs:fluid_filterer",
                1
        );

        // ---------------------------------------------------------------------
        // Furnaces, vats, reactors, and processing machines
        // ---------------------------------------------------------------------

        writer.shaped("heat_lining",
                new String[]{
                        "AAA",
                        "A A",
                        "AAA"
                },
                key(
                        "A", "mydrugs:refractory_brick"
                ),
                "mydrugs:heat_lining",
                1
        );

        writer.shaped("advanced_furnace",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "mydrugs:heavy_iron",
                        "B", "mydrugs:heating_coil",
                        "C", "mydrugs:heat_lining",
                        "D", "minecraft:furnace",
                        "E", "mydrugs:reinforced_casing",
                        "F", "mydrugs:mechanical_frame"
                ),
                "mydrugs:advanced_furnace",
                1
        );

        writer.shaped("advanced_mixing_vat",
                new String[]{
                        "ABA",
                        "CDE",
                        "AFA"
                },
                key(
                        "A", "mydrugs:tight_seal",
                        "B", "mydrugs:control_circuit",
                        "C", "mydrugs:electric_motor",
                        "D", "mydrugs:mixing_vat",
                        "E", "mydrugs:agitator",
                        "F", "mydrugs:reinforced_casing"
                ),
                "mydrugs:advanced_mixing_vat",
                1
        );

        writer.shaped("gasifier",
                new String[]{
                        "ABA",
                        "CDE",
                        " F "
                },
                key(
                        "A", "mydrugs:heat_lining",
                        "B", "mydrugs:pressure_casing",
                        "C", "mydrugs:valve",
                        "D", "mydrugs:advanced_furnace",
                        "E", "mydrugs:iron_axle",
                        "F", "mydrugs:mechanical_frame"
                ),
                "mydrugs:gasifier",
                1
        );

        writer.shaped("distiller",
                new String[]{
                        "ABA",
                        "CDE",
                        " F "
                },
                key(
                        "A", "mydrugs:copper_plate",
                        "B", "mydrugs:condenser_coil",
                        "C", "mydrugs:glass_tube",
                        "D", "mydrugs:advanced_furnace",
                        "E", "minecraft:bucket",
                        "F", "mydrugs:mechanical_frame"
                ),
                "mydrugs:distiller",
                1
        );

        writer.shaped("centrifuge",
                new String[]{
                        "ABA",
                        "CDE",
                        "FGF"
                },
                key(
                        "A", "mydrugs:reinforced_casing",
                        "B", "mydrugs:electric_motor",
                        "C", "mydrugs:valve",
                        "D", "mydrugs:mechanical_frame",
                        "E", "mydrugs:valve",
                        "F", "mydrugs:tank_wall",
                        "G", "mydrugs:thick_glass"
                ),
                "mydrugs:centrifuge",
                1
        );

        writer.shaped("growth_chamber",
                new String[]{
                        "ABA",
                        "CDE",
                        "FGF"
                },
                key(
                        "A", "mydrugs:soft_seal",
                        "B", "mydrugs:thick_glass",
                        "C", "minecraft:glowstone_dust",
                        "D", "mydrugs:mechanical_frame",
                        "E", "mydrugs:watering_connection",
                        "F", "mydrugs:treated_planks",
                        "G", "minecraft:bucket"
                ),
                "mydrugs:growth_chamber",
                1
        );

        writer.shaped("chemical_reactor",
                new String[]{
                        "ABA",
                        "CDE",
                        "AFA"
                },
                key(
                        "A", "mydrugs:valve",
                        "B", "mydrugs:thick_glass",
                        "C", "mydrugs:glass_tube",
                        "D", "mydrugs:reaction_core",
                        "E", "mydrugs:heating_coil",
                        "F", "mydrugs:pressure_casing"
                ),
                "mydrugs:chemical_reactor",
                1
        );

        writer.shaped("biochemical_reactor",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "mydrugs:injector_nozzle",
                        "B", "mydrugs:control_circuit",
                        "C", "mydrugs:pressure_seal",
                        "D", "mydrugs:growth_chamber",
                        "E", "mydrugs:soft_seal",
                        "F", "mydrugs:chemical_reactor"
                ),
                "mydrugs:biochemical_reactor",
                1
        );

        // ---------------------------------------------------------------------
        // Fluid, gas, pipes
        // ---------------------------------------------------------------------

        writer.shaped("gas_tank",
                new String[]{
                        "ABA",
                        "CDC",
                        " E "
                },
                key(
                        "A", "mydrugs:tank_wall",
                        "B", "mydrugs:thick_glass",
                        "C", "mydrugs:pressure_seal",
                        "D", "mydrugs:mechanical_frame",
                        "E", "mydrugs:valve"
                ),
                "mydrugs:gas_tank",
                1
        );

        writer.shaped("gas_pump",
                new String[]{
                        " A ",
                        "BCB",
                        "DED"
                },
                key(
                        "A", "mydrugs:pump_head",
                        "B", "mydrugs:valve",
                        "C", "mydrugs:iron_axle",
                        "D", "mydrugs:copper_tube",
                        "E", "mydrugs:mechanical_frame"
                ),
                "mydrugs:gas_pump",
                1
        );

        writer.shaped("fluid_pump",
                new String[]{
                        " A ",
                        "BCB",
                        "DED"
                },
                key(
                        "A", "mydrugs:pump_head",
                        "B", "mydrugs:fluid_filter",
                        "C", "mydrugs:electric_motor",
                        "D", "mydrugs:copper_tube",
                        "E", "mydrugs:mechanical_frame"
                ),
                "mydrugs:fluid_pump",
                1
        );

        writer.shaped("basic_item_pipe",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:pipe_joint",
                        "B", "mydrugs:copper_tube",
                        "C", "minecraft:chest",
                        "D", "minecraft:hopper"
                ),
                "mydrugs:basic_item_pipe",
                8
        );

        writer.shaped("fast_item_pipe",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "minecraft:redstone",
                        "B", "mydrugs:steel_plate",
                        "C", "mydrugs:basic_item_pipe",
                        "D", "mydrugs:electric_motor"
                ),
                "mydrugs:fast_item_pipe",
                4
        );

        writer.shaped("basic_fluid_pipe",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:pipe_joint",
                        "B", "mydrugs:copper_tube",
                        "C", "mydrugs:thick_glass",
                        "D", "minecraft:bucket"
                ),
                "mydrugs:basic_fluid_pipe",
                8
        );

        writer.shaped("fast_fluid_pipe",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "minecraft:redstone",
                        "B", "mydrugs:steel_plate",
                        "C", "mydrugs:basic_fluid_pipe",
                        "D", "mydrugs:electric_motor"
                ),
                "mydrugs:fast_fluid_pipe",
                4
        );

        writer.shaped("basic_gas_pipe",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "mydrugs:pressure_seal",
                        "B", "mydrugs:copper_tube",
                        "C", "mydrugs:thick_glass",
                        "D", "mydrugs:pipe_joint"
                ),
                "mydrugs:basic_gas_pipe",
                8
        );

        writer.shaped("fast_gas_pipe",
                new String[]{
                        "ABA",
                        "CDC",
                        "ABA"
                },
                key(
                        "A", "minecraft:redstone",
                        "B", "mydrugs:steel_plate",
                        "C", "mydrugs:basic_gas_pipe",
                        "D", "mydrugs:electric_motor"
                ),
                "mydrugs:fast_gas_pipe",
                4
        );

        // ---------------------------------------------------------------------
        // Late industrial machines
        // ---------------------------------------------------------------------

        writer.shaped("electrolyzer",
                new String[]{
                        "ABA",
                        "CDE",
                        "FGF"
                },
                key(
                        "A", "mydrugs:electrode_pair",
                        "B", "mydrugs:advanced_control_circuit",
                        "C", "mydrugs:fluid_pump",
                        "D", "mydrugs:tank_wall",
                        "E", "mydrugs:valve",
                        "F", "mydrugs:reinforced_casing",
                        "G", "mydrugs:electric_motor"
                ),
                "mydrugs:electrolyzer",
                1
        );

        writer.shaped("btx_fractionation_tower",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "mydrugs:packed_column",
                        "B", "mydrugs:condenser_coil",
                        "C", "mydrugs:tank_wall",
                        "D", "mydrugs:distiller",
                        "E", "mydrugs:valve",
                        "F", "mydrugs:gas_pump"
                ),
                "mydrugs:btx_fractionation_tower",
                1
        );

        writer.shaped("aromatic_extractor",
                new String[]{
                        "ABA",
                        "CDE",
                        "FGF"
                },
                key(
                        "A", "mydrugs:fluid_filter",
                        "B", "mydrugs:condenser_coil",
                        "C", "mydrugs:fluid_pump",
                        "D", "mydrugs:distiller",
                        "E", "mydrugs:chemical_reactor",
                        "F", "mydrugs:tank_wall",
                        "G", "mydrugs:advanced_control_circuit"
                ),
                "mydrugs:aromatic_extractor",
                1
        );

        writer.shaped("catalytic_reformer",
                new String[]{
                        "ABA",
                        "CDE",
                        "FGF"
                },
                key(
                        "A", "mydrugs:pressure_casing",
                        "B", "mydrugs:catalyst_bed",
                        "C", "mydrugs:gas_pump",
                        "D", "mydrugs:chemical_reactor",
                        "E", "mydrugs:heating_coil",
                        "F", "mydrugs:reinforced_casing",
                        "G", "mydrugs:advanced_control_circuit"
                ),
                "mydrugs:catalytic_reformer",
                1
        );

        writer.shaped("steam_cracker",
                new String[]{
                        "ABA",
                        "CDE",
                        "FGF"
                },
                key(
                        "A", "mydrugs:heating_coil",
                        "B", "mydrugs:pressure_casing",
                        "C", "mydrugs:gas_pump",
                        "D", "mydrugs:gasifier",
                        "E", "mydrugs:condenser_coil",
                        "F", "mydrugs:reinforced_casing",
                        "G", "mydrugs:advanced_control_circuit"
                ),
                "mydrugs:steam_cracker",
                1
        );

        // ---------------------------------------------------------------------
        // Utility / narrative / special blocks and items
        // ---------------------------------------------------------------------

        writer.shaped("watering_connection",
                new String[]{
                        " A ",
                        " B ",
                        " C "
                },
                key(
                        "A", "minecraft:copper_ingot",
                        "B", "minecraft:iron_ingot",
                        "C", "minecraft:bucket"
                ),
                "mydrugs:watering_connection",
                1
        );

        writer.shaped("therapist_desk",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "#minecraft:planks",
                        "B", "minecraft:book",
                        "C", "mydrugs:treated_planks",
                        "D", "minecraft:lectern",
                        "E", "minecraft:stick",
                        "F", "mydrugs:personal_diary"
                ),
                "mydrugs:therapist_desk",
                1
        );

        writer.shaped("recovery_anchor",
                new String[]{
                        "ABA",
                        "CDC",
                        "AEA"
                },
                key(
                        "A", "minecraft:crying_obsidian",
                        "B", "minecraft:amethyst_block",
                        "C", "minecraft:gold_ingot",
                        "D", "mydrugs:personal_diary",
                        "E", "minecraft:echo_shard"
                ),
                "mydrugs:recovery_anchor",
                1
        );

        writer.shaped("psychotrope_distillery",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "mydrugs:copper_tube",
                        "B", "minecraft:glass",
                        "C", "mydrugs:distillation_coil",
                        "D", "mydrugs:reinforced_casing",
                        "E", "minecraft:iron_ingot",
                        "F", "minecraft:furnace"
                ),
                "mydrugs:psychotrope_distillery",
                1
        );

        writer.shaped("distillate_engine",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "mydrugs:copper_tube",
                        "B", "mydrugs:strain_vent",
                        "C", "mydrugs:current_regulator",
                        "D", "mydrugs:reinforced_casing",
                        "E", "minecraft:redstone",
                        "F", "mydrugs:reaction_core"
                ),
                "mydrugs:distillate_engine",
                1
        );

        writer.shaped("psychotrope_resonator",
                new String[]{
                        "ABA",
                        "CDC",
                        "EFE"
                },
                key(
                        "A", "minecraft:amethyst_shard",
                        "B", "mydrugs:dream_residue",
                        "C", "mydrugs:resonance_lens",
                        "D", "mydrugs:psy_receptacle",
                        "E", "mydrugs:recovery_anchor",
                        "F", "mydrugs:personal_diary"
                ),
                "mydrugs:psychotrope_resonator",
                1
        );

        writer.shaped("personal_diary",
                new String[]{
                        " A ",
                        "BCB",
                        " B "
                },
                key(
                        "A", "minecraft:ink_sac",
                        "B", "minecraft:paper",
                        "C", "minecraft:book"
                ),
                "mydrugs:personal_diary",
                1
        );

        writer.shaped("headphones",
                new String[]{
                        "B B",
                        "ACA",
                        "D D"
                },
                key(
                        "A", "minecraft:string",
                        "B", "minecraft:iron_ingot",
                        "C", "minecraft:jukebox",
                        "D", "minecraft:redstone"
                ),
                "mydrugs:headphones",
                1
        );

        writer.shaped("roller",
                new String[]{
                        " A ",
                        " B ",
                        " C "
                },
                key(
                        "A", "minecraft:coal",
                        "B", "minecraft:paper",
                        "C", "minecraft:iron_ingot"
                ),
                "mydrugs:roller",
                1
        );

        writer.shaped("bang",
                new String[]{
                        "  A",
                        " B ",
                        "AA "
                },
                key(
                        "A", "minecraft:glass",
                        "B", "mydrugs:glass_tube"
                ),
                "mydrugs:bang",
                1
        );

        writer.shapeless("hash_piece",
                new Object[]{
                        "mydrugs:hash_brick"
                },
                "mydrugs:hash_piece",
                16
        );

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "MyDrugs Vanilla Recipe Snapshots";
    }
}
