package org.mydrugs.mydrugs.client.diary;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.ModItems;

/**
 * Maps a {@link DrugId} to a representative item stack for inventory icon rendering.
 * Centralizes the icon mapping so the diary screen and HUD can share it.
 */
public final class DrugIconHelper {
    private DrugIconHelper() {
    }

    public static ItemStack stackFor(DrugId id) {
        if (id == null) return ItemStack.EMPTY;
        return switch (id) {
            case WEED -> safeStack(ModItems.CANNABIS_POWDER);
            case HASH -> safeStack(ModItems.HASH_PIECE);
            case TOBACCO -> safeStack(ModItems.TOBACCO_LEAF);
            case COFFEE -> new ItemStack(Items.COCOA_BEANS);
            case ALCOHOL -> new ItemStack(Items.POTION);
            case COCAINE -> safeStack(ModItems.METH_POWDER); // closest cocaine-ish placeholder
            case CRACK -> safeStack(ModItems.METH_POWDER);
            case METH -> safeStack(ModItems.METH_SHARD);
            case LSD -> safeStack(ModItems.LSD_DROP);
            case MUSHROOMS -> new ItemStack(Items.RED_MUSHROOM);
            default -> new ItemStack(Items.PAPER);
        };
    }

    private static ItemStack safeStack(DeferredItem<?> holder) {
        try {
            return new ItemStack(holder.get());
        } catch (Throwable ignored) {
            return new ItemStack(Items.PAPER);
        }
    }
}
