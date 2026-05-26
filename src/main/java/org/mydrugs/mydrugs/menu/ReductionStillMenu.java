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
import org.mydrugs.mydrugs.blocks.entity.ReductionStillBlockEntity;
import org.mydrugs.mydrugs.menu.layout.ReductionStillLayout;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public class ReductionStillMenu extends AbstractMachineMenu {
    private static final int MACHINE_SLOT_COUNT = ReductionStillBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public ReductionStillMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(3), ContainerLevelAccess.NULL);
    }

    public ReductionStillMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.REDUCTION_STILL.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 3);
        this.container = container;
        this.data = data;
        this.access = access;

        this.addSlot(new Slot(container, ReductionStillBlockEntity.SLOT_CUTTINGS, ReductionStillLayout.CUTTINGS_X, ReductionStillLayout.CUTTINGS_Y));
        this.addSlot(new Slot(container, ReductionStillBlockEntity.SLOT_SOLVENT, ReductionStillLayout.SOLVENT_X, ReductionStillLayout.SOLVENT_Y));
        this.addSlot(new OutputSlot(container, ReductionStillBlockEntity.SLOT_EXTRACT_OUTPUT, ReductionStillLayout.EXTRACT_X, ReductionStillLayout.EXTRACT_Y));
        this.addSlot(new OutputSlot(container, ReductionStillBlockEntity.SLOT_PULP_OUTPUT, ReductionStillLayout.PULP_X, ReductionStillLayout.PULP_Y));
        this.addPlayerInventorySlots(playerInventory, ReductionStillLayout.PLAYER_INV_X, ReductionStillLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    public int getProgress() {
        return data.get(0);
    }

    public int getMaxProgress() {
        return Math.max(1, data.get(1));
    }

    public ReductionStillBlockEntity.Status getStillStatus() {
        int ord = data.get(2);
        ReductionStillBlockEntity.Status[] all = ReductionStillBlockEntity.Status.values();
        if (ord < 0 || ord >= all.length) return ReductionStillBlockEntity.Status.IDLE_NO_CUTTINGS;
        return all[ord];
    }

    public int getScaledProgress(int pixels) {
        return getProgress() * pixels / getMaxProgress();
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.REDUCTION_STILL.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMoved;
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        quickMoved = sourceStack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveToPlayerInventory(sourceStack, PLAYER_INV_START, HOTBAR_END)) return ItemStack.EMPTY;
        } else {
            if (this.slots.get(ReductionStillBlockEntity.SLOT_SOLVENT).mayPlace(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, ReductionStillBlockEntity.SLOT_SOLVENT, ReductionStillBlockEntity.SLOT_SOLVENT + 1, false)) return ItemStack.EMPTY;
            } else if (this.slots.get(ReductionStillBlockEntity.SLOT_CUTTINGS).mayPlace(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, ReductionStillBlockEntity.SLOT_CUTTINGS, ReductionStillBlockEntity.SLOT_CUTTINGS + 1, false)) return ItemStack.EMPTY;
            } else if (!this.moveBetweenPlayerInventoryAndHotbar(sourceStack, index, PLAYER_INV_START, PLAYER_INV_END, HOTBAR_START, HOTBAR_END)) {
                return ItemStack.EMPTY;
            }
        }

        return this.finishQuickMove(player, sourceSlot, sourceStack, quickMoved);
    }
}
