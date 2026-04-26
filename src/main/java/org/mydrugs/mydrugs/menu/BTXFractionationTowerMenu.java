package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.BTXFractionationTowerBlockEntity;
import org.mydrugs.mydrugs.menu.layout.BTXFractionationTowerLayout;

public class BTXFractionationTowerMenu extends AbstractMachineMenu {
    public static final int INPUT_CONTAINER_SLOT = 0;
    public static final int BENZENE_CONTAINER_SLOT = 1;
    public static final int TOLUENE_CONTAINER_SLOT = 2;
    public static final int XYLENE_CONTAINER_SLOT = 3;
    public static final int FUEL_SLOT = 4;

    public static final int MACHINE_SLOT_COUNT = 5;
    public static final int DATA_COUNT = 12;

    public static final int DUMP_INPUT_BUTTON_ID = 0;
    public static final int DUMP_BENZENE_BUTTON_ID = 1;
    public static final int DUMP_TOLUENE_BUTTON_ID = 2;
    public static final int DUMP_XYLENE_BUTTON_ID = 3;

    public static final int TANK_CAPACITY = 4000;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public BTXFractionationTowerMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public BTXFractionationTowerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.BTX_FRACTIONATION_TOWER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                INPUT_CONTAINER_SLOT,
                BTXFractionationTowerLayout.INPUT_SLOT_X,
                BTXFractionationTowerLayout.INPUT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BTXFractionationTowerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                BENZENE_CONTAINER_SLOT,
                BTXFractionationTowerLayout.BENZENE_SLOT_X,
                BTXFractionationTowerLayout.BENZENE_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BTXFractionationTowerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                TOLUENE_CONTAINER_SLOT,
                BTXFractionationTowerLayout.TOLUENE_SLOT_X,
                BTXFractionationTowerLayout.TOLUENE_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BTXFractionationTowerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                XYLENE_CONTAINER_SLOT,
                BTXFractionationTowerLayout.XYLENE_SLOT_X,
                BTXFractionationTowerLayout.XYLENE_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BTXFractionationTowerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                FUEL_SLOT,
                BTXFractionationTowerLayout.FUEL_SLOT_X,
                BTXFractionationTowerLayout.FUEL_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BTXFractionationTowerBlockEntity.isFuel(stack, playerInventory.player.level());
            }
        });

        this.addPlayerInventorySlots(playerInventory, BTXFractionationTowerLayout.PLAYER_INV_X, BTXFractionationTowerLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.BTX_FRACTIONATION_TOWER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof BTXFractionationTowerButtonHandler handler) {
            return switch (id) {
                case DUMP_INPUT_BUTTON_ID,
                     DUMP_BENZENE_BUTTON_ID,
                     DUMP_TOLUENE_BUTTON_ID,
                     DUMP_XYLENE_BUTTON_ID -> handler.onBTXFractionationTowerButtonPressed(player, id);
                default -> false;
            };
        }

        return false;
    }

    public int getInputTankAmount() {
        return this.data.get(0);
    }

    public int getBenzeneTankAmount() {
        return this.data.get(1);
    }

    public int getTolueneTankAmount() {
        return this.data.get(2);
    }

    public int getXyleneTankAmount() {
        return this.data.get(3);
    }

    public int getProgress() {
        return this.data.get(4);
    }

    public int getMaxProgress() {
        return this.data.get(5);
    }

    public int getBurnTimeRemaining() {
        return this.data.get(6);
    }

    public int getBurnTimeTotal() {
        return this.data.get(7);
    }

    public int getInputFluidSyncId() {
        return this.data.get(8);
    }

    public int getBenzeneFluidSyncId() {
        return this.data.get(9);
    }

    public int getTolueneFluidSyncId() {
        return this.data.get(10);
    }

    public int getXyleneFluidSyncId() {
        return this.data.get(11);
    }

    public Fluid getInputFluid() {
        return decodeFluid(this.getInputFluidSyncId());
    }

    public Fluid getBenzeneFluid() {
        return decodeFluid(this.getBenzeneFluidSyncId());
    }

    public Fluid getTolueneFluid() {
        return decodeFluid(this.getTolueneFluidSyncId());
    }

    public Fluid getXyleneFluid() {
        return decodeFluid(this.getXyleneFluidSyncId());
    }

    public Component getInputFluidIdText() {
        Fluid fluid = this.getInputFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public Component getBenzeneFluidIdText() {
        Fluid fluid = this.getBenzeneFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public Component getTolueneFluidIdText() {
        Fluid fluid = this.getTolueneFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public Component getXyleneFluidIdText() {
        Fluid fluid = this.getXyleneFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public boolean isWorking() {
        return this.getProgress() > 0 && this.getMaxProgress() > 0;
    }

    public boolean isLit() {
        return this.getBurnTimeRemaining() > 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledBurnTime(int pixels) {
        int remaining = this.getBurnTimeRemaining();
        int total = this.getBurnTimeTotal();
        return total > 0 ? remaining * pixels / total : 0;
    }

    public int getScaledInputTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getInputTankAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledBenzeneTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getBenzeneTankAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledTolueneTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getTolueneTankAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledXyleneTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getXyleneTankAmount() * pixels / TANK_CAPACITY : 0;
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
                if (BTXFractionationTowerBlockEntity.isFluidContainer(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, INPUT_CONTAINER_SLOT, FUEL_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (BTXFractionationTowerBlockEntity.isFuel(rawStack, player.level())) {
                    if (!this.moveItemStackTo(rawStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveBetweenPlayerInventoryAndHotbar(
                            rawStack,
                            quickMovedSlotIndex,
                            PLAYER_INV_START,
                            PLAYER_INV_END,
                            HOTBAR_START,
                            HOTBAR_END
                    )) {
                        return ItemStack.EMPTY;
                    }
                }
            } else {
                return ItemStack.EMPTY;
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return quickMovedStack;
    }

    public interface BTXFractionationTowerButtonHandler {
        boolean onBTXFractionationTowerButtonPressed(Player player, int buttonId);
    }
}
