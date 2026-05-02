package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.AromaticExtractorBlockEntity;
import org.mydrugs.mydrugs.menu.layout.AromaticExtractorLayout;

public class AromaticExtractorMenu extends AbstractMachineMenu {
    public static final int INPUT_CONTAINER_SLOT = 0;
    public static final int CATALYST_CONTAINER_SLOT = 1;
    public static final int OUTPUT_A_CONTAINER_SLOT = 2;
    public static final int OUTPUT_B_CONTAINER_SLOT = 3;
    public static final int FUEL_SLOT = 4;

    public static final int MACHINE_SLOT_COUNT = 5;
    public static final int DATA_COUNT = 12;

    public static final int DUMP_INPUT_BUTTON_ID = 0;
    public static final int DUMP_CATALYST_BUTTON_ID = 1;
    public static final int DUMP_OUTPUT_A_BUTTON_ID = 2;
    public static final int DUMP_OUTPUT_B_BUTTON_ID = 3;

    public static final int INPUT_TANK_CAPACITY = AromaticExtractorBlockEntity.INPUT_CAPACITY;
    public static final int CATALYST_TANK_CAPACITY = AromaticExtractorBlockEntity.CATALYST_CAPACITY;
    public static final int OUTPUT_TANK_CAPACITY = AromaticExtractorBlockEntity.OUTPUT_CAPACITY;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AromaticExtractorMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public AromaticExtractorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.AROMATIC_EXTRACTOR.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                INPUT_CONTAINER_SLOT,
                AromaticExtractorLayout.INPUT_SLOT_X,
                AromaticExtractorLayout.INPUT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AromaticExtractorBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                CATALYST_CONTAINER_SLOT,
                AromaticExtractorLayout.CATALYST_SLOT_X,
                AromaticExtractorLayout.CATALYST_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AromaticExtractorBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_A_CONTAINER_SLOT,
                AromaticExtractorLayout.OUTPUT_A_SLOT_X,
                AromaticExtractorLayout.OUTPUT_A_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AromaticExtractorBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_B_CONTAINER_SLOT,
                AromaticExtractorLayout.OUTPUT_B_SLOT_X,
                AromaticExtractorLayout.OUTPUT_B_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AromaticExtractorBlockEntity.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(
                container,
                FUEL_SLOT,
                AromaticExtractorLayout.FUEL_SLOT_X,
                AromaticExtractorLayout.FUEL_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return AromaticExtractorBlockEntity.isFuel(stack, playerInventory.player.level());
            }
        });

        this.addPlayerInventorySlots(playerInventory, AromaticExtractorLayout.PLAYER_INV_X, AromaticExtractorLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.AROMATIC_EXTRACTOR.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof AromaticExtractorButtonHandler handler) {
            return switch (id) {
                case DUMP_INPUT_BUTTON_ID,
                     DUMP_CATALYST_BUTTON_ID,
                     DUMP_OUTPUT_A_BUTTON_ID,
                     DUMP_OUTPUT_B_BUTTON_ID -> handler.onAromaticExtractorButtonPressed(player, id);
                default -> false;
            };
        }

        return false;
    }

    public int getInputTankAmount() {
        return this.data.get(0);
    }

    public int getCatalystTankAmount() {
        return this.data.get(1);
    }

    public int getOutputATankAmount() {
        return this.data.get(2);
    }

    public int getOutputBTankAmount() {
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

    public int getCatalystFluidSyncId() {
        return this.data.get(9);
    }

    public int getOutputAFluidSyncId() {
        return this.data.get(10);
    }

    public int getOutputBFluidSyncId() {
        return this.data.get(11);
    }

    public Fluid getInputFluid() {
        return decodeFluid(this.getInputFluidSyncId());
    }

    public Fluid getCatalystFluid() {
        return decodeFluid(this.getCatalystFluidSyncId());
    }

    public Fluid getOutputAFluid() {
        return decodeFluid(this.getOutputAFluidSyncId());
    }

    public Fluid getOutputBFluid() {
        return decodeFluid(this.getOutputBFluidSyncId());
    }

    public Component getInputFluidIdText() {
        Fluid fluid = this.getInputFluid();
        return fluid == Fluids.EMPTY ? Component.translatable("screen.mydrugs.ui.empty") : fluid.getFluidType().getDescription();
    }

    public Component getCatalystFluidIdText() {
        Fluid fluid = this.getCatalystFluid();
        return fluid == Fluids.EMPTY ? Component.translatable("screen.mydrugs.ui.empty") : fluid.getFluidType().getDescription();
    }

    public Component getOutputAFluidIdText() {
        Fluid fluid = this.getOutputAFluid();
        return fluid == Fluids.EMPTY ? Component.translatable("screen.mydrugs.ui.empty") : fluid.getFluidType().getDescription();
    }

    public Component getOutputBFluidIdText() {
        Fluid fluid = this.getOutputBFluid();
        return fluid == Fluids.EMPTY ? Component.translatable("screen.mydrugs.ui.empty") : fluid.getFluidType().getDescription();
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
        return INPUT_TANK_CAPACITY > 0 ? this.getInputTankAmount() * pixels / INPUT_TANK_CAPACITY : 0;
    }

    public int getScaledCatalystTank(int pixels) {
        return CATALYST_TANK_CAPACITY > 0 ? this.getCatalystTankAmount() * pixels / CATALYST_TANK_CAPACITY : 0;
    }

    public int getScaledOutputATank(int pixels) {
        return OUTPUT_TANK_CAPACITY > 0 ? this.getOutputATankAmount() * pixels / OUTPUT_TANK_CAPACITY : 0;
    }

    public int getScaledOutputBTank(int pixels) {
        return OUTPUT_TANK_CAPACITY > 0 ? this.getOutputBTankAmount() * pixels / OUTPUT_TANK_CAPACITY : 0;
    }

    public int getCatalystSpeedPercent() {
        int min = AromaticExtractorBlockEntity.MIN_CATALYST_AMOUNT;
        if (this.getCatalystTankAmount() < min) {
            return 100;
        }
        int bonusRange = Math.max(1, CATALYST_TANK_CAPACITY - min);
        double bonus = Math.min(1.0D, Math.max(0.0D, (this.getCatalystTankAmount() - min) / (double) bonusRange));
        double multiplier = 1.0D + (AromaticExtractorBlockEntity.MAX_CATALYST_SPEED_MULTIPLIER - 1.0D) * bonus;
        return (int) Math.round(multiplier * 100.0D);
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
                if (AromaticExtractorBlockEntity.isFluidContainer(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, INPUT_CONTAINER_SLOT, FUEL_SLOT, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (AromaticExtractorBlockEntity.isFuel(rawStack, player.level())) {
                    if (!this.moveItemStackTo(rawStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveBetweenPlayerInventoryAndHotbar(
                        rawStack,
                        quickMovedSlotIndex,
                        PLAYER_INV_START,
                        PLAYER_INV_END,
                        HOTBAR_START,
                        HOTBAR_END
                )) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return ItemStack.EMPTY;
    }

    public interface AromaticExtractorButtonHandler {
        boolean onAromaticExtractorButtonPressed(Player player, int buttonId);
    }
}
