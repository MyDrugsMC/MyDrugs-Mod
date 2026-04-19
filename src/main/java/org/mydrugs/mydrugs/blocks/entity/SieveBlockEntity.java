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
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.menu.SieveMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;

import java.util.Optional;

public final class SieveBlockEntity extends BlockEntity implements MenuProvider, Container {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_RESULT = 1;
    public static final int SLOT_BONUS = 2;
    public static final int SLOT_COUNT = 3;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 200;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    };

    private final float shakeEnergy = 0.0F;
    private final float extraProgressBuffer = 0.0F;
    private float shakeProgressBuffer = 0.0F;
    private int idleTicks = 0;

    public SieveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SIEVE.get(), pos, state);
    }

    private static boolean canAccept(ItemStack existing, ItemStack incoming, int slotLimit) {
        if (incoming.isEmpty()) return true;

        if (existing.isEmpty()) {
            return incoming.getCount() <= Math.min(slotLimit, incoming.getMaxStackSize());
        }

        if (!ItemStack.isSameItemSameComponents(existing, incoming)) {
            return false;
        }

        int max = Math.min(slotLimit, existing.getMaxStackSize());
        return existing.getCount() + incoming.getCount() <= max;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SieveBlockEntity be) {
        Optional<RecipeHolder<SieveRecipe>> match = be.getRecipe();

        if (match.isEmpty()) {
            if (be.progress != 0 || be.shakeProgressBuffer != 0.0F) {
                be.progress = 0;
                be.shakeProgressBuffer = 0.0F;
                be.idleTicks = 0;
                be.markDirtyAndSync();
            }
            return;
        }

        SieveRecipe recipe = match.get().value();
        be.maxProgress = recipe.sieveTime();

        if (!be.canCraft(recipe)) {
            if (be.progress != 0 || be.shakeProgressBuffer != 0.0F) {
                be.progress = 0;
                be.shakeProgressBuffer = 0.0F;
                be.idleTicks = 0;
                be.markDirtyAndSync();
            }
            return;
        }

        // No automatic progress at all.
        // Only consume buffered shake input.
        int gained = (int) be.shakeProgressBuffer;
        if (gained > 0) {
            be.shakeProgressBuffer -= gained;
            be.progress += gained;
            be.idleTicks = 0;
            be.setChanged();
        } else {
            be.idleTicks++;
        }

        if (be.progress >= be.maxProgress) {
            be.craft(recipe);
            be.shakeProgressBuffer = 0.0F;
            be.idleTicks = 0;
        }
    }

    public void addShakeImpulse(float impulse) {
        if (!(this.level instanceof ServerLevel)) {
            return;
        }

        Optional<RecipeHolder<SieveRecipe>> match = this.getRecipe();
        if (match.isEmpty()) {
            return;
        }

        SieveRecipe recipe = match.get().value();
        if (!this.canCraft(recipe)) {
            return;
        }

        float clamped = Mth.clamp(impulse, 0.0F, 4.0F);
        if (clamped <= 0.01F) {
            return;
        }

        this.shakeProgressBuffer += clamped * 2.8F;
        this.idleTicks = 0;
        this.setChanged();
    }

    public ContainerData getData() {
        return this.data;
    }

    public ItemStack getRenderStack(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? this.items.get(slot) : ItemStack.EMPTY;
    }

    private void sync() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    private void markDirtyAndSync() {
        this.setChanged();
        this.sync();
    }

    private Optional<RecipeHolder<SieveRecipe>> getRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        ItemStack input = this.items.get(SLOT_INPUT);
        if (input.isEmpty()) {
            return Optional.empty();
        }

        RecipeManager recipes = serverLevel.recipeAccess();
        return recipes.getRecipeFor(
                ModRecipeTypes.SIEVING.get(),
                new SingleRecipeInput(input),
                serverLevel
        );
    }

    private Optional<RecipeHolder<SieveRecipe>> getRecipeFor(ItemStack stack) {
        if (!(this.level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return Optional.empty();
        }

        RecipeManager recipes = serverLevel.recipeAccess();
        return recipes.getRecipeFor(
                ModRecipeTypes.SIEVING.get(),
                new SingleRecipeInput(stack),
                serverLevel
        );
    }

    private boolean canCraft(SieveRecipe recipe) {
        ItemStack mainOut = recipe.result().copy();
        if (!canAccept(this.items.get(SLOT_RESULT), mainOut, this.getMaxStackSize())) {
            return false;
        }

        if (recipe.hasBonus()) {
            ItemStack bonusOut = recipe.bonusResult().orElse(ItemStack.EMPTY).copy();
            return canAccept(this.items.get(SLOT_BONUS), bonusOut, this.getMaxStackSize());
        }

        return true;
    }

    private void insertToSlot(int slot, ItemStack stack) {
        if (stack.isEmpty()) return;

        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) {
            this.items.set(slot, stack.copy());
        } else {
            ItemStack grown = existing.copy();
            grown.grow(stack.getCount());
            this.items.set(slot, grown);
        }
    }

    private void craft(SieveRecipe recipe) {
        this.removeItem(SLOT_INPUT, 1);
        this.insertToSlot(SLOT_RESULT, recipe.result().copy());

        if (recipe.hasBonus() && this.level != null && this.level.random.nextFloat() < recipe.bonusChance()) {
            this.insertToSlot(SLOT_BONUS, recipe.bonusResult().orElse(ItemStack.EMPTY).copy());
        }

        this.progress = 0;
        this.markDirtyAndSync();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mydrugs.sieve");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new SieveMenu(
                containerId,
                playerInventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT ? this.items.get(slot) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        if (slot < 0 || slot >= SLOT_COUNT || amount <= 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = this.items.get(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ItemStack taken;
        if (existing.getCount() <= amount) {
            taken = existing;
            this.items.set(slot, ItemStack.EMPTY);
        } else {
            taken = existing.split(amount);
        }

        this.markDirtyAndSync();
        return taken;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot < 0 || slot >= SLOT_COUNT) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = this.items.get(slot);
        this.items.set(slot, ItemStack.EMPTY);
        return stack;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= SLOT_COUNT) return;

        if (!stack.isEmpty()) {
            stack = stack.copy();
            stack.limitSize(this.getMaxStackSize(stack));
        }

        this.items.set(slot, stack);

        if (slot == SLOT_INPUT) {
            this.progress = 0;
        }

        this.markDirtyAndSync();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null) return false;
        if (this.level.getBlockEntity(this.worldPosition) != this) return false;

        double x = this.worldPosition.getX() + 0.5;
        double y = this.worldPosition.getY() + 0.5;
        double z = this.worldPosition.getZ() + 0.5;
        return player.distanceToSqr(x, y, z) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_INPUT && this.getRecipeFor(stack).isPresent();
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.markDirtyAndSync();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < SLOT_COUNT; i++) {
            this.items.set(i, ItemStack.EMPTY);
        }

        this.progress = 0;
        this.maxProgress = 200;

        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);

            if (slot >= 0 && slot < SLOT_COUNT && !stack.isEmpty()) {
                this.items.set(slot, stack);
            }
        }

        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 200);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput.ValueOutputList list = output.childrenList("items");
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack stack = this.items.get(i);
            if (stack.isEmpty()) continue;

            ValueOutput child = list.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.CODEC, stack);
        }

        if (list.isEmpty()) {
            output.discard("items");
        }

        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
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
}