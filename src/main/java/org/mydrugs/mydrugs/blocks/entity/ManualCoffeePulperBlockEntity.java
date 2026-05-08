package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.machine.manual.ManualMachineSpeedHelper;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.menu.ManualCoffeePulperMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.coffee_pulping.CoffeePulpingRecipe;

import java.util.Optional;

public class ManualCoffeePulperBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_BEAN_OUTPUT = 1;
    public static final int SLOT_BIOMASS_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = 80;
    private float rollerAngle;
    private float workBuffer;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> Math.round(rollerAngle * 10.0F);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = Math.max(1, value);
                case 2 -> rollerAngle = value / 10.0F;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 3;
        }
    };

    public ManualCoffeePulperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MANUAL_COFFEE_PULPER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ManualCoffeePulperBlockEntity be) {
        if (be.workBuffer <= 0.0F) {
            return;
        }
        Optional<RecipeHolder<CoffeePulpingRecipe>> match = be.getRecipe();
        if (match.isEmpty()) {
            be.progress = 0;
            be.workBuffer = 0.0F;
            be.markDirtyAndSync();
            return;
        }
        CoffeePulpingRecipe recipe = match.get().value();
        be.maxProgress = recipe.work();
        if (!be.canCraft(recipe)) {
            be.workBuffer = 0.0F;
            be.markDirtyAndSync();
            return;
        }
        int gained = (int) be.workBuffer;
        if (gained > 0) {
            be.workBuffer -= gained;
            be.progress = Mth.clamp(be.progress + gained, 0, be.maxProgress);
            if (be.progress >= be.maxProgress) {
                be.craft(recipe);
            }
            be.markDirtyAndSync();
        }
    }

    public void addManualWork(Player player, float amount) {
        if (!(this.level instanceof ServerLevel)) return;
        if (player.distanceToSqr(this.worldPosition.getX() + 0.5D, this.worldPosition.getY() + 0.5D, this.worldPosition.getZ() + 0.5D) > 64.0D) return;
        Optional<RecipeHolder<CoffeePulpingRecipe>> match = getRecipe();
        if (match.isEmpty() || !canCraft(match.get().value())) return;
        float multiplier = player instanceof ServerPlayer serverPlayer
                ? ManualMachineSpeedHelper.getSpeedMultiplier(serverPlayer, ManualMachineType.COFFEE_PULPER)
                : 1.0F;
        float clamped = Mth.clamp(amount * multiplier, 0.0F, 16.0F);
        this.workBuffer += clamped;
        this.rollerAngle = (this.rollerAngle + clamped * 18.0F) % 360.0F;
        markDirtyAndSync();
    }

    private Optional<RecipeHolder<CoffeePulpingRecipe>> getRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) return Optional.empty();
        ItemStack input = this.items.get(SLOT_INPUT);
        if (input.isEmpty()) return Optional.empty();
        return serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.COFFEE_PULPING.get(), new SingleRecipeInput(input), serverLevel);
    }

    private boolean canAccept(int slot, ItemStack stack) {
        if (stack.isEmpty()) return true;
        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, stack)) return false;
        return existing.getCount() + stack.getCount() <= Math.min(existing.getMaxStackSize(), getMaxStackSize());
    }

    private boolean canCraft(CoffeePulpingRecipe recipe) {
        return canAccept(SLOT_BEAN_OUTPUT, recipe.beanResult()) && canAccept(SLOT_BIOMASS_OUTPUT, recipe.biomassResult());
    }

    private void insertOutput(int slot, ItemStack stack) {
        if (stack.isEmpty()) return;
        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) {
            this.items.set(slot, stack.copy());
        } else {
            existing.grow(stack.getCount());
        }
    }

    private void craft(CoffeePulpingRecipe recipe) {
        this.removeItem(SLOT_INPUT, 1);
        insertOutput(SLOT_BEAN_OUTPUT, recipe.beanResult().copy());
        insertOutput(SLOT_BIOMASS_OUTPUT, recipe.biomassResult().copy());
        this.progress = 0;
        this.workBuffer = 0.0F;
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
    }

    public ContainerData getData() {
        return data;
    }

    public float getRollerAngle() {
        return rollerAngle;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mydrugs.manual_coffee_pulper");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new ManualCoffeePulperMenu(containerId, inventory, this, this.data, ContainerLevelAccess.create(player.level(), this.worldPosition));
    }

    @Override
    public int getContainerSize() { return SLOT_COUNT; }
    @Override
    public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override
    public ItemStack getItem(int slot) { return slot >= 0 && slot < SLOT_COUNT ? items.get(slot) : ItemStack.EMPTY; }
    @Override
    public ItemStack removeItem(int slot, int amount) { ItemStack stack = net.minecraft.world.ContainerHelper.removeItem(items, slot, amount); if (!stack.isEmpty()) setChanged(); return stack; }
    @Override
    public ItemStack removeItemNoUpdate(int slot) { return net.minecraft.world.ContainerHelper.takeItem(items, slot); }
    @Override
    public void setItem(int slot, ItemStack stack) { if (slot >= 0 && slot < SLOT_COUNT) { items.set(slot, stack); setChanged(); } }
    @Override
    public boolean stillValid(Player player) { return Container.stillValidBlockEntity(this, player); }
    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot != SLOT_INPUT || stack.isEmpty()) return false;
        if (this.level == null || this.level.isClientSide()) return stack.is(ModItems.COFFEE_CHERRIES.get());
        return getRecipeFor(stack).isPresent();
    }
    @Override
    public void clearContent() {
        for (int i = 0; i < items.size(); i++) {
            items.set(i, ItemStack.EMPTY);
        }
    }

    private Optional<RecipeHolder<CoffeePulpingRecipe>> getRecipeFor(ItemStack stack) {
        if (!(this.level instanceof ServerLevel serverLevel) || stack.isEmpty()) return Optional.empty();
        return serverLevel.recipeAccess().getRecipeFor(ModRecipeTypes.COFFEE_PULPING.get(), new SingleRecipeInput(stack), serverLevel);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) output.store("item_" + i, ItemStack.CODEC, items.get(i));
        }
        output.putInt("progress", progress);
        output.putInt("max_progress", maxProgress);
        output.putInt("roller_angle_x10", Math.round(rollerAngle * 10.0F));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (int i = 0; i < items.size(); i++) {
            items.set(i, input.read("item_" + i, ItemStack.CODEC).orElse(ItemStack.EMPTY));
        }
        progress = input.getIntOr("progress", 0);
        maxProgress = input.getIntOr("max_progress", 80);
        rollerAngle = input.getIntOr("roller_angle_x10", 0) / 10.0F;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide()) return;
        for (ItemStack stack : items) if (!stack.isEmpty()) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
    }

    private void markDirtyAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) { return this.saveWithoutMetadata(registries); }
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
    @Override
    public void onDataPacket(Connection connection, ValueInput input) { super.onDataPacket(connection, input); }
}
