package org.mydrugs.mydrugs.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualAction;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualEngine;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualFocus;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualJudgement;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;
import org.mydrugs.mydrugs.menu.slot.OutputSlot;

public final class PsyMixerMenu extends AbstractContainerMenu {
    private static final int SLOT_COUNT = PsyMixerMultiblock.SLOT_COUNT;
    private static final int PLAYER_INV_START = SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_END = PLAYER_INV_END + 9;

    private final Container container;
    private final BlockPos corePos;
    private final Player player;
    private final ContainerData ritualData;

    // Slot positions (ritual circle layout)
    private static final int CENTER_X = 82;
    private static final int CENTER_Y = 52;
    private static final int RADIUS = 30;
    private static final int PLAYER_INV_X = 10;
    private static final int PLAYER_INV_Y = 114;
    private static final int HOTBAR_Y = 172;

    private static final int DATA_RUNNING = 0;
    private static final int DATA_PROGRESS = 1;
    private static final int DATA_MAX_PROGRESS = 2;
    private static final int DATA_CURRENT_ACTION = 3;
    private static final int DATA_ACTION_INDEX = 4;
    private static final int DATA_ACTION_COUNT = 5;
    private static final int DATA_INPUT_COOLDOWN = 6;
    private static final int DATA_GOOD_HITS = 7;
    private static final int DATA_MISTAKES = 8;
    private static final int DATA_FOCUS_INDEX = 9;
    private static final int DATA_QUALITY_PREVIEW = 10;
    private static final int DATA_STREAK = 11;
    private static final int DATA_LAST_JUDGEMENT = 12;
    private static final int DATA_FEEDBACK_TICKS = 13;
    private static final int DATA_LAST_ACCURACY = 14;
    private static final int DATA_TARGET_PHASE = 15;
    private static final int DATA_CURRENT_TIMING_WINDOW = 16;
    private static final int DATA_ACTION_TICK = 17;
    private static final int DATA_ACTION_TIMEOUT = 18;
    private static final int DATA_MAX_MISTAKES = 19;
    private static final int DATA_COUNT = 20;
    private static final int FLOAT_SCALE = 10_000;

