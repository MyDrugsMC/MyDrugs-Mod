package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.energy.PsychotropeEnergyMachines;
import org.mydrugs.mydrugs.gas.*;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.machine.MachineStatus;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;
import org.mydrugs.mydrugs.machine.fuel.FuelResolver;
import org.mydrugs.mydrugs.machine.fuel.MachineFuelUtil;
import org.mydrugs.mydrugs.machine.transfer.GasTransferUtil;
import org.mydrugs.mydrugs.menu.GasifierMenu;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.gasifier.GasifierRecipe;

import java.util.Objects;

public class GasifierBlockEntity extends BlockEntity implements Container, MenuProvider, MachineStatusProvider {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_FUEL = 1;
    public static final int SLOT_EXPORT = 2;
    public static final int SLOT_COUNT = 3;

    public static final int TANK_CAPACITY = 4_000;

    private static final FuelResolver FUEL = MachineFuelUtil.VANILLA;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);

    private final GasTank outputTank = new GasTank(
            TANK_CAPACITY,
            Objects::nonNull,
            this::setChanged
    );

    private int progress = 0;
    private int maxProgress = 100;

    private int burnTimeRemaining = 0;
    private int burnTimeTotal = 0;
    private MachineStatus machineStatus = MachineStatus.IDLE;

    private final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) outputTank.getAmount();
                case 1 -> progress;
                case 2 -> maxProgress;
                case 3 -> ModGases.getSyncId(outputTank.getGasType());
                case 4 -> burnTimeRemaining;
                case 5 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> outputTank.loadStored(outputTank.getGasType(), value);
                case 1 -> progress = value;
                case 2 -> maxProgress = value;
                case 3 -> outputTank.loadStored(ModGases.bySyncId(value), outputTank.getAmount());
                case 4 -> burnTimeRemaining = value;
                case 5 -> burnTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 6;
        }
    };

    public GasifierBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GASIFIER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, GasifierBlockEntity be) {
        if (!level.isClientSide()) {
            be.tickServer();
        }
    }

    public static boolean isValidInput(ItemStack stack, @Nullable Level level) {
        if (level == null || stack.isEmpty() || level.isClientSide()) {
            return false;
        }

        return ((ServerLevel) level).recipeAccess()
                .getRecipeFor(
                        ModRecipeTypes.GASIFIER.get(),
                        new SingleRecipeInput(stack),
                        level
                )
                .isPresent();
    }

    public static boolean isFuel(ItemStack stack, Level level) {
        return MachineFuelUtil.isFuel(stack, level, FUEL);
    }

    public static int getFuelBurnDuration(ItemStack stack, Level level) {
        return MachineFuelUtil.getBurnTime(stack, level, FUEL);
    }

    private void tickServer() {
        boolean dirty = false;
        boolean sync = false;

        if (this.burnTimeRemaining > 0) {
            this.burnTimeRemaining--;
            dirty = true;
        }

        RecipeHolder<GasifierRecipe> holder = this.getCurrentRecipe();
        GasifierRecipe recipe = holder == null ? null : holder.value();

        if (canProcess(recipe)) {
            if (this.progress == 0 || this.maxProgress != recipe.processTime()) {
                this.maxProgress = recipe.processTime();
                dirty = true;
            }

            boolean poweredByEnergy = PsychotropeEnergyMachines.tryUseEnergyTick(this);

            if (!this.isLit() && !poweredByEnergy && this.consumeFuel()) {
                dirty = true;
                sync = true;
            }

            if (this.isLit() || poweredByEnergy) {
                dirty |= this.setMachineStatus(MachineStatus.RUNNING);
                this.progress++;
                dirty = true;

                if (this.progress >= this.maxProgress) {
                    this.craft(recipe);
                    this.progress = 0;
                    dirty = true;
                    sync = true;
                }
            } else if (this.progress != 0) {
                dirty |= this.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
                this.progress = 0;
                dirty = true;
            } else {
                dirty |= this.setMachineStatus(MachineStatus.NOT_ENOUGH_ENERGY);
            }
        } else if (this.progress != 0) {
            dirty |= this.setMachineStatus(recipe == null ? MachineStatus.NO_MATCHING_RECIPE : MachineStatus.OUTPUT_GAS_TANK_FULL);
            this.progress = 0;
            dirty = true;
        } else {
            dirty |= this.setMachineStatus(recipe == null ? MachineStatus.NO_MATCHING_RECIPE : MachineStatus.OUTPUT_GAS_TANK_FULL);
        }

        long moved = GasTransferUtil.tryFillOutputSlot(this.items, SLOT_EXPORT, this.outputTank) ? 1L : 0L;
        if (moved > 0) {
            dirty = true;
            sync = true;
        }

        if (dirty) {
            this.setChanged();
        }

        if (sync) {
            this.syncToClient();
        }
    }

    private boolean canProcess(@Nullable GasifierRecipe recipe) {
        if (recipe == null) {
            return false;
        }

        if (this.items.get(SLOT_INPUT).isEmpty()) {
            return false;
        }

        if (recipe.gas() == null) {
            return false;
        }

        return this.outputTank.fill(
                GasStack.of(recipe.gas(), recipe.gasAmount()),
                true
        ) == recipe.gasAmount();
    }

    private void craft(GasifierRecipe recipe) {
        ItemStack input = this.items.get(SLOT_INPUT);
        if (input.isEmpty() || recipe.gas() == null) {
            return;
        }

        this.outputTank.fill(
                GasStack.of(recipe.gas(), recipe.gasAmount()),
                false
        );

        input.shrink(1);
        if (input.isEmpty()) {
            this.items.set(SLOT_INPUT, ItemStack.EMPTY);
        }
        org.mydrugs.mydrugs.advancement.AdvancementEventHooks.machineRecipeCompleted(this);
    }

    private boolean consumeFuel() {
        MachineFuelUtil.FuelUse fuelUse = MachineFuelUtil.consumeOne(
                this.items.get(SLOT_FUEL),
                this.level,
                FUEL
        );

        if (!fuelUse.consumed()) {
            return false;
        }

        this.burnTimeRemaining = fuelUse.burnTime();
        this.burnTimeTotal = fuelUse.burnTime();
        this.items.set(SLOT_FUEL, fuelUse.remainingStack());
        return true;
    }

    private long tryFillExportSlotTank() {
        if (this.outputTank.isEmpty()) {
            return 0;
        }

        ItemStack exportStack = this.items.get(SLOT_EXPORT);
        if (exportStack.isEmpty()) {
            return 0;
        }

        IGasHandler itemTank = exportStack.getCapability(ModGasCapabilities.ITEM, null);
        if (itemTank == null) {
            return 0;
        }

        return GasTransport.move(this.outputTank, itemTank, this.outputTank.getAmount());
    }

    private void syncToClient() {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public boolean isLit() {
        return this.burnTimeRemaining > 0;
    }

    public GasTank getOutputTank() {
        return this.outputTank;
    }

    public IGasHandler getGasHandler(net.minecraft.core.Direction side) {
        return this.outputTank;
    }

    public ContainerData getContainerData() {
        return this.data;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.mydrugs.gasifier");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new GasifierMenu(
                containerId,
                playerInventory,
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
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : this.items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
            this.syncToClient();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);

        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }

        if (slot == SLOT_INPUT) {
            this.progress = 0;
        }

        this.setChanged();
        this.syncToClient();
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level == null || this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        }

        return player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D
        ) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == SLOT_INPUT) {
            return isValidInput(stack, this.level);
        }

        if (slot == SLOT_FUEL) {
            return isFuel(stack, level);
        }

        if (slot == SLOT_EXPORT) {
            return stack.is(ModItems.GAS_TANK_ITEM.get());
        }

        return false;
    }

    @Override
    public void clearContent() {
        this.items.clear();
        this.setChanged();
        this.syncToClient();
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (int i = 0; i < this.items.size(); i++) {
            this.items.set(i, ItemStack.EMPTY);
        }

        for (ValueInput child : input.childrenListOrEmpty("items")) {
            int slot = child.getIntOr("slot", -1);
            if (slot >= 0 && slot < this.items.size()) {
                ItemStack stack = child.read("stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
                this.items.set(slot, stack);
            }
        }

        this.progress = input.getIntOr("progress", 0);
        this.maxProgress = input.getIntOr("max_progress", 100);

        this.burnTimeRemaining = input.getIntOr("burn_time_remaining", 0);
        this.burnTimeTotal = input.getIntOr("burn_time_total", 0);

        String gasId = input.getStringOr("gas_id", "");
        long gasAmount = input.getLongOr("gas_amount", 0L);

        GasType gas = gasId.isBlank() ? null : ModGases.get(ResourceLocation.parse(gasId));
        this.outputTank.loadStored(gas, gasAmount);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        ValueOutput.ValueOutputList list = output.childrenList("items");
        for (int i = 0; i < this.items.size(); i++) {
            ItemStack stack = this.items.get(i);
            if (stack.isEmpty()) {
                continue;
            }

            ValueOutput child = list.addChild();
            child.putInt("slot", i);
            child.store("stack", ItemStack.OPTIONAL_CODEC, stack);
        }

        output.putInt("progress", this.progress);
        output.putInt("max_progress", this.maxProgress);

        output.putInt("burn_time_remaining", this.burnTimeRemaining);
        output.putInt("burn_time_total", this.burnTimeTotal);

        GasType gas = this.outputTank.getGasType();
        output.putString("gas_id", gas == null ? "" : gas.id().toString());
        output.putLong("gas_amount", this.outputTank.getAmount());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private @Nullable RecipeHolder<GasifierRecipe> getCurrentRecipe() {
        if (this.level == null || this.level.isClientSide()) {
            return null;
        }

        return ((ServerLevel) this.level).recipeAccess()
                .getRecipeFor(
                        ModRecipeTypes.GASIFIER.get(),
                        new SingleRecipeInput(this.items.get(SLOT_INPUT)),
                        this.level
                )
                .orElse(null);
    }
}
