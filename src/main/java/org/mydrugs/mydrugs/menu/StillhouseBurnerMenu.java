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
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.StillhouseBurnerBlockEntity;
import org.mydrugs.mydrugs.menu.layout.StillhouseBurnerLayout;

public final class StillhouseBurnerMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 7;
    private static final int MACHINE_SLOT_COUNT = StillhouseBurnerBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public StillhouseBurnerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public StillhouseBurnerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.STILLHOUSE_BURNER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;
        container.startOpen(playerInventory.player);
        this.addSlot(new Slot(container, StillhouseBurnerBlockEntity.SLOT_FUEL_CONTAINER, StillhouseBurnerLayout.FUEL_SLOT_X, StillhouseBurnerLayout.FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return StillhouseBurnerBlockEntity.isFuelContainer(stack);
            }
        });
        this.addPlayerInventorySlots(playerInventory, StillhouseBurnerLayout.PLAYER_INV_X, StillhouseBurnerLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.STILLHOUSE_BURNER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int fuelAmount() {
        return this.data.get(0);
    }

    public Fluid fuelFluid() {
        int id = this.data.get(1);
        return id < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(id);
    }

    public int currentStored() {
        return this.data.get(2);
    }

    public int currentCapacity() {
        return this.data.get(3);
    }

    public int generationRate() {
        return this.data.get(4);
    }

    public int validTargets() {
        return this.data.get(5);
    }

    public int fuelPcPerMb() {
        return this.data.get(6);
    }

    public int scaledFuel(int pixels) {
        return fuelAmount() * pixels / StillhouseBurnerBlockEntity.FUEL_CAPACITY_MB;
    }

    public int scaledCurrent(int pixels) {
        int capacity = currentCapacity();
        return capacity > 0 ? currentStored() * pixels / capacity : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);
        if (quickMovedSlot == null || !quickMovedSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack rawStack = quickMovedSlot.getItem();
        quickMovedStack = rawStack.copy();
        if (quickMovedSlotIndex < MACHINE_SLOT_COUNT) {
            if (!this.moveToPlayerInventory(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        } else if (quickMovedSlotIndex < HOTBAR_END) {
            if (!StillhouseBurnerBlockEntity.isFuelContainer(rawStack)
                    || !this.moveItemStackTo(rawStack, StillhouseBurnerBlockEntity.SLOT_FUEL_CONTAINER, StillhouseBurnerBlockEntity.SLOT_FUEL_CONTAINER + 1, false)) {
                return ItemStack.EMPTY;
            }
        } else {
            return ItemStack.EMPTY;
        }

        return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
    }
}
