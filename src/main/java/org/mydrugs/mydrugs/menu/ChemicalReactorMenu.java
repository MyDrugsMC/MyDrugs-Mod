package org.mydrugs.mydrugs.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.ChemicalReactorBlockEntity;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.layout.ChemicalReactorLayout;

public class ChemicalReactorMenu extends AbstractMachineMenu {
    public static final int SLOT_FUEL = ChemicalReactorBlockEntity.SLOT_FUEL;
    public static final int SLOT_PRIMARY_GAS_TRANSFER = ChemicalReactorBlockEntity.SLOT_PRIMARY_GAS_TRANSFER;
    public static final int SLOT_SECONDARY_TRANSFER = ChemicalReactorBlockEntity.SLOT_SECONDARY_TRANSFER;
    public static final int SLOT_OUTPUT_TRANSFER = ChemicalReactorBlockEntity.SLOT_OUTPUT_TRANSFER;

    public static final int MACHINE_SLOT_COUNT = ChemicalReactorBlockEntity.SLOT_COUNT;
    public static final int DATA_COUNT = 20;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final ItemStacksResourceHandler itemHandler;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;

    public ChemicalReactorMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                new ItemStacksResourceHandler(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL,
                buf.readBlockPos()
        );
    }

    public ChemicalReactorMenu(
            int containerId,
            Inventory playerInventory,
            ItemStacksResourceHandler itemHandler,
            ContainerData data,
            ContainerLevelAccess access,
            BlockPos blockPos
    ) {
        super(ModMenus.CHEMICAL_REACTOR.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);

        this.itemHandler = itemHandler;
        this.data = data;
        this.access = access;
        this.blockPos = blockPos;

        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                SLOT_FUEL,
                ChemicalReactorLayout.FUEL_SLOT_X,
                ChemicalReactorLayout.FUEL_SLOT_Y
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ChemicalReactorBlockEntity.isFuel(stack, playerInventory.player.level());
            }
        });

        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                SLOT_PRIMARY_GAS_TRANSFER,
                ChemicalReactorLayout.PRIMARY_GAS_TANK_X,
                transferSlotY()
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ChemicalReactorBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                SLOT_SECONDARY_TRANSFER,
                ChemicalReactorLayout.SECONDARY_TANK_X,
                transferSlotY()
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ChemicalReactorBlockEntity.isSecondaryTransferContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new ResourceHandlerSlot(
                itemHandler,
                itemHandler::set,
                SLOT_OUTPUT_TRANSFER,
                ChemicalReactorLayout.OUTPUT_TANK_X,
                transferSlotY()
        ) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return ChemicalReactorBlockEntity.isOutputTransferContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addPlayerInventorySlots(playerInventory, ChemicalReactorLayout.PLAYER_INV_X, ChemicalReactorLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    private static int transferSlotY() {
        return ChemicalReactorLayout.PRIMARY_GAS_TANK_Y + ChemicalReactorLayout.TANK_H + 6;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.CHEMICAL_REACTOR.get());
    }

    public int getPrimaryGasAmount() {
        return this.data.get(0);
    }

    public int getSecondaryGasAmount() {
        return this.data.get(1);
    }

    public int getSecondaryFluidAmount() {
        return this.data.get(2);
    }

    public int getOutputGasAmount() {
        return this.data.get(3);
    }

    public int getOutputFluidAmount() {
        return this.data.get(4);
    }

    public int getProgress() {
        return this.data.get(5);
    }

    public int getMaxProgress() {
        return this.data.get(6);
    }

    public int getHeat() {
        return this.data.get(7);
    }

    public int getMaxHeat() {
        return this.data.get(8);
    }

    public int getBurnTimeRemaining() {
        return this.data.get(9);
    }

    public int getBurnTimeTotal() {
        return this.data.get(10);
    }

    public int getManualEnergy() {
        return this.data.get(11);
    }

    public int getMaxManualEnergy() {
        return this.data.get(12);
    }

    public int getPrimaryGasSyncId() {
        return this.data.get(13);
    }

    public int getSecondaryGasSyncId() {
        return this.data.get(14);
    }

    public int getSecondaryFluidSyncId() {
        return this.data.get(15);
    }

    public int getOutputGasSyncId() {
        return this.data.get(16);
    }

    public int getOutputFluidSyncId() {
        return this.data.get(17);
    }

    public boolean isSecondaryFluidMode() {
        return this.data.get(18) != 0;
    }

    public boolean isOutputFluidMode() {
        return this.data.get(19) != 0;
    }

    public boolean isLit() {
        return this.getBurnTimeRemaining() > 0;
    }

    public int getScaledProgress(int pixels) {
        int max = this.getMaxProgress();
        return max > 0 ? this.getProgress() * pixels / max : 0;
    }

    public int getScaledHeat(int pixels) {
        int max = this.getMaxHeat();
        return max > 0 ? this.getHeat() * pixels / max : 0;
    }

    public int getScaledBurnTime(int pixels) {
        int total = this.getBurnTimeTotal();
        return total > 0 ? this.getBurnTimeRemaining() * pixels / total : 0;
    }

    public int getScaledManualEnergy(int pixels) {
        int max = this.getMaxManualEnergy();
        return max > 0 ? this.getManualEnergy() * pixels / max : 0;
    }

    public int getScaledPrimaryGas(int pixels) {
        return ChemicalReactorBlockEntity.GAS_TANK_CAPACITY > 0
                ? this.getPrimaryGasAmount() * pixels / ChemicalReactorBlockEntity.GAS_TANK_CAPACITY
                : 0;
    }

    public int getScaledSecondaryGas(int pixels) {
        return ChemicalReactorBlockEntity.GAS_TANK_CAPACITY > 0
                ? this.getSecondaryGasAmount() * pixels / ChemicalReactorBlockEntity.GAS_TANK_CAPACITY
                : 0;
    }

    public int getScaledSecondaryFluid(int pixels) {
        return ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY > 0
                ? this.getSecondaryFluidAmount() * pixels / ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY
                : 0;
    }

    public int getScaledOutputGas(int pixels) {
        return ChemicalReactorBlockEntity.GAS_TANK_CAPACITY > 0
                ? this.getOutputGasAmount() * pixels / ChemicalReactorBlockEntity.GAS_TANK_CAPACITY
                : 0;
    }

    public int getScaledOutputFluid(int pixels) {
        return ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY > 0
                ? this.getOutputFluidAmount() * pixels / ChemicalReactorBlockEntity.FLUID_TANK_CAPACITY
                : 0;
    }

    public @Nullable GasType getPrimaryGasType() {
        return ModGases.bySyncId(this.getPrimaryGasSyncId());
    }

    public @Nullable GasType getSecondaryGasType() {
        return ModGases.bySyncId(this.getSecondaryGasSyncId());
    }

    public @Nullable GasType getOutputGasType() {
        return ModGases.bySyncId(this.getOutputGasSyncId());
    }

    public Fluid getSecondaryFluid() {
        return decodeFluid(this.getSecondaryFluidSyncId());
    }

    public Fluid getOutputFluid() {
        return decodeFluid(this.getOutputFluidSyncId());
    }

    public int getPrimaryGasColor() {
        GasType gas = this.getPrimaryGasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getSecondaryGasColor() {
        GasType gas = this.getSecondaryGasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getOutputGasColor() {
        GasType gas = this.getOutputGasType();
        return gas == null ? 0 : gas.tint();
    }

    public String getPrimaryGasName() {
        GasType gas = this.getPrimaryGasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getSecondaryGasName() {
        GasType gas = this.getSecondaryGasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getOutputGasName() {
        GasType gas = this.getOutputGasType();
        return gas == null ? "empty" : gas.name();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        var quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            if (quickMovedSlotIndex < MACHINE_SLOT_COUNT) {
                if (!this.moveToPlayerInventory(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                boolean moved = false;

                if (ChemicalReactorBlockEntity.isFuel(rawStack, player.level())) {
                    moved = this.moveItemStackTo(rawStack, SLOT_FUEL, SLOT_FUEL + 1, false);
                } else if (ChemicalReactorBlockEntity.isGasContainer(rawStack)) {
                    moved = this.moveItemStackTo(rawStack, SLOT_PRIMARY_GAS_TRANSFER, SLOT_PRIMARY_GAS_TRANSFER + 1, false)
                            || this.moveItemStackTo(rawStack, SLOT_SECONDARY_TRANSFER, SLOT_SECONDARY_TRANSFER + 1, false)
                            || this.moveItemStackTo(rawStack, SLOT_OUTPUT_TRANSFER, SLOT_OUTPUT_TRANSFER + 1, false);
                } else if (ChemicalReactorBlockEntity.isFluidContainer(rawStack)) {
                    moved = this.moveItemStackTo(rawStack, SLOT_SECONDARY_TRANSFER, SLOT_SECONDARY_TRANSFER + 1, false)
                            || this.moveItemStackTo(rawStack, SLOT_OUTPUT_TRANSFER, SLOT_OUTPUT_TRANSFER + 1, false);
                }

                if (!moved) {
                    if (!this.moveBetweenPlayerInventoryAndHotbar(
                            rawStack,
                            quickMovedSlotIndex,
                            PLAYER_INV_START,
                            PLAYER_INV_END,
                            HOTBAR_START,
                            HOTBAR_END
                    )) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return quickMovedStack;
    }
}
