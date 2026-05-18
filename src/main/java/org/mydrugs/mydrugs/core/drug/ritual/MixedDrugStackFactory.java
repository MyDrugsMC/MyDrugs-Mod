package org.mydrugs.mydrugs.core.drug.ritual;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

public final class MixedDrugStackFactory {
    private MixedDrugStackFactory() {
    }

    public static ItemStack createStack(MixedDrugData data) {
        ItemStack stack = new ItemStack(itemFor(data.baseDrug()));
        stack.set(ModDataComponents.MIXED_DRUG_DATA.get(), data);
        return stack;
    }

    public static ItemStack createPendingStack(RitualDrugFormula formula) {
        return createStack(MixedDrugData.pending(formula));
    }

    public static ItemStack createPendingStack(RitualDrugFormula formula, PsyMixerRitualQuality quality) {
        return createStack(MixedDrugData.pending(formula, quality));
    }

    private static Item itemFor(DrugId baseDrug) {
        return switch (baseDrug) {
            case WEED -> ModItems.MIXED_WEED_DRUG.get();
            case TOBACCO -> ModItems.MIXED_TOBACCO_DRUG.get();
            case LSD -> ModItems.MIXED_LSD_DRUG.get();
            case MUSHROOMS -> ModItems.MIXED_MUSHROOMS_DRUG.get();
            case HASH -> ModItems.MIXED_HASH_DRUG.get();
            case METH -> ModItems.MIXED_METH_DRUG.get();
            case COCAINE -> ModItems.MIXED_COCAINE_DRUG.get();
            case CRACK -> ModItems.MIXED_CRACK_DRUG.get();
            case COFFEE -> ModItems.MIXED_COFFEE_DRUG.get();
            case ALCOHOL -> ModItems.DEFIANT_SPIRIT_BOTTLE.get();
            default -> ModItems.MIXED_DRUG.get();
        };
    }
}
