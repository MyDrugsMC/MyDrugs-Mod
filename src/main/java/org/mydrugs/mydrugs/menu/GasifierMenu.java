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
import org.mydrugs.mydrugs.blocks.entity.GasifierBlockEntity;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.layout.GasifierLayout;

import javax.annotation.Nullable;

public class GasifierMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOT = 0;
    public static final int EXPORT_SLOT = 1;

    public static final int MACHINE_SLOT_COUNT = 2;
    public static final int DATA_COUNT = 4;

    public static final int TANK_CAPACITY = GasifierBlockEntity.TANK_CAPACITY;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public GasifierMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public GasifierMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.GASIFIER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                INPUT_SLOT,
                GasifierLayout.INPUT_SLOT_X,
                GasifierLayout.INPUT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GasifierBlockEntity.isValidInput(stack, playerInventory.player.level());
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        this.addSlot(new Slot(
                container,
                EXPORT_SLOT,
                GasifierLayout.EXPORT_SLOT_X,
                GasifierLayout.EXPORT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModBlocks.GAS_TANK.get().asItem());
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        GasifierLayout.PLAYER_INV_X + col * GasifierLayout.SLOT_SIZE,
                        GasifierLayout.PLAYER_INV_Y + row * GasifierLayout.SLOT_SIZE
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    GasifierLayout.HOTBAR_X + col * GasifierLayout.SLOT_SIZE,
                    GasifierLayout.HOTBAR_Y
            ));
        }

        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.GASIFIER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getGasAmount() {
        return this.data.get(0);
    }

    public int getProgress() {
        return this.data.get(1);
    }

    public int getMaxProgress() {
        return this.data.get(2);
    }

    public boolean isExportArmed() {
        return this.container.getItem(EXPORT_SLOT).is(ModBlocks.GAS_TANK.get().asItem());
    }

    public int getGasSyncId() {
        return this.data.get(3);
    }

    public @Nullable GasType getGasType() {
        return ModGases.bySyncId(this.getGasSyncId());
    }

    public Component getGasName() {
        GasType gas = this.getGasType();
        return gas == null
                ? Component.literal("empty")
                : Component.literal(gas.id().toString());
    }

    public int getScaledGasTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getGasAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
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
                if (GasifierBlockEntity.isValidInput(rawStack, player.level())) {
                    if (!this.moveItemStackTo(rawStack, INPUT_SLOT, INPUT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (rawStack.is(ModBlocks.GAS_TANK.get().asItem())) {
                    if (!this.moveItemStackTo(rawStack, EXPORT_SLOT, EXPORT_SLOT + 1, false)) {
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
}