    // Convenience constructor for client deserialization
    public PsyMixerMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf data) {
        this(containerId, playerInventory, new SimpleContainer(SLOT_COUNT), data.readBlockPos(), new SimpleContainerData(DATA_COUNT));
    }

    public PsyMixerMenu(int containerId, Inventory playerInventory, Container container, BlockPos corePos) {
        this(containerId, playerInventory, container, corePos, createData(container));
    }

    private PsyMixerMenu(int containerId, Inventory playerInventory, Container container, BlockPos corePos, ContainerData ritualData) {
        super(ModMenus.PSY_MIXER.get(), containerId);
        this.container = container;
        this.corePos = corePos;
        this.player = playerInventory.player;
        this.ritualData = ritualData;

        checkContainerSize(container, SLOT_COUNT);
        this.addDataSlots(ritualData);

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
        this.addSlot(new OutputSlot(container, PsyMixerMultiblock.SLOT_OUTPUT, CENTER_X + RADIUS + 34, CENTER_Y));

        // Player inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * 18, PLAYER_INV_Y + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * 18, HOTBAR_Y));
        }
    }

    public BlockPos getCorePos() {
        return corePos;
    }

    public int getMenuId() {
        return this.containerId;
    }

    public boolean isRunning() {
        return ritualData.get(DATA_RUNNING) != 0;
    }

    public int getProgress() {
        return ritualData.get(DATA_PROGRESS);
    }

    public int getMaxProgress() {
        return Math.max(1, ritualData.get(DATA_MAX_PROGRESS));
    }

    public PsyMixerRitualAction getCurrentAction() {
        return PsyMixerRitualAction.byId(ritualData.get(DATA_CURRENT_ACTION));
    }

    public int getActionIndex() {
        return ritualData.get(DATA_ACTION_INDEX);
    }

    public int getActionCount() {
        return ritualData.get(DATA_ACTION_COUNT);
    }

    public PsyMixerRitualQuality getQualityPreview() {
        return PsyMixerRitualQuality.byId(ritualData.get(DATA_QUALITY_PREVIEW));
    }

    public int getActionTick() {
        return ritualData.get(DATA_ACTION_TICK);
    }

    public int getActionTimeout() {
        return Math.max(1, ritualData.get(DATA_ACTION_TIMEOUT));
    }

    public float getServerPhase() {
        return PsyMixerRitualEngine.phase(getProgress(), getMaxProgress());
    }

    public float getTimingWindow() {
        return Math.max(0.02F, ritualData.get(DATA_CURRENT_TIMING_WINDOW) / (float) FLOAT_SCALE);
    }

    public int getRhythmInputCooldown() {
        return ritualData.get(DATA_INPUT_COOLDOWN);
    }

    public int getGoodHits() {
        return ritualData.get(DATA_GOOD_HITS);
    }

    public int getBadHits() {
        return getMistakes();
    }

    public int getMistakes() {
        return ritualData.get(DATA_MISTAKES);
    }

    public int getMaxMistakes() {
        return ritualData.get(DATA_MAX_MISTAKES);
    }

    public PsyMixerRitualFocus getFocus() {
        return PsyMixerRitualFocus.byId(ritualData.get(DATA_FOCUS_INDEX));
    }

    public int getFocusSlot() {
        return getFocus().slot();
    }

    public float getTargetPhase() {
        return ritualData.get(DATA_TARGET_PHASE) / (float) FLOAT_SCALE;
    }

    public float getResonance() {
        return getActionCount() <= 0 ? 0.0F : (float) getActionIndex() / getActionCount();
    }

    public int getStreak() {
        return ritualData.get(DATA_STREAK);
    }

    public PsyMixerRitualJudgement getLastJudgement() {
        return PsyMixerRitualJudgement.byId(ritualData.get(DATA_LAST_JUDGEMENT));
    }

    public int getFeedbackTicks() {
        return ritualData.get(DATA_FEEDBACK_TICKS);
    }

    public float getLastAccuracy() {
        return ritualData.get(DATA_LAST_ACCURACY) / (float) FLOAT_SCALE;
    }

    public boolean hasRitualItem(int slot) {
        return slot >= 0 && slot < SLOT_COUNT && this.slots.get(slot).hasItem();
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player.level().getBlockEntity(corePos) instanceof FormedPsyMixerCoreBlockEntity core)) {
            return false;
        }
        return core.isStructureIntact()
                && player.distanceToSqr(corePos.getX() + 0.5, corePos.getY() + 0.5, corePos.getZ() + 0.5) <= 64.0;
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

    private static ContainerData createData(Container container) {
        if (container instanceof FormedPsyMixerCoreBlockEntity core) {
            return new PsyMixerRitualData(core);
        }
        return new SimpleContainerData(DATA_COUNT);
    }

    private static final class PsyMixerRitualData implements ContainerData {
        private final FormedPsyMixerCoreBlockEntity core;

        private PsyMixerRitualData(FormedPsyMixerCoreBlockEntity core) {
            this.core = core;
        }

        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_RUNNING -> core.isRunning() ? 1 : 0;
                case DATA_PROGRESS -> core.getProgress();
                case DATA_MAX_PROGRESS -> core.getRitualMaxTime();
                case DATA_CURRENT_ACTION -> core.getCurrentActionId();
                case DATA_ACTION_INDEX -> core.getCurrentActionIndex();
                case DATA_ACTION_COUNT -> core.getActionCount();
                case DATA_INPUT_COOLDOWN -> core.getRhythmInputCooldown();
                case DATA_GOOD_HITS -> core.getGoodHits();
                case DATA_MISTAKES -> core.getMistakes();
                case DATA_FOCUS_INDEX -> core.getFocusIndex();
                case DATA_QUALITY_PREVIEW -> core.getCurrentQualityPreview().id();
                case DATA_STREAK -> core.getStreak();
                case DATA_LAST_JUDGEMENT -> core.getLastJudgement();
                case DATA_FEEDBACK_TICKS -> core.getFeedbackTicks();
                case DATA_LAST_ACCURACY -> Math.round(core.getLastAccuracy() * FLOAT_SCALE);
                case DATA_TARGET_PHASE -> Math.round(core.getCurrentTargetPhase() * FLOAT_SCALE);
                case DATA_CURRENT_TIMING_WINDOW -> Math.round(core.getCurrentTimingWindow() * FLOAT_SCALE);
                case DATA_ACTION_TICK -> core.getActionTick();
                case DATA_ACTION_TIMEOUT -> core.getActionTimeout();
                case DATA_MAX_MISTAKES -> core.getMaxMistakes();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    }
}
