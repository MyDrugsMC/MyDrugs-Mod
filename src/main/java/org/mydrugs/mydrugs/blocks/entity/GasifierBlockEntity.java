package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.GasifierBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasTank;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.GasifierMenu;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.gasifier.GasifierRecipe;

import java.util.Objects;

public class GasifierBlockEntity extends BlockEntity implements Container, MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_EXPORT = 1;
    public static final int SLOT_COUNT = 2;

    public static final int TANK_CAPACITY = 4_000;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private final GasTank outputTank = new GasTank(
            TANK_CAPACITY,
            Objects::nonNull,
            this::onMachineChanged
    );

    private int progress = 0;
    private int maxProgress = 100;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) outputTank.getAmount();
                case 1 -> progress;
                case 2 -> maxProgress;
                case 3 -> ModGases.getSyncId(outputTank.getGasType());
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> outputTank.loadStored(outputTank.getGasType(), value);
                case 1 -> progress = value;
                case 2 -> maxProgress = value;
                case 3 -> outputTank.loadStored(ModGases.bySyncId(value), outputTank.getAmount());
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public GasifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GASIFIER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasifierBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        be.tickServer();
    }

    private void tickServer() {
        boolean changed = false;

        RecipeHolder<GasifierRecipe> holder = this.getCurrentRecipe();
        GasifierRecipe recipe = holder == null ? null : holder.value();

        if (canProcess(recipe)) {
            if (this.progress == 0 || this.maxProgress != recipe.processTime()) {
                this.maxProgress = recipe.processTime();
                changed = true;
            }

            this.progress++;

            if (this.progress >= this.maxProgress) {
                craft(recipe);
                this.progress = 0;
                changed = true;
            }
        } else if (this.progress != 0) {
            this.progress = 0;
            changed = true;
        }

        long before = this.outputTank.getAmount();
        this.tryExportToFrontTank();
        if (before != this.outputTank.getAmount()) {
            changed = true;
        }

        if (changed) {
            this.onMachineChanged();
        }
    }

    private boolean canProcess(@Nullable GasifierRecipe recipe) {
        if (recipe == null) {
            return false;
        }

        if (this.items.get(SLOT_INPUT).isEmpty()) {
            return false;
        }

        if (recipe.gas() == null) {
            return false;
        }

        return this.outputTank.fill(
                org.mydrugs.mydrugs.gas.GasStack.of(recipe.gas(), recipe.gasAmount()),
                true
        ) == recipe.gasAmount();
    }

    private void craft(GasifierRecipe recipe) {
        ItemStack input = this.items.get(SLOT_INPUT);
        if (input.isEmpty() || recipe.gas() == null) {
            return;
        }

        this.outputTank.fill(
                org.mydrugs.mydrugs.gas.GasStack.of(recipe.gas(), recipe.gasAmount()),
                false
        );

        input.shrink(1);
        if (input.isEmpty()) {
            this.items.set(SLOT_INPUT, ItemStack.EMPTY);
        }
    }

    private void tryExportToFrontTank() {
        if (this.level == null || this.outputTank.isEmpty()) {
            return;
        }

        // "Link/export" slot must contain your Gas Tank block item.
        ItemStack exportStack = this.items.get(SLOT_EXPORT);
        if (!exportStack.is(ModBlocks.GAS_TANK.get().asItem())) {
            return;
        }

        Direction facing = this.getBlockState().getValue(GasifierBlock.FACING);
        BlockPos targetPos = this.worldPosition.relative(facing);

        if (!(this.level.getBlockEntity(targetPos) instanceof GasTankBlockEntity gasTankBe)) {
            return;
        }

        GasStack stored = this.outputTank.getGasInTank(0);
        long moved = gasTankBe.getTank().fill(stored, false);
        if (moved > 0) {
            this.outputTank.drain(moved, false);
        }
    }

    private void onMachineChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public GasTank getOutputTank() {
        return this.outputTank;
    }

    public ContainerData getContainerData() {
        return this.data;
    }

    public static boolean isValidInput(ItemStack stack, Level level) {
        if (level == null || stack.isEmpty() || level.isClientSide()) {
            return false;
        }

        return ((ServerLevel) level).recipeAccess()
                .getRecipeFor(
                        ModRecipeTypes.GASIFIER.get(),
                        new SingleRecipeInput(stack),
                        level
                )
                .isPresent();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mydrugs.gasifier");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GasifierMenu(
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
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.onMachineChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (slot == SLOT_INPUT) {
            this.progress = 0;
        }

        this.onMachineChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }

        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {
            return isValidInput(stack, this.level);
        }

        if (slot == SLOT_EXPORT) {
            return stack.is(ModBlocks.GAS_TANK.get().asItem());
        }

        return false;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.onMachineChanged();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }

        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            if (slot >= 0 && slot < this.items.size()) {
                ItemStack stack = child.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
                this.items.set(slot, stack);
            }
        }

        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 100);

        String gasId = input.getStringOr("gas_id", "");
        long gasAmount = input.getLongOr("gas_amount", 0L);

        GasType gas = gasId.isBlank() ? null : ModGases.get(ResourceLocation.parse(gasId));
        this.outputTank.loadStored(gas, gasAmount);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput.ValueOutputList list = output.childrenList("items");
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            ValueOutput child = list.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.OPTIONAL_CODEC, stack);
        }

        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);

        GasType gas = this.outputTank.getGasType();
        output.putString("gas_id", gas == null ? "" : gas.id().toString());
        output.putLong("gas_amount", this.outputTank.getAmount());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private @Nullable RecipeHolder<GasifierRecipe> getCurrentRecipe() {
        if (this.level == null || this.level.isClientSide()) {
            return null;
        }

        return ((ServerLevel) this.level).recipeAccess()
                .getRecipeFor(
                        ModRecipeTypes.GASIFIER.get(),
                        new SingleRecipeInput(this.items.get(SLOT_INPUT)),
                        this.level
                )
                .orElse(null);
    }
}