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
    public static final TagKey<Item> PSY_ANVIL_CORES = tag("psy_anvil_cores");
    public static final TagKey<Item> PSY_MIXER_AWAKENING_CORES = tag("psy_mixer_awakening_cores");
    public static final TagKey<Item> RESONANCE_SHARDS = tag("resonance_shards");
    public static final TagKey<Item> RITUAL_FIBERS = tag("ritual_fibers");

    private ModItemTags() {
    }

    private static TagKey<Item> tag(String path) {
        return TagKey.create(
                Registries.ITEM,
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path)
        );
    }
}
