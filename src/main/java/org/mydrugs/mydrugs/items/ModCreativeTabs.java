package org.mydrugs.mydrugs.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.crops.ModCrops;
import org.mydrugs.mydrugs.fluids.ModFluids;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Curated creative tab definitions.
 *
 * <p>Each tab receives an explicit, ordered list of contents instead of a blind dump of every
 * {@link DeferredRegister} entry. The {@code materials} tab additionally acts as a deterministic
 * catch-all for any registered item that no curated tab claims, so newly added items remain
 * reachable in creative without flooding the {@code main} tab. Space foods are confined to the
 * food tab. Blocks with no item form are naturally excluded because they never appear in the
 * item registers.</p>
 */
public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyDrugs.MODID);

    // ===== main: progression guide, core tools, a few signature drugs =====
    private static final List<DeferredItem<?>> MAIN = List.of(
            ModItems.PROGRESSION_GUIDE,
            ModItems.DRUG_ANALYZER,
            ModItems.PORTABLE_GRINDER,
            ModItems.GRINDING_TOOL,
            ModItems.ROLLER,
            ModItems.VANILLA_BIOME_FINDER,
            ModItems.JOINT,
            ModItems.CIGARETTE,
            ModItems.CANNABIS_POWDER,
            ModItems.METH_SHARD,
            ModItems.COCAINE_POWDER,
            ModItems.LSD_DROP,
            ModItems.MAGIC_MUSHROOM,
            ModItems.HASH_PIECE,
            ModItems.CRACK_SHARD,
            ModItems.MIXED_DRUG
    );

    // ===== machines: machine blocks, pipes, pumps, upgrades, wrench/filter =====
    private static final List<DeferredItem<?>> MACHINES = List.of(
            ModBlocks.GRINDING_BOWL_ITEM,
            ModBlocks.STOMP_CRAFTER_ITEM,
            ModBlocks.MANUAL_COFFEE_PULPER_ITEM,
            ModBlocks.COFFEE_DRYING_MAT_ITEM,
            ModBlocks.DRYING_RACK_ITEM,
            ModBlocks.CLAY_VAT_ITEM,
            ModBlocks.SIEVE_ITEM,
            ModBlocks.MIXING_VAT_ITEM,
            ModBlocks.ADVANCED_MIXING_VAT_ITEM,
            ModBlocks.ADVANCED_FURNACE_ITEM,
            ModBlocks.DISTILLER_ITEM,
            ModBlocks.REDUCTION_STILL_ITEM,
            ModBlocks.CENTRIFUGE_ITEM,
            ModBlocks.FLUID_FILTERER_ITEM,
            ModBlocks.EVAPORATION_TRAY_ITEM,
            ModBlocks.BIOCHEMICAL_REACTOR_ITEM,
            ModBlocks.GROWTH_CHAMBER_ITEM,
            ModBlocks.CHEMICAL_REACTOR_ITEM,
            ModBlocks.GASIFIER_ITEM,
            ModBlocks.ELECTROLYZER_ITEM,
            ModBlocks.STEAM_CRACKER_ITEM,
            ModBlocks.CATALYTIC_REFORMER_ITEM,
            ModBlocks.AROMATIC_EXTRACTOR_ITEM,
            ModBlocks.BTX_FRACTIONATION_TOWER_ITEM,
            ModBlocks.PSYCHOTROPE_DISTILLERY_ITEM,
            ModBlocks.DISTILLATE_ENGINE_ITEM,
            ModBlocks.PSY_ANVIL_ITEM,
            ModBlocks.GENE_EXTRACTOR_ITEM,
            ModBlocks.CRISPR_CAS9_COMBINATOR_ITEM,
            ModBlocks.BACTERIAL_INCUBATOR_ITEM,
            ModBlocks.HEMOGENIC_INFUSER_ITEM,
            ModBlocks.AUTOCLAVE_ITEM,
            ModBlocks.FLUID_PUMP_ITEM,
            ModBlocks.GAS_PUMP_ITEM,
            ModBlocks.BASIC_ITEM_PIPE_ITEM,
            ModBlocks.FAST_ITEM_PIPE_ITEM,
            ModBlocks.BASIC_FLUID_PIPE_ITEM,
            ModBlocks.FAST_FLUID_PIPE_ITEM,
            ModBlocks.BASIC_GAS_PIPE_ITEM,
            ModBlocks.FAST_GAS_PIPE_ITEM,
            ModItems.PIPE_WRENCH,
            ModItems.PIPE_FILTER_UPGRADE,
            ModItems.MACHINE_TRANSFER_UPGRADE,
            ModItems.ENERGY_UPGRADE,
            ModItems.AUTOMATION_UPGRADE,
            ModItems.FLUID_FILTER
    );

    // ===== plants: crop/plant blocks, harvested plant matter (seeds added dynamically) =====
    private static final List<DeferredItem<?>> PLANTS = List.of(
            ModItems.TOBACCO_LEAF,
            ModItems.DRIED_TOBACCO_LEAF,
            ModItems.CANNABIS_LEAF,
            ModItems.CURED_CANNABIS_LEAF,
            ModItems.DRIED_CANNABIS_LEAF,
            ModItems.COCA_LEAF,
            ModItems.DRIED_COCA_LEAF,
            ModItems.ERGOT,
            ModItems.INFECTED_RYE,
            ModItems.RYE,
            ModItems.MALT,
            ModItems.FUNGAL_FIBER,
            ModItems.FUNGAL_CULTURE,
            ModItems.COFFEE_CHERRIES,
            ModItems.WET_COFFEE_BEAN,
            ModItems.COFFEE_BEAN,
            ModItems.ALOE_VERA,
            ModItems.LAVENDER,
            ModItems.DRIED_LAVENDER,
            ModItems.VALERIAN_ROOT,
            ModItems.EPHEDRA_EXTRACT,
            ModBlocks.MAGIC_MUSHROOM_BLOCK_ITEM,
            ModBlocks.MAGIC_MUSHROOM_STEM_ITEM,
            ModBlocks.PSYCHEDELIC_MYCELIUM_ITEM,
            ModBlocks.BITTER_NUT_BUSH_ITEM,
            ModBlocks.THIRD_EYE_PETAL_ITEM
    );

    // ===== fluids_and_gases: gas tank + fluid handling (buckets added dynamically) =====
    private static final List<DeferredItem<?>> FLUIDS_AND_GASES = List.of(
            ModItems.GAS_TANK_ITEM,
            ModItems.GLASS_BOTTLE
    );

    // ===== recovery_and_psyche: diary, music, recovery blocks, psy + inner-dimension content =====
    private static final List<DeferredItem<?>> RECOVERY_AND_PSYCHE = List.of(
            ModItems.PERSONAL_DIARY,
            ModItems.HEADPHONES,
            ModItems.BLANK_MUSIC_DISC,
            ModItems.PERSONAL_MUSIC_DISC,
            ModItems.HERBAL_TEA,
            ModItems.CALMING_MIXTURE,
            ModItems.SLEEPING_AID,
            ModItems.OVERDOSE_ANTIDOTE,
            ModBlocks.THERAPIST_DESK_ITEM,
            ModBlocks.RECOVERY_ANCHOR_ITEM,
            ModBlocks.DISC_SCRIBER_ITEM,
            ModBlocks.RECOVERY_JUKEBOX_ITEM,
            ModBlocks.PSYCHOTROPE_RESONATOR_ITEM,
            ModItems.PSY_RECEPTACLE,
            ModItems.PSY_BLUEPRINT,
            ModItems.MYCELIAL_RESONATOR,
            ModItems.PSYCHOTROPE_LENS,
            ModItems.STRAIN_VENT,
            ModItems.CURRENT_REGULATOR,
            ModItems.INTEGRATION_CORE,
            ModItems.BASIC_INTEGRATION_CORE,
            ModItems.ADVANCED_INTEGRATION_CORE,
            ModItems.REFINED_INTEGRATION_CORE,
            ModItems.PRISTINE_INTEGRATION_CORE,
            ModItems.PRIME_INTEGRATION_CORE,
            ModItems.THUNDER_BOTTLE,
            ModItems.LIGHTNING_BOTTLE,
            ModItems.SHROOM_HARVESTER,
            // ritual ingredients
            ModItems.RITUAL_THREADS,
            ModItems.PSYCHOTROPIC_PIGMENT,
            ModItems.RITUAL_RESIN,
            ModItems.UNSTABLE_RESIDUE,
            ModItems.CALMING_SPORES,
            ModItems.BITTER_NUT,
            ModItems.CHARGED_SINEW,
            ModItems.FRACTURED_IMPULSE,
            ModItems.CHARGED_CORE,
            ModItems.BROKEN_COURAGE,
            ModItems.DREAMCAP_SPORES,
            ModItems.UNSTABLE_ESSENCE,
            ModItems.LUCID_EXTRACT,
            ModItems.CALMING_RESIN,
            ModItems.REDLINE_FUEL,
            ModItems.OVERDRIVE_FUEL,
            // psychedelic / integration materials
            ModItems.DREAM_RESIDUE,
            ModItems.MYCELIAL_INSIGHT,
            ModItems.PRESSED_CALM,
            ModItems.FERMENTED_MEMORY,
            ModItems.INNER_DEMON_REMAINS,
            // ritual blocks
            ModBlocks.WOVEN_VINE_FRAME_ITEM,
            ModBlocks.RITUAL_BARK_ITEM,
            ModBlocks.CHARCOAL_GLYPH_BLOCK_ITEM,
            ModBlocks.PAINTED_CLAY_BOWL_ITEM,
            ModBlocks.HANGING_VINE_BUNDLE_ITEM,
            ModBlocks.MYCELIAL_PADDING_ITEM,
            // inner-dimension nodes / plants
            ModBlocks.LUCID_ECHO_NODE_ITEM,
            ModBlocks.BITTER_ECHO_NODE_ITEM,
            ModBlocks.CALMING_ECHO_NODE_ITEM,
            ModBlocks.PRESSED_CALM_NODE_ITEM,
            ModBlocks.FERMENTED_MEMORY_NODE_ITEM,
            ModBlocks.REDLINE_CRYSTAL_NODE_ITEM,
            ModBlocks.DREAM_RESIDUE_GEODE_ITEM,
            ModBlocks.OVERDRIVE_SLAG_ITEM,
            ModBlocks.MYCELIAL_INSIGHT_NODE_ITEM,
            ModBlocks.BREATH_GRASS_ITEM,
            ModBlocks.CALMING_FERN_ITEM,
            ModBlocks.MEMORY_REEDS_ITEM,
            ModBlocks.REDLINE_THORN_ITEM,
            ModBlocks.MYCELIAL_ROOT_ITEM
    );

    // ===== food_and_consumables: drug consumables, drinks, edibles (space foods dynamic) =====
    private static final List<DeferredItem<?>> FOOD_AND_CONSUMABLES = List.of(
            ModItems.CANNABIS_POWDER,
            ModItems.METH_SHARD,
            ModItems.METH_POWDER,
            ModItems.LSD_DROP,
            ModItems.MAGIC_MUSHROOM_POWDER,
            ModItems.BANG,
            ModItems.HASH_BRICK,
            ModItems.HASH_PIECE,
            ModItems.COCA_PASTE,
            ModItems.COCAINE_POWDER,
            ModItems.CRACK_SHARD,
            ModItems.CRACK_PLATE,
            ModItems.COCAINE_PLATE,
            ModItems.TOBACCO_HANDFUL,
            ModItems.CIGARETTE,
            ModItems.JOINT,
            ModItems.MIXED_DRUG,
            ModItems.MIXED_WEED_DRUG,
            ModItems.MIXED_TOBACCO_DRUG,
            ModItems.MIXED_LSD_DRUG,
            ModItems.MIXED_MUSHROOMS_DRUG,
            ModItems.MIXED_HASH_DRUG,
            ModItems.MIXED_METH_DRUG,
            ModItems.MIXED_COCAINE_DRUG,
            ModItems.MIXED_CRACK_DRUG,
            ModItems.MIXED_COFFEE_DRUG,
            ModItems.DEFIANT_SPIRIT_BOTTLE,
            ModItems.COFFEE_CUP,
            ModItems.CAFFEINE_BAR,
            ModItems.MARRAKECH_BUTTER,
            ModItems.CUP
    );

    public static final Supplier<CreativeModeTab> MAIN_TAB = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.main"))
                    .icon(() -> new ItemStack(ModItems.PROGRESSION_GUIDE.get()))
                    .displayItems((params, output) -> accept(output, MAIN))
                    .build());

    public static final Supplier<CreativeModeTab> MACHINES_TAB = CREATIVE_MODE_TABS.register("machines", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.machines"))
                    .icon(() -> new ItemStack(ModBlocks.ADVANCED_FURNACE_ITEM.get()))
                    .withTabsBefore(MyDrugs.rl("main"))
                    .displayItems((params, output) -> accept(output, MACHINES))
                    .build());

    public static final Supplier<CreativeModeTab> MATERIALS_TAB = CREATIVE_MODE_TABS.register("materials", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.materials"))
                    .icon(() -> new ItemStack(ModItems.PLATINUM_INGOT.get()))
                    .withTabsBefore(MyDrugs.rl("machines"))
                    .displayItems((params, output) -> acceptUnclaimed(output))
                    .build());

    public static final Supplier<CreativeModeTab> PLANTS_TAB = CREATIVE_MODE_TABS.register("plants", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.plants"))
                    .icon(() -> new ItemStack(ModItems.CANNABIS_LEAF.get()))
                    .withTabsBefore(MyDrugs.rl("materials"))
                    .displayItems((params, output) -> {
                        accept(output, PLANTS);
                        for (DeferredHolder<Item, ? extends Item> holder : ModCrops.ITEMS.getEntries()) {
                            output.accept(holder.get());
                        }
                    })
                    .build());

    public static final Supplier<CreativeModeTab> FLUIDS_AND_GASES_TAB = CREATIVE_MODE_TABS.register("fluids_and_gases", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.fluids_and_gases"))
                    .icon(() -> new ItemStack(ModItems.GAS_TANK_ITEM.get()))
                    .withTabsBefore(MyDrugs.rl("plants"))
                    .displayItems((params, output) -> {
                        accept(output, FLUIDS_AND_GASES);
                        for (DeferredHolder<Item, ? extends Item> holder : ModFluids.FLUID_ITEMS.getEntries()) {
                            output.accept(holder.get());
                        }
                    })
                    .build());

    public static final Supplier<CreativeModeTab> RECOVERY_AND_PSYCHE_TAB = CREATIVE_MODE_TABS.register("recovery_and_psyche", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.recovery_and_psyche"))
                    .icon(() -> new ItemStack(ModItems.PERSONAL_DIARY.get()))
                    .withTabsBefore(MyDrugs.rl("fluids_and_gases"))
                    .displayItems((params, output) -> accept(output, RECOVERY_AND_PSYCHE))
                    .build());

    public static final Supplier<CreativeModeTab> FOOD_AND_CONSUMABLES_TAB = CREATIVE_MODE_TABS.register("food_and_consumables", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.food_and_consumables"))
                    .icon(() -> new ItemStack(ModItems.CAFFEINE_BAR.get()))
                    .withTabsBefore(MyDrugs.rl("recovery_and_psyche"))
                    .displayItems((params, output) -> {
                        accept(output, FOOD_AND_CONSUMABLES);
                        for (DeferredItem<SpaceFoodItem> holder : ModItems.SPACE_FOODS_BY_BASE_ID.values()) {
                            output.accept(holder.get());
                        }
                    })
                    .build());

    private ModCreativeTabs() {
    }

    private static void accept(CreativeModeTab.Output output, List<DeferredItem<?>> items) {
        for (DeferredItem<?> item : items) {
            output.accept(item.get());
        }
    }

    /**
     * Adds every registered item/block-item not claimed by a curated tab. Space foods are excluded
     * because they live exclusively in the food tab.
     */
    private static void acceptUnclaimed(CreativeModeTab.Output output) {
        Set<Item> claimed = claimedItems();
        for (DeferredHolder<Item, ? extends Item> holder : ModItems.ITEMS.getEntries()) {
            Item item = holder.get();
            if (item instanceof SpaceFoodItem) {
                continue;
            }
            if (claimed.add(item)) {
                output.accept(item);
            }
        }
        for (DeferredHolder<Item, ? extends Item> holder : ModBlocks.ITEMS.getEntries()) {
            Item item = holder.get();
            if (claimed.add(item)) {
                output.accept(item);
            }
        }
    }

    private static Set<Item> claimedItems() {
        Set<Item> claimed = Collections.newSetFromMap(new IdentityHashMap<>());
        claimed.add(ModItems.RESONANCE_LENS.get()); // Compatibility-only registry ID; hide from curated tabs.
        List<List<DeferredItem<?>>> curated = List.of(
                MAIN, MACHINES, PLANTS, FLUIDS_AND_GASES, RECOVERY_AND_PSYCHE, FOOD_AND_CONSUMABLES);
        for (List<DeferredItem<?>> list : curated) {
            for (DeferredItem<?> item : list) {
                claimed.add(item.get());
            }
        }
        for (DeferredHolder<Item, ? extends Item> holder : ModCrops.ITEMS.getEntries()) {
            claimed.add(holder.get());
        }
        for (DeferredHolder<Item, ? extends Item> holder : ModFluids.FLUID_ITEMS.getEntries()) {
            claimed.add(holder.get());
        }
        return claimed;
    }

    public static List<String> tabPaths() {
        return new ArrayList<>(List.of(
                "main", "machines", "materials", "plants",
                "fluids_and_gases", "recovery_and_psyche", "food_and_consumables"));
    }
}
