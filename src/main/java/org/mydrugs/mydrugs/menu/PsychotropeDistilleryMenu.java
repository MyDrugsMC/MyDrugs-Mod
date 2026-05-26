package org.mydrugs.mydrugs.menu;

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
import org.mydrugs.mydrugs.blocks.entity.PsychotropeDistilleryBlockEntity;
import org.mydrugs.mydrugs.menu.layout.PsychotropeDistilleryLayout;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public final class PsychotropeDistilleryMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 6;
    public static final int MACHINE_SLOT_COUNT = PsychotropeDistilleryBlockEntity.SLOT_COUNT;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public PsychotropeDistilleryMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public PsychotropeDistilleryMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.PSYCHOTROPE_DISTILLERY.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, PsychotropeDistilleryBlockEntity.SLOT_DRUG_INPUT,
                PsychotropeDistilleryLayout.DRUG_SLOT_X, PsychotropeDistilleryLayout.DRUG_SLOT_Y));
        this.addSlot(new Slot(container, PsychotropeDistilleryBlockEntity.SLOT_REAGENT,
                PsychotropeDistilleryLayout.REAGENT_SLOT_X, PsychotropeDistilleryLayout.REAGENT_SLOT_Y));
        this.addSlot(new Slot(container, PsychotropeDistilleryBlockEntity.SLOT_FUEL,
                PsychotropeDistilleryLayout.FUEL_SLOT_X, PsychotropeDistilleryLayout.FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PsychotropeDistilleryBlockEntity.isFuel(stack, playerInventory.player.level());
            }
        });
        this.addSlot(new OutputSlot(container, PsychotropeDistilleryBlockEntity.SLOT_EXTRACT_OUTPUT,
                PsychotropeDistilleryLayout.EXTRACT_SLOT_X, PsychotropeDistilleryLayout.EXTRACT_SLOT_Y));
        this.addSlot(new OutputSlot(container, PsychotropeDistilleryBlockEntity.SLOT_RESIDUE_OUTPUT,
                PsychotropeDistilleryLayout.RESIDUE_SLOT_X, PsychotropeDistilleryLayout.RESIDUE_SLOT_Y));

        this.addPlayerInventorySlots(playerInventory, PsychotropeDistilleryLayout.PLAYER_INV_X, PsychotropeDistilleryLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.PSYCHOTROPE_DISTILLERY.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int progress() {
        return this.data.get(0);
    }

    public int maxProgress() {
        return this.data.get(1);
    }

    public int burnTimeRemaining() {
        return this.data.get(2);
    }

    public int burnTimeTotal() {
        return this.data.get(3);
    }

    public int residueProgress() {
        return this.data.get(4);
    }

    public int getScaledProgress(int pixels) {
        int max = this.maxProgress();
        return max > 0 ? this.progress() * pixels / max : 0;
    }

    public int getScaledBurn(int pixels) {
        int total = this.burnTimeTotal();
        return total > 0 ? this.burnTimeRemaining() * pixels / total : 0;
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
                if (PsychotropeDistilleryBlockEntity.isFuel(rawStack, player.level())) {
                    if (!this.moveItemStackTo(rawStack, PsychotropeDistilleryBlockEntity.SLOT_FUEL, PsychotropeDistilleryBlockEntity.SLOT_FUEL + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(rawStack, PsychotropeDistilleryBlockEntity.SLOT_DRUG_INPUT, PsychotropeDistilleryBlockEntity.SLOT_REAGENT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return ItemStack.EMPTY;
    }
}
