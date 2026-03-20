package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipes;

public class GrindingBowlBlockEntity extends BlockEntity {
    private ItemStack storedStack = ItemStack.EMPTY;
    private int progress = 0;
    private int maxProgress = 0;

    public GrindingBowlBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GRINDING_BOWL.get(), pos, state);
    }

    public boolean isEmpty() {
        return this.storedStack.isEmpty();
    }

    public ItemStack getStoredStack() {
        return this.storedStack;
    }

    public int getProgress() {
        return this.progress;
    }

    public int getMaxProgress() {
        return this.maxProgress;
    }

    public boolean canInsert(ItemStack stack) {
        return this.storedStack.isEmpty() && GrindingRecipes.get(stack, (ServerLevel) level) != null;
    }

    public boolean insertOne(ItemStack playerStack) {
        if (!canInsert(playerStack)) {
            return false;
        }

        this.storedStack = playerStack.copyWithCount(1);
        this.progress = 0;

        if (this.level instanceof ServerLevel serverLevel) {
            GrindingRecipe recipe = GrindingRecipes.get(this.storedStack, serverLevel);
            this.maxProgress = recipe != null ? recipe.work() : 0;
        } else {
            this.maxProgress = 0;
        }

        playerStack.shrink(1);
        this.markUpdated();
        return true;
    }

    public boolean canGrind() {
        return !this.storedStack.isEmpty() && GrindingRecipes.get(this.storedStack, (ServerLevel) level) != null;
    }

    public boolean grindOnce() {
        GrindingRecipe recipe = GrindingRecipes.get(storedStack, (ServerLevel) level);

        if (recipe == null) return false;

        this.progress++;

        if (this.progress >= recipe.work()) {
            this.storedStack = recipe.result().copy();
            this.progress = 0;
        }

        this.markUpdated();
        return true;
    }

    public ItemStack removeStack() {
        ItemStack out = this.storedStack;
        this.storedStack = ItemStack.EMPTY;
        this.progress = 0;
        this.maxProgress = 0;
        this.markUpdated();
        return out;
    }

    private void markUpdated() {
        this.setChanged();

        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.storedStack = input.read("stored_item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 0);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable("stored_item", ItemStack.CODEC, this.storedStack.isEmpty() ? null : this.storedStack);
        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (this.level instanceof ServerLevel serverLevel && !this.storedStack.isEmpty()) {
            Containers.dropItemStack(
                    serverLevel,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    this.storedStack
            );
            this.storedStack = ItemStack.EMPTY;
            this.progress = 0;
        }
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