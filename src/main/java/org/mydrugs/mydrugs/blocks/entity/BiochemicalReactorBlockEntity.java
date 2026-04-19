package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
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
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.blocks.BiochemicalReactorBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.item.MachineItemUtil;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.menu.BiochemicalReactorMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipe;
import org.mydrugs.mydrugs.recipes.biochemical_reactor.BiochemicalReactorRecipeInput;

import java.util.Optional;

public class BiochemicalReactorBlockEntity extends BaseContainerBlockEntity implements BiochemicalReactorMenu.ReactorButtonHandler {
    public static final int SLOT_ERGOT = 0;
    public static final int SLOT_TRYPTOPHAN = 1;
    public static final int SLOT_CHARCOAL = 2;
    public static final int SLOT_OUTPUT_CONTAINER = 3;
    public static final int SLOT_COUNT = 4;

    public static final int OUTPUT_TANK_CAPACITY = 4000;

    private static final int MAX_HEAT = 100;
    private static final int MAX_MANUAL_ENERGY = 100;

    private static final int BASE_SPEED_UNITS = 25; // 0.25 progress / tick
    private static final int CHARCOAL_HEAT_RESERVE = 1200;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private final StoredFluidTank outputTank = new StoredFluidTank(
            OUTPUT_TANK_CAPACITY,
            this::sync
    );

    private int progressUnits = 0;
    private int maxProgressUnits = 0;

    private int heat = 0;
    private int fuelHeatTicks = 0;
    private int manualEnergy = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progressUnits;
                case 1 -> maxProgressUnits;
                case 2 -> heat;
                case 3 -> MAX_HEAT;
                case 4 -> manualEnergy;
                case 5 -> MAX_MANUAL_ENERGY;
                case 6 -> outputTank.getAmount();
                case 7 -> OUTPUT_TANK_CAPACITY;
                case 8 -> outputTank.encodeFluidSyncId();
                case 9 -> isWorking() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progressUnits = value;
                case 1 -> maxProgressUnits = value;
                case 2 -> heat = value;
                case 4 -> manualEnergy = value;
                default -> {
                    // client dummy menu only
                }
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    public BiochemicalReactorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BIOCHEMICAL_REACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BiochemicalReactorBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;
        boolean previousLit = state.getValue(BiochemicalReactorBlock.LIT);

        changed |= be.handleFuelAndHeat(serverLevel);
        changed |= be.handleManualDecay(serverLevel);

        if (FluidTransferUtil.tryFillOutputSlot(be, SLOT_OUTPUT_CONTAINER, be.outputTank)) {
            changed = true;
        }

        Optional<RecipeHolder<BiochemicalReactorRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
        if (recipeHolder.isEmpty()) {
            if (be.progressUnits != 0) {
                be.progressUnits = 0;
                changed = true;
            }
            changed |= be.updateLitState(false);

            if (changed) {
                be.sync();
            }
            return;
        }

        BiochemicalReactorRecipe recipe = recipeHolder.get().value();
        be.maxProgressUnits = recipe.processingTime() * 100;

        if (!be.canProcess(recipe)) {
            if (be.progressUnits != 0) {
                be.progressUnits = 0;
                changed = true;
            }
            changed |= be.updateLitState(false);

            if (changed) {
                be.sync();
            }
            return;
        }

        int speedUnits = be.computeSpeedUnits(recipe);
        if (speedUnits > 0) {
            be.progressUnits += speedUnits;
            changed = true;
        }

        if (be.progressUnits >= be.maxProgressUnits) {
            be.finishRecipe(recipe);
            be.progressUnits = 0;
            changed = true;
        }

        boolean nowLit = speedUnits > BASE_SPEED_UNITS || be.fuelHeatTicks > 0 || be.heat > 0;
        if (previousLit != nowLit) {
            changed |= be.updateLitState(nowLit);
        }

