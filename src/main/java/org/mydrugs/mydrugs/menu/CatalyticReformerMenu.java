package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.CatalyticReformerBlockEntity;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.menu.layout.CatalyticReformerLayout;

public class CatalyticReformerMenu extends AbstractMachineMenu {
    public static final int INPUT_1_TRANSFER_SLOT = 0;
    public static final int INPUT_2_TRANSFER_SLOT = 1;
    public static final int OUTPUT_1_TRANSFER_SLOT = 2;
    public static final int OUTPUT_2_TRANSFER_SLOT = 3;
    public static final int OUTPUT_3_TRANSFER_SLOT = 4;
    public static final int CATALYST_SLOT = 5;

    public static final int MACHINE_SLOT_COUNT = 6;
    public static final int DATA_COUNT = 22;

    public static final int DUMP_INPUT_1_BUTTON_ID = 0;
    public static final int DUMP_INPUT_2_BUTTON_ID = 1;
    public static final int DUMP_OUTPUT_1_BUTTON_ID = 2;
    public static final int DUMP_OUTPUT_2_BUTTON_ID = 3;
    public static final int DUMP_OUTPUT_3_BUTTON_ID = 4;

    public static final int FLUID_TANK_CAPACITY = CatalyticReformerBlockEntity.FLUID_CAPACITY;
    public static final int GAS_TANK_CAPACITY = CatalyticReformerBlockEntity.GAS_CAPACITY;

    private static final int PLAYER_INV_START = MACHINE_SLOT_COUNT;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final Container container;
    private final ContainerData data;
    private final ContainerLevelAccess access;

