package org.mydrugs.mydrugs.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public final class PsyMixerMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = PsyMixerMultiblock.SLOT_COUNT;
    private static final int PLAYER_INV_START = SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Container container;
    private final BlockPos corePos;
    private final Player player;

    // Slot positions (ritual circle layout)
    private static final int CENTER_X = 80;
    private static final int CENTER_Y = 30;
    private static final int RADIUS = 28;

    // Convenience constructor for client deserialization
    public PsyMixerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), data.readBlockPos());
    }

    public PsyMixerMenu(int containerId, Inventory playerInventory, Container container, BlockPos corePos) {
        super(ModMenus.PSY_MIXER.get(), containerId);
        this.container = container;
        this.corePos = corePos;
        this.player = playerInventory.player;

        checkContainerSize(container, SLOT_COUNT);

        // Center: base drug
        this.addSlot(new RitualSlot(container, PsyMixerMultiblock.SLOT_BASE, CENTER_X, CENTER_Y));
        // Left: material
        this.addSlot(new RitualSlot(container, PsyMixerMultiblock.SLOT_MATERIAL, CENTER_X - RADIUS, CENTER_Y));
        // Top: catalyst
        this.addSlot(new RitualSlot(container, PsyMixerMultiblock.SLOT_CATALYST, CENTER_X, CENTER_Y - RADIUS));
        // Right: stabilizer
        this.addSlot(new RitualSlot(container, PsyMixerMultiblock.SLOT_STABILIZER, CENTER_X + RADIUS, CENTER_Y));
        // Bottom: vessel
        this.addSlot(new RitualSlot(container, PsyMixerMultiblock.SLOT_VESSEL, CENTER_X, CENTER_Y + RADIUS));
        // Output (right side)
        this.addSlot(new OutputSlot(container, PsyMixerMultiblock.SLOT_OUTPUT, CENTER_X + RADIUS + 30, CENTER_Y));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public int getMenuId() {
        return this.containerId;
    }

    public boolean isRunning() {
        if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity be) {
            return be.isRunning();
        }
        return false;
    }

    public int getProgress() {
        if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity be) {
            return be.getProgress();
        }
        return 0;
    }

    public int getMaxProgress() {
        if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity be) {
            return be.getRitualMaxTime();
        }
        return 1;
    }

    public float getInstability() {
        if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity be) {
            return be.getInstability();
        }
        return 0.0F;
    }

    public float getServerPhase() {
        if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity be) {
            return be.getServerPhase();
        }
        return 0.0F;
    }

    public float getTimingWindow() {
        if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity be) {
            return be.getTimingWindow();
        }
        return 0.12F;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity)) {
            return false;
        }
        return player.distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot sourceSlot = this.slots.get(index);
        if (!sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack quickMoved = sourceStack.copy();

        if (index < SLOT_COUNT) {
            if (!this.moveItemStackTo(sourceStack, PLAYER_INV_START, HOTBAR_END, true)) return ItemStack.EMPTY;
        } else {
            // try inserting into ritual slots (not output)
            if (!this.moveItemStackTo(sourceStack, 0, SLOT_COUNT - 1, false)) return ItemStack.EMPTY;
        }

        if (sourceStack.isEmpty()) {
            sourceSlot.setByPlayer(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        if (sourceStack.getCount() == quickMoved.getCount()) return ItemStack.EMPTY;
        sourceSlot.onTake(player, sourceStack);
        return quickMoved;
    }

    private final class RitualSlot extends Slot {
        public RitualSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity core) {
                return core.canPlayerInsert(this.getContainerSlot());
            }
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            if (player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity core) {
                return core.canPlayerInsert(this.getContainerSlot());
            }
            return true;
        }
    }
}
