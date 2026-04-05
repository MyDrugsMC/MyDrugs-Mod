package org.mydrugs.mydrugs.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
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
import net.minecraft.world.item.Items;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;

public class ChemicalReactorMenu extends AbstractContainerMenu {
    public static final int FUEL_SLOT = 0;
    public static final int MACHINE_SLOT_COUNT = 1;
    public static final int DATA_COUNT = 13;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;

    public ChemicalReactorMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL,
                buf.readBlockPos()
        );
    }

    public ChemicalReactorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access, BlockPos blockPos) {
        super(ModMenus.CHEMICAL_REACTOR.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;
        this.blockPos = blockPos;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, FUEL_SLOT, ChemicalReactorLayout.FUEL_SLOT_X, ChemicalReactorLayout.FUEL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(Items.CHARCOAL);
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        ChemicalReactorLayout.PLAYER_INV_X + col * ChemicalReactorLayout.SLOT_SIZE,
                        ChemicalReactorLayout.PLAYER_INV_Y + row * ChemicalReactorLayout.SLOT_SIZE
                ));
            }
        }

        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    ChemicalReactorLayout.HOTBAR_X + col * ChemicalReactorLayout.SLOT_SIZE,
                    ChemicalReactorLayout.HOTBAR_Y
            ));
        }

        this.addDataSlots(data);
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.CHEMICAL_REACTOR.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getPrimaryGasAmount() {
        return this.data.get(0);
    }

    public int getSecondaryGasAmount() {
        return this.data.get(1);
    }

    public int getSecondaryFluidAmount() {
        return this.data.get(2);
    }

    public int getOutputGasAmount() {
        return this.data.get(3);
    }

    public int getOutputFluidAmount() {
        return this.data.get(4);
    }

    public int getProgress() {
        return this.data.get(5);
    }

    public int getMaxProgress() {
        return this.data.get(6);
    }

    public int getHeat() {
        return this.data.get(7);
    }

    public int getMaxHeat() {
        return this.data.get(8);
    }

    public int getBurnTimeRemaining() {
        return this.data.get(9);
    }

    public int getBurnTimeTotal() {
        return this.data.get(10);
    }

    public int getManualEnergy() {
        return this.data.get(11);
    }

    public int getMaxManualEnergy() {
        return this.data.get(12);
    }

    public boolean isLit() {
        return this.getBurnTimeRemaining() > 0;
    }

    public int getScaledProgress(int pixels) {
        int max = this.getMaxProgress();
        return max > 0 ? this.getProgress() * pixels / max : 0;
    }

    public int getScaledHeat(int pixels) {
        int max = this.getMaxHeat();
        return max > 0 ? this.getHeat() * pixels / max : 0;
    }

    public int getScaledBurnTime(int pixels) {
        int total = this.getBurnTimeTotal();
        return total > 0 ? this.getBurnTimeRemaining() * pixels / total : 0;
    }

    public int getScaledManualEnergy(int pixels) {
        int max = this.getMaxManualEnergy();
        return max > 0 ? this.getManualEnergy() * pixels / max : 0;
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
                if (rawStack.is(Items.CHARCOAL)) {
                    if (!this.moveItemStackTo(rawStack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
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

    public ChemicalReactorBlockEntity getBlockEntity(Player player) {
        if (player.level().getBlockEntity(this.blockPos) instanceof ChemicalReactorBlockEntity blockEntity) {
            return blockEntity;
        }
        return null;
    }
}