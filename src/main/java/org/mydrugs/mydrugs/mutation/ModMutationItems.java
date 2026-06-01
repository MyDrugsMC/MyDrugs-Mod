package org.mydrugs.mydrugs.mutation;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.items.*;
import org.mydrugs.mydrugs.items.registry.ItemSpec;

public final class ModMutationItems {
    public static final DeferredItem<Item> SYRINGE =
            new ItemSpec<Item>("syringe", SyringeItem::new, props -> props.stacksTo(1)).register(ModItems.ITEMS);
    public static final DeferredItem<Item> DNA_SCRAPER =
            new ItemSpec<Item>("dna_scraper", DnaScraperItem::new, props -> props.stacksTo(1).durability(96)).register(ModItems.ITEMS);
    public static final DeferredItem<Item> DNA_SCRAP =
            new ItemSpec<Item>("dna_scrap", DnaScrapItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<Item> DNA_GENE =
            new ItemSpec<Item>("dna_gene", DnaGeneItem::new).register(ModItems.ITEMS);
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
