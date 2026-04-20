package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;

public class ClayVatBlockEntity extends BlockEntity {
    public static final int FLUID_CAPACITY = 8000;

    @Nullable
    private ResourceLocation fluidId = null;
    private int fluidAmount = 0;

    public ClayVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CLAY_VAT.get(), pos, state);
    }

    public boolean hasFluid() {
        return fluidId != null && fluidAmount > 0;
    }

    @Nullable
    public ResourceLocation getVisualFluidId() {
        return hasFluid() ? fluidId : null;
    }

    public int getVisualFluidAmount() {
        return fluidAmount;
    }

    public float getVisualFluidRatio() {
        return Math.min(1.0f, fluidAmount / (float) FLUID_CAPACITY);
    }

    private int getInsertableAmount(ResourceLocation incomingId, int requestedAmount) {
        if (requestedAmount <= 0) {
            return 0;
        }

        int freeSpace = FLUID_CAPACITY - fluidAmount;
        if (freeSpace <= 0) {
            return 0;
        }

        // Empty vat: any fluid can enter
        if (!hasFluid()) {
            return Math.min(requestedAmount, freeSpace);
        }

        // Non-empty vat: only same fluid can enter
        if (!incomingId.equals(fluidId)) {
            return 0;
        }

        return Math.min(requestedAmount, freeSpace);
    }

    private void addFluid(ResourceLocation incomingId, int amount) {
        if (amount <= 0) {
            return;
        }

        if (!hasFluid()) {
            fluidId = incomingId;
            fluidAmount = amount;
            return;
        }

        if (incomingId.equals(fluidId)) {
            fluidAmount += amount;
        }
    }

    private void removeFluid(int amount) {
        if (amount <= 0 || !hasFluid()) {
            return;
        }

        fluidAmount -= amount;
        if (fluidAmount <= 0) {
            fluidAmount = 0;
            fluidId = null;
        }
    }

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        Fluid fluid = resource.getFluid();
        if (fluid == Fluids.EMPTY) {
            return false;
        }

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(fluid);
        if (incomingId == null) {
            return false;
        }

        int requested = getInsertableAmount(incomingId, containedAmount);
        if (requested <= 0) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, requested, tx);
            if (extracted <= 0) {
                return false;
            }

            tx.commit();
            addFluid(incomingId, extracted);
        }

        notifyUpdate();
        return true;
    }

    public boolean tryExtractFluidToHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty() || !hasFluid()) {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = FluidResource.of(fluid);

        int transferred;
        try (var tx = Transaction.openRoot()) {
            transferred = handler.insert(resource, fluidAmount, tx);
            if (transferred <= 0) {
                return false;
            }

            tx.commit();
        }

        removeFluid(transferred);
        notifyUpdate();
        return true;
    }

    private void notifyUpdate() {
        setChanged();

        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putString("fluid", fluidId == null ? "" : fluidId.toString());
        output.putInt("fluid_amount", fluidAmount);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        String id = input.getStringOr("fluid", "");
        fluidId = id.isEmpty() ? null : ResourceLocation.parse(id);
        fluidAmount = input.getIntOr("fluid_amount", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}