package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
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
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.AromaticExtractorMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.aromatic_extractor.AromaticExtractorRecipe;
import org.mydrugs.mydrugs.recipes.aromatic_extractor.AromaticExtractorRecipeInput;

import java.util.Optional;

public class AromaticExtractorBlockEntity extends BaseContainerBlockEntity implements AromaticExtractorMenu.AromaticExtractorButtonHandler {
    public static final int INPUT_CAPACITY = 4000;
    public static final int OUTPUT_CAPACITY = 4000;
    public static final int CATALYST_CAPACITY = 8000;
    public static final int MIN_CATALYST_AMOUNT = 500;
    public static final double MAX_CATALYST_SPEED_MULTIPLIER = 4.0D;

    private final LockedTransferSlots fluidTransferLocks = new LockedTransferSlots(2);
    private final StoredFluidTank inputTank = new StoredFluidTank(INPUT_CAPACITY, this::sync);
    private final StoredFluidTank catalystTank = new StoredFluidTank(CATALYST_CAPACITY, this::sync);
    private final StoredFluidTank outputATank = new StoredFluidTank(OUTPUT_CAPACITY, this::sync);
    private final StoredFluidTank outputBTank = new StoredFluidTank(OUTPUT_CAPACITY, this::sync);
    private NonNullList<ItemStack> items = NonNullList.withSize(AromaticExtractorMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 200;

    private int burnTimeRemaining = 0;
    private int burnTimeTotal = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTank.getAmount();
                case 1 -> catalystTank.getAmount();
                case 2 -> outputATank.getAmount();
                case 3 -> outputBTank.getAmount();
                case 4 -> progress;
                case 5 -> maxProgress;
                case 6 -> burnTimeRemaining;
                case 7 -> burnTimeTotal;
                case 8 -> inputTank.encodeFluidSyncId();
                case 9 -> catalystTank.encodeFluidSyncId();
                case 10 -> outputATank.encodeFluidSyncId();
                case 11 -> outputBTank.encodeFluidSyncId();
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
            return AromaticExtractorMenu.DATA_COUNT;
        }
    };

    public AromaticExtractorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AROMATIC_EXTRACTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AromaticExtractorBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = FluidTransferUtil.tryProcessTransferSlot(
                be,
                AromaticExtractorMenu.INPUT_CONTAINER_SLOT,
                be.inputTank,
                be.fluidTransferLocks,
                0
        );

        if (FluidTransferUtil.tryProcessTransferSlot(
                be,
                AromaticExtractorMenu.CATALYST_CONTAINER_SLOT,
                be.catalystTank,
                be.fluidTransferLocks,
                1
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                AromaticExtractorMenu.OUTPUT_A_CONTAINER_SLOT,
                be.outputATank
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                AromaticExtractorMenu.OUTPUT_B_CONTAINER_SLOT,
                be.outputBTank
        )) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0) {
            be.burnTimeRemaining--;
            changed = true;
        }

        Optional<RecipeHolder<AromaticExtractorRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
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

        AromaticExtractorRecipe recipe = recipeHolder.get().value();
        be.maxProgress = be.scaledProcessTicks(recipe);

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
                changed = true;
            }
        }

        if (changed) {
            be.sync();
        }
    }

    private int scaledProcessTicks(AromaticExtractorRecipe recipe) {
        int baseTicks = Math.max(1, recipe.baseTicks());
        int required = Math.max(MIN_CATALYST_AMOUNT, recipe.catalyst().amount());
        int available = Math.max(required, this.catalystTank.getAmount());
        int bonusCapacity = Math.max(1, CATALYST_CAPACITY - required);
        double bonus = Math.min(1.0D, Math.max(0.0D, (available - required) / (double) bonusCapacity));
        double multiplier = 1.0D + (MAX_CATALYST_SPEED_MULTIPLIER - 1.0D) * bonus;
        return Math.max(1, (int) Math.ceil(baseTicks / multiplier));
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

    public static boolean isFluidContainer(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() instanceof GlassBottleItem) {
            return true;
        }

        return ItemAccess.forStack(stack).getCapability(Capabilities.Fluid.ITEM) != null;
    }

    public static boolean isFuel(ItemStack stack, @Nullable Level level) {
        return !stack.isEmpty()
                && level != null
                && stack.getBurnTime(null, level.fuelValues()) > 0;
    }

    private Optional<RecipeHolder<AromaticExtractorRecipe>> getCurrentRecipe(ServerLevel level) {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        ResourceLocation catalystFluidId = this.catalystTank.getFluidId();
        if (inputFluidId == null || catalystFluidId == null || this.inputTank.isEmpty() || this.catalystTank.isEmpty()) {
            return Optional.empty();
        }

        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.AROMATIC_EXTRACTOR.get(),
                new AromaticExtractorRecipeInput(
                        inputFluidId,
                        this.inputTank.getAmount(),
                        catalystFluidId,
                        this.catalystTank.getAmount()
                ),
                level
        );
    }

    private boolean canCraft(AromaticExtractorRecipe recipe) {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        ResourceLocation catalystFluidId = this.catalystTank.getFluidId();
        if (inputFluidId == null || catalystFluidId == null) {
            return false;
        }

        if (!recipe.input().fluid().equals(inputFluidId)) {
            return false;
        }

        if (!recipe.catalyst().fluid().equals(catalystFluidId)) {
            return false;
        }

        if (this.inputTank.getAmount() < recipe.input().amount()) {
            return false;
        }

        if (this.catalystTank.getAmount() < recipe.catalyst().amount()) {
            return false;
        }

        FluidStack outputA = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (outputA.isEmpty() || this.outputATank.getAddableAmount(outputA) < outputA.getAmount()) {
            return false;
        }

        FluidStack outputB = toFluidStack(recipe.output2().fluid(), recipe.output2().amount());
        return !outputB.isEmpty() && this.outputBTank.getAddableAmount(outputB) >= outputB.getAmount();
    }

    private void craft(AromaticExtractorRecipe recipe) {
        this.inputTank.extract(recipe.input().amount(), false);

        FluidStack outputA = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (!outputA.isEmpty()) {
            this.outputATank.insert(outputA, false);
        }

        FluidStack outputB = toFluidStack(recipe.output2().fluid(), recipe.output2().amount());
        if (!outputB.isEmpty()) {
            this.outputBTank.insert(outputB, false);
        }
    }

    private boolean tryConsumeFuel() {
        ItemStack fuelStack = this.getItem(AromaticExtractorMenu.FUEL_SLOT);
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
            this.setItem(AromaticExtractorMenu.FUEL_SLOT, remainder);
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

            FluidStack incoming = new FluidStack(fluid, containedAmount);
            int moved = insertIntoBestInputTank(incoming, true);
            if (moved <= 0) {
                return false;
            }

            insertIntoBestInputTank(incoming.copyWithAmount(moved), false);
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
        int requested = insertIntoBestInputTank(incoming, true);
        if (requested <= 0) {
            return false;
        }

        try (var tx = Transaction.openRoot()) {
            int extracted = handler.extract(resource, requested, tx);
            if (extracted <= 0) {
                return false;
            }

            tx.commit();
            insertIntoBestInputTank(incoming.copyWithAmount(extracted), false);
        }

        this.sync();
        return true;
    }

    private int insertIntoBestInputTank(FluidStack incoming, boolean simulate) {
        if (incoming.isEmpty()) {
            return 0;
        }

        if (sameFluid(this.inputTank, incoming)) {
            return this.inputTank.insert(incoming, simulate);
        }
        if (sameFluid(this.catalystTank, incoming)) {
            return this.catalystTank.insert(incoming, simulate);
        }

        ResourceLocation incomingId = BuiltInRegistries.FLUID.getKey(incoming.getFluid());
        boolean knownCatalyst = incomingId != null && this.isKnownCatalystFluid(incomingId);
        if (knownCatalyst && this.catalystTank.isEmpty()) {
            return this.catalystTank.insert(incoming, simulate);
        }
        if (this.inputTank.isEmpty()) {
            return this.inputTank.insert(incoming, simulate);
        }
        if (this.catalystTank.isEmpty()) {
            return this.catalystTank.insert(incoming, simulate);
        }

        int inserted = this.inputTank.insert(incoming, simulate);
        return inserted > 0 ? inserted : this.catalystTank.insert(incoming, simulate);
    }

    private boolean isKnownCatalystFluid(ResourceLocation fluidId) {
        if (!(this.level instanceof ServerLevel serverLevel)) {
            return false;
        }

        return serverLevel.recipeAccess().recipeMap()
                .byType(ModRecipeTypes.AROMATIC_EXTRACTOR.get())
                .stream()
                .map(RecipeHolder::value)
                .anyMatch(recipe -> recipe.catalyst().fluid().equals(fluidId));
    }

    private static boolean sameFluid(StoredFluidTank tank, FluidStack incoming) {
        FluidStack stored = tank.getFluid();
        return !stored.isEmpty() && FluidStack.isSameFluidSameComponents(stored, incoming);
    }

    private void sync() {
        MachineSync.sync(this);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.mydrugs.aromatic_extractor");
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
        return new AromaticExtractorMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return AromaticExtractorMenu.MACHINE_SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case AromaticExtractorMenu.INPUT_CONTAINER_SLOT,
                 AromaticExtractorMenu.CATALYST_CONTAINER_SLOT,
                 AromaticExtractorMenu.OUTPUT_A_CONTAINER_SLOT,
                 AromaticExtractorMenu.OUTPUT_B_CONTAINER_SLOT -> isFluidContainer(stack);
            case AromaticExtractorMenu.FUEL_SLOT -> isFuel(stack, this.level);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        switch (slot) {
            case AromaticExtractorMenu.INPUT_CONTAINER_SLOT -> this.fluidTransferLocks.reset(0);
            case AromaticExtractorMenu.CATALYST_CONTAINER_SLOT -> this.fluidTransferLocks.reset(1);
            default -> {
            }
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.inputTank.load(input, "InputFluid");
        this.catalystTank.load(input, "CatalystFluid");
        this.outputATank.load(input, "OutputFluid1");
        this.outputBTank.load(input, "OutputFluid2");

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", 200);
        this.burnTimeRemaining = input.getIntOr("BurnTimeRemaining", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);

        this.fluidTransferLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        this.inputTank.save(output, "InputFluid");
        this.catalystTank.save(output, "CatalystFluid");
        this.outputATank.save(output, "OutputFluid1");
        this.outputBTank.save(output, "OutputFluid2");

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
    }

    @Override
    public boolean onAromaticExtractorButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case AromaticExtractorMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = this.inputTank.dump();
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case AromaticExtractorMenu.DUMP_CATALYST_BUTTON_ID -> {
                boolean dumped = this.catalystTank.dump();
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case AromaticExtractorMenu.DUMP_OUTPUT_A_BUTTON_ID -> {
                boolean dumped = this.outputATank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            case AromaticExtractorMenu.DUMP_OUTPUT_B_BUTTON_ID -> {
                boolean dumped = this.outputBTank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(net.minecraft.core.HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(this, inputTank, catalystTank, outputATank, outputBTank);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
