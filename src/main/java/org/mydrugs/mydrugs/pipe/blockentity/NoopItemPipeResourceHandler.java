package org.mydrugs.mydrugs.pipe.blockentity;

import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

final class NoopItemPipeResourceHandler implements ResourceHandler<ItemResource> {
    static final NoopItemPipeResourceHandler INSTANCE = new NoopItemPipeResourceHandler();

    private NoopItemPipeResourceHandler() {
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public ItemResource getResource(int slot) {
        return ItemResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int slot) {
        return 0;
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        return 0;
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        return false;
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        return 0;
    }
}
