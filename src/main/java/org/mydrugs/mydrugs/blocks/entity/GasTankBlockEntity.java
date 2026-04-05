package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.GasTank;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGases;

public class GasTankBlockEntity extends BlockEntity {
    private final GasTank gasTank = new GasTank(
            8_000,
            gas -> true,
            this::onGasChanged
    );

    public GasTankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_TANK.get(), pos, state);
    }

    public IGasHandler getGasHandler(Direction side) {
        return gasTank;
    }

    public GasTank getTank() {
        return gasTank;
    }

    private void onGasChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        String gasId = input.getStringOr("gas_id", "");
        long amount = input.getLongOr("gas_amount", 0L);

        GasType gas = gasId.isBlank() ? null : ModGases.get(ResourceLocation.parse(gasId));
        gasTank.loadStored(gas, amount);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        GasType gas = gasTank.getGasType();
        output.putString("gas_id", gas == null ? "" : gas.id().toString());
        output.putLong("gas_amount", gasTank.getAmount());
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