package org.mydrugs.mydrugs.recovery.item;

import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.registry.ItemSpec;

public final class ModRecoveryItems {
    public static final DeferredItem<HeadphonesItem> HEADPHONES =
            new ItemSpec<>("headphones", HeadphonesItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<HerbalTeaItem> HERBAL_TEA =
            new ItemSpec<>("herbal_tea", HerbalTeaItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<CalmingMixtureItem> CALMING_MIXTURE =
            new ItemSpec<>("calming_mixture", CalmingMixtureItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<SleepingAidItem> SLEEPING_AID =
            new ItemSpec<>("sleeping_aid", SleepingAidItem::new).register(ModItems.ITEMS);
    public static final DeferredItem<OverdoseAntidoteItem> OVERDOSE_ANTIDOTE =
            new ItemSpec<>("overdose_antidote", OverdoseAntidoteItem::new).register(ModItems.ITEMS);

    private ModRecoveryItems() {
    }
}
