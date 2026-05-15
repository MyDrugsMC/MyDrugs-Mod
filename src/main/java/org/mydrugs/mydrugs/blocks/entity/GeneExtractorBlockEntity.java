package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.AdnGeneData;
import org.mydrugs.mydrugs.items.data.AdnScrapData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.menu.GeneExtractorMenu;

import java.util.ArrayList;
import java.util.List;

public final class GeneExtractorBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int INPUT_SLOT = 0;
    public static final int OUTPUT_SLOT_A = 1;
    public static final int OUTPUT_SLOT_B = 2;
    public static final int OUTPUT_SLOT_C = 3;
    public static final int SLOT_COUNT = 4;
    public static final int OPERATION_TICKS = 300;
    public static final int ENERGY_PER_TICK = 10;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = OPERATION_TICKS;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return GeneExtractorMenu.DATA_COUNT;
        }
    };

    public GeneExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GENE_EXTRACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeneExtractorBlockEntity be) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        boolean changed = false;
        be.maxProgress = OPERATION_TICKS;

        AdnScrapData scrapData = be.getInputScrapData();
        if (scrapData == null || scrapData.stats().isEmpty()) {
            changed |= be.setMachineStatus(MachineStatus.MISSING_INPUT_ITEM);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        if (!be.canAcceptOutputSlots(Math.min(3, scrapData.stats().size()))) {
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        if (!MachineEnergyAttachments.hasEnergyStorage(be)
                || MachineEnergyAttachments.get(be).storage().extract(ENERGY_PER_TICK, true) < ENERGY_PER_TICK) {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
            if (changed) {
                be.sync();
            }
            return;
        }

        MachineEnergyAttachments.get(be).storage().extract(ENERGY_PER_TICK, false);
        be.progress++;
        changed = true;
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        if (be.progress >= be.maxProgress) {
            List<ItemStack> outputs = be.createRandomOutputs(scrapData);
            if (outputs.isEmpty() || !be.canAcceptOutputSlots(outputs.size())) {
                changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
                changed |= be.resetProgress();
                be.sync();
                return;
            }
            be.finishExtraction(outputs);
            be.progress = 0;
            changed = true;
            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(be);
        }

        if (changed) {
            be.sync();
        }
    }

    private AdnScrapData getInputScrapData() {
        ItemStack input = this.getItem(INPUT_SLOT);
        if (!input.is(ModItems.ADN_SCRAP.get())) {
            return null;
        }
        return input.get(ModDataComponents.ADN_SCRAP_DATA.get());
    }

    private List<ItemStack> createRandomOutputs(AdnScrapData scrapData) {
        List<MutationStatValue> selected = selectRandomStats(scrapData, this.level == null ? RandomSource.create() : this.level.random);
        if (selected.isEmpty()) {
            return List.of();
        }

        List<ItemStack> outputs = new ArrayList<>(3);
        for (MutationStatValue stat : selected) {
            ItemStack output = new ItemStack(ModItems.ADN_GENE.get());
            output.set(ModDataComponents.ADN_GENE_DATA.get(), AdnGeneData.singleStatFromScrap(scrapData, stat));
            outputs.add(output);
        }
        return outputs;
    }

    private static List<MutationStatValue> selectRandomStats(AdnScrapData scrapData, RandomSource random) {
        List<MutationStatValue> available = new ArrayList<>(scrapData.stats());
        List<MutationStatValue> selected = new ArrayList<>(3);

        int outputCount = Math.min(3, available.size());
        while (selected.size() < outputCount) {
            selected.add(available.remove(random.nextInt(available.size())));
        }
        return selected;
    }

    private boolean canAcceptOutputSlots(int outputCount) {
        for (int i = 0; i < outputCount; i++) {
            ItemStack current = this.getItem(OUTPUT_SLOT_A + i);
            if (!current.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void finishExtraction(List<ItemStack> outputs) {
        this.getItem(INPUT_SLOT).shrink(1);
        if (this.getItem(INPUT_SLOT).isEmpty()) {
            this.setItem(INPUT_SLOT, ItemStack.EMPTY);
        }
        for (int i = 0; i < outputs.size(); i++) {
            this.setItem(OUTPUT_SLOT_A + i, outputs.get(i));
        }
    }

    private boolean resetProgress() {
        if (this.progress == 0) {
            return false;
        }
        this.progress = 0;
        return true;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.gene_extractor");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new GeneExtractorMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        return true;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == INPUT_SLOT && stack.is(ModItems.ADN_SCRAP.get());
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == INPUT_SLOT) {
            this.progress = 0;
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = Mth.clamp(input.getIntOr("Progress", 0), 0, OPERATION_TICKS);
        this.maxProgress = input.getIntOr("MaxProgress", OPERATION_TICKS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
    }
}
