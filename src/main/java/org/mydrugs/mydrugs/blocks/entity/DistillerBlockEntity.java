package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.energy.MachineEnergyAttachments;
import org.mydrugs.mydrugs.energy.PsyCurrentMachines;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.manual.ManualMachineSpeedHelper;
import org.mydrugs.mydrugs.machine.manual.ManualMachineType;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.DistillerMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.distiller.DistillerFluidStack;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;

import java.util.Optional;

public class DistillerBlockEntity extends BaseContainerBlockEntity implements DistillerMenu.DistillerButtonHandler, MachineStatusProvider {
    public static final int FLUID_CAPACITY = 4000;
    private static final int CRANK_HEARTBEAT_TIMEOUT_TICKS = 8;

    private final LockedTransferSlots inputTransferLocks = new LockedTransferSlots(1);
    private final StoredFluidTank inputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank outputATank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank outputBTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private NonNullList<ItemStack> buckets = NonNullList.withSize(3, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 200;
    private int crankTimeoutTicks = 0;
    private int speedPercent = 0;
    private float manualSpeedMultiplier = 1.0F;
    private double manualProgressRemainder = 0.0D;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTank.getAmount();
                case 1 -> outputATank.getAmount();
                case 2 -> outputBTank.getAmount();
                case 3 -> progress;
                case 4 -> maxProgress;
                case 5 -> isCranking() ? 1 : 0;
                case 6 -> speedPercent;
                case 7 -> inputTank.encodeFluidSyncId();
                case 8 -> outputATank.encodeFluidSyncId();
                case 9 -> outputBTank.encodeFluidSyncId();
                case 10 -> MachineEnergyAttachments.get(DistillerBlockEntity.this).storage().stored();
                case 11 -> MachineEnergyAttachments.get(DistillerBlockEntity.this).storage().capacity();
                case 12 -> MachineEnergyAttachments.get(DistillerBlockEntity.this).hasAnyEnergyStorageUpgrade() ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 3 -> progress = value;
                case 4 -> maxProgress = value;
                case 5 -> crankTimeoutTicks = value > 0 ? CRANK_HEARTBEAT_TIMEOUT_TICKS : 0;
                case 6 -> speedPercent = value;
                default -> {
                    // client-only sync fields
                }
            }
        }

        @Override
        public int getCount() {
            return 13;
        }
    };

    public DistillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DistillerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;

        boolean wasCranking = be.isCranking();
        int oldSpeed = be.speedPercent;
        int oldProgress = be.progress;
        int oldMaxProgress = be.maxProgress;

        if (FluidTransferUtil.tryProcessTransferSlot(
                be,
                DistillerMenu.INPUT_CONTAINER_SLOT,
                be.inputTank,
                be.inputTransferLocks,
                0
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                DistillerMenu.OUTPUT_A_CONTAINER_SLOT,
                be.outputATank
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                DistillerMenu.OUTPUT_B_CONTAINER_SLOT,
                be.outputBTank
        )) {
            changed = true;
        }

        be.tickCrankTimeout();
        be.refreshManualStats();

        if (be.isCranking() != wasCranking || be.speedPercent != oldSpeed) {
            changed = true;
        }

        CraftCheckResult craftCheck = be.checkCraft(serverLevel);
        if (craftCheck.recipe() == null) {
            changed |= be.setMachineStatus(craftCheck.status());
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        DistillerRecipe recipe = craftCheck.recipe();
        be.maxProgress = recipe.baseTicks();

        if (be.maxProgress != oldMaxProgress) {
            changed = true;
        }

        if (!craftCheck.craftable()) {
            changed |= be.setMachineStatus(craftCheck.status());
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        int progressPerTick = be.getManualProgressPerTick();
        if (PsyCurrentMachines.tryUseAutomationCurrentTick(be)) {
            progressPerTick += 1;
        }
        if (progressPerTick > 0) {
            changed |= be.setMachineStatus(MachineStatus.RUNNING);
            be.progress += progressPerTick;
            changed = true;

            if (be.progress >= be.maxProgress) {
                be.craft(recipe, craftCheck.recipeId());
                be.progress = 0;
                changed = true;
            }
        } else {
            changed |= be.setMachineStatus(MachineStatus.PAUSED);
        }

        if (be.progress != oldProgress) {
            changed = true;
        }

        if (changed) {
            be.sync();
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

    private static boolean dumpTank(StoredFluidTank tank) {
        if (tank.isEmpty()) {
            return false;
        }

        tank.setFluid(FluidStack.EMPTY);
        return true;
    }

    @Nullable
    private static ResourceLocation getFluidId(StoredFluidTank tank) {
        FluidStack stored = tank.getFluid();
        if (stored.isEmpty()) {
            return null;
        }

        Fluid fluid = stored.getFluid();
        if (fluid == Fluids.EMPTY) {
            return null;
        }

        return BuiltInRegistries.FLUID.getKey(fluid);
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

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.distiller");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.buckets;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.buckets = items;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new DistillerMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
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

    private record CraftCheckResult(@Nullable DistillerRecipe recipe, Optional<ResourceLocation> recipeId, MachineStatus status) {
        static CraftCheckResult craftable(RecipeHolder<DistillerRecipe> holder) {
            return new CraftCheckResult(holder.value(), Optional.of(holder.id().location()), MachineStatus.RUNNING);
        }

        static CraftCheckResult blocked(@Nullable DistillerRecipe recipe, Optional<ResourceLocation> recipeId, MachineStatus status) {
            return new CraftCheckResult(recipe, recipeId, status);
        }

        boolean craftable() {
            return this.recipe != null && this.status == MachineStatus.RUNNING;
        }
    }

    @Override
    public int getContainerSize() {
        return 3;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (slot == DistillerMenu.INPUT_CONTAINER_SLOT) {
            this.inputTransferLocks.reset(0);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.buckets = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.buckets);

        this.inputTank.load(input, "input_tank");
        this.outputATank.load(input, "output_a_tank");
        this.outputBTank.load(input, "output_b_tank");

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);

        this.speedPercent = 0;
        this.manualSpeedMultiplier = 1.0F;
        this.crankTimeoutTicks = 0;
        this.manualProgressRemainder = input.getDoubleOr("ManualProgressRemainder", 0.0D);
        this.inputTransferLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.buckets);

        this.inputTank.save(output, "input_tank");
        this.outputATank.save(output, "output_a_tank");
        this.outputBTank.save(output, "output_b_tank");

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putDouble("ManualProgressRemainder", this.manualProgressRemainder);
    }

    @Override
    public boolean onDistillerButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case DistillerMenu.RUN_BUTTON_ID -> {
                this.manualSpeedMultiplier = player instanceof ServerPlayer serverPlayer
                        ? ManualMachineSpeedHelper.getSpeedMultiplier(serverPlayer, ManualMachineType.DISTILLER)
                        : 1.0F;
                this.crankTimeoutTicks = CRANK_HEARTBEAT_TIMEOUT_TICKS;
                refreshManualStats();
                sync();
                yield true;
            }

            case DistillerMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = dumpTank(this.inputTank);
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }

            case DistillerMenu.DUMP_OUTPUT_A_BUTTON_ID -> {
                boolean dumped = dumpTank(this.outputATank);
                if (dumped) {
                    sync();
                }
                yield dumped;
            }

            case DistillerMenu.DUMP_OUTPUT_B_BUTTON_ID -> {
                boolean dumped = dumpTank(this.outputBTank);
                if (dumped) {
                    sync();
                }
                yield dumped;
            }

            default -> false;
        };
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

            FluidStack incoming = new FluidStack(fluid, containedAmount);
            int moved = this.inputTank.insert(incoming, true);
            if (moved <= 0) {
                return false;
            }

            this.inputTank.insert(incoming.copyWithAmount(moved), false);
            sync();
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

        if (held.getItem() instanceof BucketItem && requested < FluidType.BUCKET_VOLUME) {
            return false;
        }

        int extractAmount = held.getItem() instanceof BucketItem
                ? FluidType.BUCKET_VOLUME
                : requested;

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, extractAmount, tx);
            if (extracted != extractAmount) {
                return false;
            }

            tx.commit();
            this.inputTank.insert(incoming.copyWithAmount(extracted), false);
        }

        sync();
        return true;
    }

    private CraftCheckResult checkCraft(ServerLevel level) {
        ResourceLocation inputFluidId = getFluidId(this.inputTank);
        if (inputFluidId == null || this.inputTank.isEmpty()) {
            return CraftCheckResult.blocked(null, Optional.empty(), MachineStatus.MISSING_INPUT_FLUID);
        }

        Optional<RecipeHolder<DistillerRecipe>> recipeHolder = this.findRecipeByFluid(level, inputFluidId);
        if (recipeHolder.isEmpty()) {
            return CraftCheckResult.blocked(null, Optional.empty(), MachineStatus.NO_MATCHING_RECIPE);
        }

        RecipeHolder<DistillerRecipe> holder = recipeHolder.get();
        DistillerRecipe recipe = holder.value();
        Optional<ResourceLocation> recipeId = Optional.of(holder.id().location());
        if (this.inputTank.getAmount() < recipe.input().amount()) {
            return CraftCheckResult.blocked(recipe, recipeId, MachineStatus.INSUFFICIENT_INPUT_FLUID);
        }

        return this.checkOutputs(holder);
    }

    private Optional<RecipeHolder<DistillerRecipe>> findRecipeByFluid(ServerLevel level, ResourceLocation inputFluidId) {
        for (RecipeHolder<DistillerRecipe> holder : level.recipeAccess().recipeMap().byType(ModRecipeTypes.DISTILLER.get())) {
            if (holder.value().input().fluid().equals(inputFluidId)) {
                return Optional.of(holder);
            }
        }

        return Optional.empty();
    }

    private CraftCheckResult checkOutputs(RecipeHolder<DistillerRecipe> holder) {
        DistillerRecipe recipe = holder.value();
        Optional<ResourceLocation> recipeId = Optional.of(holder.id().location());
        ResourceLocation inputFluidId = getFluidId(this.inputTank);
        if (inputFluidId == null) {
            return CraftCheckResult.blocked(recipe, recipeId, MachineStatus.MISSING_INPUT_FLUID);
        }

        if (!recipe.input().fluid().equals(inputFluidId)) {
            return CraftCheckResult.blocked(null, Optional.empty(), MachineStatus.NO_MATCHING_RECIPE);
        }

        FluidStack outputA = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (outputA.isEmpty() || this.outputATank.getAddableAmount(outputA) < outputA.getAmount()) {
            return outputA.isEmpty()
                    ? CraftCheckResult.blocked(recipe, recipeId, MachineStatus.INVALID_RECIPE_OUTPUT)
                    : CraftCheckResult.blocked(recipe, recipeId, MachineStatus.OUTPUT_TANK_A_FULL);
        }

        if (recipe.output2().isPresent()) {
            DistillerFluidStack output = recipe.output2().get();
            FluidStack outputB = toFluidStack(output.fluid(), output.amount());
            if (outputB.isEmpty()) {
                return CraftCheckResult.blocked(recipe, recipeId, MachineStatus.INVALID_RECIPE_OUTPUT);
            }
            if (this.outputBTank.getAddableAmount(outputB) < outputB.getAmount()) {
                return CraftCheckResult.blocked(recipe, recipeId, MachineStatus.OUTPUT_TANK_B_FULL);
            }
        }

        return CraftCheckResult.craftable(holder);
    }

    private void craft(DistillerRecipe recipe, Optional<ResourceLocation> recipeId) {
        this.inputTank.extract(recipe.input().amount(), false);

        FluidStack outputA = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (!outputA.isEmpty()) {
            this.outputATank.insert(outputA, false);
        }

        recipe.output2().ifPresent(output -> {
            FluidStack outputB = toFluidStack(output.fluid(), output.amount());
            if (!outputB.isEmpty()) {
                this.outputBTank.insert(outputB, false);
            }
        });
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(
                this,
                recipeId,
                Optional.empty(),
                Optional.of(recipe.output1().fluid()),
                Optional.empty(),
                Optional.empty()
        );
    }

    private void tickCrankTimeout() {
        if (this.crankTimeoutTicks > 0) {
            this.crankTimeoutTicks--;
        }
    }

    private boolean isCranking() {
        return this.crankTimeoutTicks > 0;
    }

    private void refreshManualStats() {
        this.speedPercent = this.isCranking() ? Math.round(this.manualSpeedMultiplier * 100.0F) : 0;
    }

    private int getManualProgressPerTick() {
        if (!this.isCranking()) {
            return 0;
        }
        this.manualProgressRemainder += this.manualSpeedMultiplier;
        int whole = (int) this.manualProgressRemainder;
        if (whole > 0) {
            this.manualProgressRemainder -= whole;
        }
        return whole;
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(this, inputTank, outputATank, outputBTank);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isFluidContainer(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }
}
