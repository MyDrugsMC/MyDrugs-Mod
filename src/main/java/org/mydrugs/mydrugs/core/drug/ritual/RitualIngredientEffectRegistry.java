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
            entry(DrugId.WEED, Items.GLOWSTONE_DUST, new RitualDrugEffectData(EffectType.GAMMA_BOOST, 30 * 20, 0.45F)),
            entry(DrugId.TOBACCO, ModItems.ALOE_VERA.get(),
                    new RitualDrugEffectData(EffectType.PRECISION, 90 * 20, 1.30F))
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
