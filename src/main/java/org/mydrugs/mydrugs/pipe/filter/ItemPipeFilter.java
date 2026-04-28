package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class ItemPipeFilter {
    private ItemPipeFilter() {
    }

    public static boolean allows(PipeFilterConfig config, ItemResource resource) {
        if (resource == null || resource.isEmpty()) {
            return false;
        }

        return config.allows(BuiltInRegistries.ITEM.getKey(resource.getItem()));
    }
}
