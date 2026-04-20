package org.mydrugs.mydrugs.menu;

import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.items.rolling.RollerLogic;
import org.mydrugs.mydrugs.menu.layout.RollerLayout;
import org.mydrugs.mydrugs.menu.slot.RollerMachineSlot;
import org.mydrugs.mydrugs.menu.slot.RollerOutputSlot;

public class RollerMenu extends AbstractMachineMenu {
    public static final int PAPER_SLOT = 0;
    public static final int FILTER_SLOT = 1;
    public static final int INGREDIENT_1_SLOT = 2;
    public static final int INGREDIENT_2_SLOT = 3;
    public static final int INGREDIENT_3_SLOT = 4;
    public static final int OUTPUT_SLOT = 5;
    public static final int MAX_PROGRESS = 100;
    private static final int MACHINE_SLOT_COUNT = 6;
    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;
    private final Container machine;
    private final ContainerData data;

    public RollerMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new SimpleContainer(MACHINE_SLOT_COUNT), new SimpleContainerData(2));
    }

    public RollerMenu(int containerId, Inventory playerInventory, Container machine, ContainerData data) {
        super(ModMenus.ROLLER.get(), containerId);

        checkContainerSize(machine, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, 2);

        this.machine = machine;
        this.data = data;
        this.data.set(1, MAX_PROGRESS);

        this.addSlot(new RollerMachineSlot(this, machine, PAPER_SLOT, RollerLayout.PAPER_X, RollerLayout.PAPER_Y));
        this.addSlot(new RollerMachineSlot(this, machine, FILTER_SLOT, RollerLayout.FILTER_X, RollerLayout.FILTER_Y));
        this.addSlot(new RollerMachineSlot(this, machine, INGREDIENT_1_SLOT, RollerLayout.INGREDIENT_1_X, RollerLayout.INGREDIENT_1_Y));
        this.addSlot(new RollerMachineSlot(this, machine, INGREDIENT_2_SLOT, RollerLayout.INGREDIENT_2_X, RollerLayout.INGREDIENT_2_Y));
        this.addSlot(new RollerMachineSlot(this, machine, INGREDIENT_3_SLOT, RollerLayout.INGREDIENT_3_X, RollerLayout.INGREDIENT_3_Y));
        this.addSlot(new RollerOutputSlot(this, machine, OUTPUT_SLOT, RollerLayout.OUTPUT_X, RollerLayout.OUTPUT_Y));

        this.addPlayerInventorySlots(playerInventory, RollerLayout.PLAYER_INV_X, RollerLayout.PLAYER_INV_Y);

        this.addDataSlots(data);
    }

    public void onMachineContentsChanged() {
        ItemStack expected = RollerLogic.createResult(this.machine);
        ItemStack output = this.machine.getItem(OUTPUT_SLOT);

        if (expected.isEmpty() || !RollerLogic.canPlaceResult(output, expected)) {
            this.setProgress(0);
        }

        this.machine.setChanged();
    }

    public void addRollProgress(float amount) {
        if (amount <= 0.0F) {
            return;
        }

        ItemStack expected = RollerLogic.createResult(this.machine);
        ItemStack output = this.machine.getItem(OUTPUT_SLOT);

        if (expected.isEmpty()) {
            this.setProgress(0);
            return;
        }

        if (!RollerLogic.canPlaceResult(output, expected)) {
            return;
        }

        int next = Mth.clamp(this.getProgress() + Mth.floor(amount), 0, this.getMaxProgress());
        this.setProgress(next);

        if (next >= this.getMaxProgress()) {
            RollerLogic.craft(this.machine);
            this.setProgress(0);
        }
    }

    public int getMenuId() {
        return this.containerId;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    private void setProgress(int value) {
        this.data.set(0, Mth.clamp(value, 0, this.getMaxProgress()));
    }

    public int getMaxProgress() {
        int max = this.data.get(1);
        return max <= 0 ? MAX_PROGRESS : max;
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max <= 0 ? 0 : progress * pixels / max;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);

        if (player.level().isClientSide()) {
            return;
        }

        for (int i = 0; i < this.machine.getContainerSize(); i++) {
            ItemStack stack = this.machine.removeItemNoUpdate(i);
            if (stack.isEmpty()) {
                continue;
            }

            if (!player.getInventory().add(stack)) {
                player.drop(stack, false);
            }
        }
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
            if (RollerLogic.isPaper(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, PAPER_SLOT, PAPER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (RollerLogic.isFilter(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, FILTER_SLOT, FILTER_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (RollerLogic.isRollingIngredient(sourceStack)) {
                if (!this.moveItemStackTo(sourceStack, INGREDIENT_1_SLOT, OUTPUT_SLOT, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < PLAYER_INV_END) {
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

        if (sourceStack.isEmpty()) {
            sourceSlot.set(ItemStack.EMPTY);
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