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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.AdvancedMixingVatBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.GasStack;
import org.mydrugs.mydrugs.gas.GasTank;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.AdvancedMixingVatMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipe;
import org.mydrugs.mydrugs.recipes.advanced_mixing_vat.AdvancedMixingVatRecipeInput;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvancedMixingVatBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity implements MenuProvider {
    public static final int RECIPE_ITEM_SLOT_COUNT = 4;

    public static final int SLOT_RECIPE_0 = 0;
    public static final int SLOT_RECIPE_1 = 1;
    public static final int SLOT_RECIPE_2 = 2;
    public static final int SLOT_RECIPE_3 = 3;

    public static final int SLOT_TANK_INPUT_A = 4;
    public static final int SLOT_TANK_INPUT_B = 5;
    public static final int SLOT_TANK_INPUT_C = 6;
    public static final int SLOT_TANK_OUTPUT = 7;

    public static final int ITEM_SLOT_COUNT = 8;

    public static final int INPUT_TANK_CAPACITY = 4000;
    public static final int OUTPUT_TANK_CAPACITY = 4000;
    public static final long GAS_TANK_CAPACITY = 4000L;

    public static final int DATA_COUNT = 13;

    private final VatItemHandler itemHandler = new VatItemHandler(ITEM_SLOT_COUNT);
    private final NonNullList<ItemStack> itemStacks = this.itemHandler.list();

    private final VatInputFluidHandler inputAHandler = new VatInputFluidHandler(INPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> inputAStacks = this.inputAHandler.list();

    private final VatInputFluidHandler inputBHandler = new VatInputFluidHandler(INPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> inputBStacks = this.inputBHandler.list();

    private final VatInputFluidHandler inputCHandler = new VatInputFluidHandler(INPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> inputCStacks = this.inputCHandler.list();

    private final VatOutputFluidHandler outputHandler = new VatOutputFluidHandler(OUTPUT_TANK_CAPACITY);
    private final NonNullList<FluidStack> outputStacks = this.outputHandler.list();

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
                case 2 -> inputCStacks.get(0).getAmount();
                case 3 -> outputStacks.get(0).getAmount();
                case 4 -> (int) Math.min(Integer.MAX_VALUE, gasTank.getAmount());
                case 5 -> progress;
                case 6 -> maxProgress;
                case 7 -> inputAStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputAStacks.get(0).getFluid());
                case 8 -> inputBStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputBStacks.get(0).getFluid());
                case 9 -> inputCStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(inputCStacks.get(0).getFluid());
                case 10 -> outputStacks.get(0).isEmpty() ? -1 : BuiltInRegistries.FLUID.getId(outputStacks.get(0).getFluid());
                case 11 -> gasTank.isEmpty() ? -1 : ModGases.getSyncId(gasTank.getGasType());
                case 12 -> hasRecipe ? 1 : 0;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 5 -> progress = value;
                case 6 -> maxProgress = value;
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

        boolean changed = false;

        if (be.tryDrainInputContainerSlot(SLOT_TANK_INPUT_A, be.inputAHandler, be.inputAStacks)) {
            changed = true;
        }

        if (be.tryDrainInputContainerSlot(SLOT_TANK_INPUT_B, be.inputBHandler, be.inputBStacks)) {
            changed = true;
        }

        if (be.tryDrainInputContainerSlot(SLOT_TANK_INPUT_C, be.inputCHandler, be.inputCStacks)) {
            changed = true;
        }

        if (be.tryFillOutputContainerSlot(SLOT_TANK_OUTPUT, be.outputHandler, be.outputStacks)) {
            changed = true;
        }

        Optional<RecipeHolder<AdvancedMixingVatRecipe>> match = be.findMatchingRecipe();

        if (match.isEmpty()) {
            if (be.progress != 0 || be.hasRecipe) {
                be.progress = 0;
                be.hasRecipe = false;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        AdvancedMixingVatRecipe recipe = match.get().value();
        FluidStack result = recipe.resultStack();

        if (!be.canAcceptOutput(result)) {
            if (be.progress != 0 || be.hasRecipe) {
                be.progress = 0;
                be.hasRecipe = false;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        be.hasRecipe = true;

        int oldMaxProgress = be.maxProgress;
        be.maxProgress = recipe.processingTime();
        if (be.maxProgress != oldMaxProgress) {
            changed = true;
        }

        be.progress++;
        changed = true;

        if (be.progress >= be.maxProgress) {
            if (be.findMatchingRecipe().isPresent() && be.canAcceptOutput(result)) {
                be.finishRecipe(recipe, result);
            }
            be.progress = 0;
            changed = true;
        }

        if (changed) {
            be.sync();
        }
    }

    private boolean tryDrainInputContainerSlot(
            int itemSlot,
            VatInputFluidHandler tankHandler,
            NonNullList<FluidStack> backingTank
    ) {
        ItemStack stack = this.itemStacks.get(itemSlot);
        if (stack.isEmpty()) {
            return false;
        }

        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(this.itemHandler, itemSlot)
                .oneByOne();

        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = handler.getResource(0);
        int containedAmount = handler.getAmountAsInt(0);

        if (resource.isEmpty() || containedAmount <= 0) {
            return false;
        }

        FluidStack tankFluid = backingTank.get(0);
        int tankAmount = tankFluid.isEmpty() ? 0 : tankFluid.getAmount();

        int freeSpace = INPUT_TANK_CAPACITY - tankAmount;
        if (freeSpace <= 0) {
            return false;
        }

        if (!tankFluid.isEmpty()) {
            FluidStack incoming = resource.toStack(containedAmount);
            if (!FluidStack.isSameFluidSameComponents(tankFluid, incoming)) {
                return false;
            }
        }

        int toMove = Math.min(containedAmount, freeSpace);

        if (stack.getItem() instanceof BucketItem && toMove < FluidType.BUCKET_VOLUME) {
            return false;
        }

        int request = stack.getItem() instanceof BucketItem
                ? FluidType.BUCKET_VOLUME
                : toMove;

        try (Transaction tx = Transaction.openRoot()) {
            int inserted = tankHandler.insert(0, resource, request, tx);
            if (inserted != request) {
                return false;
            }

            int extracted = handler.extract(resource, request, tx);
            if (extracted != request) {
                return false;
            }

            tx.commit();
            return true;
        }
    }


    private boolean tryFillOutputContainerSlot(
            int itemSlot,
            VatOutputFluidHandler tankHandler,
            NonNullList<FluidStack> backingTank
    ) {
        ItemStack stack = this.itemStacks.get(itemSlot);
        if (stack.isEmpty()) {
            return false;
        }

        FluidStack stored = backingTank.get(0);
        if (stored.isEmpty() || stored.getAmount() <= 0) {
            return false;
        }

        ItemAccess access = ItemAccess
                .forHandlerIndexStrict(this.itemHandler, itemSlot)
                .oneByOne();

        ResourceHandler<FluidResource> handler = access.getCapability(Capabilities.Fluid.ITEM);
        if (handler == null || handler.size() <= 0) {
            return false;
        }

        FluidResource resource = FluidResource.of(stored.getFluidHolder());

        int request;
        if (stack.is(net.minecraft.world.item.Items.BUCKET)) {
            if (stored.getAmount() < FluidType.BUCKET_VOLUME) {
                return false;
            }
            request = FluidType.BUCKET_VOLUME;
        } else {
            int itemAmount = handler.getAmountAsInt(0);
            FluidResource itemResource = handler.getResource(0);

            if (!itemResource.isEmpty()) {
                FluidStack itemFluid = itemResource.toStack(itemAmount);
                if (!FluidStack.isSameFluidSameComponents(stored, itemFluid)) {
                    return false;
                }
            }

            int capacity = handler.getCapacityAsInt(0, resource);
            int remainingSpace = capacity - itemAmount;
            if (remainingSpace <= 0) {
                return false;
            }

            request = Math.min(stored.getAmount(), remainingSpace);
        }

        try (Transaction tx = Transaction.openRoot()) {
            int inserted = handler.insert(resource, request, tx);
            if (inserted <= 0) {
                return false;
            }

            int extracted = tankHandler.extract(0, resource, inserted, tx);
            if (extracted != inserted) {
                return false;
            }

            tx.commit();
            return true;
        }
    }

    private Optional<RecipeHolder<AdvancedMixingVatRecipe>> findMatchingRecipe() {
        if (this.level == null || level.isClientSide()) {
            return Optional.empty();
        }

        List<ItemStack> snapshot = new ArrayList<>(RECIPE_ITEM_SLOT_COUNT);
        for (int i = 0; i < RECIPE_ITEM_SLOT_COUNT; i++) {
            snapshot.add(this.itemStacks.get(i).copy());
        }

        AdvancedMixingVatRecipeInput input = new AdvancedMixingVatRecipeInput(
                snapshot,
                this.inputAStacks.get(0).copy(),
                this.inputBStacks.get(0).copy(),
                this.inputCStacks.get(0).copy(),
                this.gasTank.getGasInTank(0)
        );

        return ((ServerLevel) this.level).recipeAccess().getRecipeFor(
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
        AdvancedMixingVatRecipe.consumeFluids(recipe.fluidInputs(), this.inputAStacks, this.inputBStacks, this.inputCStacks);

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
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
            this.level.invalidateCapabilities(this.worldPosition);
        }
    }

    private void sync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
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

        if (side == left) {
            return this.inputAHandler;
        }
        if (side == right) {
            return this.inputBHandler;
        }
        if (side == Direction.UP) {
            return this.inputCHandler;
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
        output.store("input_c", FluidStack.OPTIONAL_CODEC, this.inputCStacks.get(0));
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
        this.inputCStacks.set(0, input.read("input_c", FluidStack.OPTIONAL_CODEC).orElse(FluidStack.EMPTY));
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
        private VatItemHandler(int size) {
            super(size);
        }

        protected NonNullList<ItemStack> list() {
            return this.stacks;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousStack) {
            onMachineChanged();
        }
    }

    private abstract class BaseVatFluidHandler extends FluidStacksResourceHandler {
        private BaseVatFluidHandler(int capacity) {
            super(1, capacity);
        }

        protected NonNullList<FluidStack> list() {
            return this.stacks;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousStack) {
            onMachineChanged();
        }
    }

    private final class VatInputFluidHandler extends BaseVatFluidHandler {
        private VatInputFluidHandler(int capacity) {
            super(capacity);
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    private final class VatOutputFluidHandler extends BaseVatFluidHandler {
        private VatOutputFluidHandler(int capacity) {
            super(capacity);
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}