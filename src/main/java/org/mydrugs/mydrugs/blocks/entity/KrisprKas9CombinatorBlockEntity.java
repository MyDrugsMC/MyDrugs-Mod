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
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.data.AdnGeneData;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.MutationStatValue;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.menu.KrisprKas9CombinatorMenu;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.CRC32;

public final class KrisprKas9CombinatorBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int OPERATION_TICKS = 600;
    public static final int BASE_ENERGY_PER_TICK = 20;
    public static final int EXTRA_ENERGY_PER_STAT = 8;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = OPERATION_TICKS;
    private int sameSourceBlocked;
    private int stabilityPercent;
    private int energyPerTick = BASE_ENERGY_PER_TICK;
    private String activeInputKey = "";
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> sameSourceBlocked;
                case 3 -> stabilityPercent;
                case 4 -> energyPerTick;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> sameSourceBlocked = value;
                case 3 -> stabilityPercent = value;
                case 4 -> energyPerTick = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return KrisprKas9CombinatorMenu.DATA_COUNT;
        }
    };

    public KrisprKas9CombinatorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CRISPR_CAS9_COMBINATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, KrisprKas9CombinatorBlockEntity be) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        boolean changed = false;
        be.maxProgress = OPERATION_TICKS;

        AdnGeneData first = be.getGene(INPUT_A_SLOT);
        AdnGeneData second = be.getGene(INPUT_B_SLOT);
        if (!be.isUsableInput(first) || !be.isUsableInput(second)) {
            changed |= be.setSameSourceBlocked(false);
            changed |= be.setMachineStatus(MachineStatus.MISSING_INPUT_ITEM);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        CombinationPlan plan = CombinationPlan.create(first, second);
        be.energyPerTick = plan.energyPerTick();
        be.stabilityPercent = Math.round((1.0F - plan.failureChance()) * 100.0F);

        if (plan.sameSourceBlocked()) {
            changed |= be.setSameSourceBlocked(true);
            changed |= be.setMachineStatus(MachineStatus.PAUSED);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        changed |= be.setSameSourceBlocked(false);

        if (!be.canAcceptOutput()) {
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        String inputKey = plan.inputKey();
        if (be.progress > 0 && !inputKey.equals(be.activeInputKey)) {
            be.activeInputKey = inputKey;
            changed |= be.resetProgress();
        } else if (be.progress == 0) {
            be.activeInputKey = inputKey;
        }

        if (!MachineEnergyAttachments.hasEnergyStorage(be)
                || MachineEnergyAttachments.get(be).storage().extract(plan.energyPerTick(), true) < plan.energyPerTick()) {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
            if (changed) {
                be.sync();
            }
            return;
        }

        MachineEnergyAttachments.get(be).storage().extract(plan.energyPerTick(), false);
        be.progress++;
        changed = true;
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        if (be.progress >= be.maxProgress) {
            ItemStack output = level.random.nextFloat() < plan.failureChance()
                    ? plan.brokenOutput()
                    : plan.successOutput();

            if (!be.canAcceptOutput()) {
                changed |= be.setMachineStatus(MachineStatus.OUTPUT_SLOT_FULL);
                changed |= be.resetProgress();
                be.sync();
                return;
            }

            be.finishCombination(output);
            be.progress = 0;
            be.activeInputKey = "";
            changed = true;
            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(be);
        }

        if (changed) {
            be.sync();
        }
    }

    private AdnGeneData getGene(int slot) {
        ItemStack stack = this.getItem(slot);
        if (!stack.is(ModItems.ADN_GENE.get())) {
            return null;
        }
        return stack.get(ModDataComponents.ADN_GENE_DATA.get());
    }

    private boolean isUsableInput(AdnGeneData data) {
        return data != null && !data.broken() && !data.stats().isEmpty();
    }

    private boolean canAcceptOutput() {
        return this.getItem(OUTPUT_SLOT).isEmpty();
    }

    private void finishCombination(ItemStack output) {
        this.getItem(INPUT_A_SLOT).shrink(1);
        this.getItem(INPUT_B_SLOT).shrink(1);
        if (this.getItem(INPUT_A_SLOT).isEmpty()) {
            this.setItem(INPUT_A_SLOT, ItemStack.EMPTY);
        }
        if (this.getItem(INPUT_B_SLOT).isEmpty()) {
            this.setItem(INPUT_B_SLOT, ItemStack.EMPTY);
        }
        this.setItem(OUTPUT_SLOT, output);
    }

    private boolean resetProgress() {
        if (this.progress == 0) {
            return false;
        }
        this.progress = 0;
        this.activeInputKey = "";
        return true;
    }

    private boolean setSameSourceBlocked(boolean blocked) {
        int value = blocked ? 1 : 0;
        if (this.sameSourceBlocked == value) {
            return false;
        }
        this.sameSourceBlocked = value;
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
        return Component.translatable("container.mydrugs.crispr_cas9_combinator");
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
        return new KrisprKas9CombinatorMenu(
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
        return (slot == INPUT_A_SLOT || slot == INPUT_B_SLOT)
                && stack.is(ModItems.ADN_GENE.get())
                && stack.get(ModDataComponents.ADN_GENE_DATA.get()) != null;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == INPUT_A_SLOT || slot == INPUT_B_SLOT) {
            this.progress = 0;
            this.activeInputKey = "";
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = Mth.clamp(input.getIntOr("Progress", 0), 0, OPERATION_TICKS);
        this.maxProgress = input.getIntOr("MaxProgress", OPERATION_TICKS);
        this.sameSourceBlocked = input.getIntOr("SameSourceBlocked", 0);
        this.stabilityPercent = input.getIntOr("StabilityPercent", 0);
        this.energyPerTick = input.getIntOr("EnergyPerTick", BASE_ENERGY_PER_TICK);
        this.activeInputKey = input.getStringOr("ActiveInputKey", "");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("SameSourceBlocked", this.sameSourceBlocked);
        output.putInt("StabilityPercent", this.stabilityPercent);
        output.putInt("EnergyPerTick", this.energyPerTick);
        output.putString("ActiveInputKey", this.activeInputKey);
    }

    private record CombinationPlan(
            boolean sameSourceBlocked,
            List<String> sourceUuids,
            List<String> sourceEntityTypes,
            List<String> sourceNames,
            List<MutationStatValue> stats,
            float failureChance,
            int energyPerTick,
            String signature,
            String inputKey
    ) {
        static CombinationPlan create(AdnGeneData first, AdnGeneData second) {
            boolean blocked = hasSourceOverlap(first.sourceUuids(), second.sourceUuids());
            SourceBundle sources = SourceBundle.merge(first, second);
            List<MutationStatValue> mergedStats = mergeStats(first.stats(), second.stats());
            int uniqueStatCount = mergedStats.size();
            float failureChance = Mth.clamp(0.15F + Math.max(0, uniqueStatCount - 2) * 0.07F, 0.05F, 0.65F);
            int energyPerTick = BASE_ENERGY_PER_TICK + EXTRA_ENERGY_PER_STAT * Math.max(0, uniqueStatCount - 2);
            String signature = combinedSignature(sources.sourceUuids(), mergedStats);
            String inputKey = first.geneticSignature() + "|" + second.geneticSignature() + "|" + signature;
            return new CombinationPlan(
                    blocked,
                    sources.sourceUuids(),
                    sources.sourceEntityTypes(),
                    sources.sourceNames(),
                    mergedStats,
                    failureChance,
                    energyPerTick,
                    signature,
                    inputKey
            );
        }

        ItemStack successOutput() {
            ItemStack stack = new ItemStack(ModItems.ADN_GENE.get());
            stack.set(ModDataComponents.ADN_GENE_DATA.get(), new AdnGeneData(
                    this.sourceUuids,
                    this.sourceEntityTypes,
                    this.sourceNames,
                    this.signature,
                    false,
                    this.stats
            ));
            return stack;
        }

        ItemStack brokenOutput() {
            ItemStack stack = new ItemStack(ModItems.ADN_GENE.get());
            stack.set(ModDataComponents.ADN_GENE_DATA.get(), new AdnGeneData(
                    this.sourceUuids,
                    this.sourceEntityTypes,
                    this.sourceNames,
                    this.signature,
                    true,
                    List.of()
            ));
            return stack;
        }

        private static boolean hasSourceOverlap(List<String> first, List<String> second) {
            Set<String> seen = new LinkedHashSet<>(first);
            for (String uuid : second) {
                if (seen.contains(uuid)) {
                    return true;
                }
            }
            return false;
        }

        private static List<MutationStatValue> mergeStats(List<MutationStatValue> first, List<MutationStatValue> second) {
            Map<String, MutationStatValue> merged = new LinkedHashMap<>();
            for (MutationStatValue stat : first) {
                merged.put(stat.statId(), stat);
            }
            for (MutationStatValue stat : second) {
                MutationStatValue existing = merged.get(stat.statId());
                if (existing == null) {
                    merged.put(stat.statId(), stat);
                    continue;
                }
                float mergedValue = Mth.clamp(existing.value() + stat.value(), 0.01F, 1.0F);
                float mergedImprobability = Mth.clamp(Math.max(existing.improbabilityScore(), stat.improbabilityScore()), 0.0F, 1.0F);
                merged.put(stat.statId(), new MutationStatValue(stat.statId(), mergedValue, mergedImprobability));
            }
            return List.copyOf(merged.values());
        }

        private static String combinedSignature(List<String> sourceUuids, List<MutationStatValue> stats) {
            CRC32 crc = new CRC32();
            for (String uuid : sourceUuids) {
                crc.update(uuid.getBytes(StandardCharsets.UTF_8));
                crc.update(0);
            }
            for (MutationStatValue stat : stats) {
                crc.update(stat.statId().getBytes(StandardCharsets.UTF_8));
                crc.update(Integer.toString(Math.round(stat.value() * 1000.0F)).getBytes(StandardCharsets.UTF_8));
                crc.update(0);
            }
            return Long.toHexString(crc.getValue()).toUpperCase(java.util.Locale.ROOT);
        }
    }

    private record SourceBundle(List<String> sourceUuids, List<String> sourceEntityTypes, List<String> sourceNames) {
        static SourceBundle merge(AdnGeneData first, AdnGeneData second) {
            LinkedHashMap<String, SourceEntry> entries = new LinkedHashMap<>();
            add(entries, first);
            add(entries, second);

            List<String> uuids = new ArrayList<>();
            List<String> types = new ArrayList<>();
            List<String> names = new ArrayList<>();
            for (Map.Entry<String, SourceEntry> entry : entries.entrySet()) {
                uuids.add(entry.getKey());
                types.add(entry.getValue().entityType());
                names.add(entry.getValue().name());
            }
            return new SourceBundle(uuids, types, names);
        }

        private static void add(LinkedHashMap<String, SourceEntry> entries, AdnGeneData data) {
            for (int i = 0; i < data.sourceUuids().size(); i++) {
                String uuid = data.sourceUuids().get(i);
                String type = i < data.sourceEntityTypes().size() ? data.sourceEntityTypes().get(i) : "";
                String name = i < data.sourceNames().size() ? data.sourceNames().get(i) : "";
                entries.putIfAbsent(uuid, new SourceEntry(type, name));
            }
        }
    }

    private record SourceEntry(String entityType, String name) {
    }
}
