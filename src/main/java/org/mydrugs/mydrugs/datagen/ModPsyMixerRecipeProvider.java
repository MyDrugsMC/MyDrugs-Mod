package org.mydrugs.mydrugs.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugEffectData;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ModPsyMixerRecipeProvider implements DataProvider {
    private static final String DEFAULT_CATALYST = "mydrugs:psychotropic_pigment";
    private static final String DEFAULT_STABILIZER = "mydrugs:ritual_resin";
    private static final String DEFAULT_VESSEL = "mydrugs:painted_clay_bowl";

    private final PackOutput.PathProvider recipePathProvider;

    public ModPsyMixerRecipeProvider(PackOutput output) {
        this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        recipe(futures, cachedOutput, "brightened_cannabis_powder", DrugId.WEED, "minecraft:glowstone_dust",
                "mydrugs:brightened_cannabis_powder", 400, 0.25F, 8.0F,
                "mydrugs:psychotropic_pigment", "mydrugs:ritual_resin", false, 0.0F, 0.0F,
                effect(EffectType.GAMMA_BOOST, 120 * 20, 1.45F));
        recipe(futures, cachedOutput, DrugId.WEED, "minecraft:moss_carpet",
                effect(EffectType.STRESS_RELIEF, 90 * 20, 0.20F),
                effect(EffectType.RITUAL_STABILITY, 90 * 20, 0.10F));
        recipe(futures, cachedOutput, DrugId.WEED, "minecraft:phantom_membrane",
                effect(EffectType.FALL_CONTROL, 75 * 20, 0.35F),
                effect(EffectType.CAMERA_SWAY, 75 * 20, 0.08F));

        recipe(futures, cachedOutput, "soothing_tobacco_blend", DrugId.TOBACCO, "mydrugs:aloe_vera",
                "mydrugs:soothing_tobacco_blend", 340, 0.18F, 2.0F,
                null, null, true, 0.0F, 0.05F,
                effect(EffectType.PRECISION, 120 * 20, 1.30F),
                effect(EffectType.TREMOR_REDUCTION, 120 * 20, 0.40F));
        recipe(futures, cachedOutput, DrugId.TOBACCO, "minecraft:quartz",
                effect(EffectType.PRECISION, 90 * 20, 0.75F),
                effect(EffectType.TREMOR_REDUCTION, 90 * 20, 0.20F));
        recipe(futures, cachedOutput, DrugId.TOBACCO, "minecraft:copper_ingot",
                effect(EffectType.MANUAL_WORK_SPEED, 90 * 20, 0.15F),
                effect(EffectType.PRECISION, 90 * 20, 1.10F));

        recipe(futures, cachedOutput, DrugId.COFFEE, "minecraft:sugar",
                effect(EffectType.MANUAL_WORK_SPEED, 90 * 20, 0.12F),
                effect(EffectType.MOVEMENT_SPEED, 60 * 20, 0.04F));
        recipe(futures, cachedOutput, DrugId.COFFEE, "minecraft:redstone",
                effect(EffectType.MANUAL_WORK_SPEED, 120 * 20, 0.35F),
                effect(EffectType.MINING_SPEED, 120 * 20, 0.35F),
                effect(EffectType.HEARTBEAT, 90 * 20, 0.20F),
                effect(EffectType.TREMOR, 60 * 20, 0.08F));
        recipe(futures, cachedOutput, DrugId.COFFEE, "minecraft:cocoa_beans",
                effect(EffectType.MANUAL_WORK_SPEED, 120 * 20, 0.15F),
                effect(EffectType.STRESS_RELIEF, 60 * 20, 0.08F));

        recipe(futures, cachedOutput, DrugId.COCAINE, "minecraft:redstone",
                effect(EffectType.MOVEMENT_SPEED, 60 * 20, 0.18F),
                effect(EffectType.MANUAL_WORK_SPEED, 60 * 20, 0.25F),
                effect(EffectType.HEARTBEAT, 60 * 20, 0.20F));
        recipe(futures, cachedOutput, DrugId.COCAINE, "minecraft:blaze_powder",
                effect(EffectType.ADRENALINE_SURGE, 45 * 20, 0.35F),
                effect(EffectType.MOVEMENT_SPEED, 45 * 20, 0.15F),
                effect(EffectType.CAMERA_SWAY, 45 * 20, 0.10F));
        recipe(futures, cachedOutput, DrugId.COCAINE, "minecraft:rabbit_foot",
                effect(EffectType.DASH_POWER, 60 * 20, 0.25F),
                effect(EffectType.MOVEMENT_SPEED, 60 * 20, 0.10F));

        recipe(futures, cachedOutput, DrugId.CRACK, "minecraft:gunpowder",
                effect(EffectType.BURST_WINDOW, 35 * 20, 0.35F),
                effect(EffectType.DASH_POWER, 35 * 20, 0.35F),
                effect(EffectType.HEARTBEAT, 35 * 20, 0.30F));
        recipe(futures, cachedOutput, DrugId.CRACK, "minecraft:echo_shard",
                effect(EffectType.BURST_WINDOW, 45 * 20, 0.40F),
                effect(EffectType.INPUT_FAIL, 20 * 20, 0.08F));

        recipe(futures, cachedOutput, DrugId.METH, "minecraft:netherite_scrap",
                effect(EffectType.MINING_SPEED, 240 * 20, 1.75F),
                effect(EffectType.MANUAL_WORK_SPEED, 240 * 20, 0.50F),
                effect(EffectType.ADRENALINE_SURGE, 120 * 20, 0.45F),
                effect(EffectType.HEARTBEAT, 180 * 20, 0.45F),
                effect(EffectType.TREMOR, 120 * 20, 0.18F),
                effect(EffectType.INPUT_FAIL, 90 * 20, 0.08F));
        recipe(futures, cachedOutput, DrugId.METH, "minecraft:diamond",
                effect(EffectType.MINING_SPEED, 180 * 20, 1.15F),
                effect(EffectType.PRECISION, 180 * 20, 0.75F),
                effect(EffectType.HEARTBEAT, 120 * 20, 0.25F),
                effect(EffectType.TREMOR, 90 * 20, 0.10F));

        recipe(futures, cachedOutput, "defiant_spirit_bottle", DrugId.ALCOHOL, "mydrugs:inner_demon_remains",
                "mydrugs:defiant_spirit_bottle", 600, 0.35F, 0.0F,
                "mydrugs:psychotropic_pigment", "mydrugs:ritual_resin", false, 0.45F, -0.04F,
                effect(EffectType.DAMAGE_RESISTANCE, 120 * 20, 0.20F),
                effect(EffectType.RITUAL_STABILITY, 90 * 20, 0.15F));
        recipe(futures, cachedOutput, DrugId.ALCOHOL, "minecraft:iron_ingot",
                effect(EffectType.DAMAGE_RESISTANCE, 90 * 20, 0.12F),
                effect(EffectType.STUMBLE, 90 * 20, 0.08F));
        recipe(futures, cachedOutput, DrugId.ALCOHOL, "minecraft:ghast_tear",
                effect(EffectType.STRESS_RESISTANCE, 120 * 20, 0.30F),
                effect(EffectType.DAMAGE_RESISTANCE, 120 * 20, 0.15F));

        recipe(futures, cachedOutput, DrugId.LSD, "minecraft:lapis_lazuli",
                effect(EffectType.ORE_AURA, 120 * 20, 3.0F),
                effect(EffectType.ORE_FORTUNE, 120 * 20, 1.0F),
                effect(EffectType.RITUAL_FOCUS, 90 * 20, 0.75F),
                effect(EffectType.CAMERA_SWAY, 90 * 20, 0.10F));
        recipe(futures, cachedOutput, DrugId.LSD, "minecraft:diamond",
                effect(EffectType.ORE_FORTUNE, 150 * 20, 2.0F),
                effect(EffectType.ORE_AURA, 150 * 20, 2.5F),
                effect(EffectType.ACID_WARP, 120 * 20, 0.40F),
                effect(EffectType.CAMERA_SWAY, 120 * 20, 0.15F));
        recipe(futures, cachedOutput, DrugId.LSD, "minecraft:ender_pearl",
                effect(EffectType.MULTIBLOCK_VISION, 120 * 20, 1.00F),
                effect(EffectType.RITUAL_FOCUS, 60 * 20, 1.00F),
                effect(EffectType.CAMERA_SWAY, 60 * 20, 0.12F));

        recipe(futures, cachedOutput, DrugId.MUSHROOMS, "minecraft:amethyst_shard",
                effect(EffectType.ORE_AURA, 120 * 20, 3.5F),
                effect(EffectType.RITUAL_STABILITY, 120 * 20, 0.20F));
        recipe(futures, cachedOutput, DrugId.MUSHROOMS, "minecraft:glow_berries",
                effect(EffectType.GAMMA_BOOST, 120 * 20, 1.25F),
                effect(EffectType.ORE_AURA, 75 * 20, 2.5F));

        recipe(futures, cachedOutput, DrugId.WEED, "mydrugs:calming_spores",
                effect(EffectType.RITUAL_STABILITY, 120 * 20, 0.25F),
                effect(EffectType.STRESS_RELIEF, 120 * 20, 0.25F));
        recipe(futures, cachedOutput, DrugId.COFFEE, "mydrugs:bitter_nut",
                effect(EffectType.MANUAL_WORK_SPEED, 150 * 20, 0.22F),
                effect(EffectType.TREMOR, 60 * 20, 0.10F));
        recipe(futures, cachedOutput, DrugId.COCAINE, "mydrugs:charged_sinew",
                effect(EffectType.ADRENALINE_SURGE, 75 * 20, 0.45F),
                effect(EffectType.MANUAL_WORK_SPEED, 75 * 20, 0.30F));
        recipe(futures, cachedOutput, DrugId.CRACK, "mydrugs:fractured_impulse",
                effect(EffectType.BURST_WINDOW, 50 * 20, 0.50F),
                effect(EffectType.ADRENALINE_SURGE, 50 * 20, 0.35F));
        recipe(futures, cachedOutput, DrugId.METH, "mydrugs:charged_core",
                effect(EffectType.MANUAL_WORK_SPEED, 240 * 20, 0.40F),
                effect(EffectType.MOVEMENT_SPEED, 120 * 20, 0.15F),
                effect(EffectType.ADRENALINE_SURGE, 120 * 20, 0.30F));
        recipe(futures, cachedOutput, DrugId.ALCOHOL, "mydrugs:broken_courage",
                effect(EffectType.DAMAGE_RESISTANCE, 150 * 20, 0.25F),
                effect(EffectType.STRESS_RESISTANCE, 150 * 20, 0.25F),
                effect(EffectType.STUMBLE, 150 * 20, 0.10F));
        recipe(futures, cachedOutput, DrugId.LSD, "mydrugs:third_eye_petal",
                effect(EffectType.ORE_AURA, 180 * 20, 4.0F),
                effect(EffectType.ORE_FORTUNE, 180 * 20, 3.0F),
                effect(EffectType.MULTIBLOCK_VISION, 180 * 20, 1.00F),
                effect(EffectType.RITUAL_FOCUS, 120 * 20, 1.00F),
                effect(EffectType.CAMERA_SWAY, 120 * 20, 0.18F));
        recipe(futures, cachedOutput, DrugId.MUSHROOMS, "mydrugs:dreamcap_spores",
                effect(EffectType.RITUAL_FOCUS, 120 * 20, 1.00F),
                effect(EffectType.ORE_AURA, 120 * 20, 4.0F),
                effect(EffectType.BAD_TRIP_RESISTANCE, 120 * 20, 0.20F));

        recipe(futures, cachedOutput, DrugId.HASH, "mydrugs:calming_spores",
                effect(EffectType.RITUAL_STABILITY, 150 * 20, 0.35F),
                effect(EffectType.STRESS_RELIEF, 150 * 20, 0.30F),
                effect(EffectType.MOVEMENT_SLOWDOWN, 60 * 20, 0.04F));
        recipe(futures, cachedOutput, DrugId.HASH, "minecraft:phantom_membrane",
                effect(EffectType.FALL_CONTROL, 105 * 20, 0.45F),
                effect(EffectType.BAD_TRIP_RESISTANCE, 105 * 20, 0.12F),
                effect(EffectType.CAMERA_SWAY, 75 * 20, 0.10F));
        recipe(futures, cachedOutput, DrugId.HASH, "minecraft:ghast_tear",
                effect(EffectType.STRESS_RESISTANCE, 135 * 20, 0.28F),
                effect(EffectType.RITUAL_STABILITY, 135 * 20, 0.20F));

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private void recipe(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, DrugId baseDrug, String material, RitualDrugEffectData... effects) {
        recipe(futures, cachedOutput, generatedName(baseDrug, material), baseDrug, material,
                fallbackResult(baseDrug), 400, 0.25F, 0.0F,
                null, null, false, 0.0F, 0.0F, effects);
    }

    private void recipe(
            List<CompletableFuture<?>> futures,
            CachedOutput cachedOutput,
            String name,
            DrugId baseDrug,
            String material,
            String fallbackResult,
            int ritualTime,
            float baseInstability,
            float requiredLifetimeDose,
            String catalyst,
            String stabilizer,
            boolean preserveVesselOnSuccess,
            float failureSeverity,
            float ritualStabilityModifier,
            RitualDrugEffectData... effects
    ) {
        JsonObject json = new JsonObject();
        json.addProperty("type", "mydrugs:psy_mixer");
        json.addProperty("base", baseItem(baseDrug));
        json.addProperty("material", material);
        json.addProperty("catalyst", catalyst == null ? DEFAULT_CATALYST : catalyst);
        json.addProperty("stabilizer", stabilizer == null ? DEFAULT_STABILIZER : stabilizer);
        json.addProperty("vessel", DEFAULT_VESSEL);
        json.add("effects", effects(effects));
        json.add("fallback_result", stack(fallbackResult));
        json.addProperty("ritual_time", ritualTime);
        json.add("ritual_actions", ritualActions());
        json.addProperty("required_knowledge", requiredKnowledge(baseDrug));
        json.addProperty("required_drug", baseDrug.serializedName());
        if (requiredLifetimeDose > 0.0F) {
            json.addProperty("required_lifetime_dose", requiredLifetimeDose);
        }
        json.addProperty("show_if_locked", true);
        json.addProperty("preserve_vessel_on_success", preserveVesselOnSuccess);
        json.addProperty("preserve_vessel_on_failure", true);

        saveRecipe(futures, cachedOutput, "psy_mixer/" + name, json);
    }

    private static JsonArray ritualActions() {
        JsonArray array = new JsonArray();
        array.add("sneak");
        array.add("jump");
        array.add("right_click_air");
        array.add("walk_ring");
        array.add("look_at_core");
        array.add("timing_ring");
        array.add("stand_still");
        array.add("reopen_gui");
        return array;
    }

    private static JsonArray effects(RitualDrugEffectData... effects) {
        JsonArray array = new JsonArray();
        for (RitualDrugEffectData effect : effects) {
            JsonElement encoded = RitualDrugEffectData.CODEC.encodeStart(JsonOps.INSTANCE, effect)
                    .result()
                    .orElseThrow(() -> new IllegalStateException("Could not encode Psy Mixer effect " + effect));
            array.add(encoded);
        }
        return array;
    }

    private static JsonObject stack(String item) {
        JsonObject result = new JsonObject();
        result.addProperty("id", item);
        result.addProperty("count", 1);
        return result;
    }

    private static RitualDrugEffectData effect(EffectType type, int duration, float intensity) {
        return new RitualDrugEffectData(type, duration, intensity);
    }

    private static String baseItem(DrugId baseDrug) {
        return switch (baseDrug) {
            case WEED -> "mydrugs:cannabis_powder";
            case HASH -> "mydrugs:hash_piece";
            case METH -> "mydrugs:meth_powder";
            case COCAINE -> "mydrugs:cocaine_powder";
            case CRACK -> "mydrugs:crack_shard";
            case LSD -> "mydrugs:lsd_drop";
            case MUSHROOMS -> "mydrugs:magic_mushroom_powder";
            case ALCOHOL -> "mydrugs:glass_bottle";
            case TOBACCO -> "mydrugs:tobacco_handful";
            case COFFEE -> "mydrugs:coffee_cup";
            default -> "mydrugs:mixed_drug";
        };
    }

    private static String fallbackResult(DrugId baseDrug) {
        return switch (baseDrug) {
            case WEED -> "mydrugs:mixed_weed_drug";
            case HASH -> "mydrugs:mixed_hash_drug";
            case METH -> "mydrugs:mixed_meth_drug";
            case COCAINE -> "mydrugs:mixed_cocaine_drug";
            case CRACK -> "mydrugs:mixed_crack_drug";
            case LSD -> "mydrugs:mixed_lsd_drug";
            case MUSHROOMS -> "mydrugs:mixed_mushrooms_drug";
            case ALCOHOL -> "mydrugs:defiant_spirit_bottle";
            case TOBACCO -> "mydrugs:mixed_tobacco_drug";
            case COFFEE -> "mydrugs:mixed_coffee_drug";
            default -> "mydrugs:mixed_drug";
        };
    }

    private static String requiredKnowledge(DrugId baseDrug) {
        return switch (baseDrug) {
            case WEED, HASH -> "mydrugs:cannabinoid";
            case TOBACCO -> "mydrugs:nicotinic";
            case COFFEE -> "mydrugs:caffeine";
            case COCAINE, CRACK -> "mydrugs:stimulant";
            case METH -> "mydrugs:overclocked";
            case ALCOHOL -> "mydrugs:fermented";
            case LSD -> "mydrugs:lysergic";
            case MUSHROOMS -> "mydrugs:mycelial";
            default -> "mydrugs:cannabinoid";
        };
    }

    private static String generatedName(DrugId baseDrug, String material) {
        ResourceLocation id = ResourceLocation.parse(material);
        return baseDrug.serializedName() + "_" + id.getPath();
    }

    private void saveRecipe(List<CompletableFuture<?>> futures, CachedOutput cachedOutput, String name, JsonObject json) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name);
        Path path = this.recipePathProvider.json(id);
        futures.add(DataProvider.saveStable(cachedOutput, json, path));
    }

    @Override
    public String getName() {
        return "MyDrugs Psy Mixer Recipes";
    }
}
