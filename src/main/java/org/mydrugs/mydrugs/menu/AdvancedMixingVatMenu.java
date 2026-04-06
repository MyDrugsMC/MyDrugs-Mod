package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.AdvancedMixingVatBlockEntity;
import org.mydrugs.mydrugs.menu.layout.AdvancedMixingVatLayout;

public class AdvancedMixingVatMenu extends AbstractMachineMenu {
    public static final int MACHINE_SLOT_COUNT = AdvancedMixingVatBlockEntity.ITEM_SLOT_COUNT;

    private final ItemStacksResourceHandler itemHandler;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    public AdvancedMixingVatMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new ItemStacksResourceHandler(AdvancedMixingVatBlockEntity.ITEM_SLOT_COUNT),
                new SimpleContainerData(AdvancedMixingVatBlockEntity.DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public AdvancedMixingVatMenu(
            int containerId,
            Inventory playerInventory,
            ItemStacksResourceHandler itemHandler,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.ADVANCED_MIXING_VAT.get(), containerId);

        checkContainerDataCount(data, AdvancedMixingVatBlockEntity.DATA_COUNT);

        this.itemHandler = itemHandler;
        this.data = data;
        this.access = access;

        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_RECIPE_0,
                AdvancedMixingVatLayout.ITEM_0_X,
                AdvancedMixingVatLayout.ITEM_0_Y
        ));
        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_RECIPE_1,
                AdvancedMixingVatLayout.ITEM_1_X,
                AdvancedMixingVatLayout.ITEM_1_Y
        ));
        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_RECIPE_2,
                AdvancedMixingVatLayout.ITEM_2_X,
                AdvancedMixingVatLayout.ITEM_2_Y
        ));
        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_RECIPE_3,
                AdvancedMixingVatLayout.ITEM_3_X,
                AdvancedMixingVatLayout.ITEM_3_Y
        ));

        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_TANK_INPUT_A,
                AdvancedMixingVatLayout.TANK_A_SLOT_X,
                AdvancedMixingVatLayout.TANK_SLOT_Y
        ));
        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_TANK_INPUT_B,
                AdvancedMixingVatLayout.TANK_B_SLOT_X,
                AdvancedMixingVatLayout.TANK_SLOT_Y
        ));
        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_TANK_INPUT_C,
                AdvancedMixingVatLayout.TANK_C_SLOT_X,
                AdvancedMixingVatLayout.TANK_SLOT_Y
        ));
        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                AdvancedMixingVatBlockEntity.SLOT_TANK_OUTPUT,
                AdvancedMixingVatLayout.OUTPUT_SLOT_X,
                AdvancedMixingVatLayout.TANK_SLOT_Y
        ));

        this.addPlayerInventorySlots(playerInventory, AdvancedMixingVatLayout.PLAYER_INV_X, AdvancedMixingVatLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.ADVANCED_MIXING_VAT.get());
    }

    public int getInputATankAmount() {
        return this.data.get(0);
    }

    public int getInputBTankAmount() {
        return this.data.get(1);
    }

    public int getInputCTankAmount() {
        return this.data.get(2);
    }

    public int getOutputTankAmount() {
        return this.data.get(3);
    }

    public int getGasAmount() {
        return this.data.get(4);
    }

    public int getProgress() {
        return this.data.get(5);
    }

    public int getMaxProgress() {
        return this.data.get(6);
    }

    public int getInputAFluidSyncId() {
        return this.data.get(7);
    }

    public int getInputBFluidSyncId() {
        return this.data.get(8);
    }

    public int getInputCFluidSyncId() {
        return this.data.get(9);
    }

    public int getOutputFluidSyncId() {
        return this.data.get(10);
    }

    public int getGasSyncId() {
        return this.data.get(11);
    }

    public boolean hasValidRecipe() {
        return this.data.get(12) == 1;
    }

    public Fluid getInputAFluid() {
        return decodeFluid(this.getInputAFluidSyncId());
    }

    public Fluid getInputBFluid() {
        return decodeFluid(this.getInputBFluidSyncId());
    }

    public Fluid getInputCFluid() {
        return decodeFluid(this.getInputCFluidSyncId());
    }

    public Fluid getOutputFluid() {
        return decodeFluid(this.getOutputFluidSyncId());
    }

    private static Fluid decodeFluid(int id) {
        return id < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(id);
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledTank(int amount, int capacity, int pixels) {
        return capacity > 0 ? amount * pixels / capacity : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        var quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            if (quickMovedSlotIndex < MACHINE_SLOT_COUNT) {
                if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(rawStack, 0, MACHINE_SLOT_COUNT, false)) {
                    if (quickMovedSlotIndex < PLAYER_INV_END) {
                        if (!this.moveItemStackTo(rawStack, HOTBAR_START, HOTBAR_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    } else if (quickMovedSlotIndex < HOTBAR_END) {
                        if (!this.moveItemStackTo(rawStack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                            return ItemStack.EMPTY;
                        }
                    }
                }
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

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
    }
}