package org.mydrugs.mydrugs.recovery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.network.PersonalDiscPlaybackPayload;
import org.mydrugs.mydrugs.recovery.PersonalDiscPlaybackManager;
import org.mydrugs.mydrugs.recovery.item.PersonalMusicDiscItem;

public final class RecoveryJukeboxBlockEntity extends BlockEntity {
    private ItemStack record = ItemStack.EMPTY;
    private boolean playing;
    private long startedGameTime;

    public RecoveryJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.RECOVERY_JUKEBOX.get(), pos, state);
    }

    public boolean isEmpty() {
        return record.isEmpty();
    }

    public ItemStack getRecord() {
        return record;
    }

    public boolean isPlaying() {
        return playing;
    }

    public boolean insert(ItemStack stack, long gameTime) {
        if (!record.isEmpty() || stack.isEmpty()) {
            return false;
        }
        record = stack.copyWithCount(1);
        playing = PersonalMusicDiscItem.isPersonalDisc(record);
        startedGameTime = playing ? gameTime : 0L;
        setChanged();
        sync();
        if (playing && level instanceof ServerLevel serverLevel) {
            PersonalDiscPlaybackManager.start(serverLevel, worldPosition, record, PersonalDiscPlaybackPayload.Source.RECOVERY_JUKEBOX, startedGameTime);
        }
        return true;
    }

    public ItemStack eject() {
        ItemStack old = record;
        if (old.isEmpty()) {
            return ItemStack.EMPTY;
        }
        stopPlayback();
        record = ItemStack.EMPTY;
        playing = false;
        startedGameTime = 0L;
        setChanged();
        sync();
        return old;
    }

    public void stopPlayback() {
        if (playing && level instanceof ServerLevel serverLevel) {
            PersonalDiscPlaybackManager.stop(serverLevel, worldPosition, PersonalDiscPlaybackPayload.Source.RECOVERY_JUKEBOX);
        }
        playing = false;
    }

    public int comparatorOutput() {
        if (record.isEmpty()) {
            return 0;
        }
        return PersonalMusicDiscItem.isPersonalDisc(record) ? 12 : 11;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, RecoveryJukeboxBlockEntity be) {
        if (!(level instanceof ServerLevel serverLevel) || !be.playing || be.record.isEmpty()) {
            return;
        }
        if (!PersonalMusicDiscItem.isPersonalDisc(be.record)) {
            be.stopPlayback();
            be.setChanged();
            return;
        }
        if (level.getGameTime() % 100L == 0L) {
            PersonalDiscPlaybackManager.start(serverLevel, pos, be.record, PersonalDiscPlaybackPayload.Source.RECOVERY_JUKEBOX, be.startedGameTime);
        }
    }

    public void sync() {
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        record = input.read("record", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        playing = input.getBooleanOr("playing", false) && PersonalMusicDiscItem.isPersonalDisc(record);
        startedGameTime = input.getLongOr("started_game_time", 0L);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!record.isEmpty()) {
            output.store("record", ItemStack.CODEC, record);
        }
        output.putBoolean("playing", playing);
        output.putLong("started_game_time", startedGameTime);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (this.level == null || this.level.isClientSide()) {
            return;
        }
        stopPlayback();
        if (!record.isEmpty()) {
            Containers.dropItemStack(this.level, pos.getX(), pos.getY(), pos.getZ(), record);
            record = ItemStack.EMPTY;
        }
    }

    @Override
    public net.minecraft.nbt.CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
    }
}
