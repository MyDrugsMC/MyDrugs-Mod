package org.mydrugs.mydrugs.menu;

import net.minecraft.network.chat.Component;
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
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.GrowthChamberBlockEntity;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.menu.layout.FluidFiltererLayout;
import org.mydrugs.mydrugs.menu.layout.GrowthChamberLayout;

public class GrowthChamberMenu extends AbstractMachineMenu {
    public static final int INPUT_SLOT = 0;
    public static final int BIOMASS_SLOT = 1;
    public static final int MIDDLE_SLOT = 2;
    public static final int FINAL_SLOT = 3;

    public static final int MACHINE_SLOT_COUNT = 4;
    public static final int DATA_COUNT = 5;
    public static final int TANK_CAPACITY = 4000;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public GrowthChamberMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public GrowthChamberMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.GROWTH_CHAMBER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, INPUT_SLOT, GrowthChamberLayout.INPUT_SLOT_X, GrowthChamberLayout.INPUT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        this.addSlot(new Slot(container, BIOMASS_SLOT, GrowthChamberLayout.BIOMASS_SLOT_X, GrowthChamberLayout.BIOMASS_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }
        });

        this.addSlot(new Slot(container, MIDDLE_SLOT, GrowthChamberLayout.MIDDLE_SLOT_X, GrowthChamberLayout.MIDDLE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new Slot(container, FINAL_SLOT, GrowthChamberLayout.FINAL_SLOT_X, GrowthChamberLayout.FINAL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addPlayerInventorySlots(playerInventory, GrowthChamberLayout.PLAYER_INV_X, GrowthChamberLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.GROWTH_CHAMBER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getProgress() {
        return this.data.get(1);
    }

    public int getMaxProgress() {
        return this.data.get(2);
    }

    public int getStage() {
        return this.data.get(3);
    }

    public boolean isWorking() {
        return getProgress() > 0 && getMaxProgress() > 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = getProgress();
        int max = getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
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
                if (rawStack.is(ModItems.PLANT_BIOMASS.get())) {
                    if (!this.moveItemStackTo(rawStack, BIOMASS_SLOT, BIOMASS_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(rawStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
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

    public int getWaterAmount() {
        return this.data.get(0);
    }

    public int getGrowthProgress() {
        return this.data.get(1);
    }

    public int getGrowthMaxProgress() {
        return this.data.get(2);
    }

    public int getMatureProgress() {
        return this.data.get(3);
    }

    public int getMatureMaxProgress() {
        return this.data.get(4);
    }

    public int getScaledWaterTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getWaterAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledGrowthProgress(int pixels) {
        int progress = this.getGrowthProgress();
        int max = this.getGrowthMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledMatureProgress(int pixels) {
        int progress = this.getMatureProgress();
        int max = this.getMatureMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }
}