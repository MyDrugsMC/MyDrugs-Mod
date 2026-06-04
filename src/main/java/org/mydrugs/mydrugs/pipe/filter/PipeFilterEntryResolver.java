package org.mydrugs.mydrugs.pipe.filter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;

import java.util.Optional;

public final class PipeFilterEntryResolver {
    private PipeFilterEntryResolver() {
    }

    public static Optional<ResourceLocation> resolve(PipeResourceKind kind, ItemStack stack, Level level) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }
        return switch (kind) {
            case ITEM -> resolveItem(stack);
            case FLUID -> resolveFluid(stack);
            case GAS -> resolveGas(stack);
        };
    }

    public static Optional<ResourceLocation> resolveItem(ItemStack stack) {
        if (stack == null || stack.isEmpty() || stack.is(Items.AIR)) {
            return Optional.empty();
        }
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(stack.getItem());
        return id == null ? Optional.empty() : Optional.of(id);
    }

    public static Optional<ResourceLocation> resolveFluid(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null) {
            return Optional.empty();
        }

        for (int i = 0; i < handler.size(); i++) {
            FluidResource resource = handler.getResource(i);
            if (resource == null || resource.isEmpty() || handler.getAmountAsLong(i) <= 0L) {
                continue;
            }
            ResourceLocation id = BuiltInRegistries.FLUID.getKey(resource.getFluid());
            if (id != null && resource.getFluid() != Fluids.EMPTY) {
                return Optional.of(id);
            }
        }
        return Optional.empty();
    }

    public static Optional<ResourceLocation> resolveGas(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return Optional.empty();
        }

        IGasHandler handler = stack.getCapability(ModGasCapabilities.ITEM, null);
        if (handler == null) {
            return Optional.empty();
        }

        for (int i = 0; i < handler.getTanks(); i++) {
            GasStack gas = handler.getGasInTank(i);
            if (gas != null && !gas.isEmpty() && gas.type() != null) {
                return Optional.of(gas.type().id());
            }
        }
        return Optional.empty();
    }

    public static boolean exists(PipeResourceKind kind, ResourceLocation id) {
        if (id == null) {
            return false;
        }
        return switch (kind) {
            case ITEM -> BuiltInRegistries.ITEM.containsKey(id) && BuiltInRegistries.ITEM.getValue(id) != Items.AIR;
            case FLUID -> BuiltInRegistries.FLUID.containsKey(id) && BuiltInRegistries.FLUID.getValue(id) != Fluids.EMPTY;
            case GAS -> ModGases.get(id) != null;
        };
    }

    public static ItemStack displayStack(PipeResourceKind kind, ResourceLocation id) {
        if (!exists(kind, id)) {
            return ItemStack.EMPTY;
        }
        return switch (kind) {
            case ITEM -> new ItemStack(BuiltInRegistries.ITEM.getValue(id));
            case FLUID -> displayFluid(id);
            case GAS -> displayGas(id);
        };
    }

    public static int toSyncId(PipeResourceKind kind, ResourceLocation id) {
        if (!exists(kind, id)) {
            return -1;
        }
        return switch (kind) {
            case ITEM -> BuiltInRegistries.ITEM.getId(BuiltInRegistries.ITEM.getValue(id));
            case FLUID -> BuiltInRegistries.FLUID.getId(BuiltInRegistries.FLUID.getValue(id));
            case GAS -> ModGases.getSyncId(ModGases.get(id));
        };
    }

    public static Optional<ResourceLocation> fromSyncId(PipeResourceKind kind, int syncId) {
        if (syncId < 0) {
            return Optional.empty();
        }
        ResourceLocation id = switch (kind) {
            case ITEM -> BuiltInRegistries.ITEM.getKey(BuiltInRegistries.ITEM.byId(syncId));
            case FLUID -> BuiltInRegistries.FLUID.getKey(BuiltInRegistries.FLUID.byId(syncId));
            case GAS -> {
                var gas = ModGases.bySyncId(syncId);
                yield gas == null ? null : gas.id();
            }
        };
        return exists(kind, id) ? Optional.of(id) : Optional.empty();
    }

    private static ItemStack displayFluid(ResourceLocation id) {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
        Item bucket = fluid.getBucket();
        return bucket == Items.AIR ? ItemStack.EMPTY : new ItemStack(bucket);
    }

    private static ItemStack displayGas(ResourceLocation id) {
        ItemStack stack = new ItemStack(ModItems.GAS_TANK_ITEM.get());
        stack.set(ModDataComponents.GAS_TANK_CONTENTS.get(), new GasTankContents(id.toString(), 1L));
        return stack;
    }
}
