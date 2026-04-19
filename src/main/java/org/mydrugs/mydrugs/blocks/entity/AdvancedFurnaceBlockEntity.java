package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.fuel.FuelResolver;
import org.mydrugs.mydrugs.machine.fuel.MachineFuelUtil;
import org.mydrugs.mydrugs.machine.item.MachineItemUtil;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.menu.AdvancedFurnaceMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipeInput;

import java.util.Optional;
import java.util.function.BooleanSupplier;

public class AdvancedFurnaceBlockEntity extends BaseContainerBlockEntity {
    public static final int INPUT_A_SLOT = 0;
    public static final int INPUT_B_SLOT = 1;
    public static final int FUEL_SLOT = 2;
    public static final int OUTPUT_A_SLOT = 3;
    public static final int OUTPUT_B_SLOT = 4;
    public static final int OUTPUT_FLUID_CONTAINER_SLOT = 5;
    public static final int SLOT_COUNT = 6;

    public static final int TANK_CAPACITY = 4000;

    private static final FuelResolver ADVANCED_FURNACE_FUEL = MachineFuelUtil.VANILLA;
    private final StoredFluidTank outputTank = new StoredFluidTank(TANK_CAPACITY, this::sync);
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 200;

    private int burnTime = 0;
    private int burnDuration = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> burnTime;
                case 3 -> burnDuration;
                case 4 -> outputTank.getAmount();
                case 5 -> outputTank.encodeFluidSyncId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> burnTime = value;
                case 3 -> burnDuration = value;
                default -> {
                    // client dummy menu only
                }
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public AdvancedFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ADVANCED_FURNACE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AdvancedFurnaceBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = FluidTransferUtil.tryFillOutputSlot(be, OUTPUT_FLUID_CONTAINER_SLOT, be.outputTank);

        if (be.burnTime > 0) {
            be.burnTime--;
            changed = true;
        }

        Optional<RecipeHolder<AdvancedFurnaceRecipe>> advancedRecipe = be.getAdvancedRecipe(serverLevel);
        if (advancedRecipe.isPresent()) {
            AdvancedFurnaceRecipe recipe = advancedRecipe.get().value();

            changed |= processRecipe(
                    be,
                    recipe.cookTime(),
                    () -> be.canCraft(recipe),
                    () -> be.craft(recipe)
            );

            if (changed) {
                be.sync();
            }
            return;
        }

        Optional<VanillaRecipeMatch> vanillaRecipe = be.getVanillaRecipe(serverLevel);
        if (vanillaRecipe.isPresent()) {
            VanillaRecipeMatch match = vanillaRecipe.get();

            changed |= processRecipe(
                    be,
                    match.cookTime(),
                    () -> be.canCraft(match),
                    () -> be.craft(match)
            );

            if (changed) {
                be.sync();
            }
            return;
        }

        if (be.progress != 0) {
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    private static boolean processRecipe(
            AdvancedFurnaceBlockEntity be,
            int cookTime,
            BooleanSupplier canCraft,
            Runnable craftAction
    ) {
        boolean changed = false;

        be.maxProgress = cookTime;

        if (!canCraft.getAsBoolean()) {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }
            return changed;
        }

        if (be.burnTime <= 0 && be.consumeFuel()) {
            changed = true;
        }

        if (be.burnTime > 0) {
            be.progress++;
            changed = true;

            if (be.progress >= be.maxProgress) {
                craftAction.run();
                be.progress = 0;
                changed = true;
            }
        } else if (be.progress != 0) {
            be.progress = 0;
            changed = true;
        }

        return changed;
    }

    private static int boostedVanillaCookTime(int vanillaCookTime) {
        return Math.max(1, (vanillaCookTime * 4 + 4) / 5);
    }

    private static boolean canInsertItem(ItemStack existing, ItemStack result) {
        if (result.isEmpty()) return true;
        if (existing.isEmpty()) return true;
        if (!ItemStack.isSameItemSameComponents(existing, result)) return false;
        return existing.getCount() + result.getCount() <= existing.getMaxStackSize();
    }

    private static FluidStack toFluidStack(Optional<ResourceLocation> fluidId, int amount) {
        if (amount <= 0 || fluidId.isEmpty()) {
            return FluidStack.EMPTY;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(fluidId.get());
        if (fluid == null || fluid == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }

        return new FluidStack(fluid, amount);
    }

    private boolean consumeFuel() {
        MachineFuelUtil.FuelUse fuelUse = MachineFuelUtil.consumeOne(
                this.getItem(FUEL_SLOT),
                this.level,
                ADVANCED_FURNACE_FUEL
        );

        if (!fuelUse.consumed()) {
            return false;
        }

        this.burnTime = fuelUse.burnTime();
        this.burnDuration = fuelUse.burnTime();
        this.setItem(FUEL_SLOT, fuelUse.remainingStack());
        return true;
    }

    private Optional<RecipeHolder<AdvancedFurnaceRecipe>> getAdvancedRecipe(ServerLevel level) {
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.ADVANCED_FURNACE.get(),
                new AdvancedFurnaceRecipeInput(this.getItem(INPUT_A_SLOT), this.getItem(INPUT_B_SLOT)),
                level
        );
    }

