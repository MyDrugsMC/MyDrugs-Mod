package org.mydrugs.mydrugs.recipes.chemical_reactor;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasType;

public record GasRequirement(ResourceLocation gasId, long amount) {
    public boolean test(GasStack stack) {
        if (stack == null || stack.isEmpty() || stack.amount() < amount) {
            return false;
        }

        GasType type = stack.type();
        if (type == null) {
            return false;
        }

        return gasId.equals(type.id());
    }
}