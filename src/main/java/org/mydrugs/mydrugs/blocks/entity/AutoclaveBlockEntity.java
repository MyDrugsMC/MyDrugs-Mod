package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.SyringeItem;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.menu.AutoclaveMenu;

public final class AutoclaveBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT = 1;
    public static final int SLOT_COUNT = 2;
    public static final int OPERATION_TICKS = 200;
    public static final int ENERGY_PER_TICK = 5;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = OPERATION_TICKS;
    private MachineStatus machineStatus = MachineStatus.IDLE;

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
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return AutoclaveMenu.DATA_COUNT;
        }
    };

    public AutoclaveBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOCLAVE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AutoclaveBlockEntity be) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        boolean changed = false;
        be.maxProgress = OPERATION_TICKS;

        ItemStack input = be.getItem(INPUT_SLOT);
        if (!isSterilizable(input)) {
            changed |= be.setMachineStatus(MachineStatus.MISSING_INPUT_ITEM);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        if (!be.canAcceptOutput(input)) {
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        if (!MachineEnergyAttachments.hasEnergyStorage(be)
                || MachineEnergyAttachments.get(be).storage().extract(ENERGY_PER_TICK, true) < ENERGY_PER_TICK) {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
            if (changed) {
                be.sync();
            }
            return;
        }

        MachineEnergyAttachments.get(be).storage().extract(ENERGY_PER_TICK, false);
        be.progress++;
        changed = true;
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        if (be.progress >= be.maxProgress) {
            be.finishSterilization();
            be.progress = 0;
            changed = true;
            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(be);
        }

        if (changed) {
            be.sync();
        }
    }

    private static boolean isSterilizable(ItemStack stack) {
        return !stack.isEmpty()
                && stack.is(ModItems.SYRINGE.get())
                && SyringeItem.canSterilize(stack);
    }

    private boolean canAcceptOutput(ItemStack input) {
        ItemStack output = this.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            return true;
        }
        return output.is(ModItems.SYRINGE.get())
                && output.getCount() < output.getMaxStackSize()
                && SyringeItem.isSterile(output)
                && SyringeItem.isEmptySyringe(output);
    }

    private void finishSterilization() {
        ItemStack input = this.getItem(INPUT_SLOT);
        ItemStack sterilized = input.copyWithCount(1);
        SyringeItem.markSterile(sterilized);

        ItemStack output = this.getItem(OUTPUT_SLOT);
        if (output.isEmpty()) {
            this.setItem(OUTPUT_SLOT, sterilized);
        } else if (output.is(ModItems.SYRINGE.get()) && SyringeItem.isSterile(output) && SyringeItem.isEmptySyringe(output)) {
            output.grow(1);
        } else {
            this.setItem(OUTPUT_SLOT, sterilized);
        }

        input.shrink(1);
        if (input.isEmpty()) {
            this.setItem(INPUT_SLOT, ItemStack.EMPTY);
        }
    }

    private boolean resetProgress() {
        if (this.progress == 0) {
            return false;
        }
        this.progress = 0;
        return true;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.autoclave");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AutoclaveMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        return true;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT && isSterilizable(stack);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == INPUT_SLOT) {
            this.progress = 0;
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = Mth.clamp(input.getIntOr("Progress", 0), 0, OPERATION_TICKS);
        this.maxProgress = input.getIntOr("MaxProgress", OPERATION_TICKS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
    }
}
