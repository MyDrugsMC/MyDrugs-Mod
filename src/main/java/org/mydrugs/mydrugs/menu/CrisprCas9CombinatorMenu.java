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
import org.mydrugs.mydrugs.blocks.entity.CrisprCas9CombinatorBlockEntity;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.menu.layout.CrisprCas9CombinatorLayout;

public class CrisprCas9CombinatorMenu extends AbstractMachineMenu {
    public static final int INPUT_A_SLOT = CrisprCas9CombinatorBlockEntity.INPUT_A_SLOT;
    public static final int INPUT_B_SLOT = CrisprCas9CombinatorBlockEntity.INPUT_B_SLOT;
    public static final int OUTPUT_SLOT = CrisprCas9CombinatorBlockEntity.OUTPUT_SLOT;
    public static final int MACHINE_SLOT_COUNT = CrisprCas9CombinatorBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = 5;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CrisprCas9CombinatorMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public CrisprCas9CombinatorMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.CRISPR_CAS9_COMBINATOR.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addGeneInputSlot(container, INPUT_A_SLOT, CrisprCas9CombinatorLayout.INPUT_A_SLOT_X, CrisprCas9CombinatorLayout.INPUT_A_SLOT_Y);
        this.addGeneInputSlot(container, INPUT_B_SLOT, CrisprCas9CombinatorLayout.INPUT_B_SLOT_X, CrisprCas9CombinatorLayout.INPUT_B_SLOT_Y);
        this.addSlot(new Slot(container, OUTPUT_SLOT, CrisprCas9CombinatorLayout.OUTPUT_SLOT_X, CrisprCas9CombinatorLayout.OUTPUT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        this.addPlayerInventorySlots(playerInventory, CrisprCas9CombinatorLayout.PLAYER_INV_X, CrisprCas9CombinatorLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    private void addGeneInputSlot(Container container, int slot, int x, int y) {
        this.addSlot(new Slot(container, slot, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.is(ModItems.DNA_GENE.get()) && stack.get(ModDataComponents.DNA_GENE_DATA.get()) != null;
            }
        });
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.CRISPR_CAS9_COMBINATOR.get());
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

    public boolean isSameSourceBlocked() {
        return this.data.get(2) != 0;
    }

    public int getStabilityPercent() {
        return this.data.get(3);
    }

    public int getEnergyPerTick() {
        return this.data.get(4);
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
                if (rawStack.is(ModItems.DNA_GENE.get()) && rawStack.get(ModDataComponents.DNA_GENE_DATA.get()) != null) {
                    if (!this.moveItemStackTo(rawStack, INPUT_A_SLOT, OUTPUT_SLOT, false)) {
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

        return quickMovedStack;
    }
}
