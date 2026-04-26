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
import org.mydrugs.mydrugs.blocks.entity.BiochemicalReactorBlockEntity;
import org.mydrugs.mydrugs.machine.item.MachineItemUtil;
import org.mydrugs.mydrugs.menu.layout.BiochemicalReactorLayout;

public class BiochemicalReactorMenu extends AbstractMachineMenu {
    public static final int OUTPUT_CONTAINER_SLOT = 3;
    public static final int MACHINE_SLOT_COUNT = 4;
    public static final int DATA_COUNT = 10;

    public static final int MANUAL_BOOST_BUTTON_ID = 0;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public BiochemicalReactorMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public BiochemicalReactorMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.BIOCHEMICAL_REACTOR.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                BiochemicalReactorBlockEntity.SLOT_ERGOT,
                BiochemicalReactorLayout.ERGOT_SLOT_X,
                BiochemicalReactorLayout.ERGOT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BiochemicalReactorBlockEntity.isErgot(stack);
            }
        });

        this.addSlot(new Slot(
                container,
                BiochemicalReactorBlockEntity.SLOT_TRYPTOPHAN,
                BiochemicalReactorLayout.TRYPTOPHAN_SLOT_X,
                BiochemicalReactorLayout.TRYPTOPHAN_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BiochemicalReactorBlockEntity.isTryptophan(stack);
            }
        });

        this.addSlot(new Slot(
                container,
                BiochemicalReactorBlockEntity.SLOT_CHARCOAL,
                BiochemicalReactorLayout.CHARCOAL_SLOT_X,
                BiochemicalReactorLayout.CHARCOAL_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return BiochemicalReactorBlockEntity.isCharcoal(stack);
            }
        });

        this.addSlot(new Slot(
                container,
                OUTPUT_CONTAINER_SLOT,
                BiochemicalReactorLayout.OUTPUT_SLOT_X,
                BiochemicalReactorLayout.OUTPUT_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MachineItemUtil.isFluidContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addPlayerInventorySlots(playerInventory, BiochemicalReactorLayout.PLAYER_INV_X, BiochemicalReactorLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.BIOCHEMICAL_REACTOR.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof ReactorButtonHandler handler) {
            return switch (id) {
                case MANUAL_BOOST_BUTTON_ID -> handler.onReactorButtonPressed(player, id);
                default -> false;
            };
        }
        return false;
    }

    public int getProgressUnits() {
        return this.data.get(0);
    }

    public int getMaxProgressUnits() {
        return this.data.get(1);
    }

    public int getHeat() {
        return this.data.get(2);
    }

    public int getMaxHeat() {
        return this.data.get(3);
    }

    public int getManualEnergy() {
        return this.data.get(4);
    }

    public int getMaxManualEnergy() {
        return this.data.get(5);
    }

    public int getOutputTankAmount() {
        return this.data.get(6);
    }

    public int getOutputTankCapacity() {
        return this.data.get(7);
    }

    public int getOutputFluidSyncId() {
        return this.data.get(8);
    }

    public boolean isWorking() {
        return this.data.get(9) != 0;
    }

    public Fluid getOutputFluid() {
        return decodeFluid(this.getOutputFluidSyncId());
    }

    public Component getOutputFluidName() {
        Fluid fluid = this.getOutputFluid();
        return fluid == Fluids.EMPTY ? Component.literal("empty") : fluid.getFluidType().getDescription();
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgressUnits();
        int max = this.getMaxProgressUnits();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledHeat(int pixels) {
        int current = this.getHeat();
        int max = this.getMaxHeat();
        return max > 0 ? current * pixels / max : 0;
    }

    public int getScaledManualEnergy(int pixels) {
        int current = this.getManualEnergy();
        int max = this.getMaxManualEnergy();
        return max > 0 ? current * pixels / max : 0;
    }

    public int getScaledOutputTank(int pixels) {
        int current = this.getOutputTankAmount();
        int max = this.getOutputTankCapacity();
        return max > 0 ? current * pixels / max : 0;
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
                if (BiochemicalReactorBlockEntity.isErgot(rawStack)) {
                    if (!this.moveItemStackTo(rawStack,
                            BiochemicalReactorBlockEntity.SLOT_ERGOT,
                            BiochemicalReactorBlockEntity.SLOT_ERGOT + 1,
                            false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (BiochemicalReactorBlockEntity.isTryptophan(rawStack)) {
                    if (!this.moveItemStackTo(rawStack,
                            BiochemicalReactorBlockEntity.SLOT_TRYPTOPHAN,
                            BiochemicalReactorBlockEntity.SLOT_TRYPTOPHAN + 1,
                            false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (BiochemicalReactorBlockEntity.isCharcoal(rawStack)) {
                    if (!this.moveItemStackTo(rawStack,
                            BiochemicalReactorBlockEntity.SLOT_CHARCOAL,
                            BiochemicalReactorBlockEntity.SLOT_CHARCOAL + 1,
                            false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (MachineItemUtil.isFluidContainer(rawStack)) {
                    if (!this.moveItemStackTo(rawStack,
                            OUTPUT_CONTAINER_SLOT,
                            OUTPUT_CONTAINER_SLOT + 1,
                            false)) {
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

    public interface ReactorButtonHandler {
        boolean onReactorButtonPressed(Player player, int buttonId);
    }
}
