package org.mydrugs.mydrugs.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.machine.fuel.MachineFuelUtil;

public class AdvancedFurnaceMenu extends AbstractContainerMenu {
    private static final int MACHINE_SLOT_COUNT = 5;
    private static final int PLAYER_INV_START = 5;
    private static final int PLAYER_INV_END = 32;
    private static final int HOTBAR_START = 32;
    private static final int HOTBAR_END = 41;
    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AdvancedFurnaceMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(5),
                ContainerLevelAccess.NULL
        );
    }

    public AdvancedFurnaceMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.ADVANCED_FURNACE.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 5);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        // Machine slots
        this.addSlot(new Slot(container, AdvancedFurnaceBlockEntity.INPUT_A_SLOT, 44, 17));
        this.addSlot(new Slot(container, AdvancedFurnaceBlockEntity.INPUT_B_SLOT, 62, 17));

        this.addSlot(new Slot(container, AdvancedFurnaceBlockEntity.FUEL_SLOT, 53, 53) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MachineFuelUtil.getBurnTime(stack, playerInventory.player.level(), MachineFuelUtil.VANILLA) > 0;
            }
        });

        this.addSlot(new Slot(container, AdvancedFurnaceBlockEntity.OUTPUT_A_SLOT, 116, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new Slot(container, AdvancedFurnaceBlockEntity.OUTPUT_B_SLOT, 134, 17) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // Player inventory
        this.addStandardInventorySlots(playerInventory, 8, 84);

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ADVANCED_FURNACE.get());
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public int getBurnTime() {
        return this.data.get(2);
    }

    public int getBurnDuration() {
        return this.data.get(3);
    }

    public int getTankAmount() {
        return this.data.get(4);
    }

    public boolean isBurning() {
        return this.getBurnTime() > 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledBurn(int pixels) {
        int burn = this.getBurnTime();
        int max = this.getBurnDuration();
        return max > 0 ? burn * pixels / max : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            // output slots -> player inventory
            if (quickMovedSlotIndex == AdvancedFurnaceBlockEntity.OUTPUT_A_SLOT
                    || quickMovedSlotIndex == AdvancedFurnaceBlockEntity.OUTPUT_B_SLOT) {
                if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
                quickMovedSlot.onQuickCraft(rawStack, quickMovedStack);
            }
            // player inventory -> machine
            else if (quickMovedSlotIndex >= PLAYER_INV_START && quickMovedSlotIndex < HOTBAR_END) {
                if (MachineFuelUtil.getBurnTime(rawStack, player.level(), MachineFuelUtil.VANILLA) > 0) {
                    if (!this.moveItemStackTo(rawStack, AdvancedFurnaceBlockEntity.FUEL_SLOT, AdvancedFurnaceBlockEntity.FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(rawStack, AdvancedFurnaceBlockEntity.INPUT_A_SLOT, AdvancedFurnaceBlockEntity.FUEL_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            }
            // machine input/fuel -> player
            else if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
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

    public int getScaledTank(int pixels) {
        return AdvancedFurnaceBlockEntity.TANK_CAPACITY > 0
                ? this.getTankAmount() * pixels / AdvancedFurnaceBlockEntity.TANK_CAPACITY
                : 0;
    }
}
