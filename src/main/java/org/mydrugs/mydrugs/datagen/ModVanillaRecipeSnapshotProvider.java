package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModVanillaRecipeSnapshotProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;

    public ModVanillaRecipeSnapshotProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

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

        shaped(futures, cachedOutput, "salt_block",
                new String[]{
                        "AAA",
                        "AAA",
                        "AAA"
                },
                key(
                        "A", "mydrugs:salt"
                ),
                "mydrugs:salt_block",
                1
        );

        shapeless(futures, cachedOutput, "salt_from_salt_block",
                new Object[]{
                        "mydrugs:salt_block"
                },
                "mydrugs:salt",
                9
        );

        shaped(futures, cachedOutput, "raw_platinum_block",
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

        shapeless(futures, cachedOutput, "raw_platinum_from_raw_platinum_block",
                new Object[]{
                        "mydrugs:raw_platinum_block"
                },
                "mydrugs:raw_platinum",
                9
        );

        smelting(futures, cachedOutput, "platinum_ingot_from_raw_platinum",
                "mydrugs:raw_platinum",
                "mydrugs:platinum_ingot",
                0.7F,
                200
        );

        blasting(futures, cachedOutput, "platinum_ingot_from_blasting_raw_platinum",
                "mydrugs:raw_platinum",
                "mydrugs:platinum_ingot",
                0.7F,
                100
        );

        smelting(futures, cachedOutput, "sulfur_from_sulfur_ore",
                "mydrugs:sulfur_ore",
                "mydrugs:sulfur",
                0.5F,
                200
        );

        blasting(futures, cachedOutput, "sulfur_from_blasting_sulfur_ore",
                "mydrugs:sulfur_ore",
                "mydrugs:sulfur",
                0.5F,
                100
        );

        smelting(futures, cachedOutput, "sulfur_from_deepslate_sulfur_ore",
                "mydrugs:deepslate_sulfur_ore",
                "mydrugs:sulfur",
                0.7F,
                200
        );

        blasting(futures, cachedOutput, "sulfur_from_blasting_deepslate_sulfur_ore",
                "mydrugs:deepslate_sulfur_ore",
                "mydrugs:sulfur",
                0.7F,
                100
        );

        smelting(futures, cachedOutput, "refractory_brick",
                "mydrugs:refractory_mix",
                "mydrugs:refractory_brick",
                0.7F,
                200
        );

        blasting(futures, cachedOutput, "refractory_brick_blasting",
                "mydrugs:refractory_mix",
                "mydrugs:refractory_brick",
                0.7F,
                100
        );

        // ---------------------------------------------------------------------
        // Basic wood, clay, glass, and hand components
        // ---------------------------------------------------------------------

        shapeless(futures, cachedOutput, "treated_planks",
                new Object[]{
                        "#minecraft:planks",
                        "mydrugs:resin"
                },
                "mydrugs:treated_planks",
                4
        );

        shaped(futures, cachedOutput, "wooden_frame",
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

        shaped(futures, cachedOutput, "cupboard_piece",
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

        shaped(futures, cachedOutput, "clay_vat",
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

        shapeless(futures, cachedOutput, "cup",
                new Object[]{
                        "minecraft:brick"
                },
                "mydrugs:cup",
                1
        );

        shapeless(futures, cachedOutput, "porous_clay",
                new Object[]{
                        "minecraft:clay_ball",
                        "minecraft:sand",
                        "minecraft:gravel"
                },
                "mydrugs:porous_clay",
                2
        );

        smelting(futures, cachedOutput, "porous_ceramic_from_porous_clay",
                "mydrugs:porous_clay",
                "mydrugs:porous_ceramic",
                0.1F,
                200
        );

        shapeless(futures, cachedOutput, "raw_thick_glass",
                new Object[]{
                        "minecraft:glass",
                        "minecraft:quartz",
                        "minecraft:quartz"
                },
                "mydrugs:raw_thick_glass",
                2
        );

        smelting(futures, cachedOutput, "thick_glass_from_raw_thick_glass",
                "mydrugs:raw_thick_glass",
                "mydrugs:thick_glass",
                0.1F,
                200
        );

        shaped(futures, cachedOutput, "glass_tube",
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

        shaped(futures, cachedOutput, "copper_tube",
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

        shapeless(futures, cachedOutput, "glass_bottle",
                new Object[]{
                        "minecraft:glass_bottle"
                },
                "mydrugs:glass_bottle",
                1
        );

        shapeless(futures, cachedOutput, "progression_guide",
                new Object[]{
                        "minecraft:book",
                        "#mydrugs:progression_guide_seed_sources"
                },
                "mydrugs:progression_guide",
                1
        );

        shaped(futures, cachedOutput, "grinding_bowl",
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

        shaped(futures, cachedOutput, "grinding_tool",
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

        shaped(futures, cachedOutput, "stone_hammer",
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

        shaped(futures, cachedOutput, "syringe",
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

        shaped(futures, cachedOutput, "iron_hammer",
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

        shaped(futures, cachedOutput, "steel_hammer",
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

        shaped(futures, cachedOutput, "psy_anvil",
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

        shaped(futures, cachedOutput, "copper_strapping",
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

        shaped(futures, cachedOutput, "iron_axle",
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

        shaped(futures, cachedOutput, "hand_crank",
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

        shaped(futures, cachedOutput, "agitator",
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

        shaped(futures, cachedOutput, "stomp_plate",
                new String[]{
                        "AAA"
                },
                key(
                        "A", "mydrugs:heavy_iron"
                ),
                "mydrugs:stomp_plate",
                1
        );

        shaped(futures, cachedOutput, "stomp_plate_block",
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

        shaped(futures, cachedOutput, "mechanical_frame",
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

        shaped(futures, cachedOutput, "reinforced_casing",
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

        shaped(futures, cachedOutput, "stomp_crafter",
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

        shaped(futures, cachedOutput, "soft_seal",
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

        shaped(futures, cachedOutput, "tight_seal",
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

        shaped(futures, cachedOutput, "pressure_seal",
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

        shaped(futures, cachedOutput, "membrane",
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

        shapeless(futures, cachedOutput, "cigaret_filter",
                new Object[]{
                        "mydrugs:cupboard_piece"
                },
                "mydrugs:cigaret_filter",
                4
        );

        shapeless(futures, cachedOutput, "opium_poppy_seeds",
                new Object[]{
                        "minecraft:poppy"
                },
                "mydrugs:opium_poppy_seeds",
                1
        );

        shaped(futures, cachedOutput, "fluid_filter",
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

        shaped(futures, cachedOutput, "filter_box",
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

        shaped(futures, cachedOutput, "valve",
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

        shaped(futures, cachedOutput, "pump_head",
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

        shaped(futures, cachedOutput, "injector_nozzle",
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

        shaped(futures, cachedOutput, "tank_wall",
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

        shaped(futures, cachedOutput, "pressure_casing",
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

        shaped(futures, cachedOutput, "reaction_core",
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


        shaped(futures, cachedOutput, "control_circuit",
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

        shaped(futures, cachedOutput, "electric_motor",
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

        shaped(futures, cachedOutput, "heating_coil",
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

        shaped(futures, cachedOutput, "condenser_coil",
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

        shaped(futures, cachedOutput, "electrode_pair",
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

        shaped(futures, cachedOutput, "catalyst_bed",
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

        shaped(futures, cachedOutput, "packed_column",
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

        shaped(futures, cachedOutput, "pipe_joint",
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

        shaped(futures, cachedOutput, "pipe_wrench",
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

        shaped(futures, cachedOutput, "pipe_filter_upgrade",
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

        shaped(futures, cachedOutput, "machine_transfer_upgrade",
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

        shaped(futures, cachedOutput, "psychotrope_lens",
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

        shaped(futures, cachedOutput, "mixing_vat",
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

        shaped(futures, cachedOutput, "sieve",
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

        shaped(futures, cachedOutput, "evaporation_tray",
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

        shaped(futures, cachedOutput, "drying_rack",
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

        shaped(futures, cachedOutput, "fluid_filterer",
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

        shaped(futures, cachedOutput, "heat_lining",
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

        shaped(futures, cachedOutput, "advanced_furnace",
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

        shaped(futures, cachedOutput, "advanced_mixing_vat",
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

        shaped(futures, cachedOutput, "gasifier",
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

        shaped(futures, cachedOutput, "distiller",
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

        shaped(futures, cachedOutput, "centrifuge",
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

        shaped(futures, cachedOutput, "growth_chamber",
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

        shaped(futures, cachedOutput, "chemical_reactor",
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

        shaped(futures, cachedOutput, "biochemical_reactor",
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

        shaped(futures, cachedOutput, "gas_tank",
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

        shaped(futures, cachedOutput, "gas_pump",
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

        shaped(futures, cachedOutput, "fluid_pump",
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

        shaped(futures, cachedOutput, "basic_item_pipe",
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

        shaped(futures, cachedOutput, "fast_item_pipe",
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

        shaped(futures, cachedOutput, "basic_fluid_pipe",
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

        shaped(futures, cachedOutput, "fast_fluid_pipe",
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

        shaped(futures, cachedOutput, "basic_gas_pipe",
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

        shaped(futures, cachedOutput, "fast_gas_pipe",
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

        shaped(futures, cachedOutput, "electrolyzer",
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

        shaped(futures, cachedOutput, "btx_fractionation_tower",
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

        shaped(futures, cachedOutput, "aromatic_extractor",
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

        shaped(futures, cachedOutput, "catalytic_reformer",
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

        shaped(futures, cachedOutput, "steam_cracker",
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

        shaped(futures, cachedOutput, "watering_connection",
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

        shaped(futures, cachedOutput, "therapist_desk",
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

        shaped(futures, cachedOutput, "recovery_anchor",
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

        shaped(futures, cachedOutput, "psychotrope_component",
                new String[]{
                        "ABA",
                        "CDC",
                        "AEA"
                },
                key(
                        "A", "minecraft:amethyst_shard",
                        "B", "mydrugs:psychotrope_lens",
                        "C", "mydrugs:advanced_control_circuit",
                        "D", "mydrugs:reaction_core",
                        "E", "minecraft:ender_pearl"
                ),
                "mydrugs:psychotrope_component",
                1
        );

        shaped(futures, cachedOutput, "psychotrope_core",
                new String[]{
                        "ABA",
                        "CDC",
                        "AEA"
                },
                key(
                        "A", "mydrugs:psychotrope_component",
                        "B", "minecraft:nether_star",
                        "C", "minecraft:amethyst_block",
                        "D", "mydrugs:recovery_anchor",
                        "E", "mydrugs:advanced_control_circuit"
                ),
                "mydrugs:psychotrope_core",
                1
        );

        shaped(futures, cachedOutput, "personal_diary",
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

        shaped(futures, cachedOutput, "headphones",
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

        shaped(futures, cachedOutput, "roller",
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

        shaped(futures, cachedOutput, "bang",
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

        shapeless(futures, cachedOutput, "hash_piece",
                new Object[]{
                        "mydrugs:hash_brick"
                },
                "mydrugs:hash_piece",
                16
        );

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void shaped(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            String[] pattern,
            Map<String, Object> key,
            String result,
            int count
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shaped");
        json.addProperty("category", "misc");

        JsonArray patternArray = new JsonArray();
        for (String row : pattern) {
            patternArray.add(new JsonPrimitive(row));
        }
        json.add("pattern", patternArray);

        JsonObject keyObject = new JsonObject();
        for (Map.Entry<String, Object> entry : key.entrySet()) {
            keyObject.add(entry.getKey(), ingredient(entry.getValue()));
        }
        json.add("key", keyObject);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        resultObject.addProperty("count", count);
        json.add("result", resultObject);

        saveRecipe(futures, cachedOutput, name, json);
    }

    private void shapeless(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            Object[] ingredients,
            String result,
            int count
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "minecraft:crafting_shapeless");
        json.addProperty("category", "misc");

        JsonArray ingredientsArray = new JsonArray();
        for (Object value : ingredients) {
            ingredientsArray.add(ingredient(value));
        }
        json.add("ingredients", ingredientsArray);

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        resultObject.addProperty("count", count);
        json.add("result", resultObject);

        saveRecipe(futures, cachedOutput, name, json);
    }

    private void smelting(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            String ingredient,
            String result,
            float experience,
            int cookingTime
    ) {
        cooking(futures, cachedOutput, name, "minecraft:smelting", ingredient, result, experience, cookingTime);
    }

    private void blasting(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            String ingredient,
            String result,
            float experience,
            int cookingTime
    ) {
        cooking(futures, cachedOutput, name, "minecraft:blasting", ingredient, result, experience, cookingTime);
    }

    private void cooking(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            String type,
            String ingredient,
            String result,
            float experience,
            int cookingTime
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", type);
        json.addProperty("category", "misc");
        json.add("ingredient", ingredient(ingredient));

        JsonObject resultObject = new JsonObject();
        resultObject.addProperty("id", result);
        json.add("result", resultObject);

        json.addProperty("experience", experience);
        json.addProperty("cookingtime", cookingTime);

        saveRecipe(futures, cachedOutput, name, json);
    }

    private void saveRecipe(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            JsonObject json
    ) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, json, path));
    }

    private static JsonElement ingredient(Object value) {
        if (value instanceof String string) {
            return new JsonPrimitive(string);
        }

        if (value instanceof String[] alternatives) {
            JsonArray array = new JsonArray();
            for (String alternative : alternatives) {
                array.add(new JsonPrimitive(alternative));
            }
            return array;
        }

        throw new IllegalArgumentException("Unsupported recipe ingredient value: " + value);
    }

    private static Map<String, Object> key(Object... values) {
        if (values.length % 2 != 0) {
            throw new IllegalArgumentException("Recipe key entries must be provided as key/value pairs.");
        }

        Map<String, Object> map = new LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            if (!(values[i] instanceof String key)) {
                throw new IllegalArgumentException("Recipe key must be a String. Got: " + values[i]);
            }

            map.put(key, values[i + 1]);
        }

        return map;
    }

    private static String[] alt(String... alternatives) {
        return alternatives;
    }

    @Override
    public String getName() {
        return "MyDrugs Vanilla Recipe Snapshots";
    }
}
