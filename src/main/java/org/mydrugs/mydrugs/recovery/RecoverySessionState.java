package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class RecoverySessionState implements ValueIOSerializable {
    public RecoverySessionStage stage = RecoverySessionStage.NONE;
    public long stageStartedAt;
    public String roomDimension = "";
    public int anchorX;
    public int anchorY;
    public int anchorZ;
    public boolean hasAnchor;
    public long musicGroundingStartedAt;
    public long breathingStartedAt;
    public boolean lastPositionValid;
    public double lastX;
    public double lastY;
    public double lastZ;
    public boolean returnPromptSent;
    public long lastLeftEarlyMessageAt = -100000L;
    public long lastReturnPromptMessageAt = -100000L;
    public long lastMessageAt = -100000L;
    public String lastMessageKey = "";

    public void start(RecoveryRoomReport room, String dimension, long gameTime) {
        this.stage = RecoverySessionStage.ARRIVE;
        this.stageStartedAt = gameTime;
        setRoom(room, dimension);
        this.musicGroundingStartedAt = 0L;
        this.breathingStartedAt = 0L;
        this.returnPromptSent = false;
    }

    public void advance(RecoverySessionStage nextStage, long gameTime) {
        this.stage = nextStage == null ? RecoverySessionStage.NONE : nextStage;
        this.stageStartedAt = gameTime;
        this.musicGroundingStartedAt = 0L;
        this.breathingStartedAt = 0L;
        if (this.stage != RecoverySessionStage.RETURN) {
            this.returnPromptSent = false;
        }
    }

    public void resetProgress() {
        this.stage = RecoverySessionStage.NONE;
        this.stageStartedAt = 0L;
        this.roomDimension = "";
        this.anchorX = 0;
        this.anchorY = 0;
        this.anchorZ = 0;
        this.hasAnchor = false;
        this.musicGroundingStartedAt = 0L;
        this.breathingStartedAt = 0L;
        this.returnPromptSent = false;
    }

    public void setRoom(RecoveryRoomReport room, String dimension) {
        if (room == null) {
            this.hasAnchor = false;
            this.roomDimension = "";
            return;
        }
        BlockPos anchor = room.anchorPos();
        this.anchorX = anchor.getX();
        this.anchorY = anchor.getY();
        this.anchorZ = anchor.getZ();
        this.roomDimension = dimension == null ? "" : dimension;
        this.hasAnchor = true;
    }

    public boolean isSameRoom(RecoveryRoomReport room, String dimension) {
        if (!hasAnchor || room == null) {
            return false;
        }
        BlockPos anchor = room.anchorPos();
        return anchor.getX() == anchorX
                && anchor.getY() == anchorY
                && anchor.getZ() == anchorZ
                && roomDimension.equals(dimension == null ? "" : dimension);
    }

    public void updateLastPosition(double x, double y, double z) {
        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;
        this.lastPositionValid = true;
    }

    public RecoverySessionState copy() {
        RecoverySessionState copy = new RecoverySessionState();
        copy.stage = stage;
        copy.stageStartedAt = stageStartedAt;
        copy.roomDimension = roomDimension;
        copy.anchorX = anchorX;
        copy.anchorY = anchorY;
        copy.anchorZ = anchorZ;
        copy.hasAnchor = hasAnchor;
        copy.musicGroundingStartedAt = musicGroundingStartedAt;
        copy.breathingStartedAt = breathingStartedAt;
        copy.lastPositionValid = lastPositionValid;
        copy.lastX = lastX;
        copy.lastY = lastY;
        copy.lastZ = lastZ;
        copy.returnPromptSent = returnPromptSent;
        copy.lastLeftEarlyMessageAt = lastLeftEarlyMessageAt;
        copy.lastReturnPromptMessageAt = lastReturnPromptMessageAt;
        copy.lastMessageAt = lastMessageAt;
        copy.lastMessageKey = lastMessageKey;
        return copy;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putString("stage", stage.serializedName());
        output.putLong("stage_started_at", stageStartedAt);
        output.putString("room_dimension", roomDimension);
        output.putInt("anchor_x", anchorX);
        output.putInt("anchor_y", anchorY);
        output.putInt("anchor_z", anchorZ);
        output.putBoolean("has_anchor", hasAnchor);
        output.putLong("music_grounding_started_at", musicGroundingStartedAt);
        output.putLong("breathing_started_at", breathingStartedAt);
        output.putBoolean("last_position_valid", lastPositionValid);
        output.putFloat("last_x", (float) lastX);
        output.putFloat("last_y", (float) lastY);
        output.putFloat("last_z", (float) lastZ);
        output.putBoolean("return_prompt_sent", returnPromptSent);
        output.putLong("last_left_early_message_at", lastLeftEarlyMessageAt);
        output.putLong("last_return_prompt_message_at", lastReturnPromptMessageAt);
        output.putLong("last_message_at", lastMessageAt);
        output.putString("last_message_key", lastMessageKey);
    }

    @Override
    public void deserialize(ValueInput input) {
        stage = RecoverySessionStage.bySerializedName(input.getStringOr("stage", ""));
        stageStartedAt = input.getLongOr("stage_started_at", 0L);
        roomDimension = input.getStringOr("room_dimension", "");
        anchorX = input.getIntOr("anchor_x", 0);
        anchorY = input.getIntOr("anchor_y", 0);
        anchorZ = input.getIntOr("anchor_z", 0);
        hasAnchor = input.getBooleanOr("has_anchor", false);
        musicGroundingStartedAt = input.getLongOr("music_grounding_started_at", 0L);
        breathingStartedAt = input.getLongOr("breathing_started_at", 0L);
        lastPositionValid = input.getBooleanOr("last_position_valid", false);
        lastX = input.getFloatOr("last_x", 0.0F);
        lastY = input.getFloatOr("last_y", 0.0F);
        lastZ = input.getFloatOr("last_z", 0.0F);
        returnPromptSent = input.getBooleanOr("return_prompt_sent", false);
        lastLeftEarlyMessageAt = input.getLongOr("last_left_early_message_at", -100000L);
        lastReturnPromptMessageAt = input.getLongOr("last_return_prompt_message_at", -100000L);
        lastMessageAt = input.getLongOr("last_message_at", -100000L);
        lastMessageKey = input.getStringOr("last_message_key", "");
    }
}
