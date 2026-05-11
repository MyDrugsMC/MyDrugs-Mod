package org.mydrugs.mydrugs.core.drug.ritual;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.use.DrugStackResolver;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.ArrayList;
import java.util.List;

public final class RitualIngredientEffectRegistry {
    private static final List<Entry> ENTRIES = List.of(
            // ===== CANNABIS / WEED =====
            // calm perception, vision, ritual stability, stress relief
            entry(DrugId.WEED, Items.GLOWSTONE_DUST,
                    new RitualDrugEffectData(EffectType.GAMMA_BOOST, 30 * 20, 1.45F)),
            entry(DrugId.WEED, Items.MOSS_CARPET,
                    new RitualDrugEffectData(EffectType.STRESS_RELIEF, 90 * 20, 0.20F),
                    new RitualDrugEffectData(EffectType.RITUAL_STABILITY, 90 * 20, 0.10F)),
            entry(DrugId.WEED, Items.PHANTOM_MEMBRANE,
                    new RitualDrugEffectData(EffectType.FALL_CONTROL, 75 * 20, 0.35F),
                    new RitualDrugEffectData(EffectType.CAMERA_SWAY, 75 * 20, 0.08F)),

            // ===== TOBACCO =====
            // precision, focus, tremor control, mining/manual precision
            entry(DrugId.TOBACCO, ModItems.ALOE_VERA.get(),
                    new RitualDrugEffectData(EffectType.PRECISION, 120 * 20, 1.30F),
                    new RitualDrugEffectData(EffectType.TREMOR_REDUCTION, 120 * 20, 0.40F)),
            entry(DrugId.TOBACCO, Items.QUARTZ,
                    new RitualDrugEffectData(EffectType.PRECISION, 75 * 20, 1.18F),
                    new RitualDrugEffectData(EffectType.TREMOR_REDUCTION, 75 * 20, 0.25F)),
            entry(DrugId.TOBACCO, Items.COPPER_INGOT,
                    new RitualDrugEffectData(EffectType.MANUAL_WORK_SPEED, 90 * 20, 0.15F),
                    new RitualDrugEffectData(EffectType.PRECISION, 90 * 20, 1.10F)),

            // ===== COFFEE =====
            // work, manual machines, mild energy, nervous side effects at higher mixes
            entry(DrugId.COFFEE, Items.SUGAR,
                    new RitualDrugEffectData(EffectType.MANUAL_WORK_SPEED, 90 * 20, 0.12F),
                    new RitualDrugEffectData(EffectType.MOVEMENT_SPEED, 60 * 20, 0.04F)),
            entry(DrugId.COFFEE, Items.REDSTONE,
                    new RitualDrugEffectData(EffectType.MANUAL_WORK_SPEED, 90 * 20, 0.20F),
                    new RitualDrugEffectData(EffectType.CAMERA_SWAY, 90 * 20, 0.10F),
                    new RitualDrugEffectData(EffectType.HEARTBEAT, 60 * 20, 0.15F)),
            entry(DrugId.COFFEE, Items.COCOA_BEANS,
                    new RitualDrugEffectData(EffectType.MANUAL_WORK_SPEED, 120 * 20, 0.15F),
                    new RitualDrugEffectData(EffectType.STRESS_RELIEF, 60 * 20, 0.08F)),

            // ===== COCAINE =====
            // speed, manual overclock, combat burst, addiction-driven risk
            entry(DrugId.COCAINE, Items.REDSTONE,
                    new RitualDrugEffectData(EffectType.MOVEMENT_SPEED, 60 * 20, 0.18F),
                    new RitualDrugEffectData(EffectType.MANUAL_WORK_SPEED, 60 * 20, 0.25F),
                    new RitualDrugEffectData(EffectType.HEARTBEAT, 60 * 20, 0.20F)),
            entry(DrugId.COCAINE, Items.BLAZE_POWDER,
                    new RitualDrugEffectData(EffectType.ADRENALINE_SURGE, 45 * 20, 0.35F),
                    new RitualDrugEffectData(EffectType.MOVEMENT_SPEED, 45 * 20, 0.15F),
                    new RitualDrugEffectData(EffectType.CAMERA_SWAY, 45 * 20, 0.10F)),
            entry(DrugId.COCAINE, Items.RABBIT_FOOT,
                    new RitualDrugEffectData(EffectType.DASH_POWER, 60 * 20, 0.25F),
                    new RitualDrugEffectData(EffectType.MOVEMENT_SPEED, 60 * 20, 0.10F)),

            // ===== CRACK =====
            // short, violent burst; specialized for dash + burst-window chain
            entry(DrugId.CRACK, Items.GUNPOWDER,
                    new RitualDrugEffectData(EffectType.BURST_WINDOW, 35 * 20, 0.35F),
                    new RitualDrugEffectData(EffectType.DASH_POWER, 35 * 20, 0.35F),
                    new RitualDrugEffectData(EffectType.HEARTBEAT, 35 * 20, 0.30F)),
            entry(DrugId.CRACK, Items.ECHO_SHARD,
                    new RitualDrugEffectData(EffectType.BURST_WINDOW, 45 * 20, 0.40F),
                    new RitualDrugEffectData(EffectType.INPUT_FAIL, 20 * 20, 0.08F)),

            // ===== METH =====
            // endgame sustained overclock, mining mastery
            entry(DrugId.METH, Items.NETHERITE_SCRAP,
                    new RitualDrugEffectData(EffectType.MANUAL_WORK_SPEED, 180 * 20, 0.35F),
                    new RitualDrugEffectData(EffectType.PRECISION, 180 * 20, 1.25F),
                    new RitualDrugEffectData(EffectType.ADRENALINE_SURGE, 90 * 20, 0.25F)),
            entry(DrugId.METH, Items.DIAMOND,
                    new RitualDrugEffectData(EffectType.MINING_SPEED, 180 * 20, 0.30F),
                    new RitualDrugEffectData(EffectType.PRECISION, 180 * 20, 1.20F)),

            // ===== ALCOHOL =====
            // courage, resistance, ritual defiance
            entry(DrugId.ALCOHOL, ModItems.INNER_DEMON_REMAINS.get(),
                    new RitualDrugEffectData(EffectType.DAMAGE_RESISTANCE, 120 * 20, 0.20F),
                    new RitualDrugEffectData(EffectType.RITUAL_STABILITY, 90 * 20, 0.15F)),
            entry(DrugId.ALCOHOL, Items.IRON_INGOT,
                    new RitualDrugEffectData(EffectType.DAMAGE_RESISTANCE, 90 * 20, 0.12F),
                    new RitualDrugEffectData(EffectType.STUMBLE, 90 * 20, 0.08F)),
            entry(DrugId.ALCOHOL, Items.GHAST_TEAR,
                    new RitualDrugEffectData(EffectType.STRESS_RESISTANCE, 120 * 20, 0.30F),
                    new RitualDrugEffectData(EffectType.DAMAGE_RESISTANCE, 120 * 20, 0.15F)),

            // ===== LSD =====
            // perception, ore aura, ritual certainty, multiblock vision
            entry(DrugId.LSD, Items.LAPIS_LAZULI,
                    new RitualDrugEffectData(EffectType.ORE_AURA, 90 * 20, 3.0F),
                    new RitualDrugEffectData(EffectType.RITUAL_FOCUS, 90 * 20, 1.00F)),
            entry(DrugId.LSD, Items.ENDER_PEARL,
                    new RitualDrugEffectData(EffectType.MULTIBLOCK_VISION, 120 * 20, 1.00F),
                    new RitualDrugEffectData(EffectType.RITUAL_FOCUS, 60 * 20, 1.00F),
                    new RitualDrugEffectData(EffectType.CAMERA_SWAY, 60 * 20, 0.12F)),

            // ===== MUSHROOMS =====
            // nature, spores, ore aura, ritual certainty
            entry(DrugId.MUSHROOMS, Items.AMETHYST_SHARD,
                    new RitualDrugEffectData(EffectType.ORE_AURA, 120 * 20, 3.5F),
                    new RitualDrugEffectData(EffectType.RITUAL_STABILITY, 120 * 20, 0.20F)),
            entry(DrugId.MUSHROOMS, Items.GLOW_BERRIES,
                    new RitualDrugEffectData(EffectType.GAMMA_BOOST, 120 * 20, 1.25F),
                    new RitualDrugEffectData(EffectType.ORE_AURA, 75 * 20, 2.5F))
    );

