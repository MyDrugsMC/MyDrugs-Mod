package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.VanillaContainerWrapper;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.menu.FluidFiltererMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererFluidStack;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererItemResult;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipe;
import org.mydrugs.mydrugs.recipes.filterer.FluidFiltererRecipeInput;

import java.util.Optional;

public class FluidFiltererBlockEntity extends BaseContainerBlockEntity implements FluidFiltererMenu.FluidFiltererButtonHandler {
    public static final int FLUID_CAPACITY = 4000;

    private static final int TANK_INPUT = 0;
    private static final int TANK_OUTPUT_A = 1;
    private static final int TANK_OUTPUT_B = 2;

    private NonNullList<ItemStack> items = NonNullList.withSize(FluidFiltererMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);

    @Nullable
    private ResourceLocation inputFluidId = null;
    @Nullable
    private ResourceLocation outputFluid1Id = null;
    @Nullable
    private ResourceLocation outputFluid2Id = null;

    private int inputTankAmount = 0;
    private int outputATankAmount = 0;
    private int outputBTankAmount = 0;

    private int progress = 0;
    private int maxProgress = 0;
    private boolean buttonHeld = false;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTankAmount;
                case 1 -> outputATankAmount;
                case 2 -> outputBTankAmount;
                case 3 -> progress;
                case 4 -> maxProgress;
                case 5 -> encodeFluidForSync(inputFluidId);
                case 6 -> encodeFluidForSync(outputFluid1Id);
                case 7 -> encodeFluidForSync(outputFluid2Id);
                case 8 -> buttonHeld ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> inputTankAmount = value;
                case 1 -> outputATankAmount = value;
                case 2 -> outputBTankAmount = value;
                case 3 -> progress = value;
                case 4 -> maxProgress = value;
                case 5, 6, 7 -> {
                }
                case 8 -> buttonHeld = value != 0;
            }
        }

        @Override
        public int getCount() {
            return 9;
        }
    };

    public FluidFiltererBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUID_FILTERER.get(), pos, state);
    }

    private static int encodeFluidForSync(@Nullable ResourceLocation fluidId) {
        if (fluidId == null) {
            return -1;
        }

        Optional<Holder.Reference<Fluid>> optional = BuiltInRegistries.FLUID.get(fluidId);
        if (optional.isEmpty()) {
            return -1;
        }

        Fluid fluid = optional.get().value();
        if (fluid == Fluids.EMPTY) {
            return -1;
        }

        return BuiltInRegistries.FLUID.getId(fluid);
    }

    @Nullable
    private static ResourceLocation readFluidId(ValueInput input, String key) {
        String value = input.getStringOr(key, "");
        return value.isEmpty() ? null : ResourceLocation.parse(value);
    }

    private static void writeFluidId(ValueOutput output, String key, @Nullable ResourceLocation fluidId, int amount) {
        if (fluidId != null && amount > 0) {
            output.putString(key, fluidId.toString());
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

    public static void tick(Level level, BlockPos pos, BlockState state, FluidFiltererBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;

        if (be.tryDrainInputContainerSlot()) {
            changed = true;
        }

        if (be.tryFillOutputContainerSlot(FluidFiltererMenu.OUTPUT_A_CONTAINER_SLOT, TANK_OUTPUT_A)) {
            changed = true;
        }

        if (be.tryFillOutputContainerSlot(FluidFiltererMenu.OUTPUT_B_CONTAINER_SLOT, TANK_OUTPUT_B)) {
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

        this.inputFluidId = readFluidId(input, "InputFluid");
        this.outputFluid1Id = readFluidId(input, "OutputFluid1");
        this.outputFluid2Id = readFluidId(input, "OutputFluid2");

        this.inputTankAmount = input.getIntOr("InputTank", 0);
        this.outputATankAmount = input.getIntOr("OutputATank", 0);
        this.outputBTankAmount = input.getIntOr("OutputBTank", 0);

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 0);
        this.buttonHeld = input.getBooleanOr("ButtonHeld", false);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        writeFluidId(output, "InputFluid", this.inputFluidId, this.inputTankAmount);
        writeFluidId(output, "OutputFluid1", this.outputFluid1Id, this.outputATankAmount);
        writeFluidId(output, "OutputFluid2", this.outputFluid2Id, this.outputBTankAmount);

        output.putInt("InputTank", this.inputTankAmount);
        output.putInt("OutputATank", this.outputATankAmount);
        output.putInt("OutputBTank", this.outputBTankAmount);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putBoolean("ButtonHeld", this.buttonHeld);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);
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
                boolean dumped = dumpTank(TANK_INPUT);
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case FluidFiltererMenu.DUMP_OUTPUT_A_BUTTON_ID -> {
                boolean dumped = dumpTank(TANK_OUTPUT_A);
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            case FluidFiltererMenu.DUMP_OUTPUT_B_BUTTON_ID -> {
                boolean dumped = dumpTank(TANK_OUTPUT_B);
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    private boolean dumpTank(int tank) {
        if (getTankAmount(tank) <= 0) {
            return false;
        }

        setTank(tank, null, 0);
        return true;
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

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (incomingId == null) {
            return false;
        }

        int requested = getAddableAmount(TANK_INPUT, incomingId, containedAmount);
        if (requested <= 0) {
            return false;
        }

        if (held.getItem() instanceof BucketItem && requested < FluidType.BUCKET_VOLUME) {
            return false;
        }

        int extractAmount = held.getItem() instanceof BucketItem ? FluidType.BUCKET_VOLUME : requested;

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, extractAmount, tx);
            if (extracted != extractAmount) {
                return false;
            }

            tx.commit();
            addFluidToTank(TANK_INPUT, incomingId, extracted);
        }

        this.progress = 0;
        sync();
        return true;
    }

    public boolean tryExtractFluidToHeld(Player player, InteractionHand hand, ItemStack held) {
        if (held.isEmpty()) {
            return false;
        }

        return tryExtractFromTankToHeld(player, hand, held, TANK_OUTPUT_A)
                || tryExtractFromTankToHeld(player, hand, held, TANK_OUTPUT_B);
    }

    private boolean tryExtractFromTankToHeld(Player player, InteractionHand hand, ItemStack held, int tank) {
        int tankAmount = getTankAmount(tank);
        ResourceLocation tankFluidId = getTankFluidId(tank);

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
        if (held.is(Items.BUCKET)) {
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
            removeFluidFromTank(tank, inserted);
            sync();
            return true;
        }
    }

    private boolean tryDrainInputContainerSlot() {
        ItemStack stack = this.getItem(FluidFiltererMenu.INPUT_CONTAINER_SLOT);
        if (stack.isEmpty()) {
            return false;
        }

        var itemHandler = VanillaContainerWrapper.of(this);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(itemHandler, FluidFiltererMenu.INPUT_CONTAINER_SLOT)
                .oneByOne();

        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (incomingId == null) {
            return false;
        }

        int toMove = getAddableAmount(TANK_INPUT, incomingId, containedAmount);
        if (toMove <= 0) {
            return false;
        }

        if (stack.getItem() instanceof BucketItem && toMove < FluidType.BUCKET_VOLUME) {
            return false;
        }

        int request = stack.getItem() instanceof BucketItem
                ? FluidType.BUCKET_VOLUME
                : toMove;

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, request, tx);
            if (extracted != request) {
                return false;
            }

            tx.commit();
            addFluidToTank(TANK_INPUT, incomingId, extracted);
            return true;
        }
    }

    private boolean tryFillOutputContainerSlot(int itemSlot, int tank) {
        ItemStack stack = this.getItem(itemSlot);
        if (stack.isEmpty()) {
            return false;
        }

        int tankAmount = getTankAmount(tank);
        ResourceLocation tankFluidId = getTankFluidId(tank);
        if (tankAmount <= 0 || tankFluidId == null) {
            return false;
        }

        var itemHandler = VanillaContainerWrapper.of(this);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(itemHandler, itemSlot)
                .oneByOne();

        var handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = FluidResource.of(BuiltInRegistries.FLUID.getValue(tankFluidId));
        if (resource.isEmpty()) {
            return false;
        }

        int itemAmount = handler.getAmountAsInt(0);
        FluidResource itemResource = handler.getResource(0);

        if (!itemResource.isEmpty()) {
            ResourceLocation itemFluidId = BuiltInRegistries.FLUID.getKey(itemResource.getFluid());
            if (!tankFluidId.equals(itemFluidId)) {
                return false;
            }
        }

        int request;
        if (stack.is(Items.BUCKET)) {
            if (tankAmount < FluidType.BUCKET_VOLUME) {
                return false;
            }
            request = FluidType.BUCKET_VOLUME;
        } else {
            int capacity = handler.getCapacityAsInt(0, resource);
            int remainingSpace = capacity - itemAmount;
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
            removeFluidFromTank(tank, inserted);
            return true;
        }
    }

    private Optional<RecipeHolder<FluidFiltererRecipe>> getCurrentRecipe(ServerLevel level) {
        if (this.inputFluidId == null || this.inputTankAmount <= 0) {
            return Optional.empty();
        }

        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.FLUID_FILTERING.get(),
                new FluidFiltererRecipeInput(this.inputFluidId, this.inputTankAmount),
                level
        );
    }

    private boolean canCraft(FluidFiltererRecipe recipe) {
        if (this.inputFluidId == null) {
            return false;
        }

        if (!recipe.input().fluid().equals(this.inputFluidId)) {
            return false;
        }

        if (this.inputTankAmount < recipe.input().amount()) {
            return false;
        }

        if (!hasUsableFilter()) {
            return false;
        }

        if (getAddableAmount(TANK_OUTPUT_A, recipe.output1().fluid(), recipe.output1().amount()) < recipe.output1().amount()) {
            return false;
        }

        if (recipe.output2().isPresent()) {
            FluidFiltererFluidStack output = recipe.output2().get();
            if (getAddableAmount(TANK_OUTPUT_B, output.fluid(), output.amount()) < output.amount()) {
                return false;
            }
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
        removeFluidFromTank(TANK_INPUT, recipe.input().amount());
        addFluidToTank(TANK_OUTPUT_A, recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output -> addFluidToTank(TANK_OUTPUT_B, output.fluid(), output.amount()));
        addResidue(recipe.outputItem());
    }

    private int getAddableAmount(int tank, ResourceLocation incomingId, int requestedAmount) {
        if (requestedAmount <= 0) {
            return 0;
        }

        int currentAmount = getTankAmount(tank);
        int freeSpace = FLUID_CAPACITY - currentAmount;
        if (freeSpace <= 0) {
            return 0;
        }

        ResourceLocation currentFluid = getTankFluidId(tank);
        boolean matchesExisting = currentAmount > 0 && incomingId.equals(currentFluid);
        boolean emptyTank = currentAmount <= 0;

        if (!matchesExisting && !emptyTank) {
            return 0;
        }

        return Math.min(requestedAmount, freeSpace);
    }

    private void addFluidToTank(int tank, ResourceLocation fluidId, int amount) {
        if (amount <= 0) {
            return;
        }

        int currentAmount = getTankAmount(tank);
        ResourceLocation currentFluid = getTankFluidId(tank);

        if (currentAmount <= 0) {
            setTank(tank, fluidId, Math.min(FLUID_CAPACITY, amount));
            return;
        }

        if (fluidId.equals(currentFluid)) {
            setTank(tank, currentFluid, Math.min(FLUID_CAPACITY, currentAmount + amount));
        }
    }

    private void removeFluidFromTank(int tank, int amount) {
        if (amount <= 0) {
            return;
        }

        int remaining = getTankAmount(tank) - amount;
        if (remaining <= 0) {
            setTank(tank, null, 0);
            return;
        }

        setTank(tank, getTankFluidId(tank), remaining);
    }

    @Nullable
    private ResourceLocation getTankFluidId(int tank) {
        return switch (tank) {
            case TANK_INPUT -> this.inputFluidId;
            case TANK_OUTPUT_A -> this.outputFluid1Id;
            case TANK_OUTPUT_B -> this.outputFluid2Id;
            default -> null;
        };
    }

    private int getTankAmount(int tank) {
        return switch (tank) {
            case TANK_INPUT -> this.inputTankAmount;
            case TANK_OUTPUT_A -> this.outputATankAmount;
            case TANK_OUTPUT_B -> this.outputBTankAmount;
            default -> 0;
        };
    }

    private void setTank(int tank, @Nullable ResourceLocation fluidId, int amount) {
        int clampedAmount = Math.max(0, Math.min(FLUID_CAPACITY, amount));
        ResourceLocation finalFluid = clampedAmount > 0 ? fluidId : null;

        switch (tank) {
            case TANK_INPUT -> {
                this.inputFluidId = finalFluid;
                this.inputTankAmount = clampedAmount;
            }
            case TANK_OUTPUT_A -> {
                this.outputFluid1Id = finalFluid;
                this.outputATankAmount = clampedAmount;
            }
            case TANK_OUTPUT_B -> {
                this.outputFluid2Id = finalFluid;
                this.outputBTankAmount = clampedAmount;
            }
        }
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
                 FluidFiltererMenu.OUTPUT_A_CONTAINER_SLOT,
                 FluidFiltererMenu.OUTPUT_B_CONTAINER_SLOT -> isFluidContainer(stack);
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