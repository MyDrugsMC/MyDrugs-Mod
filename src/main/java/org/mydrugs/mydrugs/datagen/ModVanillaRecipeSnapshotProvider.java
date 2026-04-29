package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonParser;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModVanillaRecipeSnapshotProvider implements DataProvider {
    private final PackOutput.PathProvider recipePathProvider;

    public ModVanillaRecipeSnapshotProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        saveRawRecipe(futures, cachedOutput, "advanced_furnace", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    "AFA"
  ],
  "key": {
    "A": "mydrugs:heavy_iron",
    "B": "mydrugs:thick_glass",
    "C": "mydrugs:copper_plate",
    "D": "minecraft:furnace",
    "E": "mydrugs:copper_plate",
    "F": "mydrugs:mechanical_frame"
  },
  "result": {
    "id": "mydrugs:advanced_furnace",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "advanced_mixing_vat", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    " A "
  ],
  "key": {
    "A": "mydrugs:tight_seal",
    "B": "mydrugs:reinforced_casing",
    "C": "mydrugs:heat_lining",
    "D": "mydrugs:mixing_vat",
    "E": "mydrugs:agitator"
  },
  "result": {
    "id": "mydrugs:advanced_mixing_vat",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "agitator", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB"
  ],
  "key": {
    "A": "mydrugs:iron_axle",
    "B": "mydrugs:steel_plate",
    "C": "mydrugs:copper_plate"
  },
  "result": {
    "id": "mydrugs:agitator",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "bang", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "  A",
    " A ",
    "AA "
  ],
  "key": {
    "A": "minecraft:glass"
  },
  "result": {
    "id": "mydrugs:bang",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "biochemical_reactor", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDC",
    " E "
  ],
  "key": {
    "A": "mydrugs:injector_nozzle",
    "B": "mydrugs:pressure_casing",
    "C": "mydrugs:pressure_seal",
    "D": "mydrugs:growth_chamber",
    "E": "mydrugs:agitator"
  },
  "result": {
    "id": "mydrugs:biochemical_reactor",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "centrifuge", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    "FGF"
  ],
  "key": {
    "A": "mydrugs:reinforced_casing",
    "B": "mydrugs:hand_crank",
    "C": "mydrugs:valve",
    "D": "mydrugs:mechanical_frame",
    "E": "mydrugs:valve",
    "F": "mydrugs:tank_wall",
    "G": "mydrugs:thick_glass"
  },
  "result": {
    "id": "mydrugs:centrifuge",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "chemical_reactor", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    " A "
  ],
  "key": {
    "A": "mydrugs:valve",
    "B": "mydrugs:thick_glass",
    "C": "mydrugs:glass_tube",
    "D": "mydrugs:reaction_core",
    "E": "mydrugs:heat_lining"
  },
  "result": {
    "id": "mydrugs:chemical_reactor",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "clay_vat", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A A",
    "A A",
    "AAA"
  ],
  "key": {
    "A": "minecraft:clay_ball"
  },
  "result": {
    "id": "mydrugs:clay_vat",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "copper_strapping", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    "A A",
    "AAA"
  ],
  "key": {
    "A": "mydrugs:copper_plate"
  },
  "result": {
    "id": "mydrugs:copper_strapping",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "cupboard_piece", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AA",
    "AA"
  ],
  "key": {
    "A": "minecraft:sugar_cane"
  },
  "result": {
    "id": "mydrugs:cupboard_piece",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "distiller", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    " F "
  ],
  "key": {
    "A": "mydrugs:copper_plate",
    "B": "mydrugs:glass_tube",
    "C": "mydrugs:glass_tube",
    "D": "mydrugs:advanced_furnace",
    "E": "minecraft:bucket",
    "F": "mydrugs:mechanical_frame"
  },
  "result": {
    "id": "mydrugs:distiller",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "electrolyzer", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    "FGF"
  ],
  "key": {
    "A": "mydrugs:reinforced_casing",
    "B": "mydrugs:hand_crank",
    "C": "mydrugs:valve",
    "D": "mydrugs:mechanical_frame",
    "E": "mydrugs:valve",
    "F": "mydrugs:tank_wall",
    "G": "mydrugs:thick_glass"
  },
  "result": {
    "id": "mydrugs:electrolyzer",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "evaporation_tray", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A A",
    "BBB"
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "B": "minecraft:brick"
  },
  "result": {
    "id": "mydrugs:evaporation_tray",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "filter", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A"
  ],
  "key": {
    "A": "mydrugs:cupboard_piece"
  },
  "result": {
    "id": "mydrugs:filter",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "filter_box", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    "ABA",
    "AAA"
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "B": "minecraft:glass"
  },
  "result": {
    "id": "mydrugs:filter_box",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "fluid_filter", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB",
    " D "
  ],
  "key": {
    "A": "mydrugs:porous_ceramic",
    "B": "minecraft:paper",
    "C": "mydrugs:activated_coal",
    "D": "minecraft:string"
  },
  "result": {
    "id": "mydrugs:fluid_filter",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "fluid_filterer", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDC",
    " E "
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "B": "minecraft:glass",
    "C": "mydrugs:glass_tube",
    "D": "mydrugs:filter_box",
    "E": "mydrugs:porous_ceramic"
  },
  "result": {
    "id": "mydrugs:fluid_filterer",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "gas_pump", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB",
    "DED"
  ],
  "key": {
    "A": "mydrugs:pump_head",
    "B": "mydrugs:valve",
    "C": "mydrugs:iron_axle",
    "D": "mydrugs:copper_tube",
    "E": "mydrugs:mechanical_frame"
  },
  "result": {
    "id": "mydrugs:gas_pump",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "gas_tank", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDC",
    " E "
  ],
  "key": {
    "A": "mydrugs:tank_wall",
    "B": "mydrugs:thick_glass",
    "C": "mydrugs:pressure_seal",
    "D": "mydrugs:mechanical_frame",
    "E": "mydrugs:valve"
  },
  "result": {
    "id": "mydrugs:gas_tank",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "gasifier", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE"
  ],
  "key": {
    "A": "mydrugs:heat_lining",
    "B": "mydrugs:pressure_casing",
    "C": "mydrugs:valve",
    "D": "mydrugs:advanced_furnace",
    "E": "mydrugs:iron_axle"
  },
  "result": {
    "id": "mydrugs:gasifier",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "glass_bottle", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A"
  ],
  "key": {
    "A": "minecraft:glass_bottle"
  },
  "result": {
    "id": "mydrugs:glass_bottle",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "glass_tube", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A A",
    " A ",
    "A A"
  ],
  "key": {
    "A": "minecraft:glass"
  },
  "result": {
    "id": "mydrugs:glass_tube",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "grinding_bowl", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A A",
    "AAA"
  ],
  "key": {
    "A": "minecraft:brick"
  },
  "result": {
    "id": "mydrugs:grinding_bowl",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "grinding_tool", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A",
    "B "
  ],
  "key": {
    "A": "minecraft:stick",
    "B": "minecraft:stone"
  },
  "result": {
    "id": "mydrugs:grinding_tool",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "growth_chamber", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDE",
    " F "
  ],
  "key": {
    "A": "mydrugs:soft_seal",
    "B": "mydrugs:thick_glass",
    "C": "minecraft:glowstone_dust",
    "D": "mydrugs:mechanical_frame",
    "E": "mydrugs:watering_connection",
    "F": "minecraft:bucket"
  },
  "result": {
    "id": "mydrugs:growth_chamber",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "hand_crank", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "ASA",
    " A "
  ],
  "key": {
    "A": "minecraft:stick",
    "S": "mydrugs:iron_axle"
  },
  "result": {
    "id": "mydrugs:hand_crank",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "hash_piece", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A"
  ],
  "key": {
    "A": "mydrugs:hash_brick"
  },
  "result": {
    "id": "mydrugs:hash_piece",
    "count": 16
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "headphones", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "B B",
    "ACA"
  ],
  "key": {
    "A": "minecraft:string",
    "B": "minecraft:iron_ingot",
    "C": "minecraft:jukebox"
  },
  "result": {
    "id": "mydrugs:headphones",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "heat_lining", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    "A A",
    "AAA"
  ],
  "key": {
    "A": "mydrugs:refractory_brick"
  },
  "result": {
    "id": "mydrugs:heat_lining",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "injector_nozzle", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    " B "
  ],
  "key": {
    "A": "mydrugs:copper_tube",
    "B": "mydrugs:valve"
  },
  "result": {
    "id": "mydrugs:injector_nozzle",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "iron_axle", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    " B ",
    " A "
  ],
  "key": {
    "A": "mydrugs:heavy_iron",
    "B": "minecraft:iron_ingot"
  },
  "result": {
    "id": "mydrugs:iron_axle",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "iron_hammer", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    "ASA",
    "  S"
  ],
  "key": {
    "A": "minecraft:iron_ingot",
    "S": "minecraft:stick"
  },
  "result": {
    "id": "mydrugs:iron_hammer",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "mechanical_frame", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "CDC",
    "ABA"
  ],
  "key": {
    "A": "mydrugs:heavy_iron",
    "B": "#minecraft:planks",
    "C": "minecraft:stick",
    "D": "minecraft:iron_ingot"
  },
  "result": {
    "id": "mydrugs:mechanical_frame",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "membrane", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB"
  ],
  "key": {
    "A": "minecraft:leather",
    "B": "mydrugs:rubber",
    "C": "minecraft:string"
  },
  "result": {
    "id": "mydrugs:membrane",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "mixing_vat", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB",
    " D "
  ],
  "key": {
    "A": "minecraft:stick",
    "B": "mydrugs:copper_strapping",
    "C": "mydrugs:clay_vat",
    "D": "mydrugs:wooden_frame"
  },
  "result": {
    "id": "mydrugs:mixing_vat",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "personal_diary", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A"
  ],
  "key": {
    "A": "minecraft:book"
  },
  "result": {
    "id": "mydrugs:personal_diary",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "porous_ceramic_from_porous_clay", """
{
  "type": "minecraft:smelting",
  "category": "misc",
  "ingredient": "mydrugs:porous_clay",
  "result": {
    "id": "mydrugs:porous_ceramic"
  },
  "experience": 0.1,
  "cookingtime": 200
}
                """);
        saveRawRecipe(futures, cachedOutput, "porous_clay", """
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    "minecraft:clay_ball",
    "minecraft:sand"
  ],
  "result": {
    "id": "mydrugs:porous_clay",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "pressure_casing", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "C C",
    "ABA"
  ],
  "key": {
    "A": "mydrugs:pressure_seal",
    "B": "mydrugs:thick_glass",
    "C": "mydrugs:reinforced_casing"
  },
  "result": {
    "id": "mydrugs:pressure_casing",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "pressure_seal", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    " B "
  ],
  "key": {
    "A": "mydrugs:tight_seal",
    "B": "mydrugs:copper_strapping"
  },
  "result": {
    "id": "mydrugs:pressure_seal",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "pump_head", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB"
  ],
  "key": {
    "A": "mydrugs:valve",
    "B": "mydrugs:copper_tube",
    "C": "mydrugs:membrane"
  },
  "result": {
    "id": "mydrugs:pump_head",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "raw_thick_glass", """
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    "minecraft:glass",
    "minecraft:quartz"
  ],
  "result": {
    "id": "mydrugs:raw_thick_glass",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "reaction_core", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB"
  ],
  "key": {
    "A": "mydrugs:thick_glass",
    "B": "mydrugs:glass_tube",
    "C": "mydrugs:pressure_casing"
  },
  "result": {
    "id": "mydrugs:reaction_core",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "reinforced_casing", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "ABA",
    "B B",
    "ABA"
  ],
  "key": {
    "A": "mydrugs:steel_plate",
    "B": "mydrugs:heavy_iron"
  },
  "result": {
    "id": "mydrugs:reinforced_casing",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "roller", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A",
    "B",
    "C"
  ],
  "key": {
    "A": "minecraft:coal",
    "B": "minecraft:paper",
    "C": "minecraft:iron_ingot"
  },
  "result": {
    "id": "mydrugs:roller",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "sieve", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A A",
    "BCB",
    " D "
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:stick",
    "C": "mydrugs:filter",
    "D": "mydrugs:wooden_frame"
  },
  "result": {
    "id": "mydrugs:sieve",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "soft_seal", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AA",
    "AA"
  ],
  "key": {
    "A": "minecraft:leather"
  },
  "result": {
    "id": "mydrugs:soft_seal",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "steel_hammer", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    "ASA",
    "  S"
  ],
  "key": {
    "A": "mydrugs:steel_ingot",
    "S": "minecraft:stick"
  },
  "result": {
    "id": "mydrugs:steel_hammer",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "stomp_plate", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA"
  ],
  "key": {
    "A": "mydrugs:heavy_iron"
  },
  "result": {
    "id": "mydrugs:stomp_plate",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "stone_hammer", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    "ASA",
    "  S"
  ],
  "key": {
    "A": [
      "minecraft:cobblestone",
      "minecraft:stone"
    ],
    "S": "minecraft:stick"
  },
  "result": {
    "id": "mydrugs:stone_hammer",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "tank_wall", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AAA",
    " B ",
    "AAA"
  ],
  "key": {
    "A": "mydrugs:steel_plate",
    "B": "mydrugs:pressure_seal"
  },
  "result": {
    "id": "mydrugs:tank_wall",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "thick_glass_from_raw_thick_glass", """
{
  "type": "minecraft:smelting",
  "category": "misc",
  "ingredient": "mydrugs:raw_thick_glass",
  "result": {
    "id": "mydrugs:thick_glass"
  },
  "experience": 0.1,
  "cookingtime": 200
}
                """);
        saveRawRecipe(futures, cachedOutput, "tight_seal", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "AA",
    "AA"
  ],
  "key": {
    "A": "mydrugs:rubber"
  },
  "result": {
    "id": "mydrugs:tight_seal",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "treated_planks", """
{
  "type": "minecraft:crafting_shapeless",
  "category": "misc",
  "ingredients": [
    "#minecraft:planks",
    "mydrugs:resin"
  ],
  "result": {
    "id": "mydrugs:treated_planks",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "valve", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    "BCB"
  ],
  "key": {
    "A": "mydrugs:soft_seal",
    "B": "mydrugs:copper_plate",
    "C": "minecraft:iron_ingot"
  },
  "result": {
    "id": "mydrugs:valve",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "watering_connection", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    " A ",
    " B "
  ],
  "key": {
    "A": "minecraft:copper_ingot",
    "B": "minecraft:iron_ingot"
  },
  "result": {
    "id": "mydrugs:watering_connection",
    "count": 1
  }
}
                """);
        saveRawRecipe(futures, cachedOutput, "wooden_frame", """
{
  "type": "minecraft:crafting_shaped",
  "category": "misc",
  "pattern": [
    "A A",
    " B ",
    "A A"
  ],
  "key": {
    "A": "mydrugs:treated_planks",
    "B": "minecraft:stick"
  },
  "result": {
    "id": "mydrugs:wooden_frame",
    "count": 1
  }
}
                """);
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void saveRawRecipe(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, String json) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, JsonParser.parseString(json), path));
    }

    @Override
    public String getName() {
        return "MyDrugs Vanilla Recipe Snapshots";
    }
}
