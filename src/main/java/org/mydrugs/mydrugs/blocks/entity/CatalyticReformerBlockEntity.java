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
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.GasTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.CatalyticReformerMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerFluidStack;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerGasStack;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerRecipe;
import org.mydrugs.mydrugs.recipes.catalytic_reformer.CatalyticReformerRecipeInput;

import java.util.Optional;

public class CatalyticReformerBlockEntity extends BaseContainerBlockEntity implements CatalyticReformerMenu.CatalyticReformerButtonHandler {
    public static final int FLUID_CAPACITY = 4000;
    public static final int GAS_CAPACITY = 4000;

    private final LockedTransferSlots fluidInputLocks = new LockedTransferSlots(2);
    private final LockedTransferSlots gasInputLocks = new LockedTransferSlots(2);

    private final StoredFluidTank input1FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank input2FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);

    private final StoredFluidTank output1FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output2FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output3FluidTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);

    private final GasTank input1GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank input2GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);

    private final GasTank output1GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output2GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output3GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);

    private boolean input1GasMode;
    private boolean input2GasMode;
    private boolean output1GasMode;
    private boolean output2GasMode;
    private boolean output3GasMode;

    private NonNullList<ItemStack> items = NonNullList.withSize(CatalyticReformerMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);

    private int progress = 0;
    private int maxProgress = 200;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> input1GasMode ? (int) input1GasTank.getAmount() : input1FluidTank.getAmount();
                case 1 -> input2GasMode ? (int) input2GasTank.getAmount() : input2FluidTank.getAmount();
                case 2 -> output1GasMode ? (int) output1GasTank.getAmount() : output1FluidTank.getAmount();
                case 3 -> output2GasMode ? (int) output2GasTank.getAmount() : output2FluidTank.getAmount();
                case 4 -> output3GasMode ? (int) output3GasTank.getAmount() : output3FluidTank.getAmount();

                case 5 -> progress;
                case 6 -> maxProgress;

                case 7 -> input1FluidTank.encodeFluidSyncId();
                case 8 -> input2FluidTank.encodeFluidSyncId();
                case 9 -> output1FluidTank.encodeFluidSyncId();
                case 10 -> output2FluidTank.encodeFluidSyncId();
                case 11 -> output3FluidTank.encodeFluidSyncId();

                case 12 -> input1GasTank.isEmpty() ? -1 : ModGases.getSyncId(input1GasTank.getGasType());
                case 13 -> input2GasTank.isEmpty() ? -1 : ModGases.getSyncId(input2GasTank.getGasType());
                case 14 -> output1GasTank.isEmpty() ? -1 : ModGases.getSyncId(output1GasTank.getGasType());
                case 15 -> output2GasTank.isEmpty() ? -1 : ModGases.getSyncId(output2GasTank.getGasType());
                case 16 -> output3GasTank.isEmpty() ? -1 : ModGases.getSyncId(output3GasTank.getGasType());

                case 17 -> input1GasMode ? 1 : 0;
                case 18 -> input2GasMode ? 1 : 0;
                case 19 -> output1GasMode ? 1 : 0;
                case 20 -> output2GasMode ? 1 : 0;
                case 21 -> output3GasMode ? 1 : 0;

                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 5 -> progress = value;
                case 6 -> maxProgress = value;
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return CatalyticReformerMenu.DATA_COUNT;
        }
    };

    public CatalyticReformerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CATALYTIC_REFORMER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CatalyticReformerBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;

        Optional<RecipeHolder<CatalyticReformerRecipe>> recipeHolder = be.getCurrentRecipe((ServerLevel) level);
        CatalyticReformerRecipe recipe = recipeHolder.map(RecipeHolder::value).orElse(null);

        if (be.refreshModes(recipe)) {
            changed = true;
        }

        if (be.processInputTransfer(0, CatalyticReformerMenu.INPUT_1_TRANSFER_SLOT, be.input1GasMode, be.input1FluidTank, be.input1GasTank)) {
            changed = true;
        }

        if (be.processInputTransfer(1, CatalyticReformerMenu.INPUT_2_TRANSFER_SLOT, be.input2GasMode, be.input2FluidTank, be.input2GasTank)) {
            changed = true;
        }

        recipeHolder = be.getCurrentRecipe((ServerLevel) level);
        recipe = recipeHolder.map(RecipeHolder::value).orElse(null);

        if (be.refreshModes(recipe)) {
            changed = true;
        }

        if (be.processOutputTransfer(CatalyticReformerMenu.OUTPUT_1_TRANSFER_SLOT, be.output1GasMode, be.output1FluidTank, be.output1GasTank)) {
            changed = true;
        }

        if (be.processOutputTransfer(CatalyticReformerMenu.OUTPUT_2_TRANSFER_SLOT, be.output2GasMode, be.output2FluidTank, be.output2GasTank)) {
            changed = true;
        }

        if (be.processOutputTransfer(CatalyticReformerMenu.OUTPUT_3_TRANSFER_SLOT, be.output3GasMode, be.output3FluidTank, be.output3GasTank)) {
            changed = true;
        }

        if (recipe == null) {
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

        if (!be.canCraft(recipe)) {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        be.progress++;
        changed = true;

        if (be.progress >= be.maxProgress) {
            be.craft(recipe);
            be.progress = 0;
            be.refreshModes(null);
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    private boolean processInputTransfer(int lockIndex, int slot, boolean gasMode, StoredFluidTank fluidTank, GasTank gasTank) {
        return gasMode
                ? GasTransferUtil.tryProcessTransferSlot(this.items, slot, gasTank, this.gasInputLocks, lockIndex)
                : FluidTransferUtil.tryProcessTransferSlot(this, slot, fluidTank, this.fluidInputLocks, lockIndex);
    }

    private boolean processOutputTransfer(int slot, boolean gasMode, StoredFluidTank fluidTank, GasTank gasTank) {
        return gasMode
                ? GasTransferUtil.tryFillOutputSlot(this.items, slot, gasTank)
                : FluidTransferUtil.tryFillOutputSlot(this, slot, fluidTank);
    }

    private Optional<RecipeHolder<CatalyticReformerRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.CATALYTIC_REFORMER.get(),
                new CatalyticReformerRecipeInput(
                        currentFluidStack(this.input1FluidTank),
                        this.input1GasTank.getGasInTank(0),
                        currentFluidStack(this.input2FluidTank),
                        this.input2GasTank.getGasInTank(0),
                        this.getItem(CatalyticReformerMenu.CATALYST_SLOT)
                ),
                level
        );
    }

    private static FluidStack currentFluidStack(StoredFluidTank tank) {
        ResourceLocation id = tank.getFluidId();
        if (id == null || tank.isEmpty()) {
            return FluidStack.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }

        return new FluidStack(fluid, tank.getAmount());
    }

    private boolean refreshModes(@Nullable CatalyticReformerRecipe recipe) {
        boolean newInput1GasMode = resolveInputMode(
                recipe != null && recipe.inputGas1().isPresent(),
                this.input1FluidTank,
                this.input1GasTank,
                this.getItem(CatalyticReformerMenu.INPUT_1_TRANSFER_SLOT)
        );

        boolean newInput2GasMode = resolveInputMode(
                recipe != null && recipe.inputGas2().isPresent(),
                this.input2FluidTank,
                this.input2GasTank,
                this.getItem(CatalyticReformerMenu.INPUT_2_TRANSFER_SLOT)
        );

        boolean newOutput1GasMode = resolveOutputMode(
                recipe != null && recipe.outputGas1().isPresent(),
                this.output1FluidTank,
                this.output1GasTank
        );

        boolean newOutput2GasMode = resolveOutputMode(
                recipe != null && recipe.outputGas2().isPresent(),
                this.output2FluidTank,
                this.output2GasTank
        );

        boolean newOutput3GasMode = resolveOutputMode(
                recipe != null && recipe.outputGas3().isPresent(),
                this.output3FluidTank,
                this.output3GasTank
        );

        boolean changed = false;

        if (newInput1GasMode != this.input1GasMode) {
            this.input1GasMode = newInput1GasMode;
            this.fluidInputLocks.reset(0);
            this.gasInputLocks.reset(0);
            changed = true;
        }

        if (newInput2GasMode != this.input2GasMode) {
            this.input2GasMode = newInput2GasMode;
            this.fluidInputLocks.reset(1);
            this.gasInputLocks.reset(1);
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

        return changed;
    }

    private static boolean resolveInputMode(boolean recipeWantsGas, StoredFluidTank fluidTank, GasTank gasTank, ItemStack slotStack) {
        if (!gasTank.isEmpty()) {
            return true;
        }

        if (!fluidTank.isEmpty()) {
            return false;
        }

        if (recipeWantsGas) {
            return true;
        }

        if (isGasContainer(slotStack) && !isFluidContainer(slotStack)) {
            return true;
        }

        return false;
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

    private static FluidStack toFluidStack(ResourceLocation fluidId, int amount) {
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId);
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }

        return new FluidStack(fluid, amount);
    }

    private static GasStack toGasStack(ResourceLocation gasId, long amount) {
        if (amount <= 0) {
            return GasStack.EMPTY;
        }

        GasType gas = ModGases.get(gasId);
        return GasStack.of(gas, amount);
    }

    private static @Nullable GasType readGasType(ValueInput input, String key) {
        String raw = input.getStringOr(key, "");
        if (raw.isBlank()) {
            return null;
        }
        return ModGases.getNullable(raw);
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
            if (dumpGasTank(gasTank)) {
                return true;
            }
            return fluidTank.dump();
        }

        if (fluidTank.dump()) {
            return true;
        }

        return dumpGasTank(gasTank);
    }

    private boolean hasFluidInput(CatalyticReformerFluidStack required, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!gasTank.isEmpty()) {
            return false;
        }

        ResourceLocation id = fluidTank.getFluidId();
        return id != null && id.equals(required.fluid()) && fluidTank.getAmount() >= required.amount();
    }

    private boolean hasGasInput(CatalyticReformerGasStack required, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!fluidTank.isEmpty()) {
            return false;
        }

        GasStack stack = gasTank.getGasInTank(0);
        return !stack.isEmpty()
                && stack.type() != null
                && stack.type().id().equals(required.gas())
                && stack.amount() >= required.amount();
    }

    private boolean canAcceptFluidOutput(CatalyticReformerFluidStack output, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!gasTank.isEmpty()) {
            return false;
        }

        FluidStack stack = toFluidStack(output.fluid(), output.amount());
        return !stack.isEmpty() && fluidTank.getAddableAmount(stack) >= stack.getAmount();
    }

    private boolean canAcceptGasOutput(CatalyticReformerGasStack output, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!fluidTank.isEmpty()) {
            return false;
        }
        if (output == null) return true;

        GasStack stack = toGasStack(output.gas(), output.amount());
        return !stack.isEmpty() && gasTank.fill(stack, true) >= output.amount();
    }

    private boolean canCraft(CatalyticReformerRecipe recipe) {
        if (!recipe.catalyst().test(this.getItem(CatalyticReformerMenu.CATALYST_SLOT))) {
            return false;
        }

        if (recipe.inputFluid1().isPresent()) {
            if (!hasFluidInput(recipe.inputFluid1().get(), this.input1FluidTank, this.input1GasTank)) {
                return false;
            }
        } else if (!hasGasInput(recipe.inputGas1().orElseThrow(), this.input1FluidTank, this.input1GasTank)) {
            return false;
        }

        if (recipe.inputFluid2().isPresent()) {
            if (!hasFluidInput(recipe.inputFluid2().get(), this.input2FluidTank, this.input2GasTank)) {
                return false;
            }
        } else if (!hasGasInput(recipe.inputGas2().orElseThrow(), this.input2FluidTank, this.input2GasTank)) {
            return false;
        }

        if (recipe.outputFluid1().isPresent()) {
            if (!canAcceptFluidOutput(recipe.outputFluid1().get(), this.output1FluidTank, this.output1GasTank)) {
                return false;
            }
        } else if (!canAcceptGasOutput(recipe.outputGas1().orElseThrow(), this.output1FluidTank, this.output1GasTank)) {
            return false;
        }

        if (recipe.outputFluid2().isPresent()) {
            if (!canAcceptFluidOutput(recipe.outputFluid2().get(), this.output2FluidTank, this.output2GasTank)) {
                return false;
            }
        } else if (!canAcceptGasOutput(recipe.outputGas2().orElse(null), this.output2FluidTank, this.output2GasTank)) {
            return false;
        }

        if (recipe.outputFluid3().isPresent()) {
            if (!canAcceptFluidOutput(recipe.outputFluid3().get(), this.output3FluidTank, this.output3GasTank)) {
                return false;
            }
        } else if (!canAcceptGasOutput(recipe.outputGas3().orElse(null), this.output3FluidTank, this.output3GasTank)) {
            return false;
        }

        return true;
    }

    private void insertFluidOutput(CatalyticReformerFluidStack output, StoredFluidTank tank) {
        FluidStack stack = toFluidStack(output.fluid(), output.amount());
        if (!stack.isEmpty()) {
            tank.insert(stack, false);
        }
    }

    private void insertGasOutput(CatalyticReformerGasStack output, GasTank tank) {
        GasStack stack = toGasStack(output.gas(), output.amount());
        if (!stack.isEmpty()) {
            tank.fill(stack, false);
        }
    }

    private void craft(CatalyticReformerRecipe recipe) {
        recipe.inputFluid1().ifPresent(req -> this.input1FluidTank.extract(req.amount(), false));
        recipe.inputGas1().ifPresent(req -> this.input1GasTank.drain(req.amount(), false));

        recipe.inputFluid2().ifPresent(req -> this.input2FluidTank.extract(req.amount(), false));
        recipe.inputGas2().ifPresent(req -> this.input2GasTank.drain(req.amount(), false));

        if (recipe.consumeCatalyst()) {
            ItemStack catalyst = this.getItem(CatalyticReformerMenu.CATALYST_SLOT);
            catalyst.shrink(1);
            if (catalyst.isEmpty()) {
                this.setItem(CatalyticReformerMenu.CATALYST_SLOT, ItemStack.EMPTY);
            }
        }

        recipe.outputFluid1().ifPresent(output -> insertFluidOutput(output, this.output1FluidTank));
        recipe.outputGas1().ifPresent(output -> insertGasOutput(output, this.output1GasTank));

        recipe.outputFluid2().ifPresent(output -> insertFluidOutput(output, this.output2FluidTank));
        recipe.outputGas2().ifPresent(output -> insertGasOutput(output, this.output2GasTank));

        recipe.outputFluid3().ifPresent(output -> insertFluidOutput(output, this.output3FluidTank));
        recipe.outputGas3().ifPresent(output -> insertGasOutput(output, this.output3GasTank));
    }

    private void sync() {
        MachineSync.sync(this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.catalytic_reformer");
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
        return new CatalyticReformerMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return CatalyticReformerMenu.MACHINE_SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case CatalyticReformerMenu.INPUT_1_TRANSFER_SLOT,
                 CatalyticReformerMenu.INPUT_2_TRANSFER_SLOT,
                 CatalyticReformerMenu.OUTPUT_1_TRANSFER_SLOT,
                 CatalyticReformerMenu.OUTPUT_2_TRANSFER_SLOT,
                 CatalyticReformerMenu.OUTPUT_3_TRANSFER_SLOT -> isFluidContainer(stack) || isGasContainer(stack);
            case CatalyticReformerMenu.CATALYST_SLOT -> !stack.isEmpty() && !isFluidContainer(stack) && !isGasContainer(stack);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (slot == CatalyticReformerMenu.INPUT_1_TRANSFER_SLOT) {
            this.fluidInputLocks.reset(0);
            this.gasInputLocks.reset(0);
        } else if (slot == CatalyticReformerMenu.INPUT_2_TRANSFER_SLOT) {
            this.fluidInputLocks.reset(1);
            this.gasInputLocks.reset(1);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.input1FluidTank.load(input, "InputFluid1");
        this.input2FluidTank.load(input, "InputFluid2");

        this.output1FluidTank.load(input, "OutputFluid1");
        this.output2FluidTank.load(input, "OutputFluid2");
        this.output3FluidTank.load(input, "OutputFluid3");

        this.input1GasTank.loadStored(readGasType(input, "InputGas1"), input.getLongOr("InputGasAmount1", 0));
        this.input2GasTank.loadStored(readGasType(input, "InputGas2"), input.getLongOr("InputGasAmount2", 0));

        this.output1GasTank.loadStored(readGasType(input, "OutputGas1"), input.getLongOr("OutputGasAmount1", 0));
        this.output2GasTank.loadStored(readGasType(input, "OutputGas2"), input.getLongOr("OutputGasAmount2", 0));
        this.output3GasTank.loadStored(readGasType(input, "OutputGas3"), input.getLongOr("OutputGasAmount3", 0));

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);

        this.input1GasMode = input.getBooleanOr("Input1GasMode", false);
        this.input2GasMode = input.getBooleanOr("Input2GasMode", false);
        this.output1GasMode = input.getBooleanOr("Output1GasMode", false);
        this.output2GasMode = input.getBooleanOr("Output2GasMode", false);
        this.output3GasMode = input.getBooleanOr("Output3GasMode", false);

        this.refreshModes(null);
        this.fluidInputLocks.resetAll();
        this.gasInputLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        this.input1FluidTank.save(output, "InputFluid1");
        this.input2FluidTank.save(output, "InputFluid2");

        this.output1FluidTank.save(output, "OutputFluid1");
        this.output2FluidTank.save(output, "OutputFluid2");
        this.output3FluidTank.save(output, "OutputFluid3");

        GasType in1 = this.input1GasTank.getGasType();
        GasType in2 = this.input2GasTank.getGasType();
        GasType out1 = this.output1GasTank.getGasType();
        GasType out2 = this.output2GasTank.getGasType();
        GasType out3 = this.output3GasTank.getGasType();

        output.putString("InputGas1", in1 == null ? "" : in1.id().toString());
        output.putLong("InputGasAmount1", this.input1GasTank.getAmount());

        output.putString("InputGas2", in2 == null ? "" : in2.id().toString());
        output.putLong("InputGasAmount2", this.input2GasTank.getAmount());

        output.putString("OutputGas1", out1 == null ? "" : out1.id().toString());
        output.putLong("OutputGasAmount1", this.output1GasTank.getAmount());

        output.putString("OutputGas2", out2 == null ? "" : out2.id().toString());
        output.putLong("OutputGasAmount2", this.output2GasTank.getAmount());

        output.putString("OutputGas3", out3 == null ? "" : out3.id().toString());
        output.putLong("OutputGasAmount3", this.output3GasTank.getAmount());

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);

        output.putBoolean("Input1GasMode", this.input1GasMode);
        output.putBoolean("Input2GasMode", this.input2GasMode);
        output.putBoolean("Output1GasMode", this.output1GasMode);
        output.putBoolean("Output2GasMode", this.output2GasMode);
        output.putBoolean("Output3GasMode", this.output3GasMode);
    }

    @Override
    public boolean onCatalyticReformerButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case CatalyticReformerMenu.DUMP_INPUT_1_BUTTON_ID -> {
                boolean dumped = dumpMixedTank(this.input1GasMode, this.input1FluidTank, this.input1GasTank);
                if (dumped) {
                    this.progress = 0;
                    this.refreshModes(null);
                    sync();
                }
                yield dumped;
            }
            case CatalyticReformerMenu.DUMP_INPUT_2_BUTTON_ID -> {
                boolean dumped = dumpMixedTank(this.input2GasMode, this.input2FluidTank, this.input2GasTank);
                if (dumped) {
                    this.progress = 0;
                    this.refreshModes(null);
                    sync();
                }
                yield dumped;
            }
            case CatalyticReformerMenu.DUMP_OUTPUT_1_BUTTON_ID -> {
                boolean dumped = dumpMixedTank(this.output1GasMode, this.output1FluidTank, this.output1GasTank);
                if (dumped) {
                    this.refreshModes(null);
                    sync();
                }
                yield dumped;
            }
            case CatalyticReformerMenu.DUMP_OUTPUT_2_BUTTON_ID -> {
                boolean dumped = dumpMixedTank(this.output2GasMode, this.output2FluidTank, this.output2GasTank);
                if (dumped) {
                    this.refreshModes(null);
                    sync();
                }
                yield dumped;
            }
            case CatalyticReformerMenu.DUMP_OUTPUT_3_BUTTON_ID -> {
                boolean dumped = dumpMixedTank(this.output3GasMode, this.output3FluidTank, this.output3GasTank);
                if (dumped) {
                    this.refreshModes(null);
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(this,
                input1FluidTank, input2FluidTank,
                output1FluidTank, output2FluidTank, output3FluidTank
        );
    }

    public IGasHandler getGasHandler(Direction side) {
        return new CompositeGasHandler(
                input1GasTank, input2GasTank,
                output1GasTank, output2GasTank, output3GasTank
        );
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
