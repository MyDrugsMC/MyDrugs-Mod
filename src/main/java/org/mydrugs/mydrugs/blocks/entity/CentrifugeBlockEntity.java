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
import org.mydrugs.mydrugs.energy.PsychotropeEnergyMachines;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.CentrifugeMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipe;
import org.mydrugs.mydrugs.recipes.centrifuge.CentrifugeRecipeInput;

import java.util.Optional;

public class CentrifugeBlockEntity extends BaseContainerBlockEntity implements CentrifugeMenu.CentrifugeButtonHandler, MachineStatusProvider {
    public static final int FLUID_CAPACITY = 4000;
    private final LockedTransferSlots fluidTransferLocks = new LockedTransferSlots(1);
    private final StoredFluidTank inputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank outputATank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private final StoredFluidTank outputBTank = new StoredFluidTank(FLUID_CAPACITY, this::sync);
    private NonNullList<ItemStack> items = NonNullList.withSize(4, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = 200;

    private int burnTimeRemaining = 0;
    private int burnTimeTotal = 0;
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
                case 5 -> burnTimeRemaining;
                case 6 -> burnTimeTotal;
                case 7 -> inputTank.encodeFluidSyncId();
                case 8 -> outputATank.encodeFluidSyncId();
                case 9 -> outputBTank.encodeFluidSyncId();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 3 -> progress = value;
                case 4 -> maxProgress = value;
                case 5 -> burnTimeRemaining = value;
                case 6 -> burnTimeTotal = value;
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

    public CentrifugeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, CentrifugeBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        boolean changed = FluidTransferUtil.tryProcessTransferSlot(
                be,
                CentrifugeMenu.INPUT_CONTAINER_SLOT,
                be.inputTank,
                be.fluidTransferLocks,
                0
        );

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                CentrifugeMenu.OUTPUT_A_CONTAINER_SLOT,
                be.outputATank
        )) {
            changed = true;
        }

        if (FluidTransferUtil.tryFillOutputSlot(
                be,
                CentrifugeMenu.OUTPUT_B_CONTAINER_SLOT,
                be.outputBTank
        )) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0) {
            be.burnTimeRemaining--;
            changed = true;
        }

        Optional<RecipeHolder<CentrifugeRecipe>> recipeHolder = be.getCurrentRecipe(serverLevel);
        if (recipeHolder.isEmpty()) {
            changed |= be.setMachineStatus(MachineStatus.NO_MATCHING_RECIPE);
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        CentrifugeRecipe recipe = recipeHolder.get().value();
        be.maxProgress = recipe.baseTicks();

        if (!be.canCraft(recipe)) {
            changed |= be.setMachineStatus(be.inputTank.isEmpty() ? MachineStatus.MISSING_INPUT_FLUID : MachineStatus.OUTPUT_TANK_FULL);
            if (be.progress != 0) {
                be.progress = 0;
                changed = true;
            }

            if (changed) {
                be.sync();
            }
            return;
        }

        boolean poweredByEnergy = PsychotropeEnergyMachines.tryUseEnergyTick(be);
        if (be.burnTimeRemaining <= 0 && !poweredByEnergy && be.tryConsumeFuel()) {
            changed = true;
        }

        if (be.burnTimeRemaining > 0 || poweredByEnergy) {
            changed |= be.setMachineStatus(MachineStatus.RUNNING);
            be.progress++;
            changed = true;

            if (be.progress >= be.maxProgress) {
                be.craft(recipe);
                be.progress = 0;
                changed = true;
            }
        } else {
            changed |= be.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
        }

        if (changed) {
            be.sync();
        }
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

    private Optional<RecipeHolder<CentrifugeRecipe>> getCurrentRecipe(ServerLevel level) {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        if (inputFluidId == null || this.inputTank.isEmpty()) {
            return Optional.empty();
        }

        return level.recipeAccess().getRecipeFor(
                ModRecipeTypes.CENTRIFUGE.get(),
                new CentrifugeRecipeInput(inputFluidId, this.inputTank.getAmount()),
                level
        );
    }

    private boolean canCraft(CentrifugeRecipe recipe) {
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

        FluidStack outputA = toFluidStack(recipe.output1().fluid(), recipe.output1().amount());
        if (outputA.isEmpty() || this.outputATank.getAddableAmount(outputA) < outputA.getAmount()) {
            return false;
        }

        return recipe.output2().map(output -> {
            FluidStack outputB = toFluidStack(output.fluid(), output.amount());
            return !outputB.isEmpty() && this.outputBTank.getAddableAmount(outputB) >= outputB.getAmount();
        }).orElse(true);
    }

    private void craft(CentrifugeRecipe recipe) {
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
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
    }

    private boolean tryConsumeFuel() {
        ItemStack fuelStack = this.getItem(CentrifugeMenu.FUEL_SLOT);
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
            this.setItem(CentrifugeMenu.FUEL_SLOT, remainder);
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
        return Component.translatable("container.mydrugs.centrifuge");
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
        return new CentrifugeMenu(
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

    @Override
    public int getContainerSize() {
        return 4;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case CentrifugeMenu.INPUT_CONTAINER_SLOT,
                 CentrifugeMenu.OUTPUT_A_CONTAINER_SLOT,
                 CentrifugeMenu.OUTPUT_B_CONTAINER_SLOT -> isFluidContainer(stack);
            case CentrifugeMenu.FUEL_SLOT -> isFuel(stack, this.level);
            default -> false;
        };
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        super.setItem(slot, stack);

        if (slot == CentrifugeMenu.INPUT_CONTAINER_SLOT) {
            this.fluidTransferLocks.reset(0);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);

        this.inputTank.load(input, "InputFluid");
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
        this.outputATank.save(output, "OutputFluid1");
        this.outputBTank.save(output, "OutputFluid2");

        output.putInt("Progress", this.progress);
        output.putInt("MaxProgress", this.maxProgress);
        output.putInt("BurnTimeRemaining", this.burnTimeRemaining);
        output.putInt("BurnTimeTotal", this.burnTimeTotal);
    }

    @Override
    public boolean onCentrifugeButtonPressed(Player player, int buttonId) {
        if (this.level == null || this.level.isClientSide()) {
            return false;
        }

        return switch (buttonId) {
            case CentrifugeMenu.DUMP_INPUT_BUTTON_ID -> {
                boolean dumped = this.inputTank.dump();
                if (dumped) {
                    this.progress = 0;
                    sync();
                }
                yield dumped;
            }
            case CentrifugeMenu.DUMP_OUTPUT_A_BUTTON_ID -> {
                boolean dumped = this.outputATank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            case CentrifugeMenu.DUMP_OUTPUT_B_BUTTON_ID -> {
                boolean dumped = this.outputBTank.dump();
                if (dumped) {
                    sync();
                }
                yield dumped;
            }
            default -> false;
        };
    }

    public ResourceHandler<FluidResource> getFluidHandler(Direction side) {
        return new org.mydrugs.mydrugs.pipe.machine.StoredFluidTankResourceHandler(this, inputTank, outputATank, outputBTank);
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
