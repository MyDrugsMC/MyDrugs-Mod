package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;
import org.mydrugs.mydrugs.energy.PsyCurrentDistributor;
import org.mydrugs.mydrugs.energy.PsyCurrentStorage;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuel;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuelRegistry;
import org.mydrugs.mydrugs.energy.psycurrent.DistillateFuelType;
import org.mydrugs.mydrugs.energy.psycurrent.PsyCurrentTargetScan;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.menu.DistillateEngineMenu;

import java.util.Optional;

public final class DistillateEngineBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_REGULATOR = 1;
    public static final int SLOT_WASTE_OUTPUT = 2;
    public static final int SLOT_COUNT = 3;
    public static final int CURRENT_CAPACITY = PsyCurrentConstants.ENGINE_CURRENT_CAPACITY;
    public static final int MAX_RADIUS = PsyCurrentConstants.ENGINE_MAX_RADIUS;

    private final PsyCurrentStorage current = new PsyCurrentStorage(CURRENT_CAPACITY);
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int fuelTicksRemaining;
    private int fuelTicksTotal;
    private int currentPerTick;
    private int strain;
    private int overloadTicks;
    private int powerRadius = PsyCurrentConstants.ENGINE_MIN_RADIUS;
    private int targetRescanCooldown;
    private PsyCurrentTargetScan cachedScan = PsyCurrentTargetScan.EMPTY;
    private @Nullable DistillateFuel activeFuel;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> current.stored();
                case 1 -> current.capacity();
                case 2 -> fuelTicksRemaining;
                case 3 -> fuelTicksTotal;
                case 4 -> currentPerTick;
                case 5 -> strain;
                case 6 -> overloadTicks;
                case 7 -> powerRadius;
                case 8 -> cachedScan.validCount();
                case 9 -> cachedScan.fullCount();
                case 10 -> cachedScan.incompatibleCount();
                case 11 -> cachedScan.totalReceivable();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 2 -> fuelTicksRemaining = value;
                case 3 -> fuelTicksTotal = value;
                case 4 -> currentPerTick = value;
                case 5 -> strain = value;
                case 6 -> overloadTicks = value;
                case 7 -> powerRadius = Math.clamp(value, 1, MAX_RADIUS);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return DistillateEngineMenu.DATA_COUNT;
        }
    };

    public DistillateEngineBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLATE_ENGINE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DistillateEngineBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;
        if (be.targetRescanCooldown-- <= 0) {
            be.cachedScan = PsyCurrentDistributor.scan(serverLevel, pos, be.powerRadius);
            be.targetRescanCooldown = PsyCurrentConstants.ENGINE_TARGET_RESCAN_INTERVAL_TICKS;
            changed = true;
        }

        if (be.overloadTicks > 0) {
            be.overloadTicks--;
            if (serverLevel.getGameTime() % PsyCurrentConstants.ENGINE_OVERLOAD_COOL_INTERVAL_TICKS == 0) {
                be.coolStrain(PsyCurrentConstants.ENGINE_OVERLOAD_COOL_AMOUNT);
            }
            changed |= PsyCurrentDistributor.distributeStored(serverLevel, be.current, be.cachedScan.valid());
            changed |= be.setMachineStatus(MachineStatus.PAUSED);
            if (changed) {
                be.sync();
            }
            return;
        }

        if (be.activeFuel == null && be.fuelTicksRemaining <= 0) {
            StartResult result = be.tryStartFuel();
            changed |= result.changed();
            if (!result.didStart()) {
                changed |= PsyCurrentDistributor.distributeStored(serverLevel, be.current, be.cachedScan.valid());
                changed |= be.decayIdleStrain(serverLevel);
                changed |= be.setMachineStatus(result.status());
                if (changed) {
                    be.sync();
                }
                return;
            }
        }

        int elapsed = be.fuelTicksTotal - be.fuelTicksRemaining;
        int generated = be.activeFuel == null ? 0 : be.activeFuel.outputForTick(elapsed);
        if (generated > 0 && PsyCurrentDistributor.totalReceivable(serverLevel, be.cachedScan.valid(), be.current) < generated) {
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_TANK_FULL);
            changed |= PsyCurrentDistributor.distributeStored(serverLevel, be.current, be.cachedScan.valid());
            if (changed) {
                be.sync();
            }
            return;
        }

        if (generated > 0) {
            PsyCurrentDistributor.distribute(serverLevel, generated, be.current, be.cachedScan.valid());
            AdvancementEventHooks.psychotropeEvent(serverLevel, pos, "psy_current_generated", "", generated, be.current.stored());
        }

        be.fuelTicksRemaining--;
        changed = true;
        changed |= be.applyActiveFuelStrain(elapsed);
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        if (be.strain >= PsyCurrentConstants.ENGINE_MAX_STRAIN) {
            be.enterOverload();
            changed = true;
        } else if (be.fuelTicksRemaining <= 0) {
            be.activeFuel = null;
            be.currentPerTick = 0;
        }

        if (changed) {
            be.sync();
        }
    }

    public static boolean isFuel(ItemStack stack) {
        return DistillateFuelRegistry.isFuel(stack);
    }

    public static boolean isRegulator(ItemStack stack) {
        return stack.is(ModItems.CURRENT_REGULATOR.get()) || stack.is(ModItems.STRAIN_VENT.get());
    }

    public PsyCurrentStorage current() {
        return this.current;
    }

    /** Server-only: most recent target scan used by {@link DistillateEngineBlockEntity#tick}. */
    public PsyCurrentTargetScan cachedScan() {
        return this.cachedScan;
    }

    public int powerRadius() {
        return this.powerRadius;
    }

    public int strain() {
        return this.strain;
    }

    public int overloadTicks() {
        return this.overloadTicks;
    }

    public boolean isRunning() {
        return this.activeFuel != null && this.fuelTicksRemaining > 0 && this.overloadTicks == 0;
    }

    public @Nullable DistillateFuel activeFuel() {
        return this.activeFuel;
    }

    public void setPowerRadius(int radius) {
        int clamped = Math.clamp(radius, PsyCurrentConstants.ENGINE_MIN_RADIUS, MAX_RADIUS);
        if (this.powerRadius != clamped) {
            this.powerRadius = clamped;
            this.targetRescanCooldown = 0;
            sync();
        }
    }

    /** Server-only: send the area-preview payload to the requesting player. */
    public void broadcastAreaPreview(net.minecraft.server.level.ServerPlayer player) {
        if (player == null) {
            return;
        }
        net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                player,
                new org.mydrugs.mydrugs.network.DistillateEnginePreviewPayload(
                        this.worldPosition, this.powerRadius, AREA_PREVIEW_TICKS
                )
        );
    }

    public static final int AREA_PREVIEW_TICKS = PsyCurrentConstants.ENGINE_AREA_PREVIEW_TICKS;

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.distillate_engine");
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
        return new DistillateEngineMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SLOT_FUEL -> isFuel(stack);
            case SLOT_REGULATOR -> isRegulator(stack);
            default -> false;
        };
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.current.deserialize(input.childOrEmpty("psy_current"));
        this.fuelTicksRemaining = input.getIntOr("fuel_ticks_remaining", 0);
        this.fuelTicksTotal = input.getIntOr("fuel_ticks_total", 0);
        this.currentPerTick = input.getIntOr("current_per_tick", 0);
        this.strain = Math.clamp(input.getIntOr("strain", 0), 0, PsyCurrentConstants.ENGINE_MAX_STRAIN);
        this.overloadTicks = input.getIntOr("overload_ticks", 0);
        this.powerRadius = Math.clamp(input.getIntOr("power_radius", PsyCurrentConstants.ENGINE_MIN_RADIUS),
                PsyCurrentConstants.ENGINE_MIN_RADIUS, MAX_RADIUS);
        this.machineStatus = MachineStatus.byNetworkId(input.getIntOr("machine_status", MachineStatus.IDLE.networkId()));
        this.activeFuel = readFuel(input.getStringOr("active_fuel_item", ""));
        this.targetRescanCooldown = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        this.current.serialize(output.child("psy_current"));
        output.putInt("fuel_ticks_remaining", this.fuelTicksRemaining);
        output.putInt("fuel_ticks_total", this.fuelTicksTotal);
        output.putInt("current_per_tick", this.currentPerTick);
        output.putInt("strain", this.strain);
        output.putInt("overload_ticks", this.overloadTicks);
        output.putInt("power_radius", this.powerRadius);
        output.putInt("machine_status", this.machineStatus.networkId());
        output.putString("active_fuel_item", this.activeFuel == null ? "" : BuiltInRegistries.ITEM.getKey(this.activeFuel.item()).toString());
    }

    private StartResult tryStartFuel() {
        ItemStack stack = this.getItem(SLOT_FUEL);
        if (stack.isEmpty()) {
            return StartResult.unchanged(MachineStatus.MISSING_INPUT_ITEM);
        }
        if (DistillateFuelRegistry.isRejectedPsychedelicMaterial(stack)) {
            return StartResult.unchanged(MachineStatus.NO_MATCHING_RECIPE);
        }

        Optional<DistillateFuel> fuel = DistillateFuelRegistry.get(stack);
        if (fuel.isEmpty()) {
            return StartResult.unchanged(MachineStatus.NO_MATCHING_RECIPE);
        }

        this.activeFuel = fuel.get();
        this.fuelTicksRemaining = this.activeFuel.durationTicks();
        this.fuelTicksTotal = this.activeFuel.durationTicks();
        this.currentPerTick = Math.max(1, this.activeFuel.totalCurrent() / this.activeFuel.durationTicks());
        this.strain = Math.clamp(this.strain + this.activeFuel.strainOnStart(), 0, PsyCurrentConstants.ENGINE_MAX_STRAIN);
        stack.shrink(1);
        this.setItem(SLOT_FUEL, stack);
        return StartResult.started();
    }

    private boolean applyActiveFuelStrain(int elapsedTicks) {
        if (this.activeFuel == null || elapsedTicks <= 0 || elapsedTicks % PsyCurrentConstants.ENGINE_STRAIN_TICK_INTERVAL != 0) {
            return false;
        }

        int strainDelta = this.activeFuel.strainPerSecond();
        if (strainDelta > 0 && this.getItem(SLOT_REGULATOR).is(ModItems.CURRENT_REGULATOR.get())) {
            strainDelta = Math.max(0, strainDelta - PsyCurrentConstants.ENGINE_CURRENT_REGULATOR_STRAIN_REDUCTION);
        }
        if (strainDelta == 0) {
            return false;
        }

        int old = this.strain;
        this.strain = Math.clamp(this.strain + strainDelta, 0, PsyCurrentConstants.ENGINE_MAX_STRAIN);
        return this.strain != old;
    }

    private boolean decayIdleStrain(ServerLevel level) {
        if (this.activeFuel != null || level.getGameTime() % PsyCurrentConstants.ENGINE_IDLE_STRAIN_DECAY_INTERVAL_TICKS != 0) {
            return false;
        }
        int cooling = this.getItem(SLOT_REGULATOR).is(ModItems.STRAIN_VENT.get())
                ? PsyCurrentConstants.ENGINE_STRAIN_VENT_DECAY
                : PsyCurrentConstants.ENGINE_IDLE_STRAIN_DECAY;
        return coolStrain(cooling);
    }

    private boolean coolStrain(int amount) {
        int old = this.strain;
        this.strain = Math.max(0, this.strain - Math.max(0, amount));
        return this.strain != old;
    }

    private void enterOverload() {
        this.overloadTicks = PsyCurrentConstants.ENGINE_OVERLOAD_COOLDOWN_TICKS;
        this.fuelTicksRemaining = 0;
        this.fuelTicksTotal = 0;
        this.currentPerTick = 0;
        this.activeFuel = null;
        this.strain = PsyCurrentConstants.ENGINE_MAX_STRAIN;
        addWasteOnOverload();
        setMachineStatus(MachineStatus.PAUSED);
    }

    private void addWasteOnOverload() {
        ItemStack waste = new ItemStack(ModItems.BURNT_NERVE_RESIDUE.get());
        ItemStack existing = this.getItem(SLOT_WASTE_OUTPUT);
        if (existing.isEmpty()) {
            this.setItem(SLOT_WASTE_OUTPUT, waste);
        } else if (ItemStack.isSameItemSameComponents(existing, waste) && existing.getCount() < existing.getMaxStackSize()) {
            existing.grow(1);
            this.setItem(SLOT_WASTE_OUTPUT, existing);
        }
    }

    private @Nullable DistillateFuel readFuel(String itemId) {
        if (itemId.isBlank()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(itemId);
        if (id == null) {
            return null;
        }
        Item item = BuiltInRegistries.ITEM.getValue(id);
        if (item == null || item == Items.AIR) {
            return null;
        }
        return DistillateFuelRegistry.get(new ItemStack(item)).orElse(null);
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        return true;
    }

    private void sync() {
        MachineSync.sync(this);
    }

    private record StartResult(boolean didStart, boolean changed, MachineStatus status) {
        static StartResult started() {
            return new StartResult(true, true, MachineStatus.RUNNING);
        }

        static StartResult unchanged(MachineStatus status) {
            return new StartResult(false, false, status);
        }
    }
}
