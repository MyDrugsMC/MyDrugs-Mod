package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.AdvancedFurnaceBlockEntity;
import org.mydrugs.mydrugs.machine.fuel.MachineFuelUtil;
import org.mydrugs.mydrugs.machine.item.MachineItemUtil;
import org.mydrugs.mydrugs.menu.layout.AdvancedFurnaceLayout;

public final class AdvancedFurnaceMenu extends AbstractMachineMenu {
    private static final int MACHINE_SLOT_COUNT = AdvancedFurnaceBlockEntity.SLOT_COUNT;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public AdvancedFurnaceMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(6),
                ContainerLevelAccess.NULL
        );
    }

    public AdvancedFurnaceMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.ADVANCED_FURNACE.get(), containerId);

        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 6);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(
                container,
                AdvancedFurnaceBlockEntity.INPUT_A_SLOT,
                AdvancedFurnaceLayout.INPUT_A_X,
                AdvancedFurnaceLayout.INPUT_A_Y
        ));

        this.addSlot(new Slot(
                container,
                AdvancedFurnaceBlockEntity.INPUT_B_SLOT,
                AdvancedFurnaceLayout.INPUT_B_X,
                AdvancedFurnaceLayout.INPUT_B_Y
        ));

        this.addSlot(new Slot(
                container,
                AdvancedFurnaceBlockEntity.FUEL_SLOT,
                AdvancedFurnaceLayout.FUEL_X,
                AdvancedFurnaceLayout.FUEL_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MachineFuelUtil.getBurnTime(stack, playerInventory.player.level(), MachineFuelUtil.VANILLA) > 0;
            }
        });

        this.addSlot(new Slot(
                container,
                AdvancedFurnaceBlockEntity.OUTPUT_A_SLOT,
                AdvancedFurnaceLayout.OUTPUT_A_X,
                AdvancedFurnaceLayout.OUTPUT_A_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new Slot(
                container,
                AdvancedFurnaceBlockEntity.OUTPUT_B_SLOT,
                AdvancedFurnaceLayout.OUTPUT_B_X,
                AdvancedFurnaceLayout.OUTPUT_B_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addSlot(new Slot(
                container,
                AdvancedFurnaceBlockEntity.OUTPUT_FLUID_CONTAINER_SLOT,
                AdvancedFurnaceLayout.OUTPUT_CONTAINER_X,
                AdvancedFurnaceLayout.OUTPUT_CONTAINER_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return MachineItemUtil.isFluidContainer(stack);
            }
        });

        this.addPlayerInventorySlots(playerInventory, AdvancedFurnaceLayout.PLAYER_INV_X, AdvancedFurnaceLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.ADVANCED_FURNACE.get());
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public int getBurnTime() {
        return this.data.get(2);
    }

    public int getBurnDuration() {
        return this.data.get(3);
    }

    public int getTankAmount() {
        return this.data.get(4);
    }

    public int getTankFluidSyncId() {
        return this.data.get(5);
    }

    public boolean isBurning() {
        return this.getBurnTime() > 0;
    }

    public Fluid getTankFluid() {
        int syncId = this.getTankFluidSyncId();
        Fluid fluid = BuiltInRegistries.FLUID.byId(syncId);
        return fluid == null ? Fluids.EMPTY : fluid;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledBurn(int pixels) {
        int burn = this.getBurnTime();
        int max = this.getBurnDuration();
        return max > 0 ? burn * pixels / max : 0;
    }

    public int getScaledTank(int pixels) {
        return AdvancedFurnaceBlockEntity.TANK_CAPACITY > 0
                ? this.getTankAmount() * pixels / AdvancedFurnaceBlockEntity.TANK_CAPACITY
                : 0;
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
            boolean movedToMachine = false;

            if (MachineFuelUtil.getBurnTime(sourceStack, player.level(), MachineFuelUtil.VANILLA) > 0) {
                movedToMachine = this.moveItemStackTo(
                        sourceStack,
                        AdvancedFurnaceBlockEntity.FUEL_SLOT,
                        AdvancedFurnaceBlockEntity.FUEL_SLOT + 1,
                        false
                );
            }

            if (!movedToMachine && MachineItemUtil.isFluidContainer(sourceStack)) {
                movedToMachine = this.moveItemStackTo(
                        sourceStack,
                        AdvancedFurnaceBlockEntity.OUTPUT_FLUID_CONTAINER_SLOT,
                        AdvancedFurnaceBlockEntity.OUTPUT_FLUID_CONTAINER_SLOT + 1,
                        false
                );
            }

            if (!movedToMachine) {
                movedToMachine = this.moveItemStackTo(
                        sourceStack,
                        AdvancedFurnaceBlockEntity.INPUT_A_SLOT,
                        AdvancedFurnaceBlockEntity.FUEL_SLOT,
                        false
                );
            }

            if (!movedToMachine) {
                if (index < PLAYER_INV_END) {
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
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
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