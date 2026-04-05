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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ChemicalReactorBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasTank;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.ChemicalReactorMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ChemicalReactorRecipeInput;
import org.mydrugs.mydrugs.recipes.chemical_reactor.ReactorOutputKind;

import java.util.Optional;

public class ChemicalReactorBlockEntity extends BlockEntity implements MenuProvider {
    public static final int FUEL_SLOT = 0;
    public static final int ITEM_SLOTS = 1;

    public static final int GAS_TANK_CAPACITY = 4000;
    public static final int FLUID_TANK_CAPACITY = 4000;
    public static final int MAX_HEAT = 1000;
    public static final int MAX_MANUAL_ENERGY = 200;

    private static final int CHARCOAL_BURN_TIME = 1600;

    private static final int SECONDARY_FLUID_TANK = 0;
    private static final int OUTPUT_FLUID_TANK = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(ITEM_SLOTS, ItemStack.EMPTY);
    private final SimpleContainer dropInventory = new SimpleContainer(ITEM_SLOTS);

    private final GasTank primaryGasTank = new GasTank(GAS_TANK_CAPACITY, gas -> true, this::onContentsChanged);
    private final GasTank secondaryGasTank = new GasTank(GAS_TANK_CAPACITY, gas -> true, this::onContentsChanged);
    private final GasTank gasOutputTank = new GasTank(GAS_TANK_CAPACITY, gas -> true, this::onContentsChanged);

    private final NonNullList<FluidStack> fluidStacks = NonNullList.withSize(2, FluidStack.EMPTY);

    private final ResourceHandler<FluidResource> fluidHandler =
            new FluidStacksResourceHandler(fluidStacks, FLUID_TANK_CAPACITY) {
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
                protected void onContentsChanged(int index, FluidStack previousContents) {
                    ChemicalReactorBlockEntity.this.onFluidContentsChanged();
                }
            };

    private int burnTimeRemaining;
    private int burnTimeTotal;
    private int heat;
    private int progress;
    private int maxProgress = 200;
    private int manualEnergy;
    private boolean active;

    private boolean secondaryFluidMode;
    private boolean outputFluidMode;

    @SuppressWarnings("removal")
    private final ItemStackHandler itemHandler = new ItemStackHandler(ITEM_SLOTS) {
        @Override
        protected void onContentsChanged(int slot) {
            ChemicalReactorBlockEntity.this.items.set(slot, this.getStackInSlot(slot));
            ChemicalReactorBlockEntity.this.dropInventory.setItem(slot, this.getStackInSlot(slot));
            ChemicalReactorBlockEntity.this.onContentsChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == FUEL_SLOT && stack.is(Items.CHARCOAL);
        }
    };

