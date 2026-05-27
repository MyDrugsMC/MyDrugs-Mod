package org.mydrugs.mydrugs.items;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModItemTags {
    public static final TagKey<Item> INTEGRATION_CORE_SEED_SOURCES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "integration_core_seed_sources")
    );

    private ModItemTags() {
    }
}
