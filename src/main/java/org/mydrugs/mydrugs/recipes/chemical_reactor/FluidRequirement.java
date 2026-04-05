package org.mydrugs.mydrugs.recipes.chemical_reactor;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

public record FluidRequirement(ResourceLocation fluidId, int amount) {
    public boolean test(FluidStack stack) {
        if (stack == null || stack.isEmpty() || stack.getAmount() < amount) {
            return false;
        }

        ResourceLocation stackId = BuiltInRegistries.FLUID.getKey(stack.getFluid());
        return fluidId.equals(stackId);
    }
}