    private Optional<VanillaRecipeMatch> getVanillaRecipe(ServerLevel level) {
        Optional<VanillaRecipeMatch> inputA = this.getVanillaRecipeForSlot(level, INPUT_A_SLOT);
        if (inputA.isPresent()) {
            return inputA;
        }

        return this.getVanillaRecipeForSlot(level, INPUT_B_SLOT);
    }

    private Optional<VanillaRecipeMatch> getVanillaRecipeForSlot(ServerLevel level, int inputSlot) {
        ItemStack input = this.getItem(inputSlot);
        if (input.isEmpty()) {
            return Optional.empty();
        }

        SingleRecipeInput recipeInput = new SingleRecipeInput(input);
        Optional<RecipeHolder<SmeltingRecipe>> holder = level.recipeAccess().getRecipeFor(
                RecipeType.SMELTING,
                recipeInput,
                level
        );

        if (holder.isEmpty()) {
            return Optional.empty();
        }

        ItemStack result = holder.get().value().assemble(recipeInput, level.registryAccess());
        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(new VanillaRecipeMatch(
                inputSlot,
                result.copy(),
                boostedVanillaCookTime(holder.get().value().cookingTime())
        ));
    }

    private boolean canCraft(AdvancedFurnaceRecipe recipe) {
        if (!canInsertItem(this.getItem(OUTPUT_A_SLOT), recipe.resultA())) return false;
        if (!canInsertItem(this.getItem(OUTPUT_B_SLOT), recipe.resultB())) return false;

        FluidStack recipeFluid = toFluidStack(recipe.fluidOutput(), recipe.fluidAmount());
        return recipeFluid.isEmpty() || this.outputTank.getAddableAmount(recipeFluid) >= recipeFluid.getAmount();
    }

    private boolean canCraft(VanillaRecipeMatch match) {
        return this.findVanillaOutputSlot(match.result()) != -1;
    }

    private void craft(AdvancedFurnaceRecipe recipe) {
        this.removeItem(INPUT_A_SLOT, 1);

        if (recipe.inputB().isPresent()) {
            this.removeItem(INPUT_B_SLOT, 1);
        }

        insertItem(OUTPUT_A_SLOT, recipe.resultA());
        insertItem(OUTPUT_B_SLOT, recipe.resultB());

        FluidStack recipeFluid = toFluidStack(recipe.fluidOutput(), recipe.fluidAmount());
        if (!recipeFluid.isEmpty()) {
            this.outputTank.insert(recipeFluid, false);
        }
    }

    private void craft(VanillaRecipeMatch match) {
        int outputSlot = this.findVanillaOutputSlot(match.result());
        if (outputSlot == -1) {
            return;
        }

        this.removeItem(match.inputSlot(), 1);
        this.insertItem(outputSlot, match.result());
    }

    private int findVanillaOutputSlot(ItemStack result) {
        if (canInsertItem(this.getItem(OUTPUT_A_SLOT), result)) {
            return OUTPUT_A_SLOT;
        }

        if (canInsertItem(this.getItem(OUTPUT_B_SLOT), result)) {
            return OUTPUT_B_SLOT;
        }

        return -1;
    }

    private void insertItem(int slot, ItemStack result) {
        if (result.isEmpty()) {
            return;
        }

        ItemStack existing = this.getItem(slot);
        if (existing.isEmpty()) {
            this.setItem(slot, result.copy());
        } else {
            existing.grow(result.getCount());
            this.setItem(slot, existing);
        }
    }

    private void sync() {
        MachineSync.sync(this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.advanced_furnace");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AdvancedFurnaceMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
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
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case OUTPUT_A_SLOT, OUTPUT_B_SLOT -> false;
            case FUEL_SLOT -> MachineFuelUtil.isFuel(stack, this.level, ADVANCED_FURNACE_FUEL);
            case OUTPUT_FLUID_CONTAINER_SLOT -> MachineItemUtil.isFluidContainer(stack);
            default -> true;
        };
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 200);
        this.burnTime = input.getIntOr("burn_time", 0);
        this.burnDuration = input.getIntOr("burn_duration", 0);
        this.outputTank.load(input, "output_tank");
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
        output.putInt("burn_time", this.burnTime);
        output.putInt("burn_duration", this.burnDuration);
        this.outputTank.save(output, "output_tank");
    }

    private record VanillaRecipeMatch(int inputSlot, ItemStack result, int cookTime) {
    }
}