package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
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
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.FluidFiltererMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererItemResult;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipe;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipeInput;

import java.util.Optional;

public class FluidFiltererBlockEntity extends BaseContainerBlockEntity implements FluidFiltererMenu.FluidFiltererButtonHandler {
    public static final int FLUID_CAPACITY = 4000;
    private final LockedTransferSlots inputTransferLocks = new LockedTransferSlots(1);
    private final StoredFluidTank inputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank outputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private NonNullList<ItemStack> items = NonNullList.withSize(FluidFiltererMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 0;
    private boolean buttonHeld = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTank.getAmount();
                case 1 -> outputTank.getAmount();
                case 2 -> progress;
                case 3 -> maxProgress;
                case 4 -> inputTank.encodeFluidSyncId();
                case 5 -> outputTank.encodeFluidSyncId();
                case 6 -> buttonHeld ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 2 -> progress = value;
                case 3 -> maxProgress = value;
                case 6 -> buttonHeld = value != 0;
                default -> {
                    // client-only sync fields
                }
            }
        }

        @Override
        public int getCount() {
            return 7;
        }
    };

    public FluidFiltererBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_FILTERER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FluidFiltererBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = FluidTransferUtil.tryProcessTransferSlot(
                be,
                FluidFiltererMenu.INPUT_CONTAINER_SLOT,
                be.inputTank,
                be.inputTransferLocks,
                0
        );

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                FluidFiltererMenu.OUTPUT_A_CONTAINER_SLOT,
                be.outputTank
        )) {
            changed = true;
        }

        Optional<RecipeHolder<FluidFiltererRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);

        if (recipeHolder.isPresent()) {
            FluidFiltererRecipe recipe = recipeHolder.get().value();
            int required = Math.max(1, recipe.clicksRequired());

            if (be.maxProgress != required) {
                be.maxProgress = required;
                changed = true;
            }

            if (!be.canCraft(recipe)) {
                if (be.progress != 0) {
                    be.progress = 0;
                    changed = true;
                }
            } else if (be.buttonHeld) {
                if (be.advanceFiltering(recipe, null)) {
                    changed = true;
                }
            }
        } else {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (be.maxProgress != 0) {
                be.maxProgress = 0;
                changed = true;
            }
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
        return Component.translatable("container.mydrugs.fluid_filterer");
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
        return new FluidFiltererMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return FluidFiltererMenu.MACHINE_SLOT_COUNT;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.inputTank.load(input, "input_tank");
        this.outputTank.load(input, "output_tank");

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 0);
        this.buttonHeld = input.getBooleanOr("ButtonHeld", false);

        this.inputTransferLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        this.inputTank.save(output, "input_tank");
        this.outputTank.save(output, "output_tank");

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putBoolean("ButtonHeld", this.buttonHeld);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (slot == FluidFiltererMenu.INPUT_CONTAINER_SLOT) {
            this.inputTransferLocks.reset(0);
        }

        if (slot == FluidFiltererMenu.FILTER_SLOT) {
            this.progress = 0;
            this.buttonHeld = false;
            this.sync();
        }
    }

    @Override
    public boolean onFiltererButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case FluidFiltererMenu.RUN_BUTTON_START_ID -> {
                this.buttonHeld = true;
                sync();
                yield true;
            }
            case FluidFiltererMenu.RUN_BUTTON_STOP_ID -> {
                this.buttonHeld = false;
                sync();
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean onDumpButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case FluidFiltererMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = dumpTank(this.inputTank);
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case FluidFiltererMenu.DUMP_OUTPUT_A_BUTTON_ID -> {
                boolean dumped = dumpTank(this.outputTank);
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    private boolean advanceFiltering(FluidFiltererRecipe recipe, @Nullable Player player) {
        this.maxProgress = Math.max(1, recipe.clicksRequired());

        if (!canCraft(recipe)) {
            this.progress = 0;
            return false;
        }

        int hungerCost = Math.max(0, recipe.hungerPerTick());
        if (!canPlayerPayHunger(player, hungerCost)) {
            return false;
        }

        if (!hasUsableFilter()) {
            this.progress = 0;
            this.buttonHeld = false;
            return false;
        }

        payHunger(player, hungerCost);
        damageFilter();

        if (!hasUsableFilter()) {
            this.progress = 0;
            this.buttonHeld = false;
            return false;
        }

        this.progress++;

        if (this.progress >= this.maxProgress) {
            craft(recipe);
            this.progress = 0;
        }

        return true;
    }

    private boolean canPlayerPayHunger(@Nullable Player player, int hungerCost) {
        if (hungerCost <= 0) {
            return true;
        }

        if (player == null) {
            return true;
        }

        if (player.getAbilities().instabuild) {
            return true;
        }

        FoodData foodData = player.getFoodData();
        return foodData.getFoodLevel() >= hungerCost;
    }

    private void payHunger(@Nullable Player player, int hungerCost) {
        if (hungerCost <= 0) {
            return;
        }

        if (player == null) {
            return;
        }

        if (player.getAbilities().instabuild) {
            return;
        }

        FoodData foodData = player.getFoodData();
        foodData.setFoodLevel(Math.max(0, foodData.getFoodLevel() - hungerCost));
    }

    private boolean hasUsableFilter() {
        ItemStack stack = this.getItem(FluidFiltererMenu.FILTER_SLOT);
        if (stack.isEmpty()) {
            return false;
        }

        if (!stack.is(ModItems.FLUID_FILTER.get())) {
            return false;
        }

        return !stack.isDamageableItem() || stack.getDamageValue() < stack.getMaxDamage();
    }

    private void damageFilter() {
        ItemStack stack = this.getItem(FluidFiltererMenu.FILTER_SLOT);
        if (stack.isEmpty() || !stack.is(ModItems.FLUID_FILTER.get())) {
            return;
        }

        if (!stack.isDamageableItem()) {
            return;
        }

        stack.setDamageValue(stack.getDamageValue() + 1);

        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            this.setItem(FluidFiltererMenu.FILTER_SLOT, ItemStack.EMPTY);
        } else {
            this.setChanged();
        }
    }

    public boolean tryInsertFluidFromHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) {
            return false;
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

        this.progress = 0;
        sync();
        return true;
    }

    public boolean tryExtractFluidToHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }

        int tankAmount = this.outputTank.getAmount();
        ResourceLocation tankFluidId = this.outputTank.getFluidId();

        if (tankAmount <= 0 || tankFluidId == null) {
            return false;
        }

        ItemAccess access = ItemAccess.forPlayerInteraction(player, hand).oneByOne();
        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(tankFluidId);
        if (fluid == Fluids.EMPTY) {
            return false;
        }

        FluidResource resource = FluidResource.of(fluid);

        int currentItemAmount = handler.getAmountAsInt(0);
        FluidResource currentItemResource = handler.getResource(0);

        if (!currentItemResource.isEmpty()) {
            ResourceLocation itemFluidId = BuiltInRegistries.FLUID.getKey(currentItemResource.getFluid());
            if (!tankFluidId.equals(itemFluidId)) {
                return false;
            }
        }

        int request;
        if (held.is(net.minecraft.world.item.Items.BUCKET)) {
            if (tankAmount < FluidType.BUCKET_VOLUME) {
                return false;
            }
            request = FluidType.BUCKET_VOLUME;
        } else {
            int capacity = handler.getCapacityAsInt(0, resource);
            int remainingSpace = capacity - currentItemAmount;
            if (remainingSpace <= 0) {
                return false;
            }
            request = Math.min(tankAmount, remainingSpace);
        }

        try (var tx = Transaction.openRoot()) {
            int inserted = handler.insert(resource, request, tx);
            if (inserted <= 0) {
                return false;
            }

            tx.commit();
            this.outputTank.extract(inserted, false);
            sync();
            return true;
        }
    }

    private Optional<RecipeHolder<FluidFiltererRecipe>> getCurrentRecipe(ServerLevel level) {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        if (inputFluidId == null || this.inputTank.isEmpty()) {
            return Optional.empty();
        }

        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.FLUID_FILTERING.get(),
                new FluidFiltererRecipeInput(inputFluidId, this.inputTank.getAmount()),
                level
        );
    }

    private boolean canCraft(FluidFiltererRecipe recipe) {
        if (this.inputTank.isEmpty()) {
            return false;
        }

        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        if (inputFluidId == null) {
            return false;
        }

        if (!recipe.input().fluid().equals(inputFluidId)) {
            return false;
        }

        if (this.inputTank.getAmount() < recipe.input().amount()) {
            return false;
        }

        if (!hasUsableFilter()) {
            return false;
        }

        if (recipe.output2().isPresent()) {
            return false;
        }

        FluidStack output = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (output.isEmpty() || this.outputTank.getAddableAmount(output) < output.getAmount()) {
            return false;
        }

        return canStoreResidue(recipe.outputItem());
    }

    private boolean canStoreResidue(Optional<FluidFiltererItemResult> optionalResult) {
        if (optionalResult.isEmpty()) {
            return true;
        }

        ItemStack output = optionalResult.get().createStack();
        if (output.isEmpty()) {
            return true;
        }

        ItemStack residueSlot = this.getItem(FluidFiltererMenu.RESIDUE_SLOT);
        if (residueSlot.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(residueSlot, output)) {
            return false;
        }

        int limit = Math.min(residueSlot.getMaxStackSize(), this.getMaxStackSize());
        return residueSlot.getCount() + output.getCount() <= limit;
    }

    private void addResidue(Optional<FluidFiltererItemResult> optionalResult) {
        if (optionalResult.isEmpty()) {
            return;
        }

        ItemStack output = optionalResult.get().createStack();
        if (output.isEmpty()) {
            return;
        }

        ItemStack residueSlot = this.getItem(FluidFiltererMenu.RESIDUE_SLOT);
        if (residueSlot.isEmpty()) {
            this.setItem(FluidFiltererMenu.RESIDUE_SLOT, output.copy());
            return;
        }

        residueSlot.grow(output.getCount());
        this.setChanged();
    }

    private void craft(FluidFiltererRecipe recipe) {
        this.inputTank.extract(recipe.input().amount(), false);

        FluidStack output = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (!output.isEmpty()) {
            this.outputTank.insert(output, false);
        }

        addResidue(recipe.outputItem());
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case FluidFiltererMenu.INPUT_CONTAINER_SLOT,
                 FluidFiltererMenu.OUTPUT_A_CONTAINER_SLOT -> isFluidContainer(stack);
            case FluidFiltererMenu.FILTER_SLOT -> stack.is(ModItems.FLUID_FILTER.get());
            case FluidFiltererMenu.RESIDUE_SLOT -> false;
            default -> false;
        };
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }
}