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
import org.mydrugs.mydrugs.blocks.entity.SteamCrackerBlockEntity;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.layout.SteamCrackerLayout;

public class SteamCrackerMenu extends AbstractMachineMenu {
    public static final int INPUT_TRANSFER_SLOT = 0;
    public static final int OUTPUT_1_TRANSFER_SLOT = 1;
    public static final int OUTPUT_2_TRANSFER_SLOT = 2;
    public static final int OUTPUT_3_TRANSFER_SLOT = 3;
    public static final int OUTPUT_4_TRANSFER_SLOT = 4;
    public static final int FUEL_SLOT = 5;
    public static final int MACHINE_SLOT_COUNT = 6;
    public static final int DATA_COUNT = 24;

    public static final int DUMP_INPUT_BUTTON_ID = 0;
    public static final int DUMP_OUTPUT_1_BUTTON_ID = 1;
    public static final int DUMP_OUTPUT_2_BUTTON_ID = 2;
    public static final int DUMP_OUTPUT_3_BUTTON_ID = 3;
    public static final int DUMP_OUTPUT_4_BUTTON_ID = 4;

    public static final int FLUID_TANK_CAPACITY = SteamCrackerBlockEntity.FLUID_CAPACITY;
    public static final int GAS_TANK_CAPACITY = SteamCrackerBlockEntity.GAS_CAPACITY;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public SteamCrackerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public SteamCrackerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.STEAM_CRACKER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;
        container.startOpen(playerInventory.player);

        addContainerSlot(INPUT_TRANSFER_SLOT, SteamCrackerLayout.INPUT_SLOT_X, SteamCrackerLayout.SLOT_Y);
        addContainerSlot(OUTPUT_1_TRANSFER_SLOT, SteamCrackerLayout.OUTPUT_1_SLOT_X, SteamCrackerLayout.SLOT_Y);
        addContainerSlot(OUTPUT_2_TRANSFER_SLOT, SteamCrackerLayout.OUTPUT_2_SLOT_X, SteamCrackerLayout.SLOT_Y);
        addContainerSlot(OUTPUT_3_TRANSFER_SLOT, SteamCrackerLayout.OUTPUT_3_SLOT_X, SteamCrackerLayout.SLOT_Y);
        addContainerSlot(OUTPUT_4_TRANSFER_SLOT, SteamCrackerLayout.OUTPUT_4_SLOT_X, SteamCrackerLayout.SLOT_Y);

        this.addSlot(new Slot(container, FUEL_SLOT, SteamCrackerLayout.FUEL_SLOT_X, SteamCrackerLayout.FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SteamCrackerBlockEntity.isFuel(stack, playerInventory.player.level());
            }
        });

        this.addPlayerInventorySlots(playerInventory, SteamCrackerLayout.PLAYER_INV_X, SteamCrackerLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    private void addContainerSlot(int slot, int x, int y) {
        this.addSlot(new Slot(this.container, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return SteamCrackerBlockEntity.isFluidContainer(stack) || SteamCrackerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.STEAM_CRACKER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof SteamCrackerButtonHandler handler) {
            return switch (id) {
                case DUMP_INPUT_BUTTON_ID,
                     DUMP_OUTPUT_1_BUTTON_ID,
                     DUMP_OUTPUT_2_BUTTON_ID,
                     DUMP_OUTPUT_3_BUTTON_ID,
                     DUMP_OUTPUT_4_BUTTON_ID -> handler.onSteamCrackerButtonPressed(player, id);
                default -> false;
            };
        }
        return false;
    }

    public int getAmount(int tank) {
        return this.data.get(tank);
    }

    public int getProgress() {
        return this.data.get(5);
    }

    public int getMaxProgress() {
        return this.data.get(6);
    }

    public int getBurnTimeRemaining() {
        return this.data.get(7);
    }

    public int getBurnTimeTotal() {
        return this.data.get(8);
    }

    public Fluid getFluid(int tank) {
        return decodeFluid(this.data.get(9 + tank));
    }

    public @Nullable GasType getGasType(int tank) {
        return ModGases.bySyncId(this.data.get(14 + tank));
    }

    public boolean isGasMode(int tank) {
        return this.data.get(19 + tank) != 0;
    }

    public int getGasColor(int tank) {
        GasType gas = this.getGasType(tank);
        return gas == null ? 0 : gas.tint();
    }

    public String getGasName(int tank) {
        GasType gas = this.getGasType(tank);
        return gas == null ? "empty" : gas.name();
    }

    public int getScaledProgress(int pixels) {
        int max = this.getMaxProgress();
        return max > 0 ? this.getProgress() * pixels / max : 0;
    }

    public int getScaledBurn(int pixels) {
        int total = this.getBurnTimeTotal();
        return total > 0 ? this.getBurnTimeRemaining() * pixels / total : 0;
    }

    public int getScaledTank(int tank, int pixels) {
        int capacity = this.isGasMode(tank) ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getAmount(tank) * pixels / capacity : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);
        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();
            if (quickMovedSlotIndex < MACHINE_SLOT_COUNT) {
                if (!this.moveToPlayerInventory(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (quickMovedSlotIndex < HOTBAR_END) {
                boolean moved = false;
                if (SteamCrackerBlockEntity.isFluidContainer(rawStack) || SteamCrackerBlockEntity.isGasContainer(rawStack)) {
                    moved = this.moveItemStackTo(rawStack, INPUT_TRANSFER_SLOT, OUTPUT_4_TRANSFER_SLOT + 1, false);
                } else if (SteamCrackerBlockEntity.isFuel(rawStack, player.level())) {
                    moved = this.moveItemStackTo(rawStack, FUEL_SLOT, FUEL_SLOT + 1, false);
                }
                if (!moved && !this.moveBetweenPlayerInventoryAndHotbar(rawStack, quickMovedSlotIndex, PLAYER_INV_START, PLAYER_INV_END, HOTBAR_START, HOTBAR_END)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }
        return quickMovedStack;
    }

    public interface SteamCrackerButtonHandler {
        boolean onSteamCrackerButtonPressed(Player player, int buttonId);
    }
}
