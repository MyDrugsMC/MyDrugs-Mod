package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.CompositeGasHandler;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasTank;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.energy.PsychotropeEnergyMachines;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.GasTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.SteamCrackerMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerFluidStack;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerGasStack;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerRecipe;
import org.mydrugs.mydrugs.recipes.steam_cracker.SteamCrackerRecipeInput;

import java.util.Optional;

public class SteamCrackerBlockEntity extends BaseContainerBlockEntity implements SteamCrackerMenu.SteamCrackerButtonHandler, MachineStatusProvider {
    public static final int FLUID_CAPACITY = 4000;
    public static final int GAS_CAPACITY = 4000;

    private final LockedTransferSlots fluidInputLocks = new LockedTransferSlots(1);
    private final LockedTransferSlots gasInputLocks = new LockedTransferSlots(1);

    private final StoredFluidTank inputFluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output1FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output2FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output3FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output4FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);

    private final GasTank inputGasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output1GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output2GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output3GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output4GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);

    private boolean inputGasMode;
    private boolean output1GasMode;
    private boolean output2GasMode;
    private boolean output3GasMode;
    private boolean output4GasMode;

    private NonNullList<ItemStack> items = NonNullList.withSize(SteamCrackerMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = 200;
    private int burnTimeRemaining;
    private int burnTimeTotal;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputGasMode ? (int) inputGasTank.getAmount() : inputFluidTank.getAmount();
                case 1 -> output1GasMode ? (int) output1GasTank.getAmount() : output1FluidTank.getAmount();
                case 2 -> output2GasMode ? (int) output2GasTank.getAmount() : output2FluidTank.getAmount();
                case 3 -> output3GasMode ? (int) output3GasTank.getAmount() : output3FluidTank.getAmount();
                case 4 -> output4GasMode ? (int) output4GasTank.getAmount() : output4FluidTank.getAmount();
                case 5 -> progress;
                case 6 -> maxProgress;
                case 7 -> burnTimeRemaining;
                case 8 -> burnTimeTotal;
                case 9 -> inputFluidTank.encodeFluidSyncId();
                case 10 -> output1FluidTank.encodeFluidSyncId();
                case 11 -> output2FluidTank.encodeFluidSyncId();
                case 12 -> output3FluidTank.encodeFluidSyncId();
                case 13 -> output4FluidTank.encodeFluidSyncId();
                case 14 -> inputGasTank.isEmpty() ? -1 : ModGases.getSyncId(inputGasTank.getGasType());
                case 15 -> output1GasTank.isEmpty() ? -1 : ModGases.getSyncId(output1GasTank.getGasType());
                case 16 -> output2GasTank.isEmpty() ? -1 : ModGases.getSyncId(output2GasTank.getGasType());
                case 17 -> output3GasTank.isEmpty() ? -1 : ModGases.getSyncId(output3GasTank.getGasType());
                case 18 -> output4GasTank.isEmpty() ? -1 : ModGases.getSyncId(output4GasTank.getGasType());
                case 19 -> inputGasMode ? 1 : 0;
                case 20 -> output1GasMode ? 1 : 0;
                case 21 -> output2GasMode ? 1 : 0;
                case 22 -> output3GasMode ? 1 : 0;
                case 23 -> output4GasMode ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 5 -> progress = value;
                case 6 -> maxProgress = value;
                case 7 -> burnTimeRemaining = value;
                case 8 -> burnTimeTotal = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return SteamCrackerMenu.DATA_COUNT;
        }
    };

    public SteamCrackerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.STEAM_CRACKER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SteamCrackerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;
        Optional<RecipeHolder<SteamCrackerRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
        SteamCrackerRecipe recipe = recipeHolder.map(RecipeHolder::value).orElse(null);

        if (be.refreshModes(recipe)) {
            changed = true;
        }

        if (be.processInputTransfer()) {
            changed = true;
        }

        recipeHolder = be.getCurrentRecipe(serverLevel);
        recipe = recipeHolder.map(RecipeHolder::value).orElse(null);
        if (be.refreshModes(recipe)) {
            changed = true;
        }

        if (be.processOutputTransfers()) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0) {
            be.burnTimeRemaining--;
            changed = true;
        }

        if (recipe == null || !be.canCraft(recipe)) {
            changed |= be.setMachineStatus(recipe == null
                    ? MachineStatus.NO_MATCHING_RECIPE
                    : be.inputFluidTank.isEmpty() && be.inputGasTank.isEmpty() ? MachineStatus.MISSING_INPUT_FLUID : MachineStatus.OUTPUT_TANK_FULL);
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }
            if (changed) {
                be.sync();
            }
            return;
        }

        be.maxProgress = recipe.baseTicks();
        boolean poweredByEnergy = PsychotropeEnergyMachines.tryUseEnergyTick(be);
        if (be.burnTimeRemaining <= 0 && !poweredByEnergy && be.tryConsumeFuel()) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0 || poweredByEnergy) {
            changed |= be.setMachineStatus(MachineStatus.RUNNING);
            be.progress++;
            changed = true;
            if (be.progress >= be.maxProgress) {
                be.craft(recipe);
                be.progress = 0;
                be.refreshModes(recipe);
                changed = true;
            }
        } else {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
        }

        if (changed) {
            be.sync();
        }
    }

    private boolean processInputTransfer() {
        return this.inputGasMode
                ? GasTransferUtil.tryProcessTransferSlot(this.items, SteamCrackerMenu.INPUT_TRANSFER_SLOT, this.inputGasTank, this.gasInputLocks, 0)
                : FluidTransferUtil.tryProcessTransferSlot(this, SteamCrackerMenu.INPUT_TRANSFER_SLOT, this.inputFluidTank, this.fluidInputLocks, 0);
    }

    private boolean processOutputTransfers() {
        boolean changed = false;
        changed |= processOutputTransfer(SteamCrackerMenu.OUTPUT_1_TRANSFER_SLOT, this.output1GasMode, this.output1FluidTank, this.output1GasTank);
        changed |= processOutputTransfer(SteamCrackerMenu.OUTPUT_2_TRANSFER_SLOT, this.output2GasMode, this.output2FluidTank, this.output2GasTank);
        changed |= processOutputTransfer(SteamCrackerMenu.OUTPUT_3_TRANSFER_SLOT, this.output3GasMode, this.output3FluidTank, this.output3GasTank);
        changed |= processOutputTransfer(SteamCrackerMenu.OUTPUT_4_TRANSFER_SLOT, this.output4GasMode, this.output4FluidTank, this.output4GasTank);
        return changed;
    }

    private boolean processOutputTransfer(int slot, boolean gasMode, StoredFluidTank fluidTank, GasTank gasTank) {
        return gasMode
                ? GasTransferUtil.tryFillOutputSlot(this.items, slot, gasTank)
                : FluidTransferUtil.tryFillOutputSlot(this, slot, fluidTank);
    }

    private Optional<RecipeHolder<SteamCrackerRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.STEAM_CRACKER.get(),
                new SteamCrackerRecipeInput(currentFluidStack(this.inputFluidTank), this.inputGasTank.getGasInTank(0)),
                level
        );
    }

    private static FluidStack currentFluidStack(StoredFluidTank tank) {
        ResourceLocation id = tank.getFluidId();
        if (id == null || tank.isEmpty()) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
        return fluid == null || fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, tank.getAmount());
    }

    private boolean refreshModes(@Nullable SteamCrackerRecipe recipe) {
        boolean newInputGasMode = resolveInputMode(recipe != null && recipe.inputGas().isPresent(), this.inputFluidTank, this.inputGasTank, this.getItem(SteamCrackerMenu.INPUT_TRANSFER_SLOT));
        boolean newOutput1GasMode = resolveOutputMode(recipe != null && recipe.outputGas1().isPresent(), this.output1FluidTank, this.output1GasTank);
        boolean newOutput2GasMode = resolveOutputMode(recipe != null && recipe.outputGas2().isPresent(), this.output2FluidTank, this.output2GasTank);
        boolean newOutput3GasMode = resolveOutputMode(recipe != null && recipe.outputGas3().isPresent(), this.output3FluidTank, this.output3GasTank);
        boolean newOutput4GasMode = resolveOutputMode(recipe != null && recipe.outputGas4().isPresent(), this.output4FluidTank, this.output4GasTank);

        boolean changed = false;
        if (newInputGasMode != this.inputGasMode) {
            this.inputGasMode = newInputGasMode;
            this.fluidInputLocks.reset(0);
            this.gasInputLocks.reset(0);
            changed = true;
        }
        if (newOutput1GasMode != this.output1GasMode) {
            this.output1GasMode = newOutput1GasMode;
            changed = true;
        }
        if (newOutput2GasMode != this.output2GasMode) {
            this.output2GasMode = newOutput2GasMode;
            changed = true;
        }
        if (newOutput3GasMode != this.output3GasMode) {
            this.output3GasMode = newOutput3GasMode;
            changed = true;
        }
        if (newOutput4GasMode != this.output4GasMode) {
            this.output4GasMode = newOutput4GasMode;
            changed = true;
        }
        return changed;
    }

    private static boolean resolveInputMode(boolean recipeWantsGas, StoredFluidTank fluidTank, GasTank gasTank, ItemStack stack) {
        if (!gasTank.isEmpty()) {
            return true;
        }
        if (!fluidTank.isEmpty()) {
            return false;
        }
        if (recipeWantsGas) {
            return true;
        }
        return isGasContainer(stack) && !isFluidContainer(stack);
    }

    private static boolean resolveOutputMode(boolean recipeWantsGas, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!gasTank.isEmpty()) {
            return true;
        }
        if (!fluidTank.isEmpty()) {
            return false;
        }
        return recipeWantsGas;
    }

    public static boolean isFluidContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof GlassBottleItem) {
            return true;
        }
        return ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM) != null;
    }

    public static boolean isGasContainer(ItemStack stack) {
        return !stack.isEmpty() && stack.getCapability(ModGasCapabilities.ITEM, null) != null;
    }

    public static boolean isFuel(ItemStack stack, @Nullable Level level) {
        return !stack.isEmpty() && level != null && stack.getBurnTime(null, level.fuelValues()) > 0;
    }

    private static FluidStack toFluidStack(ResourceLocation fluidId, int amount) {
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
        return fluid == null || fluid == Fluids.EMPTY ? FluidStack.EMPTY : new FluidStack(fluid, amount);
    }

    private static GasStack toGasStack(ResourceLocation gasId, long amount) {
        if (amount <= 0) {
            return GasStack.EMPTY;
        }
        GasType gas = ModGases.get(gasId);
        return gas == null ? GasStack.EMPTY : GasStack.of(gas, amount);
    }

    private static @Nullable GasType readGasType(ValueInput input, String key) {
        String raw = input.getStringOr(key, "");
        return raw.isBlank() ? null : ModGases.getNullable(raw);
    }

    private boolean hasFluidInput(SteamCrackerFluidStack required) {
        if (!this.inputGasTank.isEmpty()) {
            return false;
        }
        ResourceLocation id = this.inputFluidTank.getFluidId();
        return id != null && id.equals(required.fluid()) && this.inputFluidTank.getAmount() >= required.amount();
    }

    private boolean hasGasInput(SteamCrackerGasStack required) {
        if (!this.inputFluidTank.isEmpty()) {
            return false;
        }
        GasStack stack = this.inputGasTank.getGasInTank(0);
        return !stack.isEmpty() && stack.type() != null && stack.type().id().equals(required.gas()) && stack.amount() >= required.amount();
    }

    private boolean canAcceptFluidOutput(SteamCrackerFluidStack output, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!gasTank.isEmpty()) {
            return false;
        }
        FluidStack stack = toFluidStack(output.fluid(), output.amount());
        return !stack.isEmpty() && fluidTank.getAddableAmount(stack) >= stack.getAmount();
    }

    private boolean canAcceptGasOutput(SteamCrackerGasStack output, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!fluidTank.isEmpty()) {
            return false;
        }
        GasStack stack = toGasStack(output.gas(), output.amount());
        return !stack.isEmpty() && gasTank.fill(stack, true) >= output.amount();
    }

    private boolean canCraft(SteamCrackerRecipe recipe) {
        if (recipe.inputFluid().isPresent()) {
            if (!hasFluidInput(recipe.inputFluid().get())) {
                return false;
            }
        } else if (!hasGasInput(recipe.inputGas().orElseThrow())) {
            return false;
        }
        return canAcceptOutput(recipe.outputFluid1(), recipe.outputGas1(), this.output1FluidTank, this.output1GasTank)
                && canAcceptOutput(recipe.outputFluid2(), recipe.outputGas2(), this.output2FluidTank, this.output2GasTank)
                && canAcceptOutput(recipe.outputFluid3(), recipe.outputGas3(), this.output3FluidTank, this.output3GasTank)
                && canAcceptOutput(recipe.outputFluid4(), recipe.outputGas4(), this.output4FluidTank, this.output4GasTank);
    }

    private boolean canAcceptOutput(Optional<SteamCrackerFluidStack> fluid, Optional<SteamCrackerGasStack> gas, StoredFluidTank fluidTank, GasTank gasTank) {
        return fluid.map(stack -> canAcceptFluidOutput(stack, fluidTank, gasTank))
                .orElseGet(() -> canAcceptGasOutput(gas.orElseThrow(), fluidTank, gasTank));
    }

    private void craft(SteamCrackerRecipe recipe) {
        recipe.inputFluid().ifPresent(input -> this.inputFluidTank.extract(input.amount(), false));
        recipe.inputGas().ifPresent(input -> this.inputGasTank.drain(input.amount(), false));
        insertOutput(recipe.outputFluid1(), recipe.outputGas1(), this.output1FluidTank, this.output1GasTank);
        insertOutput(recipe.outputFluid2(), recipe.outputGas2(), this.output2FluidTank, this.output2GasTank);
        insertOutput(recipe.outputFluid3(), recipe.outputGas3(), this.output3FluidTank, this.output3GasTank);
        insertOutput(recipe.outputFluid4(), recipe.outputGas4(), this.output4FluidTank, this.output4GasTank);
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
    }

    private void insertOutput(Optional<SteamCrackerFluidStack> fluid, Optional<SteamCrackerGasStack> gas, StoredFluidTank fluidTank, GasTank gasTank) {
        fluid.ifPresent(output -> {
            FluidStack stack = toFluidStack(output.fluid(), output.amount());
            if (!stack.isEmpty()) {
                fluidTank.insert(stack, false);
            }
        });
        gas.ifPresent(output -> {
            GasStack stack = toGasStack(output.gas(), output.amount());
            if (!stack.isEmpty()) {
                gasTank.fill(stack, false);
            }
        });
    }

    private boolean tryConsumeFuel() {
        ItemStack fuelStack = this.getItem(SteamCrackerMenu.FUEL_SLOT);
        if (fuelStack.isEmpty() || this.level == null) {
            return false;
        }
        int burn = fuelStack.getBurnTime(null, this.level.fuelValues());
        if (burn <= 0) {
            return false;
        }
        this.burnTimeRemaining = burn;
        this.burnTimeTotal = burn;
        ItemStack remainder = fuelStack.getCraftingRemainder();
        fuelStack.shrink(1);
        if (fuelStack.isEmpty()) {
            this.setItem(SteamCrackerMenu.FUEL_SLOT, remainder);
        }
        return true;
    }

    private static boolean dumpGasTank(GasTank tank) {
        if (tank.isEmpty()) {
            return false;
        }
        tank.drain(tank.getAmount(), false);
        return true;
    }

    private static boolean dumpMixedTank(boolean gasMode, StoredFluidTank fluidTank, GasTank gasTank) {
        if (gasMode) {
            return dumpGasTank(gasTank) || fluidTank.dump();
        }
        return fluidTank.dump() || dumpGasTank(gasTank);
    }

    private void sync() {
        MachineSync.sync(this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.steam_cracker");
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
        return new SteamCrackerMenu(containerId, inventory, this, this.data, ContainerLevelAccess.create(this.level, this.worldPosition));
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
        return SteamCrackerMenu.MACHINE_SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case SteamCrackerMenu.INPUT_TRANSFER_SLOT,
                 SteamCrackerMenu.OUTPUT_1_TRANSFER_SLOT,
                 SteamCrackerMenu.OUTPUT_2_TRANSFER_SLOT,
                 SteamCrackerMenu.OUTPUT_3_TRANSFER_SLOT,
                 SteamCrackerMenu.OUTPUT_4_TRANSFER_SLOT -> isFluidContainer(stack) || isGasContainer(stack);
            case SteamCrackerMenu.FUEL_SLOT -> isFuel(stack, this.level);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == SteamCrackerMenu.INPUT_TRANSFER_SLOT) {
            this.fluidInputLocks.reset(0);
            this.gasInputLocks.reset(0);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.inputFluidTank.load(input, "InputFluid");
        this.output1FluidTank.load(input, "OutputFluid1");
        this.output2FluidTank.load(input, "OutputFluid2");
        this.output3FluidTank.load(input, "OutputFluid3");
        this.output4FluidTank.load(input, "OutputFluid4");
        this.inputGasTank.loadStored(readGasType(input, "InputGas"), input.getLongOr("InputGasAmount", 0));
        this.output1GasTank.loadStored(readGasType(input, "OutputGas1"), input.getLongOr("OutputGasAmount1", 0));
        this.output2GasTank.loadStored(readGasType(input, "OutputGas2"), input.getLongOr("OutputGasAmount2", 0));
        this.output3GasTank.loadStored(readGasType(input, "OutputGas3"), input.getLongOr("OutputGasAmount3", 0));
        this.output4GasTank.loadStored(readGasType(input, "OutputGas4"), input.getLongOr("OutputGasAmount4", 0));
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);
        this.burnTimeRemaining = input.getIntOr("BurnTimeRemaining", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);
        this.inputGasMode = input.getBooleanOr("InputGasMode", false);
        this.output1GasMode = input.getBooleanOr("Output1GasMode", false);
        this.output2GasMode = input.getBooleanOr("Output2GasMode", false);
        this.output3GasMode = input.getBooleanOr("Output3GasMode", false);
        this.output4GasMode = input.getBooleanOr("Output4GasMode", false);
        this.refreshModes(null);
        this.fluidInputLocks.resetAll();
        this.gasInputLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        this.inputFluidTank.save(output, "InputFluid");
        this.output1FluidTank.save(output, "OutputFluid1");
        this.output2FluidTank.save(output, "OutputFluid2");
        this.output3FluidTank.save(output, "OutputFluid3");
        this.output4FluidTank.save(output, "OutputFluid4");
        saveGas(output, "InputGas", "InputGasAmount", this.inputGasTank);
        saveGas(output, "OutputGas1", "OutputGasAmount1", this.output1GasTank);
        saveGas(output, "OutputGas2", "OutputGasAmount2", this.output2GasTank);
        saveGas(output, "OutputGas3", "OutputGasAmount3", this.output3GasTank);
        saveGas(output, "OutputGas4", "OutputGasAmount4", this.output4GasTank);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
        output.putBoolean("InputGasMode", this.inputGasMode);
        output.putBoolean("Output1GasMode", this.output1GasMode);
        output.putBoolean("Output2GasMode", this.output2GasMode);
        output.putBoolean("Output3GasMode", this.output3GasMode);
        output.putBoolean("Output4GasMode", this.output4GasMode);
    }

    private static void saveGas(ValueOutput output, String gasKey, String amountKey, GasTank tank) {
        GasType gas = tank.getGasType();
        output.putString(gasKey, gas == null ? "" : gas.id().toString());
        output.putLong(amountKey, tank.getAmount());
    }

    @Override
    public boolean onSteamCrackerButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }
        boolean dumped = switch (buttonId) {
            case SteamCrackerMenu.DUMP_INPUT_BUTTON_ID -> dumpMixedTank(this.inputGasMode, this.inputFluidTank, this.inputGasTank);
            case SteamCrackerMenu.DUMP_OUTPUT_1_BUTTON_ID -> dumpMixedTank(this.output1GasMode, this.output1FluidTank, this.output1GasTank);
            case SteamCrackerMenu.DUMP_OUTPUT_2_BUTTON_ID -> dumpMixedTank(this.output2GasMode, this.output2FluidTank, this.output2GasTank);
            case SteamCrackerMenu.DUMP_OUTPUT_3_BUTTON_ID -> dumpMixedTank(this.output3GasMode, this.output3FluidTank, this.output3GasTank);
            case SteamCrackerMenu.DUMP_OUTPUT_4_BUTTON_ID -> dumpMixedTank(this.output4GasMode, this.output4FluidTank, this.output4GasTank);
            default -> false;
        };
        if (dumped) {
            this.progress = 0;
            this.refreshModes(null);
            this.sync();
        }
        return dumped;
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(
                this,
                inputFluidTank,
                output1FluidTank,
                output2FluidTank,
                output3FluidTank,
                output4FluidTank
        );
    }

    public IGasHandler getGasHandler(Direction side) {
        return new CompositeGasHandler(inputGasTank, output1GasTank, output2GasTank, output3GasTank, output4GasTank);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
