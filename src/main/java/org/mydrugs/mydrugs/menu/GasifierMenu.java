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
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.GasifierBlockEntity;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.layout.FluidFiltererLayout;
import org.mydrugs.mydrugs.menu.layout.GasifierLayout;

public class GasifierMenu extends AbstractMachineMenu {
    public static final int INPUT_SLOT = 0;
    public static final int FUEL_SLOT = 1;
    public static final int EXPORT_SLOT = 2;

    public static final int MACHINE_SLOT_COUNT = 3;
    public static final int DATA_COUNT = 6;

    public static final int TANK_CAPACITY = GasifierBlockEntity.TANK_CAPACITY;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final Level level;

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
        this.level = playerInventory.player.level();

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
                FUEL_SLOT,
                GasifierLayout.FUEL_SLOT_X,
                GasifierLayout.FUEL_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return GasifierBlockEntity.isFuel(stack, level);
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

        this.addPlayerInventorySlots(playerInventory, GasifierLayout.PLAYER_INV_X, GasifierLayout.PLAYER_INV_Y);

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

    public int getGasSyncId() {
        return this.data.get(3);
    }

    public int getFuelLeft() {
        return this.data.get(4);
    }

    public int getFuelTotal() {
        return this.data.get(5);
    }

    public boolean isLit() {
        return this.getFuelLeft() > 0;
    }

    public boolean isExportArmed() {
        return this.container.getItem(EXPORT_SLOT).is(ModBlocks.GAS_TANK.get().asItem());
    }

    public @Nullable GasType getGasType() {
        return ModGases.bySyncId(this.getGasSyncId());
    }

    public Component getGasName() {
        GasType gas = this.getGasType();
        return gas == null
                ? Component.literal("Empty")
                : Component.literal(gas.id().toString());
    }

    public int getScaledGasTank(int pixels) {
        if (TANK_CAPACITY <= 0 || this.getGasAmount() <= 0) {
            return 0;
        }
        return Math.max(1, this.getGasAmount() * pixels / TANK_CAPACITY);
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        if (progress <= 0 || max <= 0) {
            return 0;
        }
        return Math.max(1, progress * pixels / max);
    }

    public int getScaledFuel(int pixels) {
        int left = this.getFuelLeft();
        int total = this.getFuelTotal();
        if (left <= 0 || total <= 0) {
            return 0;
        }
        return Math.max(1, left * pixels / total);
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
                } else if (GasifierBlockEntity.isFuel(rawStack, this.level)) {
                    if (!this.moveItemStackTo(rawStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
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