package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.AdvancedMixingVatBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.machine.MachineStorage;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.FluidTankAccess;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.GasTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.machine.transfer.TransferLockSuppressor;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipeInput;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatFluidStack;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvancedMixingVatBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements net.minecraft.world.MenuProvider, MachineStatusProvider {
    public static final int RECIPE_ITEM_SLOT_COUNT = 4;

    public static final int SLOT_RECIPE_0 = 0;
    public static final int SLOT_RECIPE_1 = 1;
    public static final int SLOT_RECIPE_2 = 2;
    public static final int SLOT_RECIPE_3 = 3;

    public static final int SLOT_TANK_INPUT_A = 4;
    public static final int SLOT_TANK_INPUT_B = 5;
    public static final int SLOT_TANK_INPUT_C = 6;
    public static final int SLOT_TANK_OUTPUT = 7;
    public static final int SLOT_GAS_TRANSFER = 8;

    public static final int ITEM_SLOT_COUNT = 9;

    public static final int INPUT_TANK_CAPACITY = 4000;
    public static final int OUTPUT_TANK_CAPACITY = 4000;
    public static final long GAS_TANK_CAPACITY = 4000L;

    public static final int DATA_COUNT = 13;

    private final VatItemHandler itemHandler = new VatItemHandler(ITEM_SLOT_COUNT);
    private final NonNullList<ItemStack> itemStacks = this.itemHandler.list();

    private final VatInputFluidHandler inputAHandler = new VatInputFluidHandler(INPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> inputAStacks = this.inputAHandler.list();
    private final FluidTankAccess inputATank = FluidTankAccess.of(this.inputAStacks, 0, INPUT_TANK_CAPACITY);
    private final VatInputFluidHandler inputBHandler = new VatInputFluidHandler(INPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> inputBStacks = this.inputBHandler.list();
    private final FluidTankAccess inputBTank = FluidTankAccess.of(this.inputBStacks, 0, INPUT_TANK_CAPACITY);
    private final VatInputFluidHandler inputCHandler = new VatInputFluidHandler(INPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> inputCStacks = this.inputCHandler.list();
    private final FluidTankAccess inputCTank = FluidTankAccess.of(this.inputCStacks, 0, INPUT_TANK_CAPACITY);
    private final VatOutputFluidHandler outputHandler = new VatOutputFluidHandler(OUTPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> outputStacks = this.outputHandler.list();
    private final FluidTankAccess outputTank = FluidTankAccess.of(this.outputStacks, 0, OUTPUT_TANK_CAPACITY);
    private final GasTank gasTank = new GasTank(
            GAS_TANK_CAPACITY,
            gasType -> true,
            this::onMachineChanged
    );
    private final LockedTransferSlots fluidInputLocks = new LockedTransferSlots(3);
    private final LockedTransferSlots gasTransferLocks = new LockedTransferSlots(1);
    private boolean suppressTransferModeReset = false;
    private int progress = 0;
    private int maxProgress = 100;
    private boolean hasRecipe = false;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputAStacks.get(0).getAmount();
                case 1 -> inputBStacks.get(0).getAmount();
                case 2 -> inputCStacks.get(0).getAmount();
                case 3 -> outputStacks.get(0).getAmount();
                case 4 -> (int) Math.min(Integer.MAX_VALUE, gasTank.getAmount());
                case 5 -> progress;
                case 6 -> maxProgress;
                case 7 ->
                        inputAStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputAStacks.get(0).getFluid());
                case 8 ->
                        inputBStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputBStacks.get(0).getFluid());
                case 9 ->
                        inputCStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputCStacks.get(0).getFluid());
                case 10 ->
                        outputStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(outputStacks.get(0).getFluid());
                case 11 -> gasTank.isEmpty() ? -1 : ModGases.getSyncId(gasTank.getGasType());
                case 12 -> hasRecipe ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 5 -> progress = value;
                case 6 -> maxProgress = value;
            }
        }

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public AdvancedMixingVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_MIXING_VAT_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedMixingVatBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;

        changed |= be.runTransferWithoutReset(() ->
                FluidTransferUtil.tryProcessTransferSlot(
                        be.itemHandler,
                        be.itemStacks,
                        SLOT_TANK_INPUT_A,
                        be.inputATank,
                        be.fluidInputLocks,
                        0
                )
        );

        changed |= be.runTransferWithoutReset(() ->
                FluidTransferUtil.tryProcessTransferSlot(
                        be.itemHandler,
                        be.itemStacks,
                        SLOT_TANK_INPUT_B,
                        be.inputBTank,
                        be.fluidInputLocks,
                        1
                )
        );

        changed |= be.runTransferWithoutReset(() ->
                FluidTransferUtil.tryProcessTransferSlot(
                        be.itemHandler,
                        be.itemStacks,
                        SLOT_TANK_INPUT_C,
                        be.inputCTank,
                        be.fluidInputLocks,
                        2
                )
        );

        changed |= be.runTransferWithoutReset(() ->
                GasTransferUtil.tryProcessTransferSlot(
                        be.itemStacks,
                        SLOT_GAS_TRANSFER,
                        be.gasTank,
                        be.gasTransferLocks,
                        0
                )
        );

        changed |= FluidTransferUtil.tryFillOutputSlot(
                be.itemHandler,
                be.itemStacks,
                SLOT_TANK_OUTPUT,
                be.outputTank
        );

        // Keep your current recipe logic here unchanged.
        // ---------------------------------------------
        Optional<ResolvedRecipe> match = be.findMatchingRecipe();

        if (match.isEmpty()) {
            changed |= be.setMachineStatus(MachineStatus.NO_MATCHING_RECIPE);
            if (be.progress != 0 || be.hasRecipe) {
                be.progress = 0;
                be.hasRecipe = false;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        ResolvedRecipe recipe = match.get();

        if (!be.canAcceptOutput(recipe.result())) {
            changed |= be.setMachineStatus(MachineStatus.OUTPUT_TANK_FULL);
            if (be.progress != 0 || be.hasRecipe) {
                be.progress = 0;
                be.hasRecipe = false;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        be.hasRecipe = true;
        changed |= be.setMachineStatus(MachineStatus.RUNNING);

        int oldMaxProgress = be.maxProgress;
        be.maxProgress = recipe.processingTime();
        if (be.maxProgress != oldMaxProgress) {
            changed = true;
        }

        be.progress++;
        changed = true;

        if (be.progress >= be.maxProgress) {
            Optional<ResolvedRecipe> verify = be.findMatchingRecipe();
            if (verify.isPresent() && be.canAcceptOutput(verify.get().result())) {
                be.finishResolvedRecipe(verify.get());
            }
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    private void finishResolvedRecipe(ResolvedRecipe recipe) {
        if (recipe.isAdvanced()) {
            finishAdvancedRecipe(recipe.advancedRecipe(), recipe.result());
        } else {
            finishMixingVatRecipe(recipe.mixingRecipe(), recipe.mixingFluidAssignment(), recipe.result());
        }
    }

    private void finishAdvancedRecipe(AdvancedMixingVatRecipe recipe, FluidStack result) {
        AdvancedMixingVatRecipe.consumeItems(recipe.itemInputs(), this.itemStacks);
        AdvancedMixingVatRecipe.consumeFluids(recipe.fluidInputs(), this.inputAStacks, this.inputBStacks, this.inputCStacks);

        if (recipe.gasInput() != null) {
            this.gasTank.drain(recipe.gasInput().amount(), false);
        }

        addToOutputTank(result);
    }

    private void addToOutputTank(FluidStack result) {
        FluidStack stored = this.outputStacks.get(0);
        if (stored.isEmpty()) {
            this.outputStacks.set(0, result.copy());
        } else {
            this.outputStacks.set(0, stored.copyWithAmount(stored.getAmount() + result.getAmount()));
        }
    }

    private void finishMixingVatRecipe(MixingVatRecipe recipe, int[] tankIndices, FluidStack result) {
        consumeMixingVatItems(recipe.requiredItems());
        consumeMixingVatFluids(recipe.requiredFluids(), tankIndices);

        FluidStack stored = this.outputStacks.get(0);
        if (stored.isEmpty()) {
            this.outputStacks.set(0, result.copy());
        } else {
            this.outputStacks.set(0, stored.copyWithAmount(stored.getAmount() + result.getAmount()));
        }
    }

    private void consumeMixingVatItems(List<Ingredient> requirements) {
        for (Ingredient ingredient : requirements) {
            for (int i = 0; i < RECIPE_ITEM_SLOT_COUNT; i++) {
                ItemStack stack = this.itemStacks.get(i);
                if (stack.isEmpty()) {
                    continue;
                }

                if (ingredient.test(stack)) {
                    stack.shrink(1);
                    if (stack.isEmpty()) {
                        this.itemStacks.set(i, ItemStack.EMPTY);
                    }
                    break;
                }
            }
        }
    }

    private void consumeMixingVatFluids(List<MixingVatFluidStack> requirements, int[] tankIndices) {
        if (requirements.isEmpty()) {
            return;
        }

        FluidStack[] available = new FluidStack[tankIndices.length];
        for (int i = 0; i < tankIndices.length; i++) {
            available[i] = getInputTank(tankIndices[i]);
        }

        int[] assignment = new int[requirements.size()];
        for (int i = 0; i < assignment.length; i++) {
            assignment[i] = -1;
        }

        boolean[] used = new boolean[available.length];

        if (!assignMixingFluidRequirements(requirements, available, 0, used, assignment)) {
            return;
        }

        for (int i = 0; i < requirements.size(); i++) {
            NonNullList<FluidStack> tank = getInputTankList(tankIndices[assignment[i]]);
            shrinkFluid(tank, requirements.get(i).amount());
        }
    }

    private NonNullList<FluidStack> getInputTankList(int tankIndex) {
        return switch (tankIndex) {
            case 0 -> this.inputAStacks;
            case 1 -> this.inputBStacks;
            case 2 -> this.inputCStacks;
            default -> throw new IllegalArgumentException("Invalid tank index: " + tankIndex);
        };
    }

    private boolean assignMixingFluidRequirements(
            List<MixingVatFluidStack> requirements,
            FluidStack[] available,
            int requirementIndex,
            boolean[] used,
            int[] assignment
    ) {
        if (requirementIndex >= requirements.size()) {
            return true;
        }

        MixingVatFluidStack requirement = requirements.get(requirementIndex);

        for (int i = 0; i < available.length; i++) {
            if (used[i]) {
                continue;
            }

            FluidStack present = available[i];
            if (present.isEmpty()) {
                continue;
            }

            var presentId = BuiltInRegistries.FLUID.getKey(present.getFluid());
            if (presentId == null) {
                continue;
            }

            if (!requirement.fluid().equals(presentId)) {
                continue;
            }

            if (present.getAmount() < requirement.amount()) {
                continue;
            }

            used[i] = true;
            assignment[requirementIndex] = i;

            if (assignMixingFluidRequirements(requirements, available, requirementIndex + 1, used, assignment)) {
                return true;
            }

            used[i] = false;
            assignment[requirementIndex] = -1;
        }

        return false;
    }

    private void shrinkFluid(NonNullList<FluidStack> tank, int amount) {
        FluidStack current = tank.get(0);
        if (current.isEmpty() || amount <= 0) {
            return;
        }

        int remaining = current.getAmount() - amount;
        if (remaining <= 0) {
            tank.set(0, FluidStack.EMPTY);
        } else {
            tank.set(0, current.copyWithAmount(remaining));
        }
    }

    private FluidStack getInputTank(int tankIndex) {
        return switch (tankIndex) {
            case 0 -> this.inputAStacks.get(0);
            case 1 -> this.inputBStacks.get(0);
            case 2 -> this.inputCStacks.get(0);
            default -> FluidStack.EMPTY;
        };
    }

    private boolean canAcceptOutput(FluidStack result) {
        if (result.isEmpty()) {
            return false;
        }

        FluidStack stored = this.outputStacks.get(0);
        if (stored.isEmpty()) {
            return result.getAmount() <= OUTPUT_TANK_CAPACITY;
        }

        return FluidStack.isSameFluidSameComponents(stored, result)
                && stored.getAmount() + result.getAmount() <= OUTPUT_TANK_CAPACITY;
    }

    private boolean runTransferWithoutReset(java.util.function.BooleanSupplier action) {
        return TransferLockSuppressor.run(value -> this.suppressTransferModeReset = value, action);
    }

    private void resetTransferLockForSlot(int slot) {
        switch (slot) {
            case SLOT_TANK_INPUT_A -> this.fluidInputLocks.reset(0);
            case SLOT_TANK_INPUT_B -> this.fluidInputLocks.reset(1);
            case SLOT_TANK_INPUT_C -> this.fluidInputLocks.reset(2);
            case SLOT_GAS_TRANSFER -> this.gasTransferLocks.reset(0);
        }
    }

    private void onMachineChanged() {
        MachineSync.syncAndInvalidateCaps(this);
    }

    private void sync() {
        MachineSync.sync(this);
    }

    public ItemStacksResourceHandler getMenuItemHandler() {
        return this.itemHandler;
    }

    public ContainerData getData() {
        return this.data;
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

    public ResourceHandler<ItemResource> getItemCapability(@Nullable Direction side) {
        return this.itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidCapability(@Nullable Direction side) {
        if (side == null) {
            return null;
        }

        Direction front = this.getBlockState().getValue(AdvancedMixingVatBlock.FACING);
        Direction left = front.getCounterClockWise();
        Direction right = front.getClockWise();

        if (side == left) return this.inputAHandler;
        if (side == right) return this.inputBHandler;
        if (side == Direction.UP) return this.inputCHandler;
        if (side == front) return this.outputHandler;

        return null;
    }

    public IGasHandler getGasCapability(@Nullable Direction side) {
        if (side == null) {
            return null;
        }

        Direction front = this.getBlockState().getValue(AdvancedMixingVatBlock.FACING);
        Direction back = front.getOpposite();
        return side == back ? this.gasTank : null;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mydrugs.advanced_mixing_vat");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new AdvancedMixingVatMenu(
                containerId,
                playerInventory,
                this.itemHandler,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        MachineStorage.saveItemStacks(output, "items", this.itemStacks);

        output.store("input_a", FluidStack.OPTIONAL_CODEC, this.inputAStacks.get(0));
        output.store("input_b", FluidStack.OPTIONAL_CODEC, this.inputBStacks.get(0));
        output.store("input_c", FluidStack.OPTIONAL_CODEC, this.inputCStacks.get(0));
        output.store("output", FluidStack.OPTIONAL_CODEC, this.outputStacks.get(0));

        if (!this.gasTank.isEmpty()) {
            output.putInt("gas", ModGases.getSyncId(this.gasTank.getGasType()));
            output.putLong("gas_amount", this.gasTank.getAmount());
        }

        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
        output.putBoolean("has_recipe", this.hasRecipe);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        MachineStorage.loadItemStacks(input, "items", this.itemStacks);

        this.inputAStacks.set(0, input.read("input_a", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
        this.inputBStacks.set(0, input.read("input_b", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
        this.inputCStacks.set(0, input.read("input_c", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
        this.outputStacks.set(0, input.read("output", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));

        int gasId = input.getIntOr("gas", -1);
        long gasAmount = input.getLongOr("gas_amount", 0L);
        if (gasId != -1 && gasAmount > 0) {
            GasType gas = ModGases.bySyncId(gasId);
            this.gasTank.loadStored(gas, gasAmount);
        }

        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 100);
        this.hasRecipe = input.getBooleanOr("has_recipe", false);

        this.fluidInputLocks.resetAll();
        this.gasTransferLocks.resetAll();
    }

    private Optional<ResolvedRecipe> findMatchingRecipe() {
        Optional<RecipeHolder<AdvancedMixingVatRecipe>> advanced = findMatchingAdvancedRecipe();
        if (advanced.isPresent()) {
            return Optional.of(ResolvedRecipe.advanced(advanced.get().value()));
        }

        return findMatchingMixingVatRecipe();
    }

    private Optional<ResolvedRecipe> findMatchingMixingVatRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        for (RecipeHolder<MixingVatRecipe> holder : serverLevel.recipeAccess().getRecipes()
                .stream()
                .filter(pred -> pred.value() instanceof MixingVatRecipe)
                .map(pred -> (RecipeHolder<MixingVatRecipe>) pred)
                .toList()) {
            MixingVatRecipe recipe = holder.value();

            if (recipe.resultFluid().isEmpty()) {
                continue;
            }

            if (!recipe.resultItem().isEmpty()) {
                continue;
            }

            if (!matchesMixingVatItems(recipe)) {
                continue;
            }

            int[] fluidAssignment = matchMixingVatFluids(recipe);
            if (fluidAssignment == null) {
                continue;
            }

            FluidStack result = toNeoFluidStack(recipe.resultFluid().get());
            if (result.isEmpty()) {
                continue;
            }

            return Optional.of(ResolvedRecipe.mixing(recipe, fluidAssignment, result));
        }

        return Optional.empty();
    }

    private FluidStack toNeoFluidStack(MixingVatFluidStack stack) {
        Fluid fluid = BuiltInRegistries.FLUID.getValue(stack.fluid());
        if (fluid == Fluids.EMPTY || stack.amount() <= 0) {
            return FluidStack.EMPTY;
        }

        return new FluidStack(fluid, stack.amount());
    }

    @Nullable
    private int[] matchMixingVatFluids(MixingVatRecipe recipe) {
        List<MixingVatFluidStack> requiredFluids = recipe.requiredFluids();

        if (requiredFluids.size() > 3) {
            return null;
        }

        FluidStack[] available = new FluidStack[]{
                this.inputAStacks.get(0),
                this.inputBStacks.get(0),
                this.inputCStacks.get(0)
        };

        int[] assignment = new int[requiredFluids.size()];
        for (int i = 0; i < assignment.length; i++) {
            assignment[i] = -1;
        }

        boolean[] usedTanks = new boolean[available.length];

        return assignMixingVatFluids(requiredFluids, available, 0, usedTanks, assignment)
                ? assignment
                : null;
    }

    private boolean assignMixingVatFluids(
            List<MixingVatFluidStack> requiredFluids,
            FluidStack[] available,
            int requirementIndex,
            boolean[] usedTanks,
            int[] assignment
    ) {
        if (requirementIndex >= requiredFluids.size()) {
            return true;
        }

        MixingVatFluidStack required = requiredFluids.get(requirementIndex);

        for (int tankIndex = 0; tankIndex < available.length; tankIndex++) {
            if (usedTanks[tankIndex]) {
                continue;
            }

            FluidStack present = available[tankIndex];
            if (present.isEmpty()) {
                continue;
            }

            var presentId = BuiltInRegistries.FLUID.getKey(present.getFluid());
            if (presentId == null) {
                continue;
            }

            if (!required.fluid().equals(presentId)) {
                continue;
            }

            if (present.getAmount() < required.amount()) {
                continue;
            }

            usedTanks[tankIndex] = true;
            assignment[requirementIndex] = tankIndex;

            if (assignMixingVatFluids(requiredFluids, available, requirementIndex + 1, usedTanks, assignment)) {
                return true;
            }

            usedTanks[tankIndex] = false;
            assignment[requirementIndex] = -1;
        }

        return false;
    }

    private Optional<RecipeHolder<AdvancedMixingVatRecipe>> findMatchingAdvancedRecipe() {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return Optional.empty();
        }

        List<ItemStack> snapshot = new ArrayList<>(RECIPE_ITEM_SLOT_COUNT);
        for (int i = 0; i < RECIPE_ITEM_SLOT_COUNT; i++) {
            snapshot.add(this.itemStacks.get(i).copy());
        }

        FluidStack a = this.inputAStacks.get(0).copy();
        FluidStack b = this.inputBStacks.get(0).copy();
        FluidStack c = this.inputCStacks.get(0).copy();
        GasStack gas = this.gasTank.getGasInTank(0);

        FluidStack[][] permutations = new FluidStack[][]{
                {a, b, c},
                {a, c, b},
                {b, a, c},
                {b, c, a},
                {c, a, b},
                {c, b, a}
        };

        for (RecipeHolder<AdvancedMixingVatRecipe> holder : serverLevel.recipeAccess().getRecipes()
                .stream()
                .filter(pred -> pred.value() instanceof AdvancedMixingVatRecipe)
                .map(pred -> (RecipeHolder<AdvancedMixingVatRecipe>) pred)
                .toList()) {
            AdvancedMixingVatRecipe recipe = holder.value();

            for (FluidStack[] permutation : permutations) {
                AdvancedMixingVatRecipeInput input = new AdvancedMixingVatRecipeInput(
                        snapshot,
                        permutation[0].copy(),
                        permutation[1].copy(),
                        permutation[2].copy(),
                        gas
                );

                if (recipe.matches(input, serverLevel)) {
                    return Optional.of(holder);
                }
            }
        }

        return Optional.empty();
    }

    private boolean matchesMixingVatItems(MixingVatRecipe recipe) {
        List<Ingredient> requiredItems = recipe.requiredItems();

        int totalItems = 0;
        for (int i = 0; i < RECIPE_ITEM_SLOT_COUNT; i++) {
            ItemStack stack = this.itemStacks.get(i);
            if (!stack.isEmpty()) {
                totalItems += stack.getCount();
            }
        }

        if (totalItems < requiredItems.size()) {
            return false;
        }

        int[] usedItems = new int[RECIPE_ITEM_SLOT_COUNT];

        for (Ingredient ingredient : requiredItems) {
            boolean matched = false;

            for (int i = 0; i < RECIPE_ITEM_SLOT_COUNT; i++) {
                ItemStack stack = this.itemStacks.get(i);
                if (stack.isEmpty()) {
                    continue;
                }

                if (usedItems[i] < stack.getCount() && ingredient.test(stack)) {
                    usedItems[i]++;
                    matched = true;
                    break;
                }
            }

            if (!matched) {
                return false;
            }
        }

        return true;
    }

    // -----------------------------
    // Keep all your current recipe methods below:
    // - findMatchingRecipe
    // - findMatchingAdvancedRecipe
    // - findMatchingMixingVatRecipe
    // - canAcceptOutput
    // - finishResolvedRecipe
    // - finishAdvancedRecipe
    // - finishMixingVatRecipe
    // - consumeMixingVatItems
    // - consumeMixingVatFluids
    // - addToOutputTank
    // - ResolvedRecipe inner class
    // -----------------------------
    private static final class ResolvedRecipe {
        @Nullable
        private final AdvancedMixingVatRecipe advancedRecipe;
        @Nullable
        private final MixingVatRecipe mixingRecipe;
        @Nullable
        private final int[] mixingFluidAssignment;

        private final FluidStack result;
        private final int processingTime;

        private ResolvedRecipe(
                @Nullable AdvancedMixingVatRecipe advancedRecipe,
                @Nullable MixingVatRecipe mixingRecipe,
                @Nullable int[] mixingFluidAssignment,
                FluidStack result,
                int processingTime
        ) {
            this.advancedRecipe = advancedRecipe;
            this.mixingRecipe = mixingRecipe;
            this.mixingFluidAssignment = mixingFluidAssignment;
            this.result = result;
            this.processingTime = processingTime;
        }

        public static ResolvedRecipe advanced(AdvancedMixingVatRecipe recipe) {
            return new ResolvedRecipe(
                    recipe,
                    null,
                    null,
                    recipe.resultStack().copy(),
                    Math.max(1, recipe.processingTime())
            );
        }

        public static ResolvedRecipe mixing(MixingVatRecipe recipe, int[] fluidAssignment, FluidStack result) {
            return new ResolvedRecipe(
                    null,
                    recipe,
                    fluidAssignment,
                    result.copy(),
                    Math.max(1, recipe.requiredStirs() * 20)
            );
        }

        public boolean isAdvanced() {
            return this.advancedRecipe != null;
        }

        public AdvancedMixingVatRecipe advancedRecipe() {
            return this.advancedRecipe;
        }

        public MixingVatRecipe mixingRecipe() {
            return this.mixingRecipe;
        }

        public int[] mixingFluidAssignment() {
            return this.mixingFluidAssignment;
        }

        public FluidStack result() {
            return this.result;
        }

        public int processingTime() {
            return this.processingTime;
        }
    }

    private final class VatItemHandler extends ItemStacksResourceHandler {
        private VatItemHandler(int size) {
            super(size);
        }

        private NonNullList<ItemStack> list() {
            return this.stacks;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousStack) {
            if (!suppressTransferModeReset) {
                resetTransferLockForSlot(index);
            }
            onMachineChanged();
        }
    }

    private abstract class BaseVatFluidHandler extends FluidStacksResourceHandler {
        private BaseVatFluidHandler(int capacity) {
            super(1, capacity);
        }

        protected NonNullList<FluidStack> list() {
            return this.stacks;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousStack) {
            onMachineChanged();
        }
    }

    private final class VatInputFluidHandler extends BaseVatFluidHandler {
        private VatInputFluidHandler(int capacity) {
            super(capacity);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private final class VatOutputFluidHandler extends BaseVatFluidHandler {
        private VatOutputFluidHandler(int capacity) {
            super(capacity);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}
