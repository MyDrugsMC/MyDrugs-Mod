package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Runtime binding of {@link IntegrationCoreTier} -> {@link Item}. Populated once during mod
 * registration from {@code ModItems} so the tier enum can stay free of a {@code ModItems}
 * dependency (and thus stay unit-testable).
 */
public final class IntegrationCoreTiers {
    private static final Map<IntegrationCoreTier, Supplier<? extends Item>> SUPPLIERS =
            new EnumMap<>(IntegrationCoreTier.class);
    private static final Map<Item, IntegrationCoreTier> REVERSE = new IdentityHashMap<>();
    private static boolean reverseDirty = true;

    private IntegrationCoreTiers() {
    }

    /** Binds the core item that represents a given tier. Idempotent; later calls overwrite. */
    public static void bind(IntegrationCoreTier tier, Supplier<? extends Item> supplier) {
        if (tier == null || supplier == null) {
            return;
        }
        SUPPLIERS.put(tier, supplier);
        reverseDirty = true;
    }

    public static @Nullable Item itemFor(IntegrationCoreTier tier) {
        Supplier<? extends Item> supplier = tier == null ? null : SUPPLIERS.get(tier);
        return supplier == null ? null : supplier.get();
    }

    public static ItemStack stackFor(IntegrationCoreTier tier) {
        Item item = itemFor(tier);
        return item == null ? ItemStack.EMPTY : new ItemStack(item);
    }

    public static @Nullable IntegrationCoreTier tierOfItem(Item item) {
        if (item == null) {
            return null;
        }
        rebuildReverseIfNeeded();
        return REVERSE.get(item);
    }

    public static boolean isAnyCore(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return tierOfItem(stack.getItem()) != null;
    }

    private static void rebuildReverseIfNeeded() {
        if (!reverseDirty) {
            return;
        }
        REVERSE.clear();
        for (Map.Entry<IntegrationCoreTier, Supplier<? extends Item>> entry : SUPPLIERS.entrySet()) {
            Item item = entry.getValue().get();
            if (item != null) {
                REVERSE.put(item, entry.getKey());
            }
        }
        reverseDirty = false;
    }
}
