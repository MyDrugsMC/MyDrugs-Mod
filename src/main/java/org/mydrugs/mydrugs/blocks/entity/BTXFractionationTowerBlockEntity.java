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
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.machine.fluid.StoredFluidTank;
import org.mydrugs.mydrugs.machine.transfer.FluidTransferUtil;
import org.mydrugs.mydrugs.machine.transfer.LockedTransferSlots;
import org.mydrugs.mydrugs.menu.BTXFractionationTowerMenu;

public class BTXFractionationTowerBlockEntity extends BaseContainerBlockEntity implements BTXFractionationTowerMenu.BTXFractionationTowerButtonHandler {
    public static final int FLUID_CAPACITY = 4000;
    public static final int INPUT_PER_BATCH = 1000;
    public static final int BENZENE_PER_BATCH = 350;
    public static final int TOLUENE_PER_BATCH = 300;
    public static final int XYLENE_PER_BATCH = 350;
    public static final int BASE_TICKS = 300;

    private static final ResourceLocation BTX_MIX_ID = ModFluids.rl("btx_mix");

    private final LockedTransferSlots fluidTransferLocks = new LockedTransferSlots(1);
    private final StoredFluidTank inputTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, BTXFractionationTowerBlockEntity::isBTXMixFluidStack);
    private final StoredFluidTank benzeneTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> isFluidStack(stack, ModFluids.BENZENE));
    private final StoredFluidTank tolueneTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> isFluidStack(stack, ModFluids.TOLUENE));
    private final StoredFluidTank xyleneTank = new StoredFluidTank(FLUID_CAPACITY, this::sync, stack -> isFluidStack(stack, ModFluids.XYLENE));
    private NonNullList<ItemStack> items = NonNullList.withSize(BTXFractionationTowerMenu.MACHINE_SLOT_COUNT, ItemStack.EMPTY);
    private int progress = 0;
    private int maxProgress = BASE_TICKS;

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

        be.maxProgress = BASE_TICKS;

        if (!be.canCraft()) {
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
            be.progress++;
            changed = true;

            if (be.progress >= be.maxProgress) {
                be.craft();
                be.progress = 0;
                changed = true;
            }
        }

        if (changed) {
            be.sync();
        }
    }

    private static FluidStack toFluidStack(FluidEntry entry, int amount) {
        if (amount <= 0) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(entry.source().get(), amount);
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

    private boolean canCraft() {
        ResourceLocation inputFluidId = this.inputTank.getFluidId();
        if (!BTX_MIX_ID.equals(inputFluidId)) {
            return false;
        }

        if (this.inputTank.getAmount() < INPUT_PER_BATCH) {
            return false;
        }

        FluidStack benzene = toFluidStack(ModFluids.BENZENE, BENZENE_PER_BATCH);
        if (benzene.isEmpty() || this.benzeneTank.getAddableAmount(benzene) < benzene.getAmount()) {
            return false;
        }

        FluidStack toluene = toFluidStack(ModFluids.TOLUENE, TOLUENE_PER_BATCH);
        if (toluene.isEmpty() || this.tolueneTank.getAddableAmount(toluene) < toluene.getAmount()) {
            return false;
        }

        FluidStack xylene = toFluidStack(ModFluids.XYLENE, XYLENE_PER_BATCH);
        return !xylene.isEmpty() && this.xyleneTank.getAddableAmount(xylene) >= xylene.getAmount();
    }

    private void craft() {
        this.inputTank.extract(INPUT_PER_BATCH, false);
        this.benzeneTank.insert(toFluidStack(ModFluids.BENZENE, BENZENE_PER_BATCH), false);
        this.tolueneTank.insert(toFluidStack(ModFluids.TOLUENE, TOLUENE_PER_BATCH), false);
        this.xyleneTank.insert(toFluidStack(ModFluids.XYLENE, XYLENE_PER_BATCH), false);
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
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
        this.maxProgress = input.getIntOr("MaxProgress", BASE_TICKS);
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
