package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

public final class IntegrationMaterials {
    private static final Map<DrugId, Supplier<? extends Item>> MATERIALS = new EnumMap<>(DrugId.class);

    static {
        bind(DrugId.COFFEE, ModItems.LUCID_EXTRACT);
        bind(DrugId.TOBACCO, ModItems.BITTER_RESIDUE);
        bind(DrugId.WEED, ModItems.CALMING_RESIN);
        bind(DrugId.HASH, ModItems.PRESSED_CALM);
        bind(DrugId.ALCOHOL, ModItems.FERMENTED_MEMORY);
        bind(DrugId.COCAINE, ModItems.REDLINE_FUEL);
        bind(DrugId.LSD, ModItems.DREAM_RESIDUE);
        bind(DrugId.METH, ModItems.OVERDRIVE_FUEL);
        bind(DrugId.MUSHROOMS, ModItems.MYCELIAL_INSIGHT);
    }

    private IntegrationMaterials() {
    }

    private static void bind(DrugId drugId, DeferredItem<? extends Item> item) {
        MATERIALS.put(drugId, item);
    }

    public static @Nullable Item itemFor(DrugId drugId) {
        Supplier<? extends Item> supplier = MATERIALS.get(drugId);
        return supplier == null ? null : supplier.get();
    }

    public static String itemIdFor(DrugId drugId) {
        Item item = itemFor(drugId);
        return item == null ? "" : item.builtInRegistryHolder().key().location().toString();
    }

    public static boolean matches(DrugId drugId, ItemStack stack) {
        Item item = itemFor(drugId);
        return item != null && stack.is(item);
    }

    public static @Nullable DrugId drugFor(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        for (Map.Entry<DrugId, Supplier<? extends Item>> entry : MATERIALS.entrySet()) {
            if (stack.is(entry.getValue().get())) {
                return entry.getKey();
            }
        }
        return null;
    }

    public static boolean isIntegrationMaterial(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        for (Supplier<? extends Item> supplier : MATERIALS.values()) {
            if (stack.is(supplier.get())) {
                return true;
            }
        }
        return false;
    }
}
