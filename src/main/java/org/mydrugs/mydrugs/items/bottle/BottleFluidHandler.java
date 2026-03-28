package org.mydrugs.mydrugs.items.bottle;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;

public class BottleFluidHandler extends ItemAccessResourceHandler<FluidResource> {
    public BottleFluidHandler(ItemAccess itemAccess) {
        super(itemAccess, 1);
    }

    private static BottleFluidContent getContent(ItemResource accessResource) {
        return accessResource.toStack().get(org.mydrugs.mydrugs.registry.ModDataComponents.BOTTLE_CONTENT.get());
    }

    @Override
    protected FluidResource getResourceFrom(ItemResource accessResource, int index) {
        BottleFluidContent content = getContent(accessResource);
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
    protected int getAmountFrom(ItemResource accessResource, int index) {
        BottleFluidContent content = getContent(accessResource);
        return content == null ? 0 : content.amountMb();
    }

    @Override
    protected ItemResource update(ItemResource accessResource, int index, FluidResource newResource, int newAmount) {
        ItemStack updatedStack = accessResource.toStack();

        if (newAmount <= 0) {
            GlassBottleItem.setContent(updatedStack, null, 0);
            return ItemResource.of(updatedStack);
        }

        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(newResource.getFluid());
        if (fluidId == null) {
            return ItemResource.EMPTY;
        }

        GlassBottleItem.setContent(updatedStack, fluidId, newAmount);
        return ItemResource.of(updatedStack);
    }

    @Override
    protected int getCapacity(int index, FluidResource resource) {
        return GlassBottleItem.CAPACITY_MB;
    }

    @Override
    public boolean isValid(int index, FluidResource resource) {
        if (resource.isEmpty()) {
            return false;
        }

        return GlassBottleItem.isFluidBottlable(resource.getFluid());
    }
}