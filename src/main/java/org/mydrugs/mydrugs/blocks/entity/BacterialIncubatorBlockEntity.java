package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
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
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.AdnGeneData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationPayloadData;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.menu.BacterialIncubatorMenu;

public final class BacterialIncubatorBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int GENE_SLOT = 0;
    public static final int NUTRIENT_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int OPERATION_TICKS = 600;
    private static final int BASE_ENERGY_PER_TICK = 10;
    private static final int ENERGY_PER_STAT = 4;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = OPERATION_TICKS;
    private int energyPerTick = BASE_ENERGY_PER_TICK;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> energyPerTick;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> energyPerTick = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return BacterialIncubatorMenu.DATA_COUNT;
        }
    };

    public BacterialIncubatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BACTERIAL_INCUBATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BacterialIncubatorBlockEntity be) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        boolean changed = false;
        be.maxProgress = OPERATION_TICKS;

        AdnGeneData gene = be.getInputGeneData();
        if (!isValidGene(gene) || !be.getItem(NUTRIENT_SLOT).is(ModItems.NUTRIENT_GEL.get())) {
            changed |= be.setMachineStatus(MachineStatus.MISSING_INPUT_ITEM);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        be.energyPerTick = energyPerTick(gene);
        MutationPayloadData payload = MutationPayloadData.fromGene(gene);
        ItemStack output = createVector(payload);
        if (!be.canAcceptOutput(output)) {
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        if (!MachineEnergyAttachments.hasEnergyStorage(be)
                || MachineEnergyAttachments.get(be).storage().extract(be.energyPerTick, true) < be.energyPerTick) {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
            if (changed) {
                be.sync();
            }
            return;
        }

        MachineEnergyAttachments.get(be).storage().extract(be.energyPerTick, false);
        be.progress++;
        changed = true;
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        if (be.progress >= be.maxProgress) {
            gene = be.getInputGeneData();
            if (!isValidGene(gene) || !be.getItem(NUTRIENT_SLOT).is(ModItems.NUTRIENT_GEL.get())) {
                changed |= be.setMachineStatus(MachineStatus.MISSING_INPUT_ITEM);
                changed |= be.resetProgress();
                be.sync();
                return;
            }

            output = createVector(MutationPayloadData.fromGene(gene));
            if (!be.canAcceptOutput(output)) {
                changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
                changed |= be.resetProgress();
                be.sync();
                return;
            }

            be.finishIncubation(output);
            be.progress = 0;
            changed = true;
            AdvancementEventHooks.machineRecipeCompleted(be);
        }

        if (changed) {
            be.sync();
        }
    }

    private static boolean isValidGene(AdnGeneData gene) {
        return gene != null && !gene.broken() && !gene.stats().isEmpty();
    }

    public static int energyPerTick(AdnGeneData gene) {
        return BASE_ENERGY_PER_TICK + gene.stats().size() * ENERGY_PER_STAT;
    }

    private AdnGeneData getInputGeneData() {
        ItemStack input = this.getItem(GENE_SLOT);
        if (!input.is(ModItems.ADN_GENE.get())) {
            return null;
        }
        return input.get(ModDataComponents.ADN_GENE_DATA.get());
    }

    private static ItemStack createVector(MutationPayloadData payload) {
        ItemStack output = new ItemStack(ModItems.MUTATION_VECTOR.get());
        output.set(ModDataComponents.MUTATION_PAYLOAD.get(), payload);
        return output;
    }

    private boolean canAcceptOutput(ItemStack output) {
        ItemStack current = this.getItem(OUTPUT_SLOT);
        return current.isEmpty() || ItemStack.isSameItemSameComponents(current, output) && current.getCount() < current.getMaxStackSize();
    }

    private void finishIncubation(ItemStack output) {
        this.getItem(GENE_SLOT).shrink(1);
        this.getItem(NUTRIENT_SLOT).shrink(1);
        ItemStack current = this.getItem(OUTPUT_SLOT);
        if (current.isEmpty()) {
            this.setItem(OUTPUT_SLOT, output);
        } else {
            current.grow(1);
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
        return Component.translatable("container.mydrugs.bacterial_incubator");
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
        return new BacterialIncubatorMenu(
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
        return switch (slot) {
            case GENE_SLOT -> stack.is(ModItems.ADN_GENE.get()) && isValidGene(stack.get(ModDataComponents.ADN_GENE_DATA.get()));
            case NUTRIENT_SLOT -> stack.is(ModItems.NUTRIENT_GEL.get());
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == GENE_SLOT || slot == NUTRIENT_SLOT) {
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
        this.energyPerTick = input.getIntOr("EnergyPerTick", BASE_ENERGY_PER_TICK);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("EnergyPerTick", this.energyPerTick);
    }
}