    private final IGasHandler gasHandler = new IGasHandler() {
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
            if (resource == null || resource.isEmpty()) {
                return 0;
            }

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

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) primaryGasTank.getAmount();
                case 1 -> (int) secondaryGasTank.getAmount();
                case 2 -> getSecondaryFluidAmount();
                case 3 -> (int) gasOutputTank.getAmount();
                case 4 -> getOutputFluidAmount();
                case 5 -> progress;
                case 6 -> maxProgress;
                case 7 -> heat;
                case 8 -> MAX_HEAT;
                case 9 -> burnTimeRemaining;
                case 10 -> burnTimeTotal;
                case 11 -> manualEnergy;
                case 12 -> MAX_MANUAL_ENERGY;
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
            }
        }

        @Override
        public int getCount() {
            return 13;
        }
    };

    public ChemicalReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CHEMICAL_REACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ChemicalReactorBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        boolean changed = false;
        boolean wasActive = blockEntity.active;

        changed |= blockEntity.handleFuel();
        changed |= blockEntity.updateHeat();

        Optional<RecipeHolder<ChemicalReactorRecipe>> recipeHolder = blockEntity.findMatchingRecipe();
        if (recipeHolder.isPresent()) {
            ChemicalReactorRecipe recipe = recipeHolder.get().value();
            blockEntity.secondaryFluidMode = recipe.secondaryFluid().isPresent();
            blockEntity.outputFluidMode = recipe.outputKind() == ReactorOutputKind.FLUID;
            blockEntity.maxProgress = recipe.processTime();

            if (blockEntity.canOutput(recipe)) {
                int speed = blockEntity.getProgressPerTick(recipe);
                blockEntity.progress += speed;
                changed = true;

                if (blockEntity.manualEnergy > 0) {
                    blockEntity.manualEnergy = Math.max(0, blockEntity.manualEnergy - 1);
                }

                if (blockEntity.progress >= blockEntity.maxProgress) {
                    blockEntity.progress = 0;
                    blockEntity.processRecipe(recipe);
                    changed = true;
                }

                blockEntity.active = true;
            } else {
                if (blockEntity.progress > 0) {
                    blockEntity.progress = Math.max(0, blockEntity.progress - 2);
                    changed = true;
                }
                blockEntity.active = false;
            }
        } else {
            blockEntity.secondaryFluidMode = blockEntity.getSecondaryFluidAmount() > 0;
            blockEntity.outputFluidMode = blockEntity.getOutputFluidAmount() > 0;

            if (blockEntity.progress > 0) {
                blockEntity.progress = Math.max(0, blockEntity.progress - 2);
                changed = true;
            }

            if (blockEntity.manualEnergy > 0) {
                blockEntity.manualEnergy = Math.max(0, blockEntity.manualEnergy - 1);
                changed = true;
            }

            blockEntity.active = false;
        }

        if (wasActive != blockEntity.active) {
            changed = true;
            level.setBlock(pos, state.setValue(ChemicalReactorBlock.ACTIVE, blockEntity.active), Block.UPDATE_CLIENTS);
        }

        if (changed) {
            blockEntity.onContentsChanged();
        }
    }

    private boolean handleFuel() {
        boolean changed = false;

        if (this.burnTimeRemaining > 0) {
            this.burnTimeRemaining--;
            changed = true;
        }

        if (this.burnTimeRemaining <= 0 && this.heat < 300) {
            ItemStack fuel = this.itemHandler.getStackInSlot(FUEL_SLOT);
            if (fuel.is(Items.CHARCOAL)) {
                fuel.shrink(1);
                this.itemHandler.setStackInSlot(FUEL_SLOT, fuel);
                this.burnTimeTotal = CHARCOAL_BURN_TIME;
                this.burnTimeRemaining = CHARCOAL_BURN_TIME;
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
        if (this.level == null || this.level.isClientSide()) {
            return Optional.empty();
        }

        ChemicalReactorRecipeInput input = new ChemicalReactorRecipeInput(
                this.primaryGasTank.getGasInTank(0),
                this.secondaryGasTank.getGasInTank(0),
                this.getSecondaryFluid()
        );

        return ((ServerLevel) this.level).recipeAccess().getRecipeFor(
                ModRecipeTypes.CHEMICAL_REACTOR.get(),
                input,
                this.level
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

        if (!existing.is(outputFluid)) {
            return false;
        }

        return existing.getAmount() + recipe.outputAmount() <= FLUID_TANK_CAPACITY;
    }

    private int getProgressPerTick(ChemicalReactorRecipe recipe) {
        int progress = 1;

        if (this.heat < recipe.minHeat()) {
            progress = 1;
        } else if (this.heat >= 800) {
            progress = 4;
        } else if (this.heat >= 500) {
            progress = 3;
        } else if (this.heat >= 200) {
            progress = 2;
        }

        if (this.manualEnergy > 0) {
            progress += 1;
        }

        return progress;
    }

    private void processRecipe(ChemicalReactorRecipe recipe) {
        this.primaryGasTank.drain(recipe.primaryGas().amount(), false);

        recipe.secondaryGas().ifPresent(req -> this.secondaryGasTank.drain(req.amount(), false));
        recipe.secondaryFluid().ifPresent(req -> this.removeFluidFromTank(SECONDARY_FLUID_TANK, req.amount()));

        if (recipe.outputKind() == ReactorOutputKind.GAS) {
            GasType outputType = ModGases.get(recipe.outputId());
            if (outputType != null) {
                this.gasOutputTank.fill(GasStack.of(outputType, recipe.outputAmount()), false);
            }
        } else {
            Fluid fluid = BuiltInRegistries.FLUID.getValue(recipe.outputId());
            if (fluid != Fluids.EMPTY) {
                this.addFluidToTank(OUTPUT_FLUID_TANK, new FluidStack(fluid, recipe.outputAmount()));
            }
        }

        if (recipe.heatDrain() > 0) {
            this.heat = Math.max(0, this.heat - recipe.heatDrain());
        }
    }

    private void addFluidToTank(int tank, FluidStack toAdd) {
        if (toAdd.isEmpty()) {
            return;
        }

        FluidStack existing = this.fluidStacks.get(tank);
        if (existing.isEmpty()) {
            this.fluidStacks.set(tank, toAdd.copyWithAmount(Math.min(FLUID_TANK_CAPACITY, toAdd.getAmount())));
            this.onFluidContentsChanged();
            return;
        }

        if (!FluidStack.isSameFluidSameComponents(existing, toAdd)) {
            return;
        }

        int newAmount = Math.min(FLUID_TANK_CAPACITY, existing.getAmount() + toAdd.getAmount());
        this.fluidStacks.set(tank, existing.copyWithAmount(newAmount));
        this.onFluidContentsChanged();
    }

    private void removeFluidFromTank(int tank, int amount) {
        if (amount <= 0) {
            return;
        }

        FluidStack existing = this.fluidStacks.get(tank);
        if (existing.isEmpty()) {
            return;
        }

        int remaining = existing.getAmount() - amount;
        if (remaining <= 0) {
            this.fluidStacks.set(tank, FluidStack.EMPTY);
        } else {
            this.fluidStacks.set(tank, existing.copyWithAmount(remaining));
        }

        this.onFluidContentsChanged();
    }

    public void addManualEnergy(int amount) {
        int old = this.manualEnergy;
        this.manualEnergy = Math.min(MAX_MANUAL_ENERGY, this.manualEnergy + amount);
        if (old != this.manualEnergy) {
            this.onContentsChanged();
        }
    }

    public SimpleContainer getDropInventory() {
        for (int i = 0; i < ITEM_SLOTS; i++) {
            this.dropInventory.setItem(i, this.itemHandler.getStackInSlot(i));
        }
        return this.dropInventory;
    }

    @SuppressWarnings("removal")
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return this.itemHandler;
    }

    public ResourceHandler<FluidResource> getFluidResourceHandler(@Nullable Direction side) {
        return this.fluidHandler;
    }

    public IGasHandler getGasHandler(@Nullable Direction side) {
        return this.gasHandler;
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
                new ReactorFuelContainer(),
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition),
                this.worldPosition
        );
    }

    private class ReactorFuelContainer extends SimpleContainer {
        public ReactorFuelContainer() {
            super(ITEM_SLOTS);
            this.setItem(FUEL_SLOT, itemHandler.getStackInSlot(FUEL_SLOT));
        }

        @Override
        public void setChanged() {
            super.setChanged();
            itemHandler.setStackInSlot(FUEL_SLOT, this.getItem(FUEL_SLOT));
            ChemicalReactorBlockEntity.this.onContentsChanged();
        }

        @Override
        public boolean canPlaceItem(int index, ItemStack stack) {
            return index == FUEL_SLOT && stack.is(Items.CHARCOAL);
        }
    }

    public long getPrimaryGasAmount() {
        return this.primaryGasTank.getAmount();
    }

    public long getSecondaryGasAmount() {
        return this.secondaryGasTank.getAmount();
    }

    public int getSecondaryFluidAmount() {
        return this.fluidStacks.get(SECONDARY_FLUID_TANK).getAmount();
    }

    public long getOutputGasAmount() {
        return this.gasOutputTank.getAmount();
    }

    public int getOutputFluidAmount() {
        return this.fluidStacks.get(OUTPUT_FLUID_TANK).getAmount();
    }

    public GasStack getPrimaryGas() {
        return this.primaryGasTank.getGasInTank(0);
    }

    public GasStack getSecondaryGas() {
        return this.secondaryGasTank.getGasInTank(0);
    }

    public FluidStack getSecondaryFluid() {
        return this.fluidStacks.get(SECONDARY_FLUID_TANK).copy();
    }

    public GasStack getOutputGas() {
        return this.gasOutputTank.getGasInTank(0);
    }

    public FluidStack getOutputFluid() {
        return this.fluidStacks.get(OUTPUT_FLUID_TANK).copy();
    }

    public boolean isSecondaryFluidMode() {
        return this.secondaryFluidMode;
    }

    public boolean isOutputFluidMode() {
        return this.outputFluidMode;
    }

    public int getScaledPrimaryGas(int pixels) {
        return (int) (this.primaryGasTank.getAmount() * pixels / GAS_TANK_CAPACITY);
    }

    public int getScaledSecondaryGas(int pixels) {
        return (int) (this.secondaryGasTank.getAmount() * pixels / GAS_TANK_CAPACITY);
    }

    public int getScaledSecondaryFluid(int pixels) {
        return this.getSecondaryFluidAmount() * pixels / FLUID_TANK_CAPACITY;
    }

    public int getScaledOutputGas(int pixels) {
        return (int) (this.gasOutputTank.getAmount() * pixels / GAS_TANK_CAPACITY);
    }

    public int getScaledOutputFluid(int pixels) {
        return this.getOutputFluidAmount() * pixels / FLUID_TANK_CAPACITY;
    }

    public int getPrimaryGasColor() {
        return getGasColor(this.primaryGasTank.getGasType());
    }

    public int getSecondaryGasColor() {
        return getGasColor(this.secondaryGasTank.getGasType());
    }

    public int getOutputGasColor() {
        return getGasColor(this.gasOutputTank.getGasType());
    }

    public String getPrimaryGasName() {
        return getGasName(this.primaryGasTank.getGasType());
    }

    public String getSecondaryGasName() {
        return getGasName(this.secondaryGasTank.getGasType());
    }

    public String getOutputGasName() {
        return getGasName(this.gasOutputTank.getGasType());
    }

    private static int getGasColor(GasType gasType) {
        return gasType == null ? 0 : gasType.tint();
    }

    private static String getGasName(GasType gasType) {
        return gasType == null ? "empty" : gasType.name();
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.burnTimeRemaining = input.getIntOr("BurnTimeRemaining", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);
        this.heat = input.getIntOr("Heat", 0);
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 0);
        this.manualEnergy = input.getIntOr("ManualEnergy", 0);
        this.active = input.getBooleanOr("Active", false);
        this.secondaryFluidMode = input.getBooleanOr("SecondaryFluidMode", false);
        this.outputFluidMode = input.getBooleanOr("OutputFluidMode", true);

        ContainerHelper.loadAllItems(input, this.items);
        for (int i = 0; i < ITEM_SLOTS; i++) {
            this.itemHandler.setStackInSlot(i, this.items.get(i));
            this.dropInventory.setItem(i, this.items.get(i));
        }

        ResourceLocation primaryGasId = ResourceLocation.tryParse(input.getStringOr("PrimaryGas", ""));
        ResourceLocation secondaryGasId = ResourceLocation.tryParse(input.getStringOr("SecondaryGas", ""));
        ResourceLocation outputGasId = ResourceLocation.tryParse(input.getStringOr("OutputGas", ""));

        GasType primaryGas = primaryGasId == null ? null : ModGases.get(primaryGasId);
        GasType secondaryGas = secondaryGasId == null ? null : ModGases.get(secondaryGasId);
        GasType outputGas = outputGasId == null ? null : ModGases.get(outputGasId);

        this.primaryGasTank.loadStored(primaryGas, primaryGas == null ? 0 : input.getLongOr("PrimaryGasAmount", 0));
        this.secondaryGasTank.loadStored(secondaryGas, secondaryGas == null ? 0 : input.getLongOr("SecondaryGasAmount", 0));
        this.gasOutputTank.loadStored(outputGas, outputGas == null ? 0 : input.getLongOr("OutputGasAmount", 0));

        this.fluidStacks.set(
                SECONDARY_FLUID_TANK,
                input.read("SecondaryFluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY)
        );
        this.fluidStacks.set(
                OUTPUT_FLUID_TANK,
                input.read("OutputFluid", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY)
        );
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
        output.putInt("Heat", this.heat);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("ManualEnergy", this.manualEnergy);
        output.putBoolean("Active", this.active);
        output.putBoolean("SecondaryFluidMode", this.secondaryFluidMode);
        output.putBoolean("OutputFluidMode", this.outputFluidMode);

        for (int i = 0; i < ITEM_SLOTS; i++) {
            this.items.set(i, this.itemHandler.getStackInSlot(i));
        }
        ContainerHelper.saveAllItems(output, this.items);

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

    private void onFluidContentsChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void onContentsChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            BlockState state = this.getBlockState();
            this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }
}