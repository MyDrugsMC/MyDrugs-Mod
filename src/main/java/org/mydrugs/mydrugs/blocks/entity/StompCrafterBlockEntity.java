package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingInput;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StompCrafterBlockEntity extends BlockEntity {
    private static final int MAX_SLOTS = 32;

    private final List<ItemStack> insertedItems = new ArrayList<>();
    private ItemStack displayStack = ItemStack.EMPTY;
    private int progress = 0;

    public StompCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STOMP_CRAFTER.get(), pos, state);
    }

    public List<ItemStack> getInsertedItems() {
        return this.insertedItems;
    }

    public int getContainerSize() {
        return MAX_SLOTS;
    }

    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= this.insertedItems.size()) {
            return ItemStack.EMPTY;
        }
        return this.insertedItems.get(slot);
    }

    public ItemStack getDisplayStack() {
        return this.displayStack;
    }

    public int getProgress() {
        return this.progress;
    }

    public boolean isFull() {
        return this.insertedItems.size() >= MAX_SLOTS;
    }

    public List<ItemStack> getUniqueExampleStacks() {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : this.insertedItems) {
            if (stack.isEmpty()) continue;

            boolean alreadyPresent = false;
            for (ItemStack existing : result) {
                if (ItemStack.isSameItemSameComponents(existing, stack)) {
                    alreadyPresent = true;
                    break;
                }
            }

            if (!alreadyPresent) {
                result.add(stack.copyWithCount(1));
            }
        }

        return result;
    }

    public boolean tryInsertItem(ServerLevel level, ItemStack heldStack) {
        if (heldStack.isEmpty() || this.isFull()) {
            return false;
        }

        ItemStack one = heldStack.copyWithCount(1);
        List<ItemStack> test = new ArrayList<>(this.insertedItems);
        test.add(one);

        if (!canStillMatchAnyRecipe(level, test)) {
            return false;
        }

        this.insertedItems.add(one);
        this.displayStack = buildDisplayStackFor(this.insertedItems, one);
        this.progress = 0;
        this.markUpdated();
        return true;
    }

    private static ItemStack buildDisplayStackFor(List<ItemStack> items, ItemStack basis) {
        int count = 0;
        for (ItemStack stack : items) {
            if (ItemStack.isSameItemSameComponents(stack, basis)) {
                count++;
            }
        }

        ItemStack shown = basis.copy();
        shown.setCount(Math.min(count, shown.getMaxStackSize()));
        return shown;
    }

    private boolean canStillMatchAnyRecipe(ServerLevel level, List<ItemStack> testItems) {
        for (RecipeHolder<?> rawHolder : level.recipeAccess().recipeMap().byType(ModRecipeTypes.STOMP_CRAFTING.get())) {
            if (rawHolder.value() instanceof StompCraftingRecipe recipe) {
                if (recipe.canAcceptPartial(testItems)) {
                    return true;
                }
            }
        }
        return false;
    }

    public @Nullable RecipeHolder<StompCraftingRecipe> getCurrentRecipe(ServerLevel level) {
        StompCraftingInput input = new StompCraftingInput(this.insertedItems);

        Optional<RecipeHolder<StompCraftingRecipe>> found =
                level.recipeAccess().getRecipeFor(ModRecipeTypes.STOMP_CRAFTING.get(), input, level);

        return found.orElse(null);
    }

    public void addProgressFromFall(ServerLevel level, double fallDistance) {
        RecipeHolder<StompCraftingRecipe> holder = getCurrentRecipe(level);
        if (holder == null) {
            return;
        }

        int gained = Math.max(1, (int) Math.floor(fallDistance * 12.0D));
        this.progress = Mth.clamp(this.progress + gained, 0, 100);
        this.markUpdated();

        if (this.progress >= 100) {
            ItemStack result = holder.value().assemble(
                    new StompCraftingInput(this.insertedItems),
                    level.registryAccess()
            );

            Containers.dropItemStack(
                    level,
                    this.worldPosition.getX() + 0.5D,
                    this.worldPosition.getY() + 1.0D,
                    this.worldPosition.getZ() + 0.5D,
                    result
            );

            clearCrafter();
        }
    }

    public void clearCrafter() {
        this.insertedItems.clear();
        this.displayStack = ItemStack.EMPTY;
        this.progress = 0;
        this.markUpdated();
    }

    private void markUpdated() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, 3);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.progress = input.getIntOr("progress", 0);

        this.insertedItems.clear();
        this.insertedItems.addAll(input.read("items", ItemStack.CODEC.listOf()).orElse(List.of()));

        if (this.insertedItems.size() > MAX_SLOTS) {
            this.insertedItems.subList(MAX_SLOTS, this.insertedItems.size()).clear();
        }

        this.displayStack = input.read("display", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("progress", this.progress);
        output.store("items", ItemStack.CODEC.listOf(), this.insertedItems);

        if (!this.displayStack.isEmpty()) {
            output.store("display", ItemStack.CODEC, this.displayStack);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (this.level == null || this.level.isClientSide()) {
            return;
        }

        for (ItemStack stack : this.insertedItems) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        stack
                );
            }
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }

    public boolean canAcceptInsertion(ServerLevel level, ItemStack heldStack) {
        if (heldStack.isEmpty() || this.isFull()) {
            return false;
        }

        List<ItemStack> test = new ArrayList<>(this.insertedItems);
        test.add(heldStack.copyWithCount(1));

        for (RecipeHolder<?> rawHolder : level.recipeAccess().recipeMap().byType(ModRecipeTypes.STOMP_CRAFTING.get())) {
            if (rawHolder.value() instanceof StompCraftingRecipe recipe) {
                if (recipe.canAcceptPartial(test)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void insertAcceptedItem(ItemStack oneItem) {
        if (oneItem.isEmpty() || this.isFull()) {
            return;
        }

        ItemStack inserted = oneItem.copyWithCount(1);
        this.insertedItems.add(inserted);
        this.displayStack = buildDisplayStackFor(this.insertedItems, inserted);
        this.progress = 0;
        this.markUpdated();
    }
}