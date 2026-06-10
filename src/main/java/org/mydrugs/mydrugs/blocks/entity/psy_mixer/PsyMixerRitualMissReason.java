package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public enum PsyMixerRitualMissReason {
    NONE(0, "none"),
    WRONG_ACTION(1, "wrong_action"),
    TIMEOUT(2, "timeout"),
    TOO_EARLY(3, "too_early"),
    TOO_LATE(4, "too_late"),
    MOVED(5, "moved"),
    TOO_CLOSE_TO_CORE(6, "too_close_to_core"),
    TOO_FAR_FROM_CORE(7, "too_far_from_core"),
    NOT_ENOUGH_RING_PROGRESS(8, "not_enough_ring_progress"),
    LOOKED_AWAY(9, "looked_away"),
    GUI_NOT_REOPENED(10, "gui_not_reopened"),
    ITEM_NOT_HELD(11, "item_not_held");

    private static final PsyMixerRitualMissReason[] BY_ID = values();

    public static final StreamCodec<ByteBuf, PsyMixerRitualMissReason> STREAM_CODEC = StreamCodec.of(
            (buf, reason) -> ByteBufCodecs.VAR_INT.encode(buf, reason.id),
            buf -> byId(ByteBufCodecs.VAR_INT.decode(buf))
    );

    private final int id;
    private final String serializedName;

    PsyMixerRitualMissReason(int id, String serializedName) {
        this.id = id;
        this.serializedName = serializedName;
    }

    public int id() {
        return id;
    }

    public String translationKey() {
        return "ritual.mydrugs.miss." + serializedName;
    }

    public String screenKey() {
        return "screen.mydrugs.psy_mixer.miss." + serializedName;
    }

    public static PsyMixerRitualMissReason byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }
}
