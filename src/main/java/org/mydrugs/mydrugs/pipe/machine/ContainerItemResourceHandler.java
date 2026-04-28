package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

final class ContainerItemResourceHandler implements ResourceHandler<ItemResource> {
    private final Container container;
    private final BlockEntity blockEntity;
    private final Set<Integer> insertSlots;
    private final Set<Integer> extractSlots;
    private final List<SlotJournal> journals = new ArrayList<>();

    ContainerItemResourceHandler(Container container, BlockEntity blockEntity, Set<Integer> insertSlots, Set<Integer> extractSlots) {
        this.container = container;
        this.blockEntity = blockEntity;
        this.insertSlots = Set.copyOf(insertSlots);
        this.extractSlots = Set.copyOf(extractSlots);
        for (int i = 0; i < container.getContainerSize(); i++) {
            this.journals.add(new SlotJournal(i));
        }
    }

    @Override
    public int size() {
        return this.container.getContainerSize();
    }

    @Override
    public ItemResource getResource(int slot) {
        Objects.checkIndex(slot, this.size());
        return ItemResource.of(this.container.getItem(slot));
    }

    @Override
    public long getAmountAsLong(int slot) {
        Objects.checkIndex(slot, this.size());
        return this.container.getItem(slot).getCount();
    }

    @Override
    public long getCapacityAsLong(int slot, ItemResource resource) {
        Objects.checkIndex(slot, this.size());
        if (!resource.isEmpty() && (!this.insertSlots.contains(slot) || !this.isValid(slot, resource))) {
            return 0;
        }
        return resource.isEmpty() ? Item.ABSOLUTE_MAX_STACK_SIZE : Math.min(resource.getMaxStackSize(), this.container.getMaxStackSize());
    }

    @Override
    public boolean isValid(int slot, ItemResource resource) {
        Objects.checkIndex(slot, this.size());
        return this.insertSlots.contains(slot)
                && !resource.isEmpty()
                && this.container.canPlaceItem(slot, resource.toStack(1));
    }

    @Override
    public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(slot, this.size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!this.insertSlots.contains(slot) || !this.isValid(slot, resource)) {
            return 0;
        }

        ItemStack current = this.container.getItem(slot);
        int currentAmount = current.getCount();
        if (!current.isEmpty() && !resource.matches(current)) {
            return 0;
        }

        int capacity = (int) this.getCapacityAsLong(slot, resource);
        int inserted = Math.min(amount, capacity - currentAmount);
        if (inserted <= 0) {
            return 0;
        }

        this.journals.get(slot).updateSnapshots(transaction);
        this.container.setItem(slot, resource.toStack(currentAmount + inserted));
        return inserted;
    }

    @Override
    public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
        Objects.checkIndex(slot, this.size());
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!this.extractSlots.contains(slot)) {
            return 0;
        }

        ItemStack current = this.container.getItem(slot);
        if (current.isEmpty() || !resource.matches(current)) {
            return 0;
        }

        int extracted = Math.min(amount, current.getCount());
        if (extracted <= 0) {
            return 0;
        }

        this.journals.get(slot).updateSnapshots(transaction);
        ItemStack remaining = current.copy();
        remaining.shrink(extracted);
        this.container.setItem(slot, remaining);
        return extracted;
    }

    private final class SlotJournal extends SnapshotJournal<ItemStack> {
        private final int slot;

        private SlotJournal(int slot) {
            this.slot = slot;
        }

        @Override
        protected ItemStack createSnapshot() {
            return container.getItem(this.slot).copy();
        }

        @Override
        protected void revertToSnapshot(ItemStack snapshot) {
            container.setItem(this.slot, snapshot.copy());
        }

        @Override
        protected void onRootCommit(ItemStack originalState) {
            container.setChanged();
            MachineTransferAttachments.markCapabilityChanged(blockEntity);
        }
    }
}
