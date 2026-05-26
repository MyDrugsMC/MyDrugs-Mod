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
import org.mydrugs.mydrugs.blocks.entity.DistillateEngineBlockEntity;
import org.mydrugs.mydrugs.menu.layout.DistillateEngineLayout;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public final class DistillateEngineMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 8;
    public static final int MACHINE_SLOT_COUNT = DistillateEngineBlockEntity.SLOT_COUNT;
    public static final int RADIUS_DOWN_BUTTON_ID = 0;
    public static final int RADIUS_UP_BUTTON_ID = 1;
    public static final int SHOW_AREA_BUTTON_ID = 2;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public DistillateEngineMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public DistillateEngineMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.DISTILLATE_ENGINE.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, DistillateEngineBlockEntity.SLOT_FUEL,
                DistillateEngineLayout.FUEL_SLOT_X, DistillateEngineLayout.FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DistillateEngineBlockEntity.isFuel(stack);
            }
        });
        this.addSlot(new Slot(container, DistillateEngineBlockEntity.SLOT_REGULATOR,
                DistillateEngineLayout.REGULATOR_SLOT_X, DistillateEngineLayout.REGULATOR_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DistillateEngineBlockEntity.isRegulator(stack);
            }
        });
        this.addSlot(new OutputSlot(container, DistillateEngineBlockEntity.SLOT_WASTE_OUTPUT,
                DistillateEngineLayout.WASTE_SLOT_X, DistillateEngineLayout.WASTE_SLOT_Y));

        this.addPlayerInventorySlots(playerInventory, DistillateEngineLayout.PLAYER_INV_X, DistillateEngineLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.DISTILLATE_ENGINE.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof DistillateEngineBlockEntity engine) {
            if (id == RADIUS_DOWN_BUTTON_ID) {
                engine.setPowerRadius(powerRadius() - 1);
                return true;
            }
            if (id == RADIUS_UP_BUTTON_ID) {
                engine.setPowerRadius(powerRadius() + 1);
                return true;
            }
            if (id == SHOW_AREA_BUTTON_ID && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                engine.broadcastAreaPreview(serverPlayer);
                return true;
            }
        }
        return false;
    }

    public int currentStored() {
        return this.data.get(0);
    }

    public int currentCapacity() {
        return this.data.get(1);
    }

    public int fuelTicksRemaining() {
        return this.data.get(2);
    }

    public int fuelTicksTotal() {
        return this.data.get(3);
    }

    public int currentPerTick() {
        return this.data.get(4);
    }

    public int strain() {
        return this.data.get(5);
    }

    public int overloadTicks() {
        return this.data.get(6);
    }

    public int powerRadius() {
        return this.data.get(7);
    }

    public int getScaledCurrent(int pixels) {
        int capacity = currentCapacity();
        return capacity > 0 ? currentStored() * pixels / capacity : 0;
    }

    public int getScaledFuel(int pixels) {
        int total = fuelTicksTotal();
        return total > 0 ? fuelTicksRemaining() * pixels / total : 0;
    }

    public int getScaledStrain(int pixels) {
        return strain() * pixels / 100;
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
                if (DistillateEngineBlockEntity.isFuel(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, DistillateEngineBlockEntity.SLOT_FUEL, DistillateEngineBlockEntity.SLOT_FUEL + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (DistillateEngineBlockEntity.isRegulator(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, DistillateEngineBlockEntity.SLOT_REGULATOR, DistillateEngineBlockEntity.SLOT_REGULATOR + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
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
