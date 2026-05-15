package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModSimpleClientItemProvider implements DataProvider {
    private final PackOutput.PathProvider itemClientPathProvider;
    private final PackOutput.PathProvider itemModelPathProvider;

    public ModSimpleClientItemProvider(PackOutput output) {
        this.itemClientPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
        this.itemModelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models/item");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (String itemName : List.of(
                "activated_coal",
                "advanced_control_circuit",
                "aluminium_ingot",
                "agitator",
                "automation_upgrade",
                "bang",
                "basic_fluid_pipe",
                "basic_gas_pipe",
                "basic_item_pipe",
                "calming_mixture",
                "cannabis_leaf",
                "cannabis_powder",
                "cannabis_resin",
                "cannabis_seeds",
                "catalyst_bed",
                "cigaret",
                "cigaret_filter",
                "coal_dust",
                "coca_leaf",
                "coca_paste",
                "coca_seeds",
                "cocaine_powder",
                "cocaine_plate",
                "cocaine_shard",
                "condenser_coil",
                "control_circuit",
                "copper_plate",
                "copper_strapping",
                "copper_tube",
                "crack_plate",
                "crack_shard",
                "cup",
                "cupboard_piece",
                "cured_cannabis_leaf",
                "dried_cannabis_leaf",
                "dried_coca_leaf",
                "dried_tobacco_leaf",
                "electric_motor",
                "electrode_pair",
                "energy_upgrade",
                "ergot",
                "ergotamine",
                "fast_fluid_pipe",
                "fast_gas_pipe",
                "fast_item_pipe",
                "filter_box",
                "filter_upgrade",
                "flour",
                "fluid_filter",
                "full_ammoniac_bottle",
                "fungal_culture",
                "fungal_fiber",
                "glass_tube",
                "grinding_tool",
                "hand_crank",
                "hash_brick",
                "hash_piece",
                "headphones",
                "heating_coil",
                "heat_lining",
                "heavy_iron",
                "heavy_iron_plate",
                "herbal_tea",
                "infected_rye",
                "injector_nozzle",
                "insulated_wire",
                "iron_axle",
                "iron_hammer",
                "iron_mesh",
                "joint",
                "lsd_bottle",
                "lsd_drop",
                "machine_transfer_upgrade",
                "magic_mushroom_powder",
                "malt",
                "malt_powder",
                "malt_seeds",
                "membrane",
                "meth_powder",
                "meth_shard",
                "mixing_spatula",
                "mycelial_resonator",
                "packed_column",
                "personal_diary",
                "pipe_filter_upgrade",
                "pipe_joint",
                "pipe_wrench",
                "plant_biomass",
                "plant_waste",
                "platinum_ingot",
                "overdose_antidote",
                "opium_poppy_seeds",
                "porous_ceramic",
                "porous_clay",
                "portable_grinder",
                "pressure_casing",
                "pressure_seal",
                "psy_blueprint",
                "psy_receptacle",
                "psychotrope_lens",
                "pump_head",
                "raw_aluminium",
                "raw_platinum",
                "raw_rubber",
                "raw_thick_glass",
                "reaction_core",
                "refractory_brick",
                "refractory_mix",
                "reinforced_casing",
                "resin",
                "roller",
                "rotor",
                "rubber",
                "rye",
                "rye_seeds",
                "sleeping_aid",
                "soft_seal",
                "steel_blend",
                "steel_hammer",
                "steel_ingot",
                "steel_plate",
                "stone_hammer",
                "sulfur_powder",
                "tank_wall",
                "thick_glass",
                "tight_seal",
                "tobacco_handful",
                "tobacco_leaf",
                "tobacco_seeds",
                "transfer_upgrade",
                "tryptophan",
                "valve",
                "watering_connection",
                "wooden_frame",
                "wrench"
        )) {
            futures.add(saveFlatItem(cachedOutput, itemName, "mydrugs:item/" + itemName));
        }
        futures.add(saveFlatItem(cachedOutput, "aloe_vera", "minecraft:item/cactus"));
        futures.add(saveFlatItem(cachedOutput, "aloe_vera_seeds", "minecraft:item/wheat_seeds"));
        futures.add(saveFlatItem(cachedOutput, "soothing_tobacco_blend", "mydrugs:item/tobacco_handful"));
        futures.add(saveFlatItem(cachedOutput, "inner_demon_remains", "minecraft:item/ghast_tear"));
        futures.add(saveFlatItem(cachedOutput, "progression_guide", "minecraft:item/written_book"));
        futures.add(saveFlatItem(cachedOutput, "adn_scraper", "minecraft:item/shears"));
        futures.add(saveFlatItem(cachedOutput, "adn_scrap", "minecraft:item/paper"));
        futures.add(saveFlatItem(cachedOutput, "adn_gene", "minecraft:item/amethyst_shard"));
        futures.add(saveFlatItem(cachedOutput, "mutation_vector", "minecraft:item/glass_bottle"));
        futures.add(saveFlatItem(cachedOutput, "mutagenic_blood_vial", "minecraft:item/honey_bottle"));
        futures.add(saveFlatItem(cachedOutput, "nutrient_gel", "minecraft:item/slime_ball"));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private CompletableFuture<?> saveFlatItem(CachedOutput cachedOutput, String itemName, String texturePath) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, itemName);

        JsonObject clientItemRoot = new JsonObject();
        JsonObject clientItemModel = new JsonObject();
        clientItemModel.addProperty("type", "minecraft:model");
        clientItemModel.addProperty("model", MyDrugs.MODID + ":item/" + itemName);
        clientItemRoot.add("model", clientItemModel);

        Path clientItemPath = this.itemClientPathProvider.json(id);
        CompletableFuture<?> clientItemFuture = DataProvider.saveStable(cachedOutput, clientItemRoot, clientItemPath);

        JsonObject itemModelRoot = new JsonObject();
        JsonObject textures = new JsonObject();
        itemModelRoot.addProperty("parent", "minecraft:item/generated");
        textures.addProperty("layer0", texturePath);
        itemModelRoot.add("textures", textures);

        Path itemModelPath = this.itemModelPathProvider.json(id);
        CompletableFuture<?> itemModelFuture = DataProvider.saveStable(cachedOutput, itemModelRoot, itemModelPath);

        return CompletableFuture.allOf(clientItemFuture, itemModelFuture);
    }

    @Override
    public String getName() {
        return "MyDrugs Simple Client Items";
    }
}
