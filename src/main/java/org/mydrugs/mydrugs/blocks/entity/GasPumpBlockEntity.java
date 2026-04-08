package org.mydrugs.mydrugs.blocks.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.GasPumpBlock;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.GasTank;
import org.mydrugs.mydrugs.gas.GasTransport;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.gas.ModGasCapabilities;
import org.mydrugs.mydrugs.gas.ModGases;

public class GasPumpBlockEntity extends BlockEntity {
    private final GasTank buffer = new GasTank(
            1_000,
            gas -> true,
            this::onGasChanged
    );

    public GasPumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GAS_PUMP.get(), pos, state);
    }

    public IGasHandler getGasHandler(Direction side) {
        return buffer;
    }

    private void onGasChanged() {
        this.setChanged();

        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GasPumpBlockEntity be) {
        if (level.isClientSide()) {
            return;
        }

        Direction output = state.getValue(GasPumpBlock.FACING);
        Direction input = output.getOpposite();

        IGasHandler inputHandler = level.getCapability(
                ModGasCapabilities.BLOCK,
                pos.relative(input),
                input.getOpposite()
        );

        if (inputHandler != null) {
            GasTransport.move(inputHandler, be.buffer, 250);
        } else {
            // no source block: use ambient air
            if (be.buffer.getGasType() == null || be.buffer.getGasType() == ModGases.AIR) {
                long newAmount = Math.min(1_000, be.buffer.getAmount() + 250);
                be.buffer.loadStored(ModGases.AIR, newAmount);
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }
        }

        IGasHandler outputHandler = level.getCapability(
                ModGasCapabilities.BLOCK,
                pos.relative(output),
                output.getOpposite()
        );

        if (outputHandler != null) {
            GasTransport.move(be.buffer, outputHandler, 250);
        }
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        String gasId = input.getStringOr("gas_id", "");
        long amount = input.getLongOr("gas_amount", 0L);

        GasType gas = gasId.isBlank() ? null : ModGases.get(ResourceLocation.parse(gasId));
        buffer.loadStored(gas, amount);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        GasType gas = buffer.getGasType();
        output.putString("gas_id", gas == null ? "" : gas.id().toString());
        output.putLong("gas_amount", buffer.getAmount());
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