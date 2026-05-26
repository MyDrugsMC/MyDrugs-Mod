package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fuel.MachineFuelUtil;
import org.mydrugs.mydrugs.menu.PsychotropeDistilleryMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.psychotrope_distillery.PsychotropeDistilleryRecipe;
import org.mydrugs.mydrugs.recipes.psychotrope_distillery.PsychotropeDistilleryRecipeInput;

import java.util.Optional;

public final class PsychotropeDistilleryBlockEntity extends BaseContainerBlockEntity implements MachineStatusProvider {
    public static final int SLOT_DRUG_INPUT = 0;
    public static final int SLOT_REAGENT = 1;
    public static final int SLOT_FUEL = 2;
    public static final int SLOT_EXTRACT_OUTPUT = 3;
    public static final int SLOT_RESIDUE_OUTPUT = 4;
    public static final int SLOT_COUNT = 5;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int progress;
    private int maxProgress = 200;
    private int burnTimeRemaining;
    private int burnTimeTotal;
    private int residueProgress;
    private @Nullable ResourceLocation activeRecipeId;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> progress;
                case 1 -> maxProgress;
                case 2 -> burnTimeRemaining;
                case 3 -> burnTimeTotal;
                case 4 -> residueProgress;
                case 5 -> machineStatus.networkId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> progress = value;
                case 1 -> maxProgress = value;
                case 2 -> burnTimeRemaining = value;
                case 3 -> burnTimeTotal = value;
                case 4 -> residueProgress = value;
                case 5 -> machineStatus = MachineStatus.byNetworkId(value);
                default -> {
                }
            }
        }

        @Override
        public int getCount() {
            return PsychotropeDistilleryMenu.DATA_COUNT;
        }
    };

    public PsychotropeDistilleryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PSYCHOTROPE_DISTILLERY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, PsychotropeDistilleryBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;
        if (be.burnTimeRemaining > 0) {
            be.burnTimeRemaining--;
            changed = true;
        }

        CraftCheckResult check = be.checkCraft(serverLevel);
        if (check.recipeHolder().isEmpty()) {
            changed |= be.setMachineStatus(check.status());
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        RecipeHolder<PsychotropeDistilleryRecipe> holder = check.recipeHolder().get();
        PsychotropeDistilleryRecipe recipe = holder.value();
        ResourceLocation recipeId = holder.id().location();
        if (!recipeId.equals(be.activeRecipeId)) {
            be.activeRecipeId = recipeId;
            be.residueProgress = 0;
            changed = true;
        }
        if (be.maxProgress != recipe.baseTicks()) {
            be.maxProgress = recipe.baseTicks();
            changed = true;
        }

        if (!check.craftable()) {
            changed |= be.setMachineStatus(check.status());
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        if (be.burnTimeRemaining <= 0 && be.tryConsumeFuel()) {
            changed = true;
        }

        if (be.burnTimeRemaining <= 0) {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_HEAT);
            changed |= be.resetProgress();
            if (changed) {
                be.sync();
            }
            return;
        }

        be.progress++;
        changed |= be.setMachineStatus(MachineStatus.RUNNING);
        changed = true;

        if (be.progress >= be.maxProgress) {
            be.craft(holder);
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    public static boolean isFuel(ItemStack stack, @Nullable Level level) {
        return MachineFuelUtil.isFuel(stack, level, MachineFuelUtil.VANILLA);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.psychotrope_distillery");
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
        return new PsychotropeDistilleryMenu(
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
            case SLOT_DRUG_INPUT, SLOT_REAGENT -> true;
            case SLOT_FUEL -> isFuel(stack, this.level);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
        if (slot == SLOT_DRUG_INPUT || slot == SLOT_REAGENT) {
            this.progress = 0;
        }
    }

    @Override
    public MachineStatus getMachineStatus() {
        return this.machineStatus;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 200);
        this.burnTimeRemaining = input.getIntOr("burn_time_remaining", 0);
        this.burnTimeTotal = input.getIntOr("burn_time_total", 0);
        this.residueProgress = input.getIntOr("residue_progress", 0);
        String activeRecipe = input.getStringOr("active_recipe", "");
        this.activeRecipeId = activeRecipe.isBlank() ? null : ResourceLocation.tryParse(activeRecipe);
        this.machineStatus = MachineStatus.byNetworkId(input.getIntOr("machine_status", MachineStatus.IDLE.networkId()));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);
        output.putInt("burn_time_remaining", this.burnTimeRemaining);
        output.putInt("burn_time_total", this.burnTimeTotal);
        output.putInt("residue_progress", this.residueProgress);
        output.putString("active_recipe", this.activeRecipeId == null ? "" : this.activeRecipeId.toString());
        output.putInt("machine_status", this.machineStatus.networkId());
    }

    private CraftCheckResult checkCraft(ServerLevel level) {
        ItemStack input = this.getItem(SLOT_DRUG_INPUT);
        if (input.isEmpty()) {
            return CraftCheckResult.blocked(Optional.empty(), MachineStatus.MISSING_INPUT_ITEM);
        }

        ItemStack reagent = this.getItem(SLOT_REAGENT);
        if (reagent.isEmpty()) {
            return CraftCheckResult.blocked(Optional.empty(), MachineStatus.MISSING_CATALYST);
        }

        PsychotropeDistilleryRecipeInput recipeInput = new PsychotropeDistilleryRecipeInput(input, reagent);
        Optional<RecipeHolder<PsychotropeDistilleryRecipe>> match = level.recipeAccess().getRecipeFor(
                ModRecipeTypes.PSYCHOTROPE_DISTILLERY.get(),
                recipeInput,
                level
        );
        if (match.isEmpty()) {
            return CraftCheckResult.blocked(Optional.empty(), MachineStatus.NO_MATCHING_RECIPE);
        }

        PsychotropeDistilleryRecipe recipe = match.get().value();
        if (!canAccept(SLOT_EXTRACT_OUTPUT, recipe.result())) {
            return CraftCheckResult.blocked(match, MachineStatus.OUTPUT_SLOT_FULL);
        }

        if (recipe.hasResidue() && this.residueProgress + 1 >= recipe.residueEvery()) {
            ItemStack residue = recipe.residueResult().orElse(ItemStack.EMPTY);
            if (!canAccept(SLOT_RESIDUE_OUTPUT, residue)) {
                return CraftCheckResult.blocked(match, MachineStatus.OUTPUT_SLOT_FULL);
            }
        }

        return CraftCheckResult.craftable(match);
    }

    private void craft(RecipeHolder<PsychotropeDistilleryRecipe> holder) {
        PsychotropeDistilleryRecipe recipe = holder.value();
        this.getItem(SLOT_DRUG_INPUT).shrink(1);
        this.getItem(SLOT_REAGENT).shrink(1);
        addToSlot(SLOT_EXTRACT_OUTPUT, recipe.result());

        if (recipe.hasResidue()) {
            this.residueProgress++;
            if (this.residueProgress >= recipe.residueEvery()) {
                recipe.residueResult().ifPresent(residue -> addToSlot(SLOT_RESIDUE_OUTPUT, residue));
                this.residueProgress = 0;
            }
        }

        AdvancementEventHooks.machineRecipeCompleted(
                this,
                Optional.of(holder.id().location()),
                itemId(recipe.result())
        );
    }

    private boolean tryConsumeFuel() {
        MachineFuelUtil.FuelUse fuelUse = MachineFuelUtil.consumeOne(
                this.getItem(SLOT_FUEL),
                this.level,
                MachineFuelUtil.VANILLA
        );
        if (!fuelUse.consumed()) {
            return false;
        }
        this.setItem(SLOT_FUEL, fuelUse.remainingStack());
        this.burnTimeRemaining = fuelUse.burnTime();
        this.burnTimeTotal = fuelUse.burnTime();
        return true;
    }

    private boolean resetProgress() {
        if (this.progress == 0) {
            return false;
        }
        this.progress = 0;
        return true;
    }

    private boolean setMachineStatus(MachineStatus status) {
        if (this.machineStatus == status) {
            return false;
        }
        this.machineStatus = status;
        return true;
    }

    private boolean canAccept(int slot, ItemStack incoming) {
        if (incoming.isEmpty()) {
            return true;
        }
        ItemStack existing = this.getItem(slot);
        if (existing.isEmpty()) {
            return incoming.getCount() <= Math.min(incoming.getMaxStackSize(), this.getMaxStackSize());
        }
        return ItemStack.isSameItemSameComponents(existing, incoming)
                && existing.getCount() + incoming.getCount() <= Math.min(existing.getMaxStackSize(), this.getMaxStackSize());
    }

    private void addToSlot(int slot, ItemStack incoming) {
        if (incoming.isEmpty()) {
            return;
        }
        ItemStack existing = this.getItem(slot);
        if (existing.isEmpty()) {
            this.setItem(slot, incoming.copy());
        } else {
            existing.grow(incoming.getCount());
            this.setItem(slot, existing);
        }
    }

    private Optional<ResourceLocation> itemId(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()));
    }

    private void sync() {
        MachineSync.sync(this);
    }

    private record CraftCheckResult(Optional<RecipeHolder<PsychotropeDistilleryRecipe>> recipeHolder, MachineStatus status) {
        static CraftCheckResult craftable(Optional<RecipeHolder<PsychotropeDistilleryRecipe>> holder) {
            return new CraftCheckResult(holder, MachineStatus.RUNNING);
        }

        static CraftCheckResult blocked(Optional<RecipeHolder<PsychotropeDistilleryRecipe>> holder, MachineStatus status) {
            return new CraftCheckResult(holder, status);
        }

        boolean craftable() {
            return this.recipeHolder.isPresent() && this.status == MachineStatus.RUNNING;
        }
    }
}
