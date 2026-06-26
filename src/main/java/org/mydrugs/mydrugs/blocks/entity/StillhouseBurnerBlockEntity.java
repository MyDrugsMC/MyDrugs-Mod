package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.energy.PsyCurrentDistributor;
import org.mydrugs.mydrugs.energy.PsyCurrentStorage;
import org.mydrugs.mydrugs.energy.psycurrent.PsyCurrentTargetScan;
import org.mydrugs.mydrugs.energy.stillhouse.StillhouseBurnerFuel;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.menu.StillhouseBurnerMenu;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StillhouseBurnerBlockEntity extends BlockEntity implements Container, net.minecraft.world.MenuProvider, MachineStatusProvider {
    public static final int SLOT_FUEL_CONTAINER = 0;
    public static final int SLOT_COUNT = 1;
    public static final int FUEL_CAPACITY_MB = 8000;
    public static final int CURRENT_CAPACITY = 6000;
    public static final int MAX_OUTPUT_PER_TICK = 6;
    public static final int DISTRIBUTION_RADIUS = 4;
    private static final int TARGET_RESCAN_INTERVAL_TICKS = 30;

    private final StoredFluidTank fuelTank = new StoredFluidTank(FUEL_CAPACITY_MB, this::sync, StillhouseBurnerBlockEntity::isFuelStack);
    private final PsyCurrentStorage current = new PsyCurrentStorage(CURRENT_CAPACITY);
    private final FuelTankHandler fuelHandler = new FuelTankHandler();
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int burnEnergyReserve;
    private int generationRate;
    private int targetRescanCooldown;
    private PsyCurrentTargetScan cachedScan = PsyCurrentTargetScan.EMPTY;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> fuelTank.getAmount();
                case 1 -> fuelTank.encodeFluidSyncId();
                case 2 -> current.stored();
                case 3 -> current.capacity();
                case 4 -> generationRate;
                case 5 -> cachedScan.validCount();
                case 6 -> currentFuel().map(StillhouseBurnerFuel::currentPerMb).orElse(0);
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 2 -> current.receive(Math.max(0, value - current.stored()), false);
                case 4 -> generationRate = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return StillhouseBurnerMenu.DATA_COUNT;
        }
    };

    public StillhouseBurnerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STILLHOUSE_BURNER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, StillhouseBurnerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        boolean changed = false;
        changed |= be.tryDrainFuelContainer();
        if (be.targetRescanCooldown-- <= 0) {
            be.cachedScan = be.scanStillhouseTargets(serverLevel);
            be.targetRescanCooldown = TARGET_RESCAN_INTERVAL_TICKS;
            changed = true;
        }

        int receivable = be.totalReceivable(serverLevel);
        if (receivable <= 0) {
            be.generationRate = 0;
            changed |= be.setMachineStatus(be.cachedScan.valid().isEmpty() ? MachineStatus.NO_MATCHING_RECIPE : MachineStatus.OUTPUT_TANK_FULL);
            changed |= PsyCurrentDistributor.distributeStored(serverLevel, be.current, be.cachedScan.valid());
            if (changed) {
                be.sync();
            }
            return;
        }

        Optional<StillhouseBurnerFuel> fuel = be.currentFuel();
        if (fuel.isEmpty() && be.burnEnergyReserve <= 0) {
            be.generationRate = 0;
            changed |= be.setMachineStatus(MachineStatus.MISSING_INPUT_FLUID);
            if (changed) {
                be.sync();
            }
            return;
        }

        while (be.burnEnergyReserve < MAX_OUTPUT_PER_TICK && fuel.isPresent() && be.fuelTank.getAmount() > 0) {
            be.fuelTank.extract(1, false);
            be.burnEnergyReserve += fuel.get().currentPerMb();
            changed = true;
            fuel = be.currentFuel();
        }

        int generated = Math.min(Math.min(MAX_OUTPUT_PER_TICK, be.burnEnergyReserve), be.totalReceivable(serverLevel));
        if (generated <= 0) {
            be.generationRate = 0;
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_TANK_FULL);
        } else {
            int remainder = PsyCurrentDistributor.distribute(serverLevel, generated, be.current, be.cachedScan.valid());
            int accepted = generated - remainder;
            be.burnEnergyReserve -= accepted;
            be.generationRate = accepted;
            changed = true;
            changed |= be.setMachineStatus(accepted > 0 ? MachineStatus.RUNNING : MachineStatus.OUTPUT_TANK_FULL);
            org.mydrugs.mydrugs.advancement.AdvancementEventHooks.psychotropeEvent(serverLevel, pos, "psy_current_generated", "stillhouse_burner", accepted, be.current.stored());
        }

        if (changed) {
            be.sync();
        }
    }

    private PsyCurrentTargetScan scanStillhouseTargets(ServerLevel level) {
        List<BlockPos> valid = new ArrayList<>();
        List<BlockPos> full = new ArrayList<>();
        List<BlockPos> incompatible = new ArrayList<>();
        int receivable = 0;
        int r = DISTRIBUTION_RADIUS;
        for (BlockPos targetPos : BlockPos.betweenClosed(this.worldPosition.offset(-r, -r, -r), this.worldPosition.offset(r, r, r))) {
            if (targetPos.equals(this.worldPosition)) {
                continue;
            }
            BlockEntity target = level.getBlockEntity(targetPos);
            if (!isAllowedTarget(target)) {
                if (target != null && MachineEnergyAttachments.hasEnergyStorage(target)) {
                    incompatible.add(targetPos.immutable());
                }
                continue;
            }
            if (!MachineEnergyAttachments.hasEnergyStorage(target)) {
                incompatible.add(targetPos.immutable());
                continue;
            }
            int canReceive = MachineEnergyAttachments.get(target).storage().receive(Integer.MAX_VALUE, true);
            if (canReceive > 0) {
                valid.add(targetPos.immutable());
                receivable = Math.min(Integer.MAX_VALUE, receivable + canReceive);
            } else {
                full.add(targetPos.immutable());
            }
        }
        return new PsyCurrentTargetScan(valid, full, incompatible, receivable);
    }

    private static boolean isAllowedTarget(@Nullable BlockEntity target) {
        return target instanceof MixingVatBlockEntity
                || target instanceof DistillerBlockEntity
                || target instanceof AdvancedMixingVatBlockEntity
                || target instanceof FluidPumpBlockEntity
                || target instanceof StompCrafterBlockEntity;
    }

    private int totalReceivable(ServerLevel level) {
        return PsyCurrentDistributor.totalReceivable(level, this.cachedScan.valid(), this.current);
    }

    private Optional<StillhouseBurnerFuel> currentFuel() {
        ResourceLocation id = getFluidId(this.fuelTank);
        return id == null ? Optional.empty() : StillhouseBurnerFuel.get(id);
    }

    @Nullable
    private static ResourceLocation getFluidId(StoredFluidTank tank) {
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty()) {
            return null;
        }
        Fluid fluid = stored.getFluid();
        return fluid == Fluids.EMPTY ? null : stillhouseFuelId(fluid).orElse(BuiltInRegistries.FLUID.getKey(fluid));
    }

    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        return this.fuelHandler;
    }

    public static boolean isFuelContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (fuelBucketFluidId(stack).isPresent() || fuelBottleFluidId(stack).isPresent()) {
            return true;
        }
        ResourceHandler<FluidResource> handler = ItemAccess.forStack(stack.copyWithCount(1)).getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }
        FluidResource resource = handler.getResource(0);
        if (resource.isEmpty() || handler.getAmountAsInt(0) <= 0) {
            return false;
        }
        return stillhouseFuelId(resource.getFluid()).isPresent();
    }

    private static boolean isFuelStack(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return stillhouseFuelId(stack.getFluid()).isPresent();
    }

    private static Optional<ResourceLocation> fuelBucketFluidId(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (itemId == null || !itemId.getPath().endsWith("_bucket")) {
            return Optional.empty();
        }
        ResourceLocation fluidId = ResourceLocation.fromNamespaceAndPath(
                itemId.getNamespace(),
                itemId.getPath().substring(0, itemId.getPath().length() - "_bucket".length())
        );
        return StillhouseBurnerFuel.isFuel(fluidId) ? Optional.of(fluidId) : Optional.empty();
    }

    private static Optional<ResourceLocation> fuelBottleFluidId(ItemStack stack) {
        ResourceLocation fluidId = GlassBottleItem.getStoredFluidId(stack);
        if (fluidId == null || GlassBottleItem.getStoredAmount(stack) <= 0) {
            return Optional.empty();
        }
        return StillhouseBurnerFuel.isFuel(fluidId) ? Optional.of(fluidId) : Optional.empty();
    }

    private static Optional<ResourceLocation> stillhouseFuelId(Fluid fluid) {
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        if (id == null) {
            return Optional.empty();
        }
        if (StillhouseBurnerFuel.isFuel(id)) {
            return Optional.of(id);
        }
        if (!id.getPath().startsWith("flowing_")) {
            return Optional.empty();
        }
        ResourceLocation fluidId = ResourceLocation.fromNamespaceAndPath(
                id.getNamespace(),
                id.getPath().substring("flowing_".length())
        );
        return StillhouseBurnerFuel.isFuel(fluidId) ? Optional.of(fluidId) : Optional.empty();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.mydrugs.stillhouse_burner");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new StillhouseBurnerMenu(
                containerId,
                playerInventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        this.fuelTank.save(output, "fuel_tank");
        this.current.serialize(output.child("psy_current"));
        output.putInt("burn_energy_reserve", this.burnEnergyReserve);
        output.putInt("generation_rate", this.generationRate);
        output.putInt("machine_status", this.machineStatus.networkId());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.fuelTank.load(input, "fuel_tank");
        this.current.deserialize(input.childOrEmpty("psy_current"));
        this.burnEnergyReserve = Math.max(0, input.getIntOr("burn_energy_reserve", 0));
        this.generationRate = Math.clamp(input.getIntOr("generation_rate", 0), 0, MAX_OUTPUT_PER_TICK);
        this.machineStatus = MachineStatus.byNetworkId(input.getIntOr("machine_status", MachineStatus.IDLE.networkId()));
        this.targetRescanCooldown = 0;
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        return true;
    }

    private void sync() {
        this.setChanged();
        MachineSync.sync(this);
    }

    private boolean tryDrainFuelContainer() {
        ItemStack stack = this.items.get(SLOT_FUEL_CONTAINER);
        if (!isFuelContainer(stack)) {
            return false;
        }
        if (this.tryDrainFuelBucketFallback(stack) || this.tryDrainFuelBottleFallback(stack)) {
            return true;
        }
        ResourceHandler<FluidResource> handler = ItemAccess
                .forHandlerIndexStrict(VanillaContainerWrapper.of(this), SLOT_FUEL_CONTAINER)
                .oneByOne()
                .getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return this.tryDrainFuelBucketFallback(stack);
        }
        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);
        if (resource.isEmpty() || containedAmount <= 0) {
            return this.tryDrainFuelBucketFallback(stack);
        }
        Optional<ResourceLocation> id = stillhouseFuelId(resource.getFluid());
        if (id.isEmpty()) {
            return this.tryDrainFuelBucketFallback(stack);
        }
        FluidStack incoming = resource.toStack(containedAmount);
        int addable = this.fuelTank.getAddableAmount(incoming);
        if (addable <= 0) {
            return false;
        }
        boolean bucketLike = stack.getItem() instanceof BucketItem || stack.is(Items.BUCKET);
        int request = bucketLike ? FluidType.BUCKET_VOLUME : Math.min(containedAmount, addable);
        if (bucketLike && addable < FluidType.BUCKET_VOLUME) {
            return false;
        }
        try (Transaction tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, request, tx);
            if (extracted != request) {
                return false;
            }
            tx.commit();
            return this.fuelTank.insert(incoming.copyWithAmount(extracted), false) == extracted;
        }
    }

    private boolean tryDrainFuelBucketFallback(ItemStack stack) {
        if (stack.getCount() != 1) {
            return false;
        }
        Optional<ResourceLocation> fluidId = fuelBucketFluidId(stack);
        if (fluidId.isEmpty()) {
            return false;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId.get());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        FluidStack incoming = new FluidStack(fluid, FluidType.BUCKET_VOLUME);
        if (this.fuelTank.getAddableAmount(incoming) < FluidType.BUCKET_VOLUME) {
            return false;
        }
        if (this.fuelTank.insert(incoming, false) != FluidType.BUCKET_VOLUME) {
            return false;
        }
        ItemStack remainder = stack.getCraftingRemainder();
        this.items.set(SLOT_FUEL_CONTAINER, remainder.isEmpty() ? new ItemStack(Items.BUCKET) : remainder.copy());
        this.sync();
        return true;
    }

    private boolean tryDrainFuelBottleFallback(ItemStack stack) {
        Optional<ResourceLocation> fluidId = fuelBottleFluidId(stack);
        if (fluidId.isEmpty()) {
            return false;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId.get());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }
        int containedAmount = GlassBottleItem.getStoredAmount(stack);
        FluidStack incoming = new FluidStack(fluid, containedAmount);
        int request = Math.min(containedAmount, this.fuelTank.getAddableAmount(incoming));
        if (request <= 0) {
            return false;
        }
        int drained = GlassBottleItem.drain(stack, fluidId.get(), request);
        if (drained <= 0) {
            return false;
        }
        if (this.fuelTank.insert(incoming.copyWithAmount(drained), false) != drained) {
            GlassBottleItem.fill(stack, fluidId.get(), drained);
            return false;
        }
        this.items.set(SLOT_FUEL_CONTAINER, stack);
        this.sync();
        return true;
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, amount);
        if (!removed.isEmpty()) {
            this.sync();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.sync();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return slot == SLOT_FUEL_CONTAINER && isFuelContainer(stack);
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.sync();
    }

    private final class FuelTankHandler implements ResourceHandler<FluidResource> {
        private final FuelTankJournal journal = new FuelTankJournal();

        @Override
        public int size() {
            return 1;
        }

        @Override
        public FluidResource getResource(int slot) {
            FluidStack stored = fuelTank.getFluid();
            return stored.isEmpty() ? FluidResource.EMPTY : FluidResource.of(stored.getFluid());
        }

        @Override
        public long getAmountAsLong(int slot) {
            return fuelTank.getAmount();
        }

        @Override
        public long getCapacityAsLong(int slot, FluidResource resource) {
            return FUEL_CAPACITY_MB;
        }

        @Override
        public boolean isValid(int slot, FluidResource resource) {
            if (slot != 0 || resource.isEmpty()) {
                return false;
            }
            return stillhouseFuelId(resource.getFluid()).isPresent();
        }

        @Override
        public int insert(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            if (!isValid(slot, resource) || amount <= 0) {
                return 0;
            }
            int addable = fuelTank.getAddableAmount(resource.toStack(amount));
            if (addable <= 0) {
                return 0;
            }
            this.journal.updateSnapshots(transaction);
            FluidStack stored = fuelTank.getFluid();
            if (stored.isEmpty()) {
                fuelTank.setFluidSilent(resource.toStack(addable));
            } else {
                fuelTank.setFluidSilent(stored.copyWithAmount(stored.getAmount() + addable));
            }
            return addable;
        }

        @Override
        public int extract(int slot, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private final class FuelTankJournal extends SnapshotJournal<FluidStack> {
        @Override
        protected FluidStack createSnapshot() {
            return fuelTank.getFluid().copy();
        }

        @Override
        protected void revertToSnapshot(FluidStack snapshot) {
            fuelTank.setFluidSilent(snapshot);
        }

        @Override
        protected void onRootCommit(FluidStack originalState) {
            fuelTank.markChanged();
            MachineTransferAttachments.markCapabilityChanged(StillhouseBurnerBlockEntity.this);
        }
    }
}
