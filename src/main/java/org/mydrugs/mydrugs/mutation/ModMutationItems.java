package org.mydrugs.mydrugs.mutation;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.items.*;
import org.mydrugs.mydrugs.items.registry.ItemSpec;

public final class ModMutationItems {
    public static final DeferredItem<Item> SYRINGE =
            new ItemSpec<Item>("syringe", SyringeItem::new, props -> props.stacksTo(1)).register(ModItems.ITEMS);
    public static final DeferredItem<Item> ADN_SCRAPER =
            new ItemSpec<Item>("adn_scraper", AdnScraperItem::new, props -> props.stacksTo(1).durability(96)).register(ModItems.ITEMS);
    public static final DeferredItem<Item> ADN_SCRAP =
            new ItemSpec<Item>("adn_scrap", AdnScrapItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<Item> ADN_GENE =
            new ItemSpec<Item>("adn_gene", AdnGeneItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<Item> MUTATION_VECTOR =
            ModItems.ITEMS.registerItem("mutation_vector", props -> new MutationPayloadItem(
                    props,
                    "tooltip.mydrugs.mutation_vector.empty",
                    "tooltip.mydrugs.mutation_vector.footer"
            ));
    public static final DeferredItem<Item> MUTAGENIC_BLOOD_VIAL =
            ModItems.ITEMS.registerItem("mutagenic_blood_vial", props -> new MutationPayloadItem(
                    props,
                    "tooltip.mydrugs.mutagenic_blood_vial.empty",
                    "tooltip.mydrugs.mutagenic_blood_vial.footer"
            ));
    public static final DeferredItem<Item> NUTRIENT_GEL =
            ModItems.ITEMS.registerSimpleItem("nutrient_gel");

    private ModMutationItems() {
    }
}
