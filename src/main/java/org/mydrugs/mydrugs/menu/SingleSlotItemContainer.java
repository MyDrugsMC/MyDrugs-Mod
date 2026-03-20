package org.mydrugs.mydrugs.menu;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

public class SingleSlotItemContainer extends SimpleContainer {
    private final ItemStack carrier;

    public SingleSlotItemContainer(ItemStack carrier) {
        super(1);
        this.carrier = carrier;

        if (!carrier.isEmpty()) {
            ItemContainerContents contents =
                    carrier.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY);
            contents.copyInto(this.getItems());
        }
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (!this.carrier.isEmpty()) {
            this.carrier.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(this.getItems()));
        }
    }
}