    public CatalyticReformerMenu(int containerId, Inventory playerInventory) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MACHINE_SLOT_COUNT),
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL
        );
    }

    public CatalyticReformerMenu(int containerId, Inventory playerInventory, Container container, ContainerData data, ContainerLevelAccess access) {
        super(ModMenus.CATALYTIC_REFORMER.get(), containerId);
        checkContainerSize(container, MACHINE_SLOT_COUNT);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.access = access;

        container.startOpen(playerInventory.player);

        this.addSlot(new Slot(container, INPUT_1_TRANSFER_SLOT, CatalyticReformerLayout.INPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CatalyticReformerBlockEntity.isFluidContainer(stack) || CatalyticReformerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(container, INPUT_2_TRANSFER_SLOT, CatalyticReformerLayout.INPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CatalyticReformerBlockEntity.isFluidContainer(stack) || CatalyticReformerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(container, OUTPUT_1_TRANSFER_SLOT, CatalyticReformerLayout.OUTPUT_1_SLOT_X, CatalyticReformerLayout.SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CatalyticReformerBlockEntity.isFluidContainer(stack) || CatalyticReformerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(container, OUTPUT_2_TRANSFER_SLOT, CatalyticReformerLayout.OUTPUT_2_SLOT_X, CatalyticReformerLayout.SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CatalyticReformerBlockEntity.isFluidContainer(stack) || CatalyticReformerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(container, OUTPUT_3_TRANSFER_SLOT, CatalyticReformerLayout.OUTPUT_3_SLOT_X, CatalyticReformerLayout.SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return CatalyticReformerBlockEntity.isFluidContainer(stack) || CatalyticReformerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addSlot(new Slot(container, CATALYST_SLOT, CatalyticReformerLayout.CATALYST_SLOT_X, CatalyticReformerLayout.CATALYST_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return !stack.isEmpty()
                        && !CatalyticReformerBlockEntity.isFluidContainer(stack)
                        && !CatalyticReformerBlockEntity.isGasContainer(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1;
            }
        });

        this.addPlayerInventorySlots(playerInventory, CatalyticReformerLayout.PLAYER_INV_X, CatalyticReformerLayout.PLAYER_INV_Y);
        this.addDataSlots(data);
    }

    private static Fluid decodeFluid(int syncId) {
        return syncId < 0 ? Fluids.EMPTY : BuiltInRegistries.FLUID.byId(syncId);
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, ModBlocks.CATALYTIC_REFORMER.get());
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (this.container instanceof CatalyticReformerButtonHandler handler) {
            return switch (id) {
                case DUMP_INPUT_1_BUTTON_ID,
                     DUMP_INPUT_2_BUTTON_ID,
                     DUMP_OUTPUT_1_BUTTON_ID,
                     DUMP_OUTPUT_2_BUTTON_ID,
                     DUMP_OUTPUT_3_BUTTON_ID -> handler.onCatalyticReformerButtonPressed(player, id);
                default -> false;
            };
        }

        return false;
    }

    public int getInput1Amount() {
        return this.data.get(0);
    }

    public int getInput2Amount() {
        return this.data.get(1);
    }

    public int getOutput1Amount() {
        return this.data.get(2);
    }

    public int getOutput2Amount() {
        return this.data.get(3);
    }

    public int getOutput3Amount() {
        return this.data.get(4);
    }

    public int getProgress() {
        return this.data.get(5);
    }

    public int getMaxProgress() {
        return this.data.get(6);
    }

    public int getInput1FluidSyncId() {
        return this.data.get(7);
    }

    public int getInput2FluidSyncId() {
        return this.data.get(8);
    }

    public int getOutput1FluidSyncId() {
        return this.data.get(9);
    }

    public int getOutput2FluidSyncId() {
        return this.data.get(10);
    }

    public int getOutput3FluidSyncId() {
        return this.data.get(11);
    }

    public int getInput1GasSyncId() {
        return this.data.get(12);
    }

    public int getInput2GasSyncId() {
        return this.data.get(13);
    }

    public int getOutput1GasSyncId() {
        return this.data.get(14);
    }

    public int getOutput2GasSyncId() {
        return this.data.get(15);
    }

    public int getOutput3GasSyncId() {
        return this.data.get(16);
    }

    public boolean isInput1GasMode() {
        return this.data.get(17) != 0;
    }

    public boolean isInput2GasMode() {
        return this.data.get(18) != 0;
    }

    public boolean isOutput1GasMode() {
        return this.data.get(19) != 0;
    }

    public boolean isOutput2GasMode() {
        return this.data.get(20) != 0;
    }

    public boolean isOutput3GasMode() {
        return this.data.get(21) != 0;
    }

    public Fluid getInput1Fluid() {
        return decodeFluid(this.getInput1FluidSyncId());
    }

    public Fluid getInput2Fluid() {
        return decodeFluid(this.getInput2FluidSyncId());
    }

    public Fluid getOutput1Fluid() {
        return decodeFluid(this.getOutput1FluidSyncId());
    }

    public Fluid getOutput2Fluid() {
        return decodeFluid(this.getOutput2FluidSyncId());
    }

    public Fluid getOutput3Fluid() {
        return decodeFluid(this.getOutput3FluidSyncId());
    }

    public @Nullable GasType getInput1GasType() {
        return ModGases.bySyncId(this.getInput1GasSyncId());
    }

    public @Nullable GasType getInput2GasType() {
        return ModGases.bySyncId(this.getInput2GasSyncId());
    }

    public @Nullable GasType getOutput1GasType() {
        return ModGases.bySyncId(this.getOutput1GasSyncId());
    }

    public @Nullable GasType getOutput2GasType() {
        return ModGases.bySyncId(this.getOutput2GasSyncId());
    }

    public @Nullable GasType getOutput3GasType() {
        return ModGases.bySyncId(this.getOutput3GasSyncId());
    }

    public int getInput1GasColor() {
        GasType gas = this.getInput1GasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getInput2GasColor() {
        GasType gas = this.getInput2GasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getOutput1GasColor() {
        GasType gas = this.getOutput1GasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getOutput2GasColor() {
        GasType gas = this.getOutput2GasType();
        return gas == null ? 0 : gas.tint();
    }

    public int getOutput3GasColor() {
        GasType gas = this.getOutput3GasType();
        return gas == null ? 0 : gas.tint();
    }

    public String getInput1GasName() {
        GasType gas = this.getInput1GasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getInput2GasName() {
        GasType gas = this.getInput2GasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getOutput1GasName() {
        GasType gas = this.getOutput1GasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getOutput2GasName() {
        GasType gas = this.getOutput2GasType();
        return gas == null ? "empty" : gas.name();
    }

    public String getOutput3GasName() {
        GasType gas = this.getOutput3GasType();
        return gas == null ? "empty" : gas.name();
    }

    public int getScaledProgress(int pixels) {
        int progress = this.getProgress();
        int max = this.getMaxProgress();
        return max > 0 ? progress * pixels / max : 0;
    }

    public int getScaledInput1Tank(int pixels) {
        int capacity = this.isInput1GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getInput1Amount() * pixels / capacity : 0;
    }

    public int getScaledInput2Tank(int pixels) {
        int capacity = this.isInput2GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getInput2Amount() * pixels / capacity : 0;
    }

    public int getScaledOutput1Tank(int pixels) {
        int capacity = this.isOutput1GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getOutput1Amount() * pixels / capacity : 0;
    }

    public int getScaledOutput2Tank(int pixels) {
        int capacity = this.isOutput2GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getOutput2Amount() * pixels / capacity : 0;
    }

    public int getScaledOutput3Tank(int pixels) {
        int capacity = this.isOutput3GasMode() ? GAS_TANK_CAPACITY : FLUID_TANK_CAPACITY;
        return capacity > 0 ? this.getOutput3Amount() * pixels / capacity : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int quickMovedSlotIndex) {
        ItemStack quickMovedStack = ItemStack.EMPTY;
        Slot quickMovedSlot = this.slots.get(quickMovedSlotIndex);

        if (quickMovedSlot != null && quickMovedSlot.hasItem()) {
            ItemStack rawStack = quickMovedSlot.getItem();
            quickMovedStack = rawStack.copy();

            if (quickMovedSlotIndex < MACHINE_SLOT_COUNT) {
                if (!this.moveToPlayerInventory(rawStack, PLAYER_INV_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (quickMovedSlotIndex < HOTBAR_END) {
                boolean moved = false;

                if (CatalyticReformerBlockEntity.isFluidContainer(rawStack) || CatalyticReformerBlockEntity.isGasContainer(rawStack)) {
                    moved = this.moveItemStackTo(rawStack, INPUT_1_TRANSFER_SLOT, OUTPUT_3_TRANSFER_SLOT + 1, false);
                } else {
                    moved = this.moveItemStackTo(rawStack, CATALYST_SLOT, CATALYST_SLOT + 1, false);
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
            } else {
                return ItemStack.EMPTY;
            }

            return this.finishQuickMove(player, quickMovedSlot, rawStack, quickMovedStack);
        }

        return quickMovedStack;
    }

    public interface CatalyticReformerButtonHandler {
        boolean onCatalyticReformerButtonPressed(Player player, int buttonId);
    }
}
