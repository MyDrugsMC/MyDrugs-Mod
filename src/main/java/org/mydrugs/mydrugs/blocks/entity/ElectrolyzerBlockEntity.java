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
import net.minecraft.world.InteractionHand;
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
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.GasTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.ElectrolyzerMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerFluidStack;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerGasStack;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerRecipe;
import org.mydrugs.mydrugs.recipes.electrolyzer.ElectrolyzerRecipeInput;

import java.util.Optional;

public class ElectrolyzerBlockEntity extends BaseContainerBlockEntity implements ElectrolyzerMenu.ElectrolyzerButtonHandler {
    public static final int FLUID_CAPACITY = 4000;
    public static final int GAS_CAPACITY = 4000;

    private final LockedTransferSlots fluidTransferLocks = new LockedTransferSlots(1);

    private final StoredFluidTank inputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);

    private final StoredFluidTank output1Tank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output2Tank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank output3Tank = new StoredFluidTank(FLUID_CAPACITY, this::sync);

    private final GasTank output1GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output2GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);
    private final GasTank output3GasTank = new GasTank(GAS_CAPACITY, gas -> true, this::sync);

    private boolean output1GasMode;
    private boolean output2GasMode;
    private boolean output3GasMode;

    private NonNullList<ItemStack> items = NonNullList.withSize(5, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 200;

    private int burnTimeRemaining = 0;
    private int burnTimeTotal = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTank.getAmount();

                case 1 -> output1GasMode ? (int) output1GasTank.getAmount() : output1Tank.getAmount();
                case 2 -> output2GasMode ? (int) output2GasTank.getAmount() : output2Tank.getAmount();
                case 3 -> output3GasMode ? (int) output3GasTank.getAmount() : output3Tank.getAmount();

                case 4 -> progress;
                case 5 -> maxProgress;
                case 6 -> burnTimeRemaining;
                case 7 -> burnTimeTotal;

                case 8 -> inputTank.encodeFluidSyncId();
                case 9 -> output1Tank.encodeFluidSyncId();
                case 10 -> output2Tank.encodeFluidSyncId();
                case 11 -> output3Tank.encodeFluidSyncId();

                case 12 -> output1GasTank.isEmpty() ? -1 : ModGases.getSyncId(output1GasTank.getGasType());
                case 13 -> output2GasTank.isEmpty() ? -1 : ModGases.getSyncId(output2GasTank.getGasType());
                case 14 -> output3GasTank.isEmpty() ? -1 : ModGases.getSyncId(output3GasTank.getGasType());

                case 15 -> output1GasMode ? 1 : 0;
                case 16 -> output2GasMode ? 1 : 0;
                case 17 -> output3GasMode ? 1 : 0;

                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 4 -> progress = value;
                case 5 -> maxProgress = value;
                case 6 -> burnTimeRemaining = value;
                case 7 -> burnTimeTotal = value;
                default -> {
                    // client dummy menu only
                }
            }
        }

        @Override
        public int getCount() {
            return 18;
        }
    };

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYZER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ElectrolyzerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = FluidTransferUtil.tryProcessTransferSlot(
                be,
                ElectrolyzerMenu.INPUT_CONTAINER_SLOT,
                be.inputTank,
                be.fluidTransferLocks,
                0
        );

        Optional<RecipeHolder<ElectrolyzerRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
        ElectrolyzerRecipe recipe = recipeHolder.map(RecipeHolder::value).orElse(null);

        if (be.refreshOutputModes(recipe)) {
            changed = true;
        }

        if (be.tryProcessOutputSlots()) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0) {
            be.burnTimeRemaining--;
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

        if (be.burnTimeRemaining <= 0 && be.tryConsumeFuel()) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0) {
            be.progress++;
            changed = true;

            if (be.progress >= be.maxProgress) {
                be.craft(recipe);
                be.progress = 0;
                if (be.refreshOutputModes(recipe)) {
                    changed = true;
                }
                changed = true;
            }
        }

        if (changed) {
            be.sync();
        }
    }

    private boolean tryProcessOutputSlots() {
        boolean changed = false;

        if (this.output1GasMode) {
            if (GasTransferUtil.tryFillOutputSlot(this.items, ElectrolyzerMenu.OUTPUT_1_CONTAINER_SLOT, this.output1GasTank)) {
                changed = true;
            }
        } else {
            if (FluidTransferUtil.tryFillOutputSlot(this, ElectrolyzerMenu.OUTPUT_1_CONTAINER_SLOT, this.output1Tank)) {
                changed = true;
            }
        }

        if (this.output2GasMode) {
            if (GasTransferUtil.tryFillOutputSlot(this.items, ElectrolyzerMenu.OUTPUT_2_CONTAINER_SLOT, this.output2GasTank)) {
                changed = true;
            }
        } else {
            if (FluidTransferUtil.tryFillOutputSlot(this, ElectrolyzerMenu.OUTPUT_2_CONTAINER_SLOT, this.output2Tank)) {
                changed = true;
            }
        }

        if (this.output3GasMode) {
            if (GasTransferUtil.tryFillOutputSlot(this.items, ElectrolyzerMenu.OUTPUT_3_CONTAINER_SLOT, this.output3GasTank)) {
                changed = true;
            }
        } else {
            if (FluidTransferUtil.tryFillOutputSlot(this, ElectrolyzerMenu.OUTPUT_3_CONTAINER_SLOT, this.output3Tank)) {
                changed = true;
            }
        }

        return changed;
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

    private static boolean dumpGasTank(GasTank tank) {
        if (tank.isEmpty()) {
            return false;
        }

        tank.drain(tank.getAmount(), false);
        return true;
    }

    private static @Nullable GasType readGasType(ValueInput input, String key) {
        String raw = input.getStringOr(key, "");
        if (raw.isBlank()) {
            return null;
        }
        return ModGases.getNullable(raw);
    }

    private static boolean resolveOutputGasMode(boolean recipeWantsGas, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!gasTank.isEmpty()) {
            return true;
        }
        if (!fluidTank.isEmpty()) {
            return false;
        }
        return recipeWantsGas;
    }

    private boolean refreshOutputModes(@Nullable ElectrolyzerRecipe recipe) {
        boolean newOutput1GasMode = resolveOutputGasMode(
                recipe != null && recipe.outputGas1().isPresent(),
                this.output1Tank,
                this.output1GasTank
        );
        boolean newOutput2GasMode = resolveOutputGasMode(
                recipe != null && recipe.outputGas2().isPresent(),
                this.output2Tank,
                this.output2GasTank
        );
        boolean newOutput3GasMode = resolveOutputGasMode(
                recipe != null && recipe.outputGas3().isPresent(),
                this.output3Tank,
                this.output3GasTank
        );

        boolean changed = newOutput1GasMode != this.output1GasMode
                || newOutput2GasMode != this.output2GasMode
                || newOutput3GasMode != this.output3GasMode;

        this.output1GasMode = newOutput1GasMode;
        this.output2GasMode = newOutput2GasMode;
        this.output3GasMode = newOutput3GasMode;

        return changed;
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
        return !stack.isEmpty()
                && level != null
                && stack.getBurnTime(null, level.fuelValues()) > 0;
    }

    private Optional<RecipeHolder<ElectrolyzerRecipe>> getCurrentRecipe(ServerLevel level) {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        if (inputFluidId == null || this.inputTank.isEmpty()) {
            return Optional.empty();
        }

        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.ELECTROLYZER.get(),
                new ElectrolyzerRecipeInput(inputFluidId, this.inputTank.getAmount()),
                level
        );
    }

    private boolean canAcceptFluidOutput(ElectrolyzerFluidStack output, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!gasTank.isEmpty()) {
            return false;
        }

        FluidStack stack = toFluidStack(output.fluid(), output.amount());
        return !stack.isEmpty() && fluidTank.getAddableAmount(stack) >= stack.getAmount();
    }

    private boolean canAcceptGasOutput(ElectrolyzerGasStack output, StoredFluidTank fluidTank, GasTank gasTank) {
        if (!fluidTank.isEmpty()) {
            return false;
        }

        GasStack stack = toGasStack(output.gas(), output.amount());
        return !stack.isEmpty() && gasTank.fill(stack, true) >= output.amount();
    }

    private boolean canCraft(ElectrolyzerRecipe recipe) {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        if (inputFluidId == null) {
            return false;
        }

        if (!recipe.inputFluid().fluid().equals(inputFluidId)) {
            return false;
        }

        if (this.inputTank.getAmount() < recipe.inputFluid().amount()) {
            return false;
        }

        if (recipe.outputFluid1().isPresent()) {
            if (!canAcceptFluidOutput(recipe.outputFluid1().get(), this.output1Tank, this.output1GasTank)) {
                return false;
            }
        } else if (recipe.outputGas1().isPresent()) {
            if (!canAcceptGasOutput(recipe.outputGas1().get(), this.output1Tank, this.output1GasTank)) {
                return false;
            }
        } else {
            return false;
        }

        if (recipe.outputFluid2().isPresent()) {
            if (!canAcceptFluidOutput(recipe.outputFluid2().get(), this.output2Tank, this.output2GasTank)) {
                return false;
            }
        } else if (recipe.outputGas2().isPresent()) {
            if (!canAcceptGasOutput(recipe.outputGas2().get(), this.output2Tank, this.output2GasTank)) {
                return false;
            }
        }

        if (recipe.outputFluid3().isPresent()) {
            if (!canAcceptFluidOutput(recipe.outputFluid3().get(), this.output3Tank, this.output3GasTank)) {
                return false;
            }
        } else if (recipe.outputGas3().isPresent()) {
            if (!canAcceptGasOutput(recipe.outputGas3().get(), this.output3Tank, this.output3GasTank)) {
                return false;
            }
        }

        return true;
    }

    private void insertFluidOutput(ElectrolyzerFluidStack output, StoredFluidTank tank) {
        FluidStack stack = toFluidStack(output.fluid(), output.amount());
        if (!stack.isEmpty()) {
            tank.insert(stack, false);
        }
    }

    private void insertGasOutput(ElectrolyzerGasStack output, GasTank tank) {
        GasStack stack = toGasStack(output.gas(), output.amount());
        if (!stack.isEmpty()) {
            tank.fill(stack, false);
        }
    }

    private void craft(ElectrolyzerRecipe recipe) {
        this.inputTank.extract(recipe.inputFluid().amount(), false);

        recipe.outputFluid1().ifPresent(output -> insertFluidOutput(output, this.output1Tank));
        recipe.outputGas1().ifPresent(output -> insertGasOutput(output, this.output1GasTank));

        recipe.outputFluid2().ifPresent(output -> insertFluidOutput(output, this.output2Tank));
        recipe.outputGas2().ifPresent(output -> insertGasOutput(output, this.output2GasTank));

        recipe.outputFluid3().ifPresent(output -> insertFluidOutput(output, this.output3Tank));
        recipe.outputGas3().ifPresent(output -> insertGasOutput(output, this.output3GasTank));
    }

    private boolean tryConsumeFuel() {
        ItemStack fuelStack = this.getItem(ElectrolyzerMenu.FUEL_SLOT);
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
            this.setItem(ElectrolyzerMenu.FUEL_SLOT, remainder);
        }

        return true;
    }

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }

        if (player.getAbilities().instabuild && held.getItem() instanceof GlassBottleItem) {
            ResourceLocation incomingId = GlassBottleItem.getStoredFluidId(held);
            int containedAmount = GlassBottleItem.getStoredAmount(held);

            if (incomingId == null || containedAmount <= 0) {
                return false;
            }

            Fluid fluid = BuiltInRegistries.FLUID.getValue(incomingId);
            if (fluid == null || fluid == Fluids.EMPTY) {
                return false;
            }

            int moved = this.inputTank.insert(new FluidStack(fluid, containedAmount), true);
            if (moved <= 0) {
                return false;
            }

            this.inputTank.insert(new FluidStack(fluid, moved), false);
            this.sync();
            return true;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        FluidStack incoming = resource.toStack(containedAmount);
        int requested = this.inputTank.insert(incoming, true);
        if (requested <= 0) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, requested, tx);
            if (extracted <= 0) {
                return false;
            }

            tx.commit();
            this.inputTank.insert(incoming.copyWithAmount(extracted), false);
        }

        this.sync();
        return true;
    }

    private void sync() {
        MachineSync.sync(this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.electrolyzer");
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
        return new ElectrolyzerMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return 5;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case ElectrolyzerMenu.INPUT_CONTAINER_SLOT -> isFluidContainer(stack);
            case ElectrolyzerMenu.OUTPUT_1_CONTAINER_SLOT,
                 ElectrolyzerMenu.OUTPUT_2_CONTAINER_SLOT,
                 ElectrolyzerMenu.OUTPUT_3_CONTAINER_SLOT -> isFluidContainer(stack) || isGasContainer(stack);
            case ElectrolyzerMenu.FUEL_SLOT -> isFuel(stack, this.level);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (slot == ElectrolyzerMenu.INPUT_CONTAINER_SLOT) {
            this.fluidTransferLocks.reset(0);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.inputTank.load(input, "InputFluid");

        this.output1Tank.load(input, "OutputFluid1");
        this.output2Tank.load(input, "OutputFluid2");
        this.output3Tank.load(input, "OutputFluid3");

        this.output1GasTank.loadStored(readGasType(input, "OutputGas1"), input.getLongOr("OutputGasAmount1", 0));
        this.output2GasTank.loadStored(readGasType(input, "OutputGas2"), input.getLongOr("OutputGasAmount2", 0));
        this.output3GasTank.loadStored(readGasType(input, "OutputGas3"), input.getLongOr("OutputGasAmount3", 0));

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);
        this.burnTimeRemaining = input.getIntOr("BurnTimeRemaining", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);

        this.output1GasMode = input.getBooleanOr("Output1GasMode", false);
        this.output2GasMode = input.getBooleanOr("Output2GasMode", false);
        this.output3GasMode = input.getBooleanOr("Output3GasMode", false);

        this.refreshOutputModes(null);
        this.fluidTransferLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        this.inputTank.save(output, "InputFluid");

        this.output1Tank.save(output, "OutputFluid1");
        this.output2Tank.save(output, "OutputFluid2");
        this.output3Tank.save(output, "OutputFluid3");

        GasType gas1 = this.output1GasTank.getGasType();
        GasType gas2 = this.output2GasTank.getGasType();
        GasType gas3 = this.output3GasTank.getGasType();

        output.putString("OutputGas1", gas1 == null ? "" : gas1.id().toString());
        output.putLong("OutputGasAmount1", this.output1GasTank.getAmount());

        output.putString("OutputGas2", gas2 == null ? "" : gas2.id().toString());
        output.putLong("OutputGasAmount2", this.output2GasTank.getAmount());

        output.putString("OutputGas3", gas3 == null ? "" : gas3.id().toString());
        output.putLong("OutputGasAmount3", this.output3GasTank.getAmount());

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);

        output.putBoolean("Output1GasMode", this.output1GasMode);
        output.putBoolean("Output2GasMode", this.output2GasMode);
        output.putBoolean("Output3GasMode", this.output3GasMode);
    }

    @Override
    public boolean onElectrolyzerButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case ElectrolyzerMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = this.inputTank.dump();
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case ElectrolyzerMenu.DUMP_OUTPUT_1_BUTTON_ID -> {
                boolean dumped = this.output1GasMode ? dumpGasTank(this.output1GasTank) : this.output1Tank.dump();
                if (dumped) {
                    this.refreshOutputModes(null);
                    sync();
                }
                yield dumped;
            }
            case ElectrolyzerMenu.DUMP_OUTPUT_2_BUTTON_ID -> {
                boolean dumped = this.output2GasMode ? dumpGasTank(this.output2GasTank) : this.output2Tank.dump();
                if (dumped) {
                    this.refreshOutputModes(null);
                    sync();
                }
                yield dumped;
            }
            case ElectrolyzerMenu.DUMP_OUTPUT_3_BUTTON_ID -> {
                boolean dumped = this.output3GasMode ? dumpGasTank(this.output3GasTank) : this.output3Tank.dump();
                if (dumped) {
                    this.refreshOutputModes(null);
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(this, inputTank, output1Tank, output2Tank, output3Tank);
    }

    public IGasHandler getGasHandler(Direction side) {
        return new CompositeGasHandler(output1GasTank, output2GasTank, output3GasTank);
    }

    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
