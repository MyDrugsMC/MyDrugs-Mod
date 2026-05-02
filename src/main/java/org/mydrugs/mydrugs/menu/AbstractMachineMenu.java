package org.mydrugs.mydrugs.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.menu.layout.StandardInventoryLayout;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferMenuAccess;

import java.lang.reflect.Field;

public abstract class AbstractMachineMenu extends AbstractContainerMenu implements MachineTransferMenuAccess {
    private int syncedHasEnergyStorage;
    private int syncedEnergyStored;
    private int syncedEnergyCapacity;
    private int syncedMachineStatus = MachineStatus.IDLE.networkId();

    protected AbstractMachineMenu(MenuType<?> menuType, int containerId) {
        super(menuType, containerId);
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                BlockEntity target = findTargetFromLevelAccess();
                return target != null && MachineEnergyAttachments.hasEnergyStorage(target) ? 1 : 0;
            }

            @Override
            public void set(int value) {
                syncedHasEnergyStorage = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                BlockEntity target = findTargetFromLevelAccess();
                return target != null && MachineEnergyAttachments.hasEnergyStorage(target)
                        ? MachineEnergyAttachments.get(target).storage().stored()
                        : 0;
            }

            @Override
            public void set(int value) {
                syncedEnergyStored = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                BlockEntity target = findTargetFromLevelAccess();
                return target != null && MachineEnergyAttachments.hasEnergyStorage(target)
                        ? MachineEnergyAttachments.get(target).storage().capacity()
                        : 0;
            }

            @Override
            public void set(int value) {
                syncedEnergyCapacity = value;
            }
        });
        this.addDataSlot(new DataSlot() {
            @Override
            public int get() {
                BlockEntity target = findTargetFromLevelAccess();
                if (target == null) {
                    target = findTargetFromContainer();
                }
                return target instanceof MachineStatusProvider provider
                        ? provider.getMachineStatus().networkId()
                        : MachineStatus.IDLE.networkId();
            }

            @Override
            public void set(int value) {
                syncedMachineStatus = value;
            }
        });
    }

    protected void addPlayerInventorySlots(Inventory playerInventory, int playerInvX, int playerInvY) {
        for (int row = 0; row < StandardInventoryLayout.PLAYER_INV_ROWS; row++) {
            for (int col = 0; col < StandardInventoryLayout.PLAYER_INV_COLS; col++) {
                this.addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        StandardInventoryLayout.playerSlotX(playerInvX, col),
                        StandardInventoryLayout.playerSlotY(playerInvY, row)
                ));
            }
        }

        for (int col = 0; col < StandardInventoryLayout.HOTBAR_COLS; col++) {
            this.addSlot(new Slot(
                    playerInventory,
                    col,
                    StandardInventoryLayout.hotbarSlotX(playerInvX, col),
                    StandardInventoryLayout.hotbarSlotY(playerInvY)
            ));
        }
    }

    protected boolean moveToPlayerInventory(ItemStack stack, int playerInventoryStart, int hotbarEnd) {
        return this.moveItemStackTo(stack, playerInventoryStart, hotbarEnd, true);
    }

    protected boolean moveToPlayerInventory(ItemStack stack, int playerInventoryStart, int hotbarEnd, boolean reverse) {
        return this.moveItemStackTo(stack, playerInventoryStart, hotbarEnd, reverse);
    }

    protected boolean moveBetweenPlayerInventoryAndHotbar(
            ItemStack stack,
            int sourceIndex,
            int playerInventoryStart,
            int playerInventoryEnd,
            int hotbarStart,
            int hotbarEnd
    ) {
        if (sourceIndex < playerInventoryEnd) {
            return this.moveItemStackTo(stack, hotbarStart, hotbarEnd, false);
        }

        return sourceIndex < hotbarEnd
                && this.moveItemStackTo(stack, playerInventoryStart, playerInventoryEnd, false);
    }

    protected ItemStack finishQuickMove(Player player, Slot sourceSlot, ItemStack sourceStack, ItemStack originalStack) {
        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }

        if (sourceStack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        sourceSlot.onTake(player, sourceStack);
        return originalStack;
    }

    @Override
    public @Nullable BlockEntity getMachineTransferTarget(Player player) {
        BlockEntity byAccess = findTargetFromLevelAccess();
        if (byAccess != null) {
            return byAccess;
        }

        BlockEntity byPosition = findTargetFromPosition(player);
        if (byPosition != null) {
            return byPosition;
        }

        return findTargetFromContainer();
    }

    private @Nullable BlockEntity findTargetFromLevelAccess() {
        for (Field field : allFields()) {
            if (!ContainerLevelAccess.class.isAssignableFrom(field.getType())) {
                continue;
            }

            Object value = getFieldValue(field);
            if (value instanceof ContainerLevelAccess access) {
                BlockEntity blockEntity = access.evaluate((level, pos) -> level.getBlockEntity(pos), null);
                if (blockEntity != null) {
                    return blockEntity;
                }
            }
        }

        return null;
    }

    private @Nullable BlockEntity findTargetFromPosition(Player player) {
        for (Field field : allFields()) {
            if (!BlockPos.class.isAssignableFrom(field.getType())) {
                continue;
            }

            Object value = getFieldValue(field);
            if (value instanceof BlockPos pos) {
                BlockEntity blockEntity = player.level().getBlockEntity(pos);
                if (blockEntity != null) {
                    return blockEntity;
                }
            }
        }

        return null;
    }

    private @Nullable BlockEntity findTargetFromContainer() {
        for (Field field : allFields()) {
            if (!Container.class.isAssignableFrom(field.getType())) {
                continue;
            }

            Object value = getFieldValue(field);
            if (value instanceof BlockEntity blockEntity) {
                return blockEntity;
            }
        }

        return null;
    }

    private Iterable<Field> allFields() {
        java.util.ArrayList<Field> fields = new java.util.ArrayList<>();
        Class<?> type = this.getClass();
        while (type != null && AbstractContainerMenu.class.isAssignableFrom(type)) {
            for (Field field : type.getDeclaredFields()) {
                fields.add(field);
            }
            type = type.getSuperclass();
        }
        return fields;
    }

    private @Nullable Object getFieldValue(Field field) {
        try {
            field.setAccessible(true);
            return field.get(this);
        } catch (IllegalAccessException ignored) {
            return null;
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
    }

    @Override
    public void setData(int id, int value) {
        super.setData(id, value);
    }

    public boolean hasSyncedEnergyStorage() {
        return this.syncedHasEnergyStorage != 0;
    }

    public int syncedEnergyStored() {
        return this.syncedEnergyStored;
    }

    public int syncedEnergyCapacity() {
        return this.syncedEnergyCapacity;
    }

    public MachineStatus syncedMachineStatus() {
        return MachineStatus.byNetworkId(this.syncedMachineStatus);
    }
}
