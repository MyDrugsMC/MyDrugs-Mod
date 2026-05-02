package org.mydrugs.mydrugs.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeCoreBlockEntity;
import org.mydrugs.mydrugs.machine.MachineStatus;

public final class PsychotropeGeneratorMenu extends AbstractMachineMenu {
    public static final int DATA_COUNT = 8;
    public static final int RADIUS_BUTTON_BASE = 100;

    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final BlockPos blockPos;
    private final PsychotropeCoreBlockEntity core;

    public PsychotropeGeneratorMenu(int containerId, Inventory inventory, RegistryFriendlyByteBuf buffer) {
        this(
                containerId,
                inventory,
                null,
                new SimpleContainerData(DATA_COUNT),
                ContainerLevelAccess.NULL,
                buffer.readBlockPos()
        );
    }

    public PsychotropeGeneratorMenu(
            int containerId,
            Inventory inventory,
            PsychotropeCoreBlockEntity core,
            ContainerData data,
            ContainerLevelAccess access,
            BlockPos blockPos
    ) {
        super(ModMenus.PSYCHOTROPE_GENERATOR.get(), containerId);
        checkContainerDataCount(data, DATA_COUNT);
        this.core = core;
        this.data = data;
        this.access = access;
        this.blockPos = blockPos;
        this.addDataSlots(data);
        this.addPlayerInventorySlots(inventory, 8, 92);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id >= RADIUS_BUTTON_BASE && id < RADIUS_BUTTON_BASE + PsychotropeCoreBlockEntity.MAX_RADIUS) {
            if (this.core != null) {
                this.core.setPowerRadius(id - RADIUS_BUTTON_BASE + 1);
                return true;
            }
        }
        return false;
    }

    public int getProgress() {
        return this.data.get(0);
    }

    public int getMaxProgress() {
        return this.data.get(1);
    }

    public int getEnergyStored() {
        return this.data.get(2);
    }

    public int getEnergyCapacity() {
        return this.data.get(3);
    }

    public int getPowerRadius() {
        return this.data.get(4);
    }

    public boolean isFormed() {
        return this.data.get(5) != 0;
    }

    public int getActiveDrugNetworkId() {
        return this.data.get(6);
    }

    public MachineStatus getStatus() {
        return MachineStatus.byNetworkId(this.data.get(7));
    }

    public int getScaledProgress(int pixels) {
        int max = getMaxProgress();
        return max > 0 ? getProgress() * pixels / max : 0;
    }

    public int getScaledEnergy(int pixels) {
        int capacity = getEnergyCapacity();
        return capacity > 0 ? getEnergyStored() * pixels / capacity : 0;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, ModBlocks.PSYCHOTROPE_CORE.get());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }
}
