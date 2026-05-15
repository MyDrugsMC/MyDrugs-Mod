package org.mydrugs.mydrugs.pipe.item;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.registry.ItemSpec;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterUpgradeItem;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferUpgradeItem;

public final class ModPipeItems {
    public static final DeferredItem<Item> PIPE_WRENCH =
            new ItemSpec<Item>("pipe_wrench", PipeWrenchItem::new, props -> props.stacksTo(1).durability(256)).register(ModItems.ITEMS);
    public static final DeferredItem<Item> PIPE_FILTER_UPGRADE =
            ModItems.ITEMS.registerItem(
                    "pipe_filter_upgrade",
                    PipeFilterUpgradeItem::new,
                    props -> props.stacksTo(1).component(
                            ModDataComponents.PIPE_FILTER_CONFIG.get(),
                            PipeFilterConfig.empty(PipeResourceKind.ITEM)
                    )
            );
    public static final DeferredItem<Item> MACHINE_TRANSFER_UPGRADE =
            new ItemSpec<Item>("machine_transfer_upgrade", MachineTransferUpgradeItem::new, props -> props.stacksTo(1)).register(ModItems.ITEMS);

    private ModPipeItems() {
    }
}
