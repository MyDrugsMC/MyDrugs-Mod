package org.mydrugs.mydrugs.machine.handler;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

import java.util.function.IntConsumer;

public class SyncedItemStacksHandler extends ItemStacksResourceHandler {
    private final Runnable onChanged;
    private final IntConsumer slotChangedCallback;

    public SyncedItemStacksHandler(int size, Runnable onChanged) {
        this(size, onChanged, null);
    }

    public SyncedItemStacksHandler(int size, Runnable onChanged, IntConsumer slotChangedCallback) {
        super(size);
        this.onChanged = onChanged;
        this.slotChangedCallback = slotChangedCallback;
    }

    public NonNullList<ItemStack> list() {
        return this.stacks;
    }

    @Override
    protected void onContentsChanged(int index, ItemStack previousStack) {
        if (this.slotChangedCallback != null) {
            this.slotChangedCallback.accept(index);
        }

        if (this.onChanged != null) {
            this.onChanged.run();
        }
    }
}