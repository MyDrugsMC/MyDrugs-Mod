package org.mydrugs.mydrugs.items.bottle;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.registry.ModDataComponents;

public class BottleFluidHandler extends SnapshotJournal<ItemStack> implements ResourceHandler<FluidResource> {
    private final ItemStack stack;

    public BottleFluidHandler(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int size() {
        return 1;
    }

    @Override
    public FluidResource getResource(int index) {
        checkIndex(index);

        BottleFluidContent content = getContent();
        if (content == null) {
            return FluidResource.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(content.fluidId());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidResource.EMPTY;
        }

        return FluidResource.of(fluid);
    }

    @Override
    public long getAmountAsLong(int index) {
        checkIndex(index);

        BottleFluidContent content = getContent();
        return content == null ? 0 : content.amountMb();
    }

    @Override
    public long getCapacityAsLong(int index, FluidResource resource) {
        checkIndex(index);
        return GlassBottleItem.CAPACITY_MB;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        checkIndex(index);
        return !resource.isEmpty();
    }

    @Override
    public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
        checkIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (incomingId == null) {
            return 0;
        }

        BottleFluidContent content = getContent();
        int stored = content == null ? 0 : content.amountMb();

        if (content != null && !content.fluidId().equals(incomingId)) {
            return 0;
        }

        int space = GlassBottleItem.CAPACITY_MB - stored;
        if (space <= 0) {
            return 0;
        }

        int inserted = Math.min(amount, space);
        if (inserted <= 0) {
            return 0;
        }

        updateSnapshots(transaction);
        setContent(incomingId, stored + inserted);
        return inserted;
    }

    @Override
    public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
        checkIndex(index);
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);

        BottleFluidContent content = getContent();
        if (content == null) {
            return 0;
        }

        ResourceLocation requestedId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (requestedId == null || !content.fluidId().equals(requestedId)) {
            return 0;
        }

        int extracted = Math.min(amount, content.amountMb());
        if (extracted <= 0) {
            return 0;
        }

        updateSnapshots(transaction);

        int remaining = content.amountMb() - extracted;
        if (remaining <= 0) {
            clearContent();
        } else {
            setContent(content.fluidId(), remaining);
        }

        return extracted;
    }

    @Override
    protected ItemStack createSnapshot() {
        return stack.copy();
    }

    @Override
    protected void revertToSnapshot(ItemStack snapshot) {
        BottleFluidContent content = snapshot.get(ModDataComponents.BOTTLE_CONTENT.get());
        if (content == null) {
            stack.remove(ModDataComponents.BOTTLE_CONTENT.get());
        } else {
            stack.set(ModDataComponents.BOTTLE_CONTENT.get(), content);
        }
    }

    private BottleFluidContent getContent() {
        return stack.get(ModDataComponents.BOTTLE_CONTENT.get());
    }

    private void setContent(ResourceLocation fluidId, int amountMb) {
        stack.set(ModDataComponents.BOTTLE_CONTENT.get(), new BottleFluidContent(fluidId, amountMb));
    }

    private void clearContent() {
        stack.remove(ModDataComponents.BOTTLE_CONTENT.get());
    }

    private static void checkIndex(int index) {
        if (index != 0) {
            throw new IndexOutOfBoundsException("Bottle has only one tank index: 0");
        }
    }
}