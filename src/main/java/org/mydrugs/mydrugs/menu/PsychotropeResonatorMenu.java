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
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorFailureReason;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity.ResonatorState;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.menu.layout.PsychotropeResonatorLayout;

public final class PsychotropeResonatorMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 11;
    public static final int MACHINE_SLOT_COUNT = PsychotropeResonatorBlockEntity.SLOT_COUNT;
    public static final int DREAM_ALIGNMENT_BUTTON_ID = 0;
    public static final int INTEGRATION_BUTTON_ID = 1;
    public static final int RECOVERY_RESONANCE_BUTTON_ID = 2;
    public static final int OPEN_DIMENSION_BUTTON_ID = 3;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public PsychotropeResonatorMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public PsychotropeResonatorMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            ContainerData data,
            ContainerLevelAccess access
    ) {
        super(ModMenus.PSYCHOTROPE_RESONATOR.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);
        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, PsychotropeResonatorBlockEntity.SLOT_MATERIAL,
                PsychotropeResonatorLayout.MATERIAL_SLOT_X, PsychotropeResonatorLayout.MATERIAL_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PsychotropeResonatorBlockEntity.isIntegrationMaterial(stack);
            }
        });
        this.addSlot(new Slot(container, PsychotropeResonatorBlockEntity.SLOT_INTEGRATION_CORE,
                PsychotropeResonatorLayout.CORE_SLOT_X, PsychotropeResonatorLayout.CORE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PsychotropeResonatorBlockEntity.isIntegrationCore(stack);
            }
        });
        this.addSlot(new Slot(container, PsychotropeResonatorBlockEntity.SLOT_DIARY,
                PsychotropeResonatorLayout.DIARY_SLOT_X, PsychotropeResonatorLayout.DIARY_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PsychotropeResonatorBlockEntity.isDiary(stack);
            }
        });
        this.addPlayerInventorySlots(playerInventory, PsychotropeResonatorLayout.PLAYER_INV_X, PsychotropeResonatorLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.PSYCHOTROPE_RESONATOR.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        return this.container instanceof PsychotropeResonatorBlockEntity resonator
                && resonator.onButtonPressed(player, id);
    }

    public int progress() {
        return this.data.get(0);
    }

    public int maxProgress() {
        return this.data.get(1);
    }

    public ResonatorState state() {
        return ResonatorState.byNetworkId(this.data.get(2));
    }

    public int cooldownTicks() {
        return this.data.get(3);
    }

    public boolean dimensionReady() {
        return this.data.get(4) != 0;
    }

    public boolean canOpenDimension() {
        return this.data.get(10) != 0;
    }

    public @Nullable DrugId activeIntegrationDrug() {
        return DrugId.byNetworkId(this.data.get(6));
    }

    public PsychotropeResonatorFailureReason failureReason() {
        return PsychotropeResonatorFailureReason.byNetworkId(this.data.get(7));
    }

    public @Nullable DrugId candidateDrug() {
        return DrugId.byNetworkId(this.data.get(8));
    }

    public int checklistMask() {
        return this.data.get(9);
    }

    public int getScaledProgress(int pixels) {
        int total = maxProgress();
        return total > 0 ? progress() * pixels / total : 0;
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
                if (PsychotropeResonatorBlockEntity.isIntegrationMaterial(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, PsychotropeResonatorBlockEntity.SLOT_MATERIAL,
                            PsychotropeResonatorBlockEntity.SLOT_MATERIAL + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (PsychotropeResonatorBlockEntity.isIntegrationCore(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, PsychotropeResonatorBlockEntity.SLOT_INTEGRATION_CORE,
                            PsychotropeResonatorBlockEntity.SLOT_INTEGRATION_CORE + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (PsychotropeResonatorBlockEntity.isDiary(rawStack)) {
                    if (!this.moveItemStackTo(rawStack, PsychotropeResonatorBlockEntity.SLOT_DIARY,
                            PsychotropeResonatorBlockEntity.SLOT_DIARY + 1, false)) {
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
