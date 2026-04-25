package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.ElectrolyzerBlockEntity;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.layout.ElectrolyzerLayout;

public class ElectrolyzerMenu extends AbstractMachineMenu {
    public static final int INPUT_CONTAINER_SLOT = 0;
    public static final int OUTPUT_1_CONTAINER_SLOT = 1;
    public static final int OUTPUT_2_CONTAINER_SLOT = 2;
    public static final int OUTPUT_3_CONTAINER_SLOT = 3;
    public static final int FUEL_SLOT = 4;

    public static final int MACHINE_SLOT_COUNT = 5;
    public static final int DATA_COUNT = 18;

    public static final int DUMP_INPUT_BUTTON_ID = 0;
    public static final int DUMP_OUTPUT_1_BUTTON_ID = 1;
    public static final int DUMP_OUTPUT_2_BUTTON_ID = 2;
    public static final int DUMP_OUTPUT_3_BUTTON_ID = 3;

    public static final int FLUID_TANK_CAPACITY = ElectrolyzerBlockEntity.FLUID_CAPACITY;
    public static final int GAS_TANK_CAPACITY = ElectrolyzerBlockEntity.GAS_CAPACITY;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ElectrolyzerMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public ElectrolyzerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ELECTROLYZER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                INPUT_CONTAINER_SLOT,
                ElectrolyzerLayout.INPUT_SLOT_X,
                ElectrolyzerLayout.INPUT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ElectrolyzerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_1_CONTAINER_SLOT,
                ElectrolyzerLayout.OUTPUT_1_SLOT_X,
                ElectrolyzerLayout.OUTPUT_1_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ElectrolyzerMenu.this.isOutput1GasMode()
                        ? ElectrolyzerBlockEntity.isGasContainer(stack)
                        : ElectrolyzerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_2_CONTAINER_SLOT,
                ElectrolyzerLayout.OUTPUT_2_SLOT_X,
                ElectrolyzerLayout.OUTPUT_2_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ElectrolyzerMenu.this.isOutput2GasMode()
                        ? ElectrolyzerBlockEntity.isGasContainer(stack)
                        : ElectrolyzerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_3_CONTAINER_SLOT,
                ElectrolyzerLayout.OUTPUT_3_SLOT_X,
                ElectrolyzerLayout.OUTPUT_3_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ElectrolyzerMenu.this.isOutput3GasMode()
                        ? ElectrolyzerBlockEntity.isGasContainer(stack)
                        : ElectrolyzerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                FUEL_SLOT,
                ElectrolyzerLayout.FUEL_SLOT_X,
                ElectrolyzerLayout.FUEL_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ElectrolyzerBlockEntity.isFuel(stack, playerInventory.player.level());
            }
        });

        this.addPlayerInventorySlots(playerInventory, ElectrolyzerLayout.PLAYER_INV_X, ElectrolyzerLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    private static boolean isGasContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(ModGasCapabilities.ITEM, null) != null;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ELECTROLYZER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof ElectrolyzerButtonHandler handler) {
            return switch (id) {
                case DUMP_INPUT_BUTTON_ID,
                     DUMP_OUTPUT_1_BUTTON_ID,
                     DUMP_OUTPUT_2_BUTTON_ID,
                     DUMP_OUTPUT_3_BUTTON_ID -> handler.onElectrolyzerButtonPressed(player, id);
                default -> false;
            };
        }

        return false;
    }

    public int getInputTankAmount() {
        return this.data.get(0);
    }

    public int getOutput1TankAmount() {
        return this.data.get(1);
    }

    public int getOutput2TankAmount() {
        return this.data.get(2);
    }

    public int getOutput3TankAmount() {
        return this.data.get(3);
    }

    public int getProgress() {
        return this.data.get(4);
    }

    public int getMaxProgress() {
        return this.data.get(5);
    }

    public int getBurnTimeRemaining() {
        return this.data.get(6);
    }

    public int getBurnTimeTotal() {
        return this.data.get(7);
    }

    public int getInputFluidSyncId() {
        return this.data.get(8);
    }

    public int getOutput1FluidSyncId() {
        return this.data.get(9);
    }

    public int getOutput2FluidSyncId() {
        return this.data.get(10);
    }

    public int getOutput3FluidSyncId() {
        return this.data.get(11);
    }

    public int getOutput1GasSyncId() {
        return this.data.get(12);
    }

    public int getOutput2GasSyncId() {
        return this.data.get(13);
    }

    public int getOutput3GasSyncId() {
        return this.data.get(14);
    }

    public boolean isOutput1GasMode() {
        return this.data.get(15) != 0;
    }

    public boolean isOutput2GasMode() {
        return this.data.get(16) != 0;
    }

    public boolean isOutput3GasMode() {
        return this.data.get(17) != 0;
    }

    public Fluid getInputFluid() {
        return decodeFluid(this.getInputFluidSyncId());
    }

    public Fluid getOutput1Fluid() {
        return decodeFluid(this.getOutput1FluidSyncId());
    }

    public Fluid getOutput2Fluid() {
        return decodeFluid(this.getOutput2FluidSyncId());
    }

    public Fluid getOutput3Fluid() {
        return decodeFluid(this.getOutput3FluidSyncId());
    }

    public @Nullable GasType getOutput1GasType() {
        return ModGases.bySyncId(this.getOutput1GasSyncId());
    }

    public @Nullable GasType getOutput2GasType() {
        return ModGases.bySyncId(this.getOutput2GasSyncId());
    }

    public @Nullable GasType getOutput3GasType() {
        return ModGases.bySyncId(this.getOutput3GasSyncId());
    }

    public int getOutput1GasColor() {
        GasType gas = this.getOutput1GasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getOutput2GasColor() {
        GasType gas = this.getOutput2GasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getOutput3GasColor() {
        GasType gas = this.getOutput3GasType();
        return gas == null ? 0 : gas.tint();
    }

    public String getOutput1GasName() {
        GasType gas = this.getOutput1GasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getOutput2GasName() {
        GasType gas = this.getOutput2GasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getOutput3GasName() {
        GasType gas = this.getOutput3GasType();
        return gas == null ? "empty" : gas.name();
    }

    public boolean isWorking() {
        return this.getProgress() > 0 && this.getMaxProgress() > 0;
    }

    public boolean isLit() {
        return this.getBurnTimeRemaining() > 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledBurnTime(int pixels) {
        int remaining = this.getBurnTimeRemaining();
        int total = this.getBurnTimeTotal();
        return total > 0 ? remaining * pixels / total : 0;
    }

    public int getScaledInputTank(int pixels) {
        return FLUID_TANK_CAPACITY > 0 ? this.getInputTankAmount() * pixels / FLUID_TANK_CAPACITY : 0;
    }

    public int getScaledOutput1Tank(int pixels) {
        int capacity = this.isOutput1GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getOutput1TankAmount() * pixels / capacity : 0;
    }

    public int getScaledOutput2Tank(int pixels) {
        int capacity = this.isOutput2GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getOutput2TankAmount() * pixels / capacity : 0;
    }

    public int getScaledOutput3Tank(int pixels) {
        int capacity = this.isOutput3GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getOutput3TankAmount() * pixels / capacity : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            if (quickMovedSlotIndex < MACHINE_SLOT_COUNT) {
                if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (quickMovedSlotIndex < HOTBAR_END) {
                if (ElectrolyzerBlockEntity.isFluidContainer(rawStack)) {
                    boolean moved = this.moveItemStackTo(rawStack, INPUT_CONTAINER_SLOT, INPUT_CONTAINER_SLOT + 1, false)
                            || this.moveItemStackTo(rawStack, OUTPUT_1_CONTAINER_SLOT, FUEL_SLOT, false);

                    if (!moved) {
                        return ItemStack.EMPTY;
                    }
                } else if (isGasContainer(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, OUTPUT_1_CONTAINER_SLOT, FUEL_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (ElectrolyzerBlockEntity.isFuel(rawStack, player.level())) {
                    if (!this.moveItemStackTo(rawStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (quickMovedSlotIndex < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(rawStack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (quickMovedSlotIndex < HOTBAR_END) {
                    if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            if (rawStack.isEmpty()) {
                quickMovedSlot.setByPlayer(ItemStack.EMPTY);
            } else {
                quickMovedSlot.setChanged();
            }

            if (rawStack.getCount() == quickMovedStack.getCount()) {
                return ItemStack.EMPTY;
            }

            quickMovedSlot.onTake(player, rawStack);
        }

        return quickMovedStack;
    }

    public interface ElectrolyzerButtonHandler {
        boolean onElectrolyzerButtonPressed(Player player, int buttonId);
    }
}