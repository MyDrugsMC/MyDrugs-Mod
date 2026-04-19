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
import org.mydrugs.mydrugs.blocks.entity.SieveBlockEntity;
import org.mydrugs.mydrugs.menu.layout.SieveLayout;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public final class SieveMenu extends AbstractMachineMenu {
    public static final int INPUT_SLOT = SieveBlockEntity.SLOT_INPUT;
    public static final int RESULT_SLOT = SieveBlockEntity.SLOT_RESULT;
    public static final int BONUS_SLOT = SieveBlockEntity.SLOT_BONUS;

    private static final int MACHINE_SLOT_COUNT = 3;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public SieveMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(2),
                ContainerLevelAccess.NULL
        );
    }

    public SieveMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.SIEVE.get(), containerId);

        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 2);

        this.container = container;
        this.data = data;
        this.access = access;

        this.addSlot(new Slot(container, INPUT_SLOT, SieveLayout.INPUT_X, SieveLayout.INPUT_Y));
        this.addSlot(new OutputSlot(container, RESULT_SLOT, SieveLayout.RESULT_X, SieveLayout.RESULT_Y));
        this.addSlot(new OutputSlot(container, BONUS_SLOT, SieveLayout.BONUS_X, SieveLayout.BONUS_Y));

        this.addPlayerInventorySlots(playerInventory, SieveLayout.PLAYER_INV_X, SieveLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    public boolean isCrafting() {
        return this.data.get(0) > 0 && this.data.get(1) > 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);

        if (progress <= 0 || maxProgress <= 0) {
            return 0;
        }

        return progress * pixels / maxProgress;
    }

    public int getMenuId() {
        return this.containerId;
    }

    public Container getMachineContainer() {
        return this.container;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.SIEVE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack quickMoved = ItemStack.EMPTY;
        Slot sourceSlot = this.slots.get(index);

        if (!sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        quickMoved = sourceStack.copy();

        if (index < MACHINE_SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (this.slots.get(INPUT_SLOT).mayPlace(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INV_END) {
                if (!this.moveItemStackTo(sourceStack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < HOTBAR_END) {
                if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == quickMoved.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return quickMoved;
    }
}