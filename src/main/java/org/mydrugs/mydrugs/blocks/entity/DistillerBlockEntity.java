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
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
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
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.menu.DistillerMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipeInput;

import java.lang.ref.Reference;
import java.util.ArrayDeque;
import java.util.Optional;

public class DistillerBlockEntity extends BaseContainerBlockEntity implements DistillerMenu.DistillerButtonHandler {
    public static final int FLUID_CAPACITY = 4000;

    private static final int TANK_INPUT = 0;
    private static final int TANK_OUTPUT_A = 1;
    private static final int TANK_OUTPUT_B = 2;

    private NonNullList<ItemStack> buckets = NonNullList.withSize(3, ItemStack.EMPTY);

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
    private int maxProgress = 200;

    private final ArrayDeque<Long> recentClicks = new ArrayDeque<>();
    private int clicksPerSec = 0;
    private int speedPercent = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTankAmount;
                case 1 -> outputATankAmount;
                case 2 -> outputBTankAmount;
                case 3 -> progress;
                case 4 -> maxProgress;
                case 5 -> clicksPerSec;
                case 6 -> speedPercent;
                case 7 -> encodeFluidForSync(inputFluidId);
                case 8 -> encodeFluidForSync(outputFluid1Id);
                case 9 -> encodeFluidForSync(outputFluid2Id);
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
                case 5 -> clicksPerSec = value;
                case 6 -> speedPercent = value;

