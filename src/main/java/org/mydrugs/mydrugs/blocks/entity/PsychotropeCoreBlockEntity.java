package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.PsychotropeMultiblock;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.use.DrugUseSource;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.energy.PsychotropeEnergyStorage;
import org.mydrugs.mydrugs.fluids.FluidTypesEx;
import org.mydrugs.mydrugs.items.drugs.DrugItem;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.menu.PsychotropeGeneratorMenu;

import java.util.ArrayList;
import java.util.List;

public final class PsychotropeCoreBlockEntity extends BlockEntity implements MenuProvider, MachineStatusProvider {
    public static final int MAX_RADIUS = 8;
    private static final int OPERATION_TICKS = 10;
    private static final int TARGET_RESCAN_INTERVAL = 100;

    private final PsychotropeEnergyStorage energy = new PsychotropeEnergyStorage(100_000);
    private final List<BlockPos> cachedEnergyTargets = new ArrayList<>();
    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> OPERATION_TICKS;
                case 2 -> energy.stored();
                case 3 -> energy.capacity();
                case 4 -> powerRadius;
                case 5 -> formed ? 1 : 0;
                case 6 -> activeDrug == null ? 0 : activeDrug.networkId();
                case 7 -> machineStatus.networkId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 4 -> powerRadius = Mth.clamp(value, 1, MAX_RADIUS);
                case 5 -> formed = value != 0;
                case 6 -> activeDrug = DrugId.byNetworkId(value);
                case 7 -> machineStatus = MachineStatus.byNetworkId(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return PsychotropeGeneratorMenu.DATA_COUNT;
        }
    };

    private boolean formed;
    private boolean structureDirty = true;
    private int progress;
    private int powerRadius = 1;
    private int targetRescanCooldown;
    private int exposureTicks;
    private @Nullable DrugId activeDrug;
    private ConsumptionStrategy activeStrategy = new EatingStrategy();
    private MachineStatus machineStatus = MachineStatus.IDLE;

    public PsychotropeCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PSYCHOTROPE_CORE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PsychotropeCoreBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        if (be.structureDirty) {
            be.refreshStructure(serverLevel);
        }

        if (!be.formed) {
            if (be.progress != 0 || be.activeDrug != null) {
                be.progress = 0;
                be.activeDrug = null;
            }
            be.setMachineStatus(MachineStatus.INVALID_MULTIBLOCK);
            return;
        }

        if (be.targetRescanCooldown-- <= 0) {
            be.rebuildEnergyTargets(serverLevel);
            be.targetRescanCooldown = TARGET_RESCAN_INTERVAL;
        }

        DrugSource source = be.findDrugSource();
        if (source == null) {
            be.distributeStoredEnergy(serverLevel);
            if (be.progress != 0 || be.activeDrug != null) {
                be.progress = 0;
                be.activeDrug = null;
            }
            be.setMachineStatus(MachineStatus.MISSING_INPUT_ITEM);
            return;
        }

        int generated = be.effectiveEnergy(source.model().getId());
        if (generated <= 0 || be.totalReceivable(serverLevel, true) < generated) {
            if (generated <= 0) {
                be.distributeStoredEnergy(serverLevel);
            }
            be.setMachineStatus(generated <= 0 ? MachineStatus.NO_MATCHING_RECIPE : MachineStatus.OUTPUT_TANK_FULL);
            return;
        }

        be.setMachineStatus(MachineStatus.RUNNING);
        be.activeDrug = source.model().getId();
        be.activeStrategy = source.strategy();
        be.progress++;
        be.handlePlayerExposure(serverLevel);

        boolean generatedThisTick = false;
        if (be.progress >= OPERATION_TICKS) {
            if (source.consume()) {
                be.distributeEnergy(serverLevel, generated);
                generatedThisTick = true;
                AdvancementEventHooks.psychotropeEvent(
                        serverLevel,
                        pos,
                        "energy_generated",
                        source.model().getId().serializedName(),
                        generated,
                        be.energy.stored()
                );
            }
            be.progress = 0;
        }

        if (!generatedThisTick) {
            be.distributeStoredEnergy(serverLevel);
        }

        be.sync();
    }

    public boolean isFormed() {
        return this.formed;
    }

    public void markStructureDirty() {
        this.structureDirty = true;
        this.targetRescanCooldown = 0;
    }

    public void setPowerRadius(int radius) {
        int clamped = Mth.clamp(radius, 1, MAX_RADIUS);
        if (this.powerRadius != clamped) {
            this.powerRadius = clamped;
            this.targetRescanCooldown = 0;
            sync();
        }
    }

    public PsychotropeEnergyStorage energy() {
        return this.energy;
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    private void setMachineStatus(MachineStatus status) {
        if (this.machineStatus != status) {
            this.machineStatus = status;
            sync();
        }
    }

    private void refreshStructure(ServerLevel level) {
        boolean wasFormed = this.formed;
        this.formed = PsychotropeMultiblock.validate(level, this.worldPosition);
        this.structureDirty = false;
        if (!wasFormed && this.formed) {
            emitFormationParticles(level);
            AdvancementEventHooks.psychotropeEvent(level, this.worldPosition, "multiblock_formed", "", 0, 0);
        }
        if (wasFormed != this.formed) {
            sync();
        }
    }

    private void emitFormationParticles(ServerLevel level) {
        for (int i = 0; i < 80; i++) {
            float hue = i / 80.0F;
            int rgb = java.awt.Color.HSBtoRGB(hue, 0.9F, 1.0F);
            double angle = i * Math.PI * 2.0D / 80.0D;
            double x = this.worldPosition.getX() + 0.5D + Math.cos(angle) * 2.8D;
            double y = this.worldPosition.getY() + 0.5D + level.random.nextDouble() * 4.0D - 2.0D;
            double z = this.worldPosition.getZ() + 0.5D + Math.sin(angle) * 2.8D;
            level.sendParticles(new DustParticleOptions(rgb & 0xFFFFFF, 1.2F), x, y, z, 1, 0.0D, 0.03D, 0.0D, 0.0D);
        }
    }

    private @Nullable DrugSource findDrugSource() {
        if (this.level == null) {
            return null;
        }

        DrugSource best = null;
        for (BlockPos offset : PsychotropeMultiblock.componentOffsets()) {
            if (!(this.level.getBlockEntity(this.worldPosition.offset(offset)) instanceof PsychotropeComponentBlockEntity component)) {
                continue;
            }

            ItemStack stack = component.getStoredItem();
            if (stack.getItem() instanceof DrugItem drugItem) {
                List<DrugModel> models = drugItem.getDrugModels(stack);
                if (!models.isEmpty() && DrugRegistry.getPsychotropeValue(models.get(0).getId()) > 0) {
                    best = chooseNewer(best, new DrugSource(models.get(0), drugItem.getConsumptionStrategy(), component.lastInputChangedTick(), () -> {
                        ItemStack stored = component.getStoredItem();
                        if (stored.isEmpty()) {
                            return false;
                        }
                        stored.shrink(1);
                        component.setStoredItem(stored);
                        return true;
                    }));
                }
            }

            FluidStack fluid = component.fluidTank().getFluid();
            DrugModel fluidDrug = fluid.isEmpty() ? null : FluidTypesEx.getDrugModel(fluid.getFluid());
            if (fluidDrug != null && DrugRegistry.getPsychotropeValue(fluidDrug.getId()) > 0 && fluid.getAmount() >= 10) {
                best = chooseNewer(best, new DrugSource(fluidDrug, new EatingStrategy(), component.lastInputChangedTick(), () -> {
                    if (component.fluidTank().getAmount() < 10) {
                        return false;
                    }
                    component.fluidTank().extract(10, false);
                    return true;
                }));
            }
        }
        return best;
    }

    private static DrugSource chooseNewer(@Nullable DrugSource current, DrugSource candidate) {
        return current == null || candidate.lastInputChangedTick() > current.lastInputChangedTick() ? candidate : current;
    }

    private int effectiveEnergy(DrugId drugId) {
        int base = DrugRegistry.getPsychotropeValue(drugId) * 100;
        return base * (9 - this.powerRadius) / 8;
    }

    private void rebuildEnergyTargets(ServerLevel level) {
        this.cachedEnergyTargets.clear();
        int r = this.powerRadius;
        BlockPos.betweenClosedStream(this.worldPosition.offset(-r, -r, -r), this.worldPosition.offset(r, r, r))
                .filter(pos -> !pos.equals(this.worldPosition))
                .forEach(pos -> {
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be != null && MachineEnergyAttachments.hasEnergyStorage(be)) {
                        this.cachedEnergyTargets.add(be.getBlockPos().immutable());
                    }
                });
    }

    private int totalReceivable(ServerLevel level, boolean includeInternalBattery) {
        int total = includeInternalBattery ? this.energy.capacity() - this.energy.stored() : 0;
        for (BlockPos pos : this.cachedEnergyTargets) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null && MachineEnergyAttachments.hasEnergyStorage(be)) {
                total += MachineEnergyAttachments.get(be).storage().receive(Integer.MAX_VALUE, true);
            }
        }
        return total;
    }

    private void distributeStoredEnergy(ServerLevel level) {
        if (this.energy.stored() <= 0 || this.cachedEnergyTargets.isEmpty()) {
            return;
        }

        int receivable = totalReceivable(level, false);
        if (receivable <= 0) {
            return;
        }

        int amount = Math.min(this.energy.stored(), receivable);
        int extracted = this.energy.extract(amount, false);
        int remainder = distributeToExternalTargets(level, extracted);
        if (remainder > 0) {
            this.energy.receive(remainder, false);
        }
        if (remainder != extracted) {
            sync();
        }
    }

    private void distributeEnergy(ServerLevel level, int amount) {
        int remaining = distributeToExternalTargets(level, amount);
        if (remaining > 0) {
            this.energy.receive(remaining, false);
        }
    }

    private int distributeToExternalTargets(ServerLevel level, int amount) {
        int remaining = amount;
        List<EnergyTarget> targets = new ArrayList<>();
        for (BlockPos pos : this.cachedEnergyTargets) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be != null && MachineEnergyAttachments.hasEnergyStorage(be)) {
                PsychotropeEnergyStorage storage = MachineEnergyAttachments.get(be).storage();
                if (storage.receive(1, true) > 0) {
                    targets.add(new EnergyTarget(be, storage));
                }
            }
        }

        while (remaining > 0 && !targets.isEmpty()) {
            int share = Math.max(1, remaining / targets.size());
            int movedThisPass = 0;
            for (int i = targets.size() - 1; i >= 0 && remaining > 0; i--) {
                EnergyTarget target = targets.get(i);
                int moved = target.storage().receive(Math.min(share, remaining), false);
                remaining -= moved;
                movedThisPass += moved;
                if (moved > 0) {
                    syncEnergyTarget(target.blockEntity());
                }
                if (target.storage().receive(1, true) <= 0) {
                    targets.remove(i);
                }
            }
            if (movedThisPass == 0) {
                break;
            }
        }

        return remaining;
    }

    private void handlePlayerExposure(ServerLevel level) {
        if (this.activeDrug == null) {
            this.exposureTicks = 0;
            return;
        }

        this.exposureTicks++;
        if (this.exposureTicks < 100) {
            return;
        }
        this.exposureTicks = 0;

        DrugModel model = DrugRegistry.getDrug(this.activeDrug);
        if (model == null) {
            return;
        }

        AABB area = new AABB(this.worldPosition).inflate(this.powerRadius);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, area)) {
            MyDrugs.DRUG_USE_SERVICE.consume(player, model, this.activeStrategy, DrugUseSource.PSYCHOTROPE);
        }
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.mydrugs.psychotrope_generator");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new PsychotropeGeneratorMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition),
                this.worldPosition
        );
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.energy.deserialize(input.childOrEmpty("energy"));
        this.progress = input.getIntOr("progress", 0);
        this.powerRadius = Mth.clamp(input.getIntOr("power_radius", 1), 1, MAX_RADIUS);
        String drug = input.getStringOr("active_drug", "");
        this.activeDrug = drug.isBlank() ? null : DrugId.bySerializedNameOrNull(drug);
        this.structureDirty = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.energy.serialize(output.child("energy"));
        output.putInt("progress", this.progress);
        output.putInt("power_radius", this.powerRadius);
        output.putString("active_drug", this.activeDrug == null ? "" : this.activeDrug.serializedName());
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }

    private record DrugSource(DrugModel model, ConsumptionStrategy strategy, long lastInputChangedTick, SourceConsumer consumer) {
        boolean consume() {
            return this.consumer.consume();
        }
    }

    private static void syncEnergyTarget(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null && !blockEntity.getLevel().isClientSide()) {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private record EnergyTarget(BlockEntity blockEntity, PsychotropeEnergyStorage storage) {
    }

    private interface SourceConsumer {
        boolean consume();
    }
}
