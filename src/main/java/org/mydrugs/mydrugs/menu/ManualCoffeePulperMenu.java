package org.mydrugs.mydrugs.menu;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.ManualCoffeePulperBlockEntity;
import org.mydrugs.mydrugs.menu.layout.ManualCoffeePulperLayout;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public class ManualCoffeePulperMenu extends AbstractMachineMenu {
    private static final int MACHINE_SLOT_COUNT = ManualCoffeePulperBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ManualCoffeePulperMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    public ManualCoffeePulperMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.MANUAL_COFFEE_PULPER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        this.access = access;

        this.addSlot(new Slot(container, ManualCoffeePulperBlockEntity.SLOT_INPUT, ManualCoffeePulperLayout.INPUT_X, ManualCoffeePulperLayout.INPUT_Y));
        this.addSlot(new OutputSlot(container, ManualCoffeePulperBlockEntity.SLOT_BEAN_OUTPUT, ManualCoffeePulperLayout.BEAN_OUTPUT_X, ManualCoffeePulperLayout.BEAN_OUTPUT_Y));
        this.addSlot(new OutputSlot(container, ManualCoffeePulperBlockEntity.SLOT_BIOMASS_OUTPUT, ManualCoffeePulperLayout.BIOMASS_OUTPUT_X, ManualCoffeePulperLayout.BIOMASS_OUTPUT_Y));
        this.addPlayerInventorySlots(playerInventory, ManualCoffeePulperLayout.PLAYER_INV_X, ManualCoffeePulperLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    public void addPulperWork(Player player, float amount) {
        if (this.container instanceof ManualCoffeePulperBlockEntity pulper && this.stillValid(player)) {
            pulper.addManualWork(player, amount);
        }
    }

    public int getMenuId() {
        return this.containerId;
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(1));
    }

    public float getRollerAngle() {
        return data.get(2) / 10.0F;
    }

    public int getScaledProgress(int pixels) {
        return getProgress() * pixels / getMaxProgress();
    }

    public Container getMachineContainer() {
        return container;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.MANUAL_COFFEE_PULPER.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        quickMoved = sourceStack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveToPlayerInventory(sourceStack, PLAYER_INV_START, HOTBAR_END)) return ItemStack.EMPTY;
        } else {
            if (this.slots.get(ManualCoffeePulperBlockEntity.SLOT_INPUT).mayPlace(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, ManualCoffeePulperBlockEntity.SLOT_INPUT, ManualCoffeePulperBlockEntity.SLOT_INPUT + 1, false)) return ItemStack.EMPTY;
            } else if (!this.moveBetweenPlayerInventoryAndHotbar(sourceStack, index, PLAYER_INV_START, PLAYER_INV_END, HOTBAR_START, HOTBAR_END)) {
                return ItemStack.EMPTY;
            }
        }

        return this.finishQuickMove(player, sourceSlot, sourceStack, quickMoved);
    }
}
