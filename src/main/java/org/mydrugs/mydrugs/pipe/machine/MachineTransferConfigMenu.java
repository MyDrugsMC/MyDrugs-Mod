package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.mydrugs.mydrugs.menu.ModMenus;

import java.util.List;

public class MachineTransferConfigMenu extends AbstractContainerMenu {
    private static final MachineLocalSide[] LOCAL_SIDES = MachineLocalSide.values();

    private final BlockPos targetPos;
    private final List<MachineTransferPortSpec> ports;
    private final ContainerData ruleData;
    private final Direction frontDirection;

    public MachineTransferConfigMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, BlockPos.ZERO);
    }

    public MachineTransferConfigMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, buf.readBlockPos());
    }

    public MachineTransferConfigMenu(int containerId, Inventory playerInventory, BlockPos targetPos) {
        super(ModMenus.MACHINE_TRANSFER_CONFIG.get(), containerId);
        this.targetPos = targetPos;
        BlockEntity blockEntity = playerInventory.player.level().getBlockEntity(targetPos);
        if (blockEntity != null && !playerInventory.player.level().isClientSide()) {
            MachineTransferAttachments.config(blockEntity);
        }
        this.ports = blockEntity != null ? MachineTransferAttachments.ports(blockEntity) : List.of();
        this.frontDirection = blockEntity != null ? MachineOrientation.front(blockEntity.getBlockState()) : Direction.NORTH;
        this.ruleData = createRuleData(playerInventory, blockEntity);
        this.addDataSlots(this.ruleData);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        BlockEntity blockEntity = player.level().getBlockEntity(this.targetPos);
        return blockEntity != null
                && player.distanceToSqr(
                this.targetPos.getX() + 0.5D,
                this.targetPos.getY() + 0.5D,
                this.targetPos.getZ() + 0.5D
        ) <= 64.0D
                && MachineTransferAttachments.isSupported(blockEntity)
                && MachineTransferAttachments.hasTransferUpgrade(blockEntity);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (!stillValid(player)) {
            return false;
        }

        DecodedRule decoded = decode(id);
        if (decoded == null) {
            return false;
        }

        BlockEntity blockEntity = player.level().getBlockEntity(this.targetPos);
        if (blockEntity == null) {
            return false;
        }

        MachineTransferAttachments.config(blockEntity).cycleRule(decoded.port(), decoded.side());
        MachineTransferAttachments.markCapabilityChanged(blockEntity);
        broadcastChanges();
        return true;
    }

    public BlockPos targetPos() {
        return this.targetPos;
    }

    public Direction frontDirection() {
        return this.frontDirection;
    }

    public List<MachineTransferPortSpec> ports() {
        return this.ports;
    }

    public MachineTransferSideRule rule(int portIndex, MachineLocalSide side) {
        int value = this.ruleData.get(dataIndex(portIndex, side));
        return MachineTransferSideRule.byNetworkId(value);
    }

    public Direction worldDirection(MachineLocalSide side) {
        return MachineOrientation.toWorld(this.frontDirection, side);
    }

    public static int buttonId(int portIndex, MachineLocalSide side) {
        return dataIndex(portIndex, side);
    }

    private ContainerData createRuleData(Inventory playerInventory, BlockEntity blockEntity) {
        int count = this.ports.size() * LOCAL_SIDES.length;
        if (blockEntity == null || playerInventory.player.level().isClientSide()) {
            return new SimpleContainerData(count);
        }

        return new ContainerData() {
            @Override
            public int get(int index) {
                DecodedRule decoded = decode(index);
                if (decoded == null) {
                    return MachineTransferSideRule.DISABLED.networkId();
                }

                return MachineTransferAttachments.config(blockEntity)
                        .getRule(decoded.port().id(), decoded.side())
                        .networkId();
            }

            @Override
            public void set(int index, int value) {
                DecodedRule decoded = decode(index);
                if (decoded == null) {
                    return;
                }

                MachineTransferSideRule rule = MachineTransferSideRule.byNetworkId(value);
                MachineTransferAttachments.config(blockEntity).setRule(decoded.port(), decoded.side(), rule);
                MachineTransferAttachments.markCapabilityChanged(blockEntity);
            }

            @Override
            public int getCount() {
                return count;
            }
        };
    }

    private DecodedRule decode(int index) {
        int portIndex = index / LOCAL_SIDES.length;
        int sideOrdinal = index % LOCAL_SIDES.length;
        if (portIndex < 0 || portIndex >= this.ports.size() || sideOrdinal < 0 || sideOrdinal >= LOCAL_SIDES.length) {
            return null;
        }

        return new DecodedRule(this.ports.get(portIndex), LOCAL_SIDES[sideOrdinal]);
    }

    private static int dataIndex(int portIndex, MachineLocalSide side) {
        return portIndex * LOCAL_SIDES.length + side.networkId();
    }

    private record DecodedRule(MachineTransferPortSpec port, MachineLocalSide side) {
    }
}
