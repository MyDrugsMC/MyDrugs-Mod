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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
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
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ChemicalReactorBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.FluidTankAccess;
import org.mydrugs.mydrugs.machine.fuel.FuelResolver;
import org.mydrugs.mydrugs.machine.fuel.MachineFuelUtil;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.GasTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.ChemicalReactorMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipeInput;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ReactorOutputKind;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ChemicalReactorBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements MenuProvider {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_PRIMARY_GAS_TRANSFER = 1;
    public static final int SLOT_SECONDARY_TRANSFER = 2;
    public static final int SLOT_GAS_OUTPUT_TRANSFER = 3;
    public static final int SLOT_FLUID_OUTPUT_TRANSFER = 4;
    public static final int SLOT_COUNT = 5;

    private static final int SLOT_SCHEMA_HYBRID_SECONDARY = 1;

    public static final int GAS_TANK_CAPACITY = 4000;
    public static final int FLUID_TANK_CAPACITY = 4000;
    public static final int MAX_HEAT = 1000;
    public static final int MAX_MANUAL_ENERGY = 200;

    private static final int CHARCOAL_BURN_TIME = 1600;
    private static final FuelResolver CHARCOAL_FUEL = (stack, level) ->
            stack.is(Items.CHARCOAL) ? CHARCOAL_BURN_TIME : 0;

    private static final int SECONDARY_FLUID_TANK = 0;
    private static final int OUTPUT_FLUID_TANK = 1;

    private final ReactorItemHandler itemHandler = new ReactorItemHandler(SLOT_COUNT);
    private final NonNullList<ItemStack> itemStacks = this.itemHandler.list();

    private final GasTank primaryGasTank = new GasTank(GAS_TANK_CAPACITY, gas -> true, this::onContentsChanged);
    private final GasTank secondaryGasTank = new GasTank(GAS_TANK_CAPACITY, gas -> true, this::onContentsChanged);
    private final GasTank gasOutputTank = new GasTank(GAS_TANK_CAPACITY, gas -> true, this::onContentsChanged);

    private final ReactorFluidHandler fluidHandler = new ReactorFluidHandler();
    private final NonNullList<FluidStack> fluidStacks = this.fluidHandler.list();

    private final FluidTankAccess secondaryFluidTank = FluidTankAccess.of(
            FLUID_TANK_CAPACITY,
            () -> this.fluidStacks.get(SECONDARY_FLUID_TANK),
            stack -> this.fluidStacks.set(SECONDARY_FLUID_TANK, stack)
    );

    private final FluidTankAccess outputFluidTank = FluidTankAccess.of(
            FLUID_TANK_CAPACITY,
            () -> this.fluidStacks.get(OUTPUT_FLUID_TANK),
            stack -> this.fluidStacks.set(OUTPUT_FLUID_TANK, stack)
    );

    private final LockedTransferSlots gasInputLocks = new LockedTransferSlots(2);
    private final LockedTransferSlots fluidInputLocks = new LockedTransferSlots(1);

    private final IGasHandler automationGasHandler = new IGasHandler() {
        @Override
        public int getTanks() {
            return 3;
        }

        @Override
        public GasStack getGasInTank(int tank) {
            return switch (tank) {
                case 0 -> primaryGasTank.getGasInTank(0);
                case 1 -> secondaryGasTank.getGasInTank(0);
                case 2 -> gasOutputTank.getGasInTank(0);
                default -> GasStack.EMPTY;
            };
        }

        @Override
        public long getTankCapacity(int tank) {
            return GAS_TANK_CAPACITY;
        }

        @Override
        public boolean isGasValid(int tank, GasStack stack) {
            return tank == 0 || tank == 1;
        }

        @Override
        public long fill(GasStack resource, boolean simulate) {
            long insertedPrimary = primaryGasTank.fill(resource, simulate);
            if (insertedPrimary > 0) {
                return insertedPrimary;
            }
            return secondaryGasTank.fill(resource, simulate);
        }

        @Override
        public GasStack drain(long amount, boolean simulate) {
            return gasOutputTank.drain(amount, simulate);
        }
    };

    private boolean suppressTransferModeReset = false;

    private int burnTimeRemaining;
    private int burnTimeTotal;
    private int heat;
    private int progress;
    private int maxProgress = 200;
    private int manualEnergy;
    private boolean active;
    private boolean secondaryFluidMode;
    private boolean outputFluidMode;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) primaryGasTank.getAmount();
                case 1 -> (int) secondaryGasTank.getAmount();
                case 2 -> secondaryFluidTank.getAmount();
                case 3 -> (int) gasOutputTank.getAmount();
                case 4 -> outputFluidTank.getAmount();
                case 5 -> progress;
                case 6 -> maxProgress;
                case 7 -> heat;
                case 8 -> MAX_HEAT;
                case 9 -> burnTimeRemaining;
                case 10 -> burnTimeTotal;
                case 11 -> manualEnergy;
                case 12 -> MAX_MANUAL_ENERGY;
                case 13 -> primaryGasTank.isEmpty() ? -1 : ModGases.getSyncId(primaryGasTank.getGasType());
                case 14 -> secondaryGasTank.isEmpty() ? -1 : ModGases.getSyncId(secondaryGasTank.getGasType());
                case 15 -> fluidStacks.get(SECONDARY_FLUID_TANK).isEmpty()
                        ? -1
                        : BuiltInRegistries.FLUID.getId(fluidStacks.get(SECONDARY_FLUID_TANK).getFluid());
                case 16 -> gasOutputTank.isEmpty() ? -1 : ModGases.getSyncId(gasOutputTank.getGasType());
                case 17 -> fluidStacks.get(OUTPUT_FLUID_TANK).isEmpty()
                        ? -1
                        : BuiltInRegistries.FLUID.getId(fluidStacks.get(OUTPUT_FLUID_TANK).getFluid());
                case 18 -> secondaryFluidMode ? 1 : 0;
                case 19 -> outputFluidMode ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 5 -> progress = value;
                case 6 -> maxProgress = value;
                case 7 -> heat = value;
                case 9 -> burnTimeRemaining = value;
                case 10 -> burnTimeTotal = value;
                case 11 -> manualEnergy = value;
                case 18 -> secondaryFluidMode = value != 0;
                case 19 -> outputFluidMode = value != 0;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 20;
        }
    };

    public ChemicalReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_REACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalReactorBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;
        boolean wasActive = be.active;

        changed |= be.runTransferWithoutReset(() ->
                GasTransferUtil.tryProcessTransferSlot(
                        be.itemStacks,
                        SLOT_PRIMARY_GAS_TRANSFER,
                        be.primaryGasTank,
                        be.gasInputLocks,
                        0
                )
        );

        Optional<RecipeHolder<ChemicalReactorRecipe>> recipeHolder = be.findMatchingRecipe();
        ChemicalReactorRecipe recipe = recipeHolder.map(RecipeHolder::value).orElse(null);

        changed |= be.refreshSecondaryInputMode(recipe);
        changed |= be.processSecondaryInputTransfer();

        recipeHolder = be.findMatchingRecipe();
        recipe = recipeHolder.map(RecipeHolder::value).orElse(null);

        changed |= be.refreshSecondaryInputMode(recipe);

        changed |= GasTransferUtil.tryFillOutputSlot(
                be.itemStacks,
                SLOT_GAS_OUTPUT_TRANSFER,
                be.gasOutputTank
        );

        changed |= FluidTransferUtil.tryFillOutputSlot(
                be.itemHandler,
                be.itemStacks,
                SLOT_FLUID_OUTPUT_TRANSFER,
                be.outputFluidTank
        );

        changed |= be.handleFuel();
        changed |= be.updateHeat();

        if (recipe != null) {
            changed |= be.setOutputFluidMode(recipe.outputKind() == ReactorOutputKind.FLUID);

            if (be.maxProgress != recipe.processTime()) {
                be.maxProgress = recipe.processTime();
                changed = true;
            }

            if (be.canOutput(recipe)) {
                int speed = be.getProgressPerTick(recipe);
                be.progress += speed;
                changed = true;

                if (be.manualEnergy > 0) {
                    be.manualEnergy = Math.max(0, be.manualEnergy - 1);
                }

                if (be.progress >= be.maxProgress) {
                    be.progress = 0;
                    be.processRecipe(recipe);
                    changed = true;
                }

                be.active = true;
            } else {
                if (be.progress > 0) {
                    be.progress = Math.max(0, be.progress - 2);
                    changed = true;
                }
                be.active = false;
            }
        } else {
            changed |= be.refreshSecondaryInputMode(null);
            changed |= be.setOutputFluidMode(be.outputFluidTank.getAmount() > 0);

            if (be.progress > 0) {
                be.progress = Math.max(0, be.progress - 2);
                changed = true;
            }

            if (be.manualEnergy > 0) {
                be.manualEnergy = Math.max(0, be.manualEnergy - 1);
                changed = true;
            }

            be.active = false;
        }

        if (wasActive != be.active) {
            level.setBlock(pos, state.setValue(ChemicalReactorBlock.ACTIVE, be.active), Block.UPDATE_CLIENTS);
            changed = true;
        }

        if (changed) {
            be.onContentsChanged();
        }
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

    public static boolean isSecondaryTransferContainer(ItemStack stack) {
        return isFluidContainer(stack) || isGasContainer(stack);
    }

    @Nullable
    private static GasType readGasType(ValueInput input, String key) {
        String raw = input.getStringOr(key, "");
        if (raw.isBlank()) {
            return null;
        }

        ResourceLocation id = ResourceLocation.tryParse(raw);
        return id == null ? null : ModGases.get(id);
    }

    private static int mapLoadedSlot(int savedSlot, boolean alreadyHybridSchema) {
        if (alreadyHybridSchema) {
            return savedSlot;
        }

        return switch (savedSlot) {
            case 0 -> SLOT_FUEL;
            case 1 -> SLOT_PRIMARY_GAS_TRANSFER;
            case 2, 3 -> SLOT_SECONDARY_TRANSFER;
            case 4 -> SLOT_GAS_OUTPUT_TRANSFER;
            case 5 -> SLOT_FLUID_OUTPUT_TRANSFER;
            default -> -1;
        };
    }

    private boolean runTransferWithoutReset(BooleanSupplier action) {
        this.suppressTransferModeReset = true;
        try {
            return action.getAsBoolean();
        } finally {
            this.suppressTransferModeReset = false;
        }
    }

    private boolean setOutputFluidMode(boolean mode) {
        if (this.outputFluidMode == mode) {
            return false;
        }

        this.outputFluidMode = mode;
        return true;
    }

    private boolean processSecondaryInputTransfer() {
        return this.secondaryFluidMode
                ? this.runTransferWithoutReset(() ->
                FluidTransferUtil.tryProcessTransferSlot(
                        this.itemHandler,
                        this.itemStacks,
                        SLOT_SECONDARY_TRANSFER,
                        this.secondaryFluidTank,
                        this.fluidInputLocks,
                        0
                )
        )
                : this.runTransferWithoutReset(() ->
                GasTransferUtil.tryProcessTransferSlot(
                        this.itemStacks,
                        SLOT_SECONDARY_TRANSFER,
                        this.secondaryGasTank,
                        this.gasInputLocks,
                        1
                )
        );
    }

    private boolean refreshSecondaryInputMode(@Nullable ChemicalReactorRecipe recipe) {
        boolean newSecondaryFluidMode = resolveSecondaryInputMode(
                recipe != null && recipe.secondaryFluid().isPresent(),
                recipe != null && recipe.secondaryGas().isPresent(),
                this.secondaryFluidTank,
                this.secondaryGasTank,
                this.itemStacks.get(SLOT_SECONDARY_TRANSFER)
        );

        if (newSecondaryFluidMode == this.secondaryFluidMode) {
            return false;
        }

        this.secondaryFluidMode = newSecondaryFluidMode;
        this.fluidInputLocks.reset(0);
        this.gasInputLocks.reset(1);
        return true;
    }

    private static boolean resolveSecondaryInputMode(
            boolean recipeWantsFluid,
            boolean recipeWantsGas,
            FluidTankAccess fluidTank,
            GasTank gasTank,
            ItemStack slotStack
    ) {
        if (!gasTank.isEmpty()) {
            return false;
        }

        if (fluidTank.getAmount() > 0) {
            return true;
        }

        if (recipeWantsFluid) {
            return true;
        }

        if (recipeWantsGas) {
            return false;
        }

        boolean slotHasFluidContainer = isFluidContainer(slotStack);
        boolean slotHasGasContainer = isGasContainer(slotStack);

        if (slotHasFluidContainer && !slotHasGasContainer) {
            return true;
        }

        if (slotHasGasContainer && !slotHasFluidContainer) {
            return false;
        }

        return false;
    }

    private void resetTransferLockForSlot(int slot) {
        switch (slot) {
            case SLOT_PRIMARY_GAS_TRANSFER -> this.gasInputLocks.reset(0);
            case SLOT_SECONDARY_TRANSFER -> {
                this.gasInputLocks.reset(1);
                this.fluidInputLocks.reset(0);
            }
            default -> {
            }
        }
    }

    private boolean handleFuel() {
        boolean changed = false;

        if (this.burnTimeRemaining > 0) {
            this.burnTimeRemaining--;
            changed = true;
        }

        if (this.burnTimeRemaining <= 0 && this.heat < 300) {
            MachineFuelUtil.FuelUse fuelUse = MachineFuelUtil.consumeOne(
                    this.itemStacks.get(SLOT_FUEL),
                    this.level,
                    CHARCOAL_FUEL
            );

            if (fuelUse.consumed()) {
                this.itemStacks.set(SLOT_FUEL, fuelUse.remainingStack());
                this.burnTimeTotal = fuelUse.burnTime();
                this.burnTimeRemaining = fuelUse.burnTime();
                changed = true;
            }
        }

        return changed;
    }

    private boolean updateHeat() {
        int oldHeat = this.heat;

        if (this.burnTimeRemaining > 0) {
            this.heat = Math.min(MAX_HEAT, this.heat + 2);
        } else {
            int cooling = this.active ? 2 : 1;
            this.heat = Math.max(0, this.heat - cooling);
        }

        return oldHeat != this.heat;
    }

    private Optional<RecipeHolder<ChemicalReactorRecipe>> findMatchingRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        ChemicalReactorRecipeInput input = new ChemicalReactorRecipeInput(
                this.primaryGasTank.getGasInTank(0),
                this.secondaryGasTank.getGasInTank(0),
                this.fluidStacks.get(SECONDARY_FLUID_TANK).copy()
        );

        return serverLevel.recipeAccess().getRecipeFor(
                ModRecipeTypes.CHEMICAL_REACTOR.get(),
                input,
                serverLevel
        );
    }

    private boolean canOutput(ChemicalReactorRecipe recipe) {
        if (recipe.outputKind() == ReactorOutputKind.GAS) {
            GasType outputType = ModGases.get(recipe.outputId());
            if (outputType == null) {
                return false;
            }

            GasStack existing = this.gasOutputTank.getGasInTank(0);
            if (existing.isEmpty()) {
                return recipe.outputAmount() <= GAS_TANK_CAPACITY;
            }

            GasType existingType = existing.type();
            if (existingType == null || !recipe.outputId().equals(existingType.id())) {
                return false;
            }

            return existing.amount() + recipe.outputAmount() <= GAS_TANK_CAPACITY;
        }

        Fluid outputFluid = BuiltInRegistries.FLUID.getValue(recipe.outputId());
        if (outputFluid == Fluids.EMPTY) {
            return false;
        }

        FluidStack existing = this.fluidStacks.get(OUTPUT_FLUID_TANK);
        if (existing.isEmpty()) {
            return recipe.outputAmount() <= FLUID_TANK_CAPACITY;
        }

        return existing.is(outputFluid) && existing.getAmount() + recipe.outputAmount() <= FLUID_TANK_CAPACITY;
    }

    private int getProgressPerTick(ChemicalReactorRecipe recipe) {
        int progressPerTick = 1;

        if (this.heat >= 800) {
            progressPerTick = 4;
        } else if (this.heat >= 500) {
            progressPerTick = 3;
        } else if (this.heat >= 200) {
            progressPerTick = 2;
        }

        if (this.heat < recipe.minHeat()) {
            progressPerTick = 1;
        }

        if (this.manualEnergy > 0) {
            progressPerTick += 1;
        }

        return progressPerTick;
    }

    private void processRecipe(ChemicalReactorRecipe recipe) {
        this.primaryGasTank.drain(recipe.primaryGas().amount(), false);
        recipe.secondaryGas().ifPresent(req -> this.secondaryGasTank.drain(req.amount(), false));
        recipe.secondaryFluid().ifPresent(req -> this.secondaryFluidTank.extract(req.amount(), false));

        if (recipe.outputKind() == ReactorOutputKind.GAS) {
            GasType outputType = ModGases.get(recipe.outputId());
            if (outputType != null) {
                this.gasOutputTank.fill(GasStack.of(outputType, recipe.outputAmount()), false);
            }
        } else {
            Fluid output = BuiltInRegistries.FLUID.getValue(recipe.outputId());
            if (output != Fluids.EMPTY) {
                this.outputFluidTank.insert(new FluidStack(output, recipe.outputAmount()), false);
            }
        }

        if (recipe.heatDrain() > 0) {
            this.heat = Math.max(0, this.heat - recipe.heatDrain());
        }
    }

    public void addManualEnergy(int amount) {
        int old = this.manualEnergy;
        this.manualEnergy = Math.min(MAX_MANUAL_ENERGY, this.manualEnergy + amount);
        if (old != this.manualEnergy) {
            this.onContentsChanged();
        }
    }

    public ItemStacksResourceHandler getItemHandler(@Nullable Direction side) {
        return this.itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidResourceHandler(@Nullable Direction side) {
        return this.fluidHandler;
    }

    public IGasHandler getGasHandler(@Nullable Direction side) {
        return this.automationGasHandler;
    }

    public ContainerData getData() {
        return this.data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("menu.mydrugs.chemical_reactor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new ChemicalReactorMenu(
                containerId,
                playerInventory,
                this.itemHandler,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition),
                this.worldPosition
        );
    }

    private void onContentsChanged() {
        MachineSync.syncAndInvalidateCaps(this);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < this.itemStacks.size(); i++) {
            this.itemStacks.set(i, ItemStack.EMPTY);
        }

        boolean alreadyHybridSchema = input.getIntOr("SlotSchema", 0) >= SLOT_SCHEMA_HYBRID_SECONDARY;

        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int savedSlot = child.getIntOr("slot", -1);
            int slot = mapLoadedSlot(savedSlot, alreadyHybridSchema);
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);

            if (slot >= 0 && slot < this.itemStacks.size() && this.itemStacks.get(slot).isEmpty()) {
                this.itemStacks.set(slot, stack);
            }
        }

        this.burnTimeRemaining = input.getIntOr("BurnTimeRemaining", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);
        this.heat = input.getIntOr("Heat", 0);
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 0);
        this.manualEnergy = input.getIntOr("ManualEnergy", 0);
        this.active = input.getBooleanOr("Active", false);
        this.secondaryFluidMode = input.getBooleanOr("SecondaryFluidMode", false);
        this.outputFluidMode = input.getBooleanOr("OutputFluidMode", true);

        GasType primaryGas = readGasType(input, "PrimaryGas");
        GasType secondaryGas = readGasType(input, "SecondaryGas");
        GasType outputGas = readGasType(input, "OutputGas");

        this.primaryGasTank.loadStored(primaryGas, input.getLongOr("PrimaryGasAmount", 0));
        this.secondaryGasTank.loadStored(secondaryGas, input.getLongOr("SecondaryGasAmount", 0));
        this.gasOutputTank.loadStored(outputGas, input.getLongOr("OutputGasAmount", 0));

        this.fluidStacks.set(
                SECONDARY_FLUID_TANK,
                input.read("SecondaryFluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY)
        );
        this.fluidStacks.set(
                OUTPUT_FLUID_TANK,
                input.read("OutputFluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY)
        );

        this.refreshSecondaryInputMode(null);
        this.gasInputLocks.resetAll();
        this.fluidInputLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("SlotSchema", SLOT_SCHEMA_HYBRID_SECONDARY);

        ValueOutput.ValueOutputList itemList = output.childrenList("items");
        for (int i = 0; i < this.itemStacks.size(); i++) {
            ItemStack stack = this.itemStacks.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            ValueOutput child = itemList.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.CODEC, stack);
        }

        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
        output.putInt("Heat", this.heat);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("ManualEnergy", this.manualEnergy);
        output.putBoolean("Active", this.active);
        output.putBoolean("SecondaryFluidMode", this.secondaryFluidMode);
        output.putBoolean("OutputFluidMode", this.outputFluidMode);

        GasType primaryGas = this.primaryGasTank.getGasType();
        GasType secondaryGas = this.secondaryGasTank.getGasType();
        GasType outputGas = this.gasOutputTank.getGasType();

        output.putString("PrimaryGas", primaryGas == null ? "" : primaryGas.id().toString());
        output.putLong("PrimaryGasAmount", this.primaryGasTank.getAmount());

        output.putString("SecondaryGas", secondaryGas == null ? "" : secondaryGas.id().toString());
        output.putLong("SecondaryGasAmount", this.secondaryGasTank.getAmount());

        output.putString("OutputGas", outputGas == null ? "" : outputGas.id().toString());
        output.putLong("OutputGasAmount", this.gasOutputTank.getAmount());

        output.store("SecondaryFluid", FluidStack.OPTIONAL_CODEC, this.fluidStacks.get(SECONDARY_FLUID_TANK));
        output.store("OutputFluid", FluidStack.OPTIONAL_CODEC, this.fluidStacks.get(OUTPUT_FLUID_TANK));
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class ReactorItemHandler extends ItemStacksResourceHandler {
        private ReactorItemHandler(int size) {
            super(size);
        }

        private NonNullList<ItemStack> list() {
            return this.stacks;
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousStack) {
            if (!suppressTransferModeReset) {
                resetTransferLockForSlot(slot);
            }
            ChemicalReactorBlockEntity.this.onContentsChanged();
        }
    }

    private final class ReactorFluidHandler extends FluidStacksResourceHandler {
        private ReactorFluidHandler() {
            super(2, FLUID_TANK_CAPACITY);
        }

        private NonNullList<FluidStack> list() {
            return this.stacks;
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return index == SECONDARY_FLUID_TANK && !resource.isEmpty();
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (index != SECONDARY_FLUID_TANK) {
                return 0;
            }
            return super.insert(index, resource, amount, transaction);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            if (index != OUTPUT_FLUID_TANK) {
                return 0;
            }
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousStack) {
            ChemicalReactorBlockEntity.this.onContentsChanged();
        }
    }
}