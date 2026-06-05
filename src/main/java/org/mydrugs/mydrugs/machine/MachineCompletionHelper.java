package org.mydrugs.mydrugs.machine;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.gas.GasType;

import java.util.Optional;

public final class MachineCompletionHelper {
    private MachineCompletionHelper() {
    }

    public static ResourceLocation machineId(BlockEntity blockEntity) {
        return BuiltInRegistries.BLOCK.getKey(blockEntity.getBlockState().getBlock());
    }

    public static Optional<ResourceLocation> itemId(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    public static Optional<ResourceLocation> fluidId(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return fluidId(stack.getFluid());
    }

    public static Optional<ResourceLocation> fluidId(Fluid fluid) {
        if (fluid == null || fluid == Fluids.EMPTY) {
            return Optional.empty();
        }
        return Optional.of(BuiltInRegistries.FLUID.getKey(fluid));
    }

    public static Optional<String> gasId(GasType gas) {
        return gas == null ? Optional.empty() : Optional.of(gas.id().toString());
    }
}
