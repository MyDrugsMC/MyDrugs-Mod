package org.mydrugs.mydrugs.items.registry;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Function;
import java.util.function.UnaryOperator;

public record ItemSpec<T extends Item>(
        String id,
        Function<Item.Properties, T> factory,
        UnaryOperator<Item.Properties> properties
) {
    public ItemSpec(String id, Function<Item.Properties, T> factory) {
        this(id, factory, UnaryOperator.identity());
    }

    public DeferredItem<T> register(DeferredRegister.Items items) {
        return items.registerItem(id, factory, properties);
    }
}