    private RitualIngredientEffectRegistry() {
    }

    public static RitualDrugFormula buildFormula(ItemStack baseStack, List<ItemStack> ingredientStacks) {
        DrugId baseDrug = resolveBaseDrug(baseStack);
        DrugModel baseModel = DrugRegistry.getDrug(baseDrug);
        List<RitualDrugEffectData> baseEffects = baseModel.getDrugEffects().stream()
                .map(RitualDrugEffectData::from)
                .toList();
        List<RitualDrugEffectData> added = new ArrayList<>();
        for (ItemStack ingredient : ingredientStacks) {
            if (ingredient.isEmpty()) {
                continue;
            }
            for (Entry entry : ENTRIES) {
                if (entry.baseDrug == baseDrug && ingredient.is(entry.item)) {
                    added.addAll(entry.effects);
                }
            }
        }
        return RitualDrugFormula.of(baseDrug, baseEffects, DrugEffectCombiner.combine(added));
    }

    public static boolean hasAnyKnownEffect(ItemStack baseStack, List<ItemStack> ingredientStacks) {
        return !buildFormula(baseStack, ingredientStacks).addedEffects().isEmpty();
    }

    public static DrugId resolveBaseDrug(ItemStack stack) {
        List<DrugStackResolver.ResolvedStackDrug> resolved = DrugStackResolver.resolve(stack, null);
        if (!resolved.isEmpty()) {
            return resolved.getFirst().model().getId();
        }
        return DrugId.WEED;
    }

    private static Entry entry(DrugId baseDrug, Item item, RitualDrugEffectData... effects) {
        return new Entry(baseDrug, item, List.of(effects));
    }

    private record Entry(DrugId baseDrug, Item item, List<RitualDrugEffectData> effects) {
    }
}
