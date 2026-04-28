package org.mydrugs.mydrugs.pipe.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.gas.IGasHandler;
import org.mydrugs.mydrugs.machine.MachineSync;
import org.mydrugs.mydrugs.pipe.PipeConnectionMode;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;
import org.mydrugs.mydrugs.pipe.PipeSideConfig;
import org.mydrugs.mydrugs.pipe.PipeTier;
import org.mydrugs.mydrugs.pipe.block.PipeBlock;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;
import org.mydrugs.mydrugs.pipe.network.PipeNetworkDirtyReason;
import org.mydrugs.mydrugs.pipe.network.PipeNetworkManager;

import java.util.EnumMap;
import java.util.Map;

public class PipeBlockEntity extends BlockEntity {
    private final EnumMap<Direction, PipeSideConfig> sideConfigs = new EnumMap<>(Direction.class);

    public PipeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PIPES.get(), pos, blockState);
        for (Direction direction : Direction.values()) {
            this.sideConfigs.put(direction, new PipeSideConfig());
        }
    }

    public PipeResourceKind kind() {
        if (this.getBlockState().getBlock() instanceof PipeBlock pipeBlock) {
            return pipeBlock.kind();
        }
        return PipeResourceKind.ITEM;
    }

    public PipeTier tier() {
        if (this.getBlockState().getBlock() instanceof PipeBlock pipeBlock) {
            return pipeBlock.tier();
        }
        return PipeTier.BASIC;
    }

    public PipeSideConfig getSideConfig(Direction side) {
        return this.sideConfigs.get(side);
    }

    public Map<Direction, PipeSideConfig> copySideConfigs() {
        EnumMap<Direction, PipeSideConfig> copy = new EnumMap<>(Direction.class);
        this.sideConfigs.forEach((direction, config) -> copy.put(direction, config.copy()));
        return copy;
    }

    public PipeConnectionMode cycleSide(Direction side) {
        PipeSideConfig config = this.sideConfigs.get(side);
        config.setMode(config.mode().next());
        this.onConfigurationChanged(PipeNetworkDirtyReason.SIDE_CONFIG_CHANGED);
        return config.mode();
    }

    public PipeConnectionMode disableSide(Direction side) {
        PipeSideConfig config = this.sideConfigs.get(side);
        config.setMode(PipeConnectionMode.DISABLED);
        config.setFilter(null);
        this.onConfigurationChanged(PipeNetworkDirtyReason.SIDE_CONFIG_CHANGED);
        return config.mode();
    }

    public boolean hasFilter(Direction side) {
        return this.sideConfigs.get(side).filter() != null;
    }

    public void applyFilter(Direction side, PipeFilterConfig filter) {
        this.sideConfigs.get(side).setFilter(filter);
        this.onConfigurationChanged(PipeNetworkDirtyReason.FILTER_CHANGED);
    }

    @Override
    public void setLevel(Level level) {
        super.setLevel(level);
        this.markLoadedNetworkDirty();
    }

    @Nullable
    public ResourceHandler<ItemResource> getItemHandler(@Nullable Direction side) {
        if (this.kind() != PipeResourceKind.ITEM || side == null || !this.exposesAutomation(side)) {
            return null;
        }
        return NoopItemPipeResourceHandler.INSTANCE;
    }

    @Nullable
    public ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (this.kind() != PipeResourceKind.FLUID || side == null || !this.exposesAutomation(side)) {
            return null;
        }
        return NoopFluidPipeResourceHandler.INSTANCE;
    }

    @Nullable
    public IGasHandler getGasHandler(@Nullable Direction side) {
        if (this.kind() != PipeResourceKind.GAS || side == null || !this.exposesAutomation(side)) {
            return null;
        }
        return NoopGasPipeHandler.INSTANCE;
    }

    private boolean exposesAutomation(Direction side) {
        PipeConnectionMode mode = this.sideConfigs.get(side).mode();
        return mode == PipeConnectionMode.INPUT || mode == PipeConnectionMode.OUTPUT;
    }

    private void onConfigurationChanged(PipeNetworkDirtyReason reason) {
        if (this.level != null) {
            PipeNetworkManager.markDirty(this.level, this.worldPosition, this.kind(), reason);
            for (Direction direction : Direction.values()) {
                BlockPos neighborPos = this.worldPosition.relative(direction);
                if (this.level.getBlockEntity(neighborPos) instanceof PipeBlockEntity neighbor && neighbor.kind() == this.kind()) {
                    PipeNetworkManager.markDirty(this.level, neighborPos, this.kind(), reason);
                }
            }
        }
        MachineSync.syncAndInvalidateCaps(this);
    }

    private void markLoadedNetworkDirty() {
        if (this.level != null && !this.level.isClientSide()) {
            PipeNetworkManager.markDirty(this.level, this.worldPosition, this.kind(), PipeNetworkDirtyReason.CHUNK_LOAD);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        for (Direction direction : Direction.values()) {
            PipeSideConfig config = this.sideConfigs.get(direction);
            String key = direction.getSerializedName();
            output.putString(key + "_mode", config.mode().name());
            if (config.filter() != null) {
                output.store(key + "_filter", PipeFilterConfig.CODEC, config.filter());
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        for (Direction direction : Direction.values()) {
            String key = direction.getSerializedName();
            PipeSideConfig config = this.sideConfigs.get(direction);
            config.setMode(readMode(input.getStringOr(key + "_mode", PipeConnectionMode.DISABLED.name())));
            config.setFilter(input.read(key + "_filter", PipeFilterConfig.CODEC).orElse(null));
        }
    }

    private static PipeConnectionMode readMode(String raw) {
        try {
            return PipeConnectionMode.valueOf(raw);
        } catch (IllegalArgumentException ignored) {
            return PipeConnectionMode.DISABLED;
        }
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
