package org.mydrugs.mydrugs.diary;

import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.registry.ItemSpec;

public final class ModDiaryItems {
    public static final DeferredItem<PersonalDiaryItem> PERSONAL_DIARY =
            new ItemSpec<>("personal_diary", PersonalDiaryItem::new).register(ModItems.ITEMS);

    private ModDiaryItems() {
    }
}
