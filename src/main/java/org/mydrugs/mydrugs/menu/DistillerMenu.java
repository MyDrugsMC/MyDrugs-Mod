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
import org.mydrugs.mydrugs.blocks.entity.DistillerBlockEntity;
import org.mydrugs.mydrugs.menu.layout.DistillerLayout;

public class DistillerMenu extends AbstractMachineMenu {
    public static final int INPUT_CONTAINER_SLOT = 0;
    public static final int OUTPUT_A_CONTAINER_SLOT = 1;
    public static final int OUTPUT_B_CONTAINER_SLOT = 2;

    public static final int MACHINE_SLOT_COUNT = 3;
    public static final int DATA_COUNT = 10;

    public static final int RUN_BUTTON_ID = 0;
    public static final int DUMP_INPUT_BUTTON_ID = 1;
    public static final int DUMP_OUTPUT_A_BUTTON_ID = 2;
    public static final int DUMP_OUTPUT_B_BUTTON_ID = 3;

    public static final int TANK_CAPACITY = 4000;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public DistillerMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public DistillerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.DISTILLER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                INPUT_CONTAINER_SLOT,
                DistillerLayout.INPUT_SLOT_X,
                DistillerLayout.INPUT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DistillerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_A_CONTAINER_SLOT,
                DistillerLayout.OUTPUT_A_SLOT_X,
                DistillerLayout.OUTPUT_A_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DistillerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_B_CONTAINER_SLOT,
                DistillerLayout.OUTPUT_B_SLOT_X,
                DistillerLayout.OUTPUT_B_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DistillerBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addPlayerInventorySlots(playerInventory, DistillerLayout.PLAYER_INV_X, DistillerLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.DISTILLER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof DistillerButtonHandler handler) {
            return switch (id) {
                case RUN_BUTTON_ID,
                     DUMP_INPUT_BUTTON_ID,
                     DUMP_OUTPUT_A_BUTTON_ID,
                     DUMP_OUTPUT_B_BUTTON_ID -> handler.onDistillerButtonPressed(player, id);
                default -> false;
            };
        }

        return false;
    }

    public int getInputTankAmount() {
        return this.data.get(0);
    }

    public int getOutputATankAmount() {
        return this.data.get(1);
    }

    public int getOutputBTankAmount() {
        return this.data.get(2);
    }

    public int getProgress() {
        return this.data.get(3);
    }

    public int getMaxProgress() {
        return this.data.get(4);
    }

    public int getClicksPerSecond() {
        return this.data.get(5);
    }

    public int getSpeedPercent() {
        return this.data.get(6);
    }

    public int getInputFluidSyncId() {
        return this.data.get(7);
    }

    public int getOutputAFluidSyncId() {
        return this.data.get(8);
    }

    public int getOutputBFluidSyncId() {
        return this.data.get(9);
    }

    public Fluid getInputFluid() {
        return decodeFluid(this.getInputFluidSyncId());
    }

    public Fluid getOutputAFluid() {
        return decodeFluid(this.getOutputAFluidSyncId());
    }

    public Fluid getOutputBFluid() {
        return decodeFluid(this.getOutputBFluidSyncId());
    }

    public Component getInputFluidIdText() {
        Fluid fluid = this.getInputFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public Component getOutputAFluidIdText() {
        Fluid fluid = this.getOutputAFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public Component getOutputBFluidIdText() {
        Fluid fluid = this.getOutputBFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public boolean isWorking() {
        return this.getProgress() > 0 && this.getMaxProgress() > 0;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledInputTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getInputTankAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledOutputATank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getOutputATankAmount() * pixels / TANK_CAPACITY : 0;
    }

    public int getScaledOutputBTank(int pixels) {
        return TANK_CAPACITY > 0 ? this.getOutputBTankAmount() * pixels / TANK_CAPACITY : 0;
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
                if (!this.moveItemStackTo(rawStack, 0, MACHINE_SLOT_COUNT, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return ItemStack.EMPTY;
    }

    public interface DistillerButtonHandler {
        boolean onDistillerButtonPressed(Player player, int buttonId);
    }
}