        if (changed) {
            be.sync();
        }
    }

    private boolean handleFuelAndHeat(ServerLevel level) {
        boolean changed = false;

        if (this.fuelHeatTicks <= 0 && this.heat < 90 && isCharcoal(this.getItem(SLOT_CHARCOAL))) {
            this.getItem(SLOT_CHARCOAL).shrink(1);
            this.fuelHeatTicks += CHARCOAL_HEAT_RESERVE;
            changed = true;
        }

        if (this.fuelHeatTicks > 0) {
            this.fuelHeatTicks--;

            if (level.getGameTime() % 4L == 0L && this.heat < MAX_HEAT) {
                this.heat++;
                changed = true;
            }
        } else {
            if (level.getGameTime() % 8L == 0L && this.heat > 0) {
                this.heat--;
                changed = true;
            }
        }

        return changed;
    }

    private boolean handleManualDecay(ServerLevel level) {
        if (this.manualEnergy <= 0) {
            return false;
        }

        int before = this.manualEnergy;
        if (isWorking()) {
            if (level.getGameTime() % 2L == 0L) {
                this.manualEnergy = Math.max(0, this.manualEnergy - 1);
            }
        } else {
            if (level.getGameTime() % 4L == 0L) {
                this.manualEnergy = Math.max(0, this.manualEnergy - 1);
            }
        }

        return this.manualEnergy != before;
    }

    private int computeSpeedUnits(BiochemicalReactorRecipe recipe) {
        double speed = BASE_SPEED_UNITS;

        double heatBonusUnits = (this.heat / 100.0) * (recipe.heatBonusFactor() * 100.0);
        speed += heatBonusUnits;

        int ergotCount = this.getItem(SLOT_ERGOT).getCount();
        int scaledErgot = Math.min(ergotCount, recipe.ergotSpeedCap());
        double ergotBonusUnits = scaledErgot * (recipe.ergotSpeedPerItem() * 100.0);
        speed += ergotBonusUnits;

        double manualBonusUnits = (this.manualEnergy / (double) MAX_MANUAL_ENERGY) * (recipe.manualBoostFactor() * 100.0);
        speed += manualBonusUnits;

        if (this.heat < recipe.minimumHeat()) {
            speed -= 15.0;
        }

        return Math.max(1, Mth.floor(speed));
    }

    private Optional<RecipeHolder<BiochemicalReactorRecipe>> getCurrentRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.BIOCHEMICAL_REACTOR.get(),
                new BiochemicalReactorRecipeInput(this.getItem(SLOT_ERGOT), this.getItem(SLOT_TRYPTOPHAN)),
                level
        );
    }

    private boolean canProcess(BiochemicalReactorRecipe recipe) {
        if (!recipe.ergot().matches(this.getItem(SLOT_ERGOT))) {
            return false;
        }

        if (!recipe.tryptophan().matches(this.getItem(SLOT_TRYPTOPHAN))) {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(recipe.fluidOutput().fluidId());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return false;
        }

        FluidStack result = new FluidStack(fluid, recipe.fluidOutput().amount());
        return this.outputTank.getAddableAmount(result) >= result.getAmount();
    }

    private void finishRecipe(BiochemicalReactorRecipe recipe) {
        this.removeItem(SLOT_ERGOT, recipe.ergot().count());
        this.removeItem(SLOT_TRYPTOPHAN, recipe.tryptophan().count());

        Fluid fluid = BuiltInRegistries.FLUID.getValue(recipe.fluidOutput().fluidId());
        if (fluid != null && fluid != Fluids.EMPTY) {
            this.outputTank.insert(new FluidStack(fluid, recipe.fluidOutput().amount()), false);
        }
    }

    public void addManualEnergy(int amount) {
        if (amount <= 0) {
            return;
        }

        int before = this.manualEnergy;
        this.manualEnergy = Math.min(MAX_MANUAL_ENERGY, this.manualEnergy + amount);
        if (this.manualEnergy != before) {
            this.sync();
        }
    }

    private boolean updateLitState(boolean lit) {
        if (this.level == null) {
            return false;
        }

        BlockState state = this.getBlockState();
        if (state.getBlock() instanceof BiochemicalReactorBlock && state.getValue(BiochemicalReactorBlock.LIT) != lit) {
            this.level.setBlock(this.worldPosition, state.setValue(BiochemicalReactorBlock.LIT, lit), Block.UPDATE_CLIENTS);
            return true;
        }

        return false;
    }

    private void sync() {
        MachineSync.sync(this);
    }

    public int getComparatorOutput() {
        return Mth.clamp((this.outputTank.getAmount() * 15) / OUTPUT_TANK_CAPACITY, 0, 15);
    }

    public boolean isWorking() {
        return this.maxProgressUnits > 0 && this.progressUnits > 0;
    }

    public static boolean isErgot(ItemStack stack) {
        return stack.is(ModItems.ERGOT.get());
    }

    public static boolean isTryptophan(ItemStack stack) {
        return stack.is(ModItems.TRYPTOPHAN.get());
    }

    public static boolean isCharcoal(ItemStack stack) {
        return stack.is(Items.CHARCOAL);
    }

    @Override
    public boolean onReactorButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        if (buttonId == BiochemicalReactorMenu.MANUAL_BOOST_BUTTON_ID) {
            this.addManualEnergy(12);
            return true;
        }

        return false;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.biochemical_reactor");
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
        return new BiochemicalReactorMenu(
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
            case SLOT_ERGOT -> isErgot(stack);
            case SLOT_TRYPTOPHAN -> isTryptophan(stack);
            case SLOT_CHARCOAL -> isCharcoal(stack);
            case SLOT_OUTPUT_CONTAINER -> MachineItemUtil.isFluidContainer(stack);
            default -> false;
        };
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.outputTank.load(input, "output_tank");

        this.progressUnits = input.getIntOr("ProgressUnits", 0);
        this.maxProgressUnits = input.getIntOr("MaxProgressUnits", 0);
        this.heat = input.getIntOr("Heat", 0);
        this.fuelHeatTicks = input.getIntOr("FuelHeatTicks", 0);
        this.manualEnergy = input.getIntOr("ManualEnergy", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);
        this.outputTank.save(output, "output_tank");

        output.putInt("ProgressUnits", this.progressUnits);
        output.putInt("MaxProgressUnits", this.maxProgressUnits);
        output.putInt("Heat", this.heat);
        output.putInt("FuelHeatTicks", this.fuelHeatTicks);
        output.putInt("ManualEnergy", this.manualEnergy);
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}