                // client dummy menu only; server doesn't need these written back
                case 7, 8, 9 -> {
                }
            }
        }

        @Override
        public int getCount() {
            return 10;
        }
    };

    private static int encodeFluidForSync(@Nullable ResourceLocation fluidId) {
        if (fluidId == null) {
            return -1;
        }

        Optional<Holder.Reference<Fluid>> optional = BuiltInRegistries.FLUID.get(fluidId);

        if (optional.isEmpty()) return -1;

        Fluid fluid = optional.get().value();
        if (fluid == Fluids.EMPTY) {
            return -1;
        }

        return BuiltInRegistries.FLUID.getId(fluid);
    }

    public DistillerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DISTILLER.get(), pos, state);
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
    public int getContainerSize() {
        return 3;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.buckets = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.buckets);

        this.inputFluidId = readFluidId(input, "InputFluid");
        this.outputFluid1Id = readFluidId(input, "OutputFluid1");
        this.outputFluid2Id = readFluidId(input, "OutputFluid2");

        this.inputTankAmount = input.getIntOr("InputTank", 0);
        this.outputATankAmount = input.getIntOr("OutputATank", 0);
        this.outputBTankAmount = input.getIntOr("OutputBTank", 0);
        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);

        // transient runtime values
        this.clicksPerSec = 0;
        this.speedPercent = 0;
        this.recentClicks.clear();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.buckets);

        writeFluidId(output, "InputFluid", this.inputFluidId, this.inputTankAmount);
        writeFluidId(output, "OutputFluid1", this.outputFluid1Id, this.outputATankAmount);
        writeFluidId(output, "OutputFluid2", this.outputFluid2Id, this.outputBTankAmount);

        output.putInt("InputTank", this.inputTankAmount);
        output.putInt("OutputATank", this.outputATankAmount);
        output.putInt("OutputBTank", this.outputBTankAmount);
        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
    }

    @Override
    public boolean onDistillerButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case DistillerMenu.RUN_BUTTON_ID -> {
                long now = this.level.getGameTime();
                this.recentClicks.addLast(now);
                refreshClickStats(now);
                sync();
                yield true;
            }

            case DistillerMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = dumpTank(TANK_INPUT);
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }

            case DistillerMenu.DUMP_OUTPUT_A_BUTTON_ID -> {
                boolean dumped = dumpTank(TANK_OUTPUT_A);
                if (dumped) {
                    sync();
                }
                yield dumped;
            }

            case DistillerMenu.DUMP_OUTPUT_B_BUTTON_ID -> {
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

            int moved = getAddableAmount(TANK_INPUT, incomingId, containedAmount);
            if (moved <= 0) {
                return false;
            }

            addFluidToTank(TANK_INPUT, incomingId, moved);
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

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(resource.getFluid());
        if (incomingId == null) {
            return false;
        }

        int requested = getAddableAmount(TANK_INPUT, incomingId, containedAmount);
        if (requested <= 0) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, requested, tx);
            if (extracted <= 0) {
                return false;
            }

            tx.commit();
            addFluidToTank(TANK_INPUT, incomingId, extracted);
        }

        sync();
        return true;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, DistillerBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = false;

        int oldCps = be.clicksPerSec;
        int oldSpeed = be.speedPercent;
        int oldProgress = be.progress;
        int oldMaxProgress = be.maxProgress;

        if (be.tryDrainInputContainerSlot()) {
            changed = true;
        }

        if (be.tryFillOutputContainerSlot(DistillerMenu.OUTPUT_A_CONTAINER_SLOT, TANK_OUTPUT_A)) {
            changed = true;
        }

        if (be.tryFillOutputContainerSlot(DistillerMenu.OUTPUT_B_CONTAINER_SLOT, TANK_OUTPUT_B)) {
            changed = true;
        }

        be.refreshClickStats(level.getGameTime());

        if (be.clicksPerSec != oldCps || be.speedPercent != oldSpeed) {
            changed = true;
        }

        Optional<RecipeHolder<DistillerRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
        if (recipeHolder.isEmpty()) {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        DistillerRecipe recipe = recipeHolder.get().value();
        be.maxProgress = recipe.baseTicks();

        if (be.maxProgress != oldMaxProgress) {
            changed = true;
        }

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

        int progressPerTick = be.getProgressPerTickFromCps();
        if (progressPerTick > 0) {
            be.progress += progressPerTick;
            changed = true;

            if (be.progress >= be.maxProgress) {
                be.craft(recipe);
                be.progress = 0;
            }
        }

        if (be.progress != oldProgress) {
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    private Optional<RecipeHolder<DistillerRecipe>> getCurrentRecipe(ServerLevel level) {
        if (this.inputFluidId == null || this.inputTankAmount <= 0) {
            return Optional.empty();
        }

        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.DISTILLER.get(),
                new DistillerRecipeInput(this.inputFluidId, this.inputTankAmount),
                level
        );
    }

    private boolean canCraft(DistillerRecipe recipe) {
        if (this.inputFluidId == null) {
            return false;
        }

        if (!recipe.input().fluid().equals(this.inputFluidId)) {
            return false;
        }

        if (this.inputTankAmount < recipe.input().amount()) {
            return false;
        }

        if (getAddableAmount(TANK_OUTPUT_A, recipe.output1().fluid(), recipe.output1().amount()) < recipe.output1().amount()) {
            return false;
        }

        return recipe.output2()
                .map(output -> getAddableAmount(TANK_OUTPUT_B, output.fluid(), output.amount()) >= output.amount())
                .orElse(true);
    }

    private void craft(DistillerRecipe recipe) {
        removeFluidFromTank(TANK_INPUT, recipe.input().amount());
        addFluidToTank(TANK_OUTPUT_A, recipe.output1().fluid(), recipe.output1().amount());
        recipe.output2().ifPresent(output -> addFluidToTank(TANK_OUTPUT_B, output.fluid(), output.amount()));
    }

    private void refreshClickStats(long now) {
        while (!this.recentClicks.isEmpty() && now - this.recentClicks.peekFirst() >= 20) {
            this.recentClicks.removeFirst();
        }

        this.clicksPerSec = this.recentClicks.size();
        this.speedPercent = computeSpeedPercent(this.clicksPerSec);
    }

    private int computeSpeedPercent(int cps) {
        if (cps <= 5) {
            return 0;
        }

        return Math.min(500, 100 + (cps - 6) * 25);
    }

    private int getProgressPerTickFromCps() {
        return this.clicksPerSec <= 5 ? 0 : this.clicksPerSec - 5;
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

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return isFluidContainer(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
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

    private boolean tryDrainInputContainerSlot() {
        ItemStack stack = this.getItem(DistillerMenu.INPUT_CONTAINER_SLOT);
        if (stack.isEmpty()) {
            return false;
        }

        var itemHandler = VanillaContainerWrapper.of(this);
        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(itemHandler, DistillerMenu.INPUT_CONTAINER_SLOT)
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

        // Buckets only support full-bucket moves
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

        // If the container already contains fluid, it must match the tank fluid
        if (!itemResource.isEmpty()) {
            ResourceLocation itemFluidId = BuiltInRegistries.FLUID.getKey(itemResource.getFluid());
            if (!tankFluidId.equals(itemFluidId)) {
                return false;
            }
        }

        int request;

        // Buckets only support full 1000 mB fills
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
}