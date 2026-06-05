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
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
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
import org.mydrugs.mydrugs.energy.PsyCurrentMachines;
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.BTXFractionationTowerMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.btx_fractionation.BTXFractionationRecipe;
import org.mydrugs.mydrugs.recipes.btx_fractionation.BTXFractionationRecipeInput;
import org.mydrugs.mydrugs.recipes.chemical_reactor.FluidRequirement;

import java.util.List;
import java.util.Optional;

public class BTXFractionationTowerBlockEntity extends BaseContainerBlockEntity implements BTXFractionationTowerMenu.BTXFractionationTowerButtonHandler {
    public static final int FLUID_CAPACITY = 4000;
    /** Fallback process time when no recipe is loaded yet; the JSON recipe overrides this. */
    private static final int FALLBACK_PROCESS_TICKS = 300;

    private static final ResourceLocation BTX_MIX_ID = ModFluids.rl("btx_mix");

    private final LockedTransferSlots fluidTransferLocks = new LockedTransferSlots(1);
    // BTX Mix is currently the only declared input fluid; if future recipes add new input fluids,
    // extend this filter to consult the recipe manager when the tank has a level reference.
    private final StoredFluidTank inputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, BTXFractionationTowerBlockEntity::isBTXMixFluidStack);
    private final StoredFluidTank benzeneTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> isFluidStack(stack, ModFluids.BENZENE));
    private final StoredFluidTank tolueneTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> isFluidStack(stack, ModFluids.TOLUENE));
    private final StoredFluidTank xyleneTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> isFluidStack(stack, ModFluids.XYLENE));
    private NonNullList<ItemStack> items = NonNullList.withSize(BTXFractionationTowerMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = FALLBACK_PROCESS_TICKS;

    private int burnTimeRemaining = 0;
    private int burnTimeTotal = 0;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> inputTank.getAmount();
                case 1 -> benzeneTank.getAmount();
                case 2 -> tolueneTank.getAmount();
                case 3 -> xyleneTank.getAmount();
                case 4 -> progress;
                case 5 -> maxProgress;
                case 6 -> burnTimeRemaining;
                case 7 -> burnTimeTotal;
                case 8 -> inputTank.encodeFluidSyncId();
                case 9 -> benzeneTank.encodeFluidSyncId();
                case 10 -> tolueneTank.encodeFluidSyncId();
                case 11 -> xyleneTank.encodeFluidSyncId();
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
            return BTXFractionationTowerMenu.DATA_COUNT;
        }
    };

    public BTXFractionationTowerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BTX_FRACTIONATION_TOWER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BTXFractionationTowerBlockEntity be) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        boolean changed = FluidTransferUtil.tryProcessTransferSlot(
                be,
                BTXFractionationTowerMenu.INPUT_CONTAINER_SLOT,
                be.inputTank,
                be.fluidTransferLocks,
                0
        );

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                BTXFractionationTowerMenu.BENZENE_CONTAINER_SLOT,
                be.benzeneTank
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                BTXFractionationTowerMenu.TOLUENE_CONTAINER_SLOT,
                be.tolueneTank
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                BTXFractionationTowerMenu.XYLENE_CONTAINER_SLOT,
                be.xyleneTank
        )) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0) {
            be.burnTimeRemaining--;
            changed = true;
        }

        Optional<RecipeHolder<BTXFractionationRecipe>> recipeHolder = be.findMatchingRecipe((ServerLevel) level);
        BTXFractionationRecipe recipe = recipeHolder.map(RecipeHolder::value).orElse(null);
        be.maxProgress = recipe == null ? FALLBACK_PROCESS_TICKS : recipe.processTime();

        if (recipe == null || !be.canCraft(recipe)) {
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        boolean poweredByEnergy = PsyCurrentMachines.tryUseCurrentTick(be);
        if (be.burnTimeRemaining <= 0 && !poweredByEnergy && be.tryConsumeFuel()) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0 || poweredByEnergy) {
            be.progress++;
            changed = true;

            if (be.progress >= be.maxProgress) {
                be.craft(recipeHolder.get());
                be.progress = 0;
                changed = true;
            }
        }

        if (changed) {
            be.sync();
        }
    }

    public static boolean isBTXMixFluidStack(FluidStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return BTX_MIX_ID.equals(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
    }

    private static boolean isFluidStack(FluidStack stack, FluidEntry entry) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        return ModFluids.rl(entry.name()).equals(BuiltInRegistries.FLUID.getKey(stack.getFluid()));
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

    private Optional<RecipeHolder<BTXFractionationRecipe>> findMatchingRecipe(ServerLevel level) {
        if (this.inputTank.getFluid().isEmpty()) {
            return Optional.empty();
        }
        BTXFractionationRecipeInput input = new BTXFractionationRecipeInput(this.inputTank.getFluid().copy());
        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.BTX_FRACTIONATION.get(),
                input,
                level
        );
    }

    private boolean canCraft(BTXFractionationRecipe recipe) {
        if (this.inputTank.getAmount() < recipe.input().amount()) {
            return false;
        }
        for (FluidRequirement output : recipe.outputs()) {
            StoredFluidTank tank = outputTankFor(output.fluidId());
            if (tank == null) {
                return false;
            }
            FluidStack stack = new FluidStack(BuiltInRegistries.FLUID.getValue(output.fluidId()), output.amount());
            if (stack.isEmpty() || tank.getAddableAmount(stack) < output.amount()) {
                return false;
            }
        }
        return true;
    }

    private void craft(RecipeHolder<BTXFractionationRecipe> holder) {
        BTXFractionationRecipe recipe = holder.value();
        this.inputTank.extract(recipe.input().amount(), false);
        for (FluidRequirement output : recipe.outputs()) {
            StoredFluidTank tank = outputTankFor(output.fluidId());
            if (tank == null) {
                continue;
            }
            tank.insert(new FluidStack(BuiltInRegistries.FLUID.getValue(output.fluidId()), output.amount()), false);
        }
        Optional<ResourceLocation> resultFluid = recipe.outputs().isEmpty()
                ? Optional.empty()
                : Optional.of(recipe.outputs().getFirst().fluidId());
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(
                this,
                Optional.of(holder.id().location()),
                Optional.empty(),
                resultFluid,
                Optional.empty(),
                Optional.empty()
        );
    }

    private @Nullable StoredFluidTank outputTankFor(ResourceLocation fluidId) {
        if (ModFluids.rl(ModFluids.BENZENE.name()).equals(fluidId)) {
            return this.benzeneTank;
        }
        if (ModFluids.rl(ModFluids.TOLUENE.name()).equals(fluidId)) {
            return this.tolueneTank;
        }
        if (ModFluids.rl(ModFluids.XYLENE.name()).equals(fluidId)) {
            return this.xyleneTank;
        }
        return null;
    }

    private boolean tryConsumeFuel() {
        ItemStack fuelStack = this.getItem(BTXFractionationTowerMenu.FUEL_SLOT);
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
            this.setItem(BTXFractionationTowerMenu.FUEL_SLOT, remainder);
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
        return Component.translatable("container.mydrugs.btx_fractionation_tower");
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
        return new BTXFractionationTowerMenu(
                containerId,
                inventory,
                this,
                this.data,
                ContainerLevelAccess.create(this.level, this.worldPosition)
        );
    }

    @Override
    public int getContainerSize() {
        return BTXFractionationTowerMenu.MACHINE_SLOT_COUNT;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case BTXFractionationTowerMenu.INPUT_CONTAINER_SLOT,
                 BTXFractionationTowerMenu.BENZENE_CONTAINER_SLOT,
                 BTXFractionationTowerMenu.TOLUENE_CONTAINER_SLOT,
                 BTXFractionationTowerMenu.XYLENE_CONTAINER_SLOT -> isFluidContainer(stack);
            case BTXFractionationTowerMenu.FUEL_SLOT -> isFuel(stack, this.level);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (slot == BTXFractionationTowerMenu.INPUT_CONTAINER_SLOT) {
            this.fluidTransferLocks.reset(0);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.inputTank.load(input, "InputFluid");
        this.benzeneTank.load(input, "BenzeneFluid");
        this.tolueneTank.load(input, "TolueneFluid");
        this.xyleneTank.load(input, "XyleneFluid");

        this.progress = input.getIntOr("Progress", 0);
        this.maxProgress = input.getIntOr("MaxProgress", FALLBACK_PROCESS_TICKS);
        this.burnTimeRemaining = input.getIntOr("BurnTimeRemaining", 0);
        this.burnTimeTotal = input.getIntOr("BurnTimeTotal", 0);

        this.fluidTransferLocks.resetAll();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ContainerHelper.saveAllItems(output, this.items);

        this.inputTank.save(output, "InputFluid");
        this.benzeneTank.save(output, "BenzeneFluid");
        this.tolueneTank.save(output, "TolueneFluid");
        this.xyleneTank.save(output, "XyleneFluid");

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
    }

    @Override
    public boolean onBTXFractionationTowerButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case BTXFractionationTowerMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = this.inputTank.dump();
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case BTXFractionationTowerMenu.DUMP_BENZENE_BUTTON_ID -> {
                boolean dumped = this.benzeneTank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            case BTXFractionationTowerMenu.DUMP_TOLUENE_BUTTON_ID -> {
                boolean dumped = this.tolueneTank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            case BTXFractionationTowerMenu.DUMP_XYLENE_BUTTON_ID -> {
                boolean dumped = this.xyleneTank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(this, inputTank, benzeneTank, tolueneTank, xyleneTank);
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
