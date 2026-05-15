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
import org.mydrugs.mydrugs.blocks.entity.BacterialIncubatorBlockEntity;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.AdnGeneData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.menu.layout.BacterialIncubatorLayout;

public class BacterialIncubatorMenu extends AbstractMachineMenu {
    public static final int GENE_SLOT = BacterialIncubatorBlockEntity.GENE_SLOT;
    public static final int NUTRIENT_SLOT = BacterialIncubatorBlockEntity.NUTRIENT_SLOT;
    public static final int OUTPUT_SLOT = BacterialIncubatorBlockEntity.OUTPUT_SLOT;
    public static final int MACHINE_SLOT_COUNT = BacterialIncubatorBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = 3;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public BacterialIncubatorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(DATA_COUNT), ContainerLevelAccess.NULL);
    }

    public BacterialIncubatorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.BACTERIAL_INCUBATOR.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, GENE_SLOT, BacterialIncubatorLayout.GENE_SLOT_X, BacterialIncubatorLayout.GENE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isValidGeneStack(stack);
            }
        });
        this.addSlot(new Slot(container, NUTRIENT_SLOT, BacterialIncubatorLayout.NUTRIENT_SLOT_X, BacterialIncubatorLayout.NUTRIENT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.NUTRIENT_GEL.get());
            }
        });
        this.addSlot(new Slot(container, OUTPUT_SLOT, BacterialIncubatorLayout.OUTPUT_SLOT_X, BacterialIncubatorLayout.OUTPUT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addPlayerInventorySlots(playerInventory, BacterialIncubatorLayout.PLAYER_INV_X, BacterialIncubatorLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    private static boolean isValidGeneStack(ItemStack stack) {
        if (!stack.is(ModItems.ADN_GENE.get())) {
            return false;
        }
        AdnGeneData data = stack.get(ModDataComponents.ADN_GENE_DATA.get());
        return data != null && !data.broken() && !data.stats().isEmpty();
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.BACTERIAL_INCUBATOR.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public int getEnergyPerTick() {
        return this.data.get(2);
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
                if (!this.moveToPlayerInventory(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (quickMovedSlotIndex < HOTBAR_END) {
                if (isValidGeneStack(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, GENE_SLOT, GENE_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (rawStack.is(ModItems.NUTRIENT_GEL.get())) {
                    if (!this.moveItemStackTo(rawStack, NUTRIENT_SLOT, NUTRIENT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveBetweenPlayerInventoryAndHotbar(rawStack, quickMovedSlotIndex, PLAYER_INV_START, PLAYER_INV_END, HOTBAR_START, HOTBAR_END)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return quickMovedStack;
    }
}
