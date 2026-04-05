package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.AdvancedMixingVatBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvancedMixingVatBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements MenuProvider {
    public static final int ITEM_SLOT_COUNT = 4;
    public static final int INPUT_TANK_CAPACITY = 4000;
    public static final int OUTPUT_TANK_CAPACITY = 4000;
    public static final long GAS_TANK_CAPACITY = 4000L;
    public static final int DATA_COUNT = 11;

    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(ITEM_SLOT_COUNT, ItemStack.EMPTY);
    private final NonNullList<FluidStack> inputAStacks = NonNullList.withSize(1, FluidStack.EMPTY);
    private final NonNullList<FluidStack> inputBStacks = NonNullList.withSize(1, FluidStack.EMPTY);
    private final NonNullList<FluidStack> outputStacks = NonNullList.withSize(1, FluidStack.EMPTY);

    private final VatItemHandler itemHandler = new VatItemHandler(this.itemStacks);
    private final VatInputFluidHandler inputAHandler = new VatInputFluidHandler(this.inputAStacks, INPUT_TANK_CAPACITY);
    private final VatInputFluidHandler inputBHandler = new VatInputFluidHandler(this.inputBStacks, INPUT_TANK_CAPACITY);
    private final VatOutputFluidHandler outputHandler = new VatOutputFluidHandler(this.outputStacks, OUTPUT_TANK_CAPACITY);

    private final GasTank gasTank = new GasTank(
            GAS_TANK_CAPACITY,
            gasType -> true,
            this::onMachineChanged
    );

    private final IGasHandler gasInputOnly = new IGasHandler() {
        @Override
        public int getTanks() {
            return gasTank.getTanks();
        }

        @Override
        public GasStack getGasInTank(int tank) {
            return gasTank.getGasInTank(tank);
        }

        @Override
        public long getTankCapacity(int tank) {
            return gasTank.getTankCapacity(tank);
        }

        @Override
        public boolean isGasValid(int tank, GasStack stack) {
            return gasTank.isGasValid(tank, stack);
        }

        @Override
        public long fill(GasStack resource, boolean simulate) {
            return gasTank.fill(resource, simulate);
        }

        @Override
        public GasStack drain(long amount, boolean simulate) {
            return GasStack.EMPTY;
        }
    };

    private int progress = 0;
    private int maxProgress = 100;
    private boolean hasRecipe = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputAStacks.get(0).getAmount();
                case 1 -> inputBStacks.get(0).getAmount();
                case 2 -> outputStacks.get(0).getAmount();
                case 3 -> (int) Math.min(Integer.MAX_VALUE, gasTank.getAmount());
                case 4 -> progress;
                case 5 -> maxProgress;
                case 6 -> inputAStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputAStacks.get(0).getFluid());
                case 7 -> inputBStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputBStacks.get(0).getFluid());
                case 8 -> outputStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(outputStacks.get(0).getFluid());
                case 9 -> gasTank.isEmpty() ? -1 : ModGases.getSyncId(gasTank.getGasType());
                case 10 -> hasRecipe ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 4 -> progress = value;
                case 5 -> maxProgress = value;
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

        Optional<RecipeHolder<AdvancedMixingVatRecipe>> match = be.findMatchingRecipe();

        if (match.isEmpty()) {
            boolean changed = be.progress != 0 || be.hasRecipe;
            be.progress = 0;
            be.hasRecipe = false;
            if (changed) {
                be.onMachineChanged();
            }
            return;
        }

        AdvancedMixingVatRecipe recipe = match.get().value();
        FluidStack result = recipe.resultStack();
        boolean canOutput = be.canAcceptOutput(result);

        if (!canOutput) {
            boolean changed = be.progress != 0 || be.hasRecipe;
            be.progress = 0;
            be.hasRecipe = false;
            if (changed) {
                be.onMachineChanged();
            }
            return;
        }

        be.hasRecipe = true;
        be.maxProgress = recipe.processingTime();
        be.progress++;
        be.setChanged();

        if (be.progress >= be.maxProgress) {
            if (be.findMatchingRecipe().isPresent() && be.canAcceptOutput(result)) {
                be.finishRecipe(recipe, result);
            }
            be.progress = 0;
            be.onMachineChanged();
        }
    }

    private Optional<RecipeHolder<AdvancedMixingVatRecipe>> findMatchingRecipe() {
        if (this.level == null || level.isClientSide()) {
            return Optional.empty();
        }

        List<ItemStack> snapshot = new ArrayList<>(this.itemStacks.size());
        for (ItemStack stack : this.itemStacks) {
            snapshot.add(stack.copy());
        }

        AdvancedMixingVatRecipeInput input = new AdvancedMixingVatRecipeInput(
                snapshot,
                this.inputAStacks.get(0).copy(),
                this.inputBStacks.get(0).copy(),
                this.gasTank.getGasInTank(0)
        );

        return ((ServerLevel)this.level).recipeAccess().getRecipeFor(
                ModRecipeTypes.ADVANCED_MIXING_VAT_RECIPE_TYPE.get(),
                input,
                this.level
        );
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

    private void finishRecipe(AdvancedMixingVatRecipe recipe, FluidStack result) {
        AdvancedMixingVatRecipe.consumeItems(recipe.itemInputs(), this.itemStacks);
        AdvancedMixingVatRecipe.consumeFluids(recipe.fluidInputs(), this.inputAStacks, this.inputBStacks);

        if (recipe.gasInput() != null) {
            this.gasTank.drain(recipe.gasInput().amount(), false);
        }

        FluidStack stored = this.outputStacks.get(0);
        if (stored.isEmpty()) {
            this.outputStacks.set(0, result.copy());
        } else {
            this.outputStacks.set(0, stored.copyWithAmount(stored.getAmount() + result.getAmount()));
        }
    }

    public void dropAllItems() {
        if (this.level == null) {
            return;
        }

        for (ItemStack stack : this.itemStacks) {
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        this.level,
                        this.worldPosition.getX(),
                        this.worldPosition.getY(),
                        this.worldPosition.getZ(),
                        stack
                );
            }
        }

        for (int i = 0; i < this.itemStacks.size(); i++) {
            this.itemStacks.set(i, ItemStack.EMPTY);
        }
    }

    private void onMachineChanged() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public ItemStacksResourceHandler getMenuItemHandler() {
        return this.itemHandler;
    }

    public ContainerData getData() {
        return this.data;
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
        Direction back = front.getOpposite();

        if (side == left) {
            return this.inputAHandler;
        }
        if (side == right) {
            return this.inputBHandler;
        }
        if (side == front) {
            return this.outputHandler;
        }

        return null;
    }

    public IGasHandler getGasCapability(@Nullable Direction side) {
        if (side == null) {
            return null;
        }

        Direction front = this.getBlockState().getValue(AdvancedMixingVatBlock.FACING);
        Direction back = front.getOpposite();
        return side == back ? this.gasInputOnly : null;
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

        output.store("input_a", FluidStack.OPTIONAL_CODEC, this.inputAStacks.get(0));
        output.store("input_b", FluidStack.OPTIONAL_CODEC, this.inputBStacks.get(0));
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

        for (int i = 0; i < this.itemStacks.size(); i++) {
            this.itemStacks.set(i, ItemStack.EMPTY);
        }

        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            ItemStack stack = child.read("stack", ItemStack.CODEC).orElse(ItemStack.EMPTY);
            if (slot >= 0 && slot < this.itemStacks.size()) {
                this.itemStacks.set(slot, stack);
            }
        }

        this.inputAStacks.set(0, input.read("input_a", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
        this.inputBStacks.set(0, input.read("input_b", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
        this.outputStacks.set(0, input.read("output", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));

        int gasId = input.getIntOr("gas", -1);
        long gasAmount = input.getLongOr("gas_amount", 0L);
        if (gasId != -1 && gasAmount > 0) {
            this.gasTank.loadStored(ModGases.bySyncId(gasId), gasAmount);
        }

        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 100);
        this.hasRecipe = input.getBooleanOr("has_recipe", false);
    }

    private final class VatItemHandler extends ItemStacksResourceHandler {
        private VatItemHandler(NonNullList<ItemStack> stacks) {
            super(stacks);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousStack) {
            onMachineChanged();
        }
    }

    private abstract class BaseVatFluidHandler extends FluidStacksResourceHandler {
        private BaseVatFluidHandler(NonNullList<FluidStack> stacks, int capacity) {
            super(stacks, capacity);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousStack) {
            onMachineChanged();
        }
    }

    private final class VatInputFluidHandler extends BaseVatFluidHandler {
        private VatInputFluidHandler(NonNullList<FluidStack> stacks, int capacity) {
            super(stacks, capacity);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private final class VatOutputFluidHandler extends BaseVatFluidHandler {
        private VatOutputFluidHandler(NonNullList<FluidStack> stacks, int capacity) {
            super(stacks, capacity);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }
}