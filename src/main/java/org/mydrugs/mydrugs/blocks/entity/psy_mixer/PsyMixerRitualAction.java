package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum PsyMixerRitualAction {
    NONE(0, "none", 0, 20),
    SNEAK(1, "sneak", 1, 80),
    JUMP(2, "jump", 1, 80),
    RIGHT_CLICK_AIR(3, "right_click_air", 1, 80),
    WALK_RING(4, "walk_ring", 1, 160),
    LOOK_AT_CORE(5, "look_at_core", 1, 100),
    TIMING_RING(6, "timing_ring", 3, 120),
    STAND_STILL(7, "stand_still", 1, 100),
    HOLD_ITEM(8, "hold_item", 1, 100),
    REOPEN_GUI(9, "reopen_gui", 1, 160);

    private static final PsyMixerRitualAction[] BY_ID = values();

    public static final Codec<PsyMixerRitualAction> CODEC = Codec.STRING.xmap(
            PsyMixerRitualAction::bySerializedName,
            PsyMixerRitualAction::serializedName
    );

    public static final StreamCodec<ByteBuf, PsyMixerRitualAction> STREAM_CODEC = StreamCodec.of(
            PsyMixerRitualAction::encode,
            PsyMixerRitualAction::decode
    );

    private final int id;
    private final String serializedName;
    private final int maxQualityPoints;
    private final int defaultTimeoutTicks;

    PsyMixerRitualAction(int id, String serializedName, int maxQualityPoints, int defaultTimeoutTicks) {
        this.id = id;
        this.serializedName = serializedName;
        this.maxQualityPoints = maxQualityPoints;
        this.defaultTimeoutTicks = defaultTimeoutTicks;
    }

    public int id() {
        return id;
    }

    public String serializedName() {
        return serializedName;
    }

    public int maxQualityPoints() {
        return maxQualityPoints;
    }

    public int defaultTimeoutTicks() {
        return defaultTimeoutTicks;
    }

    public String promptKey() {
        return "ritual.mydrugs.action." + serializedName + ".prompt";
    }

    public String hintKey() {
        return "ritual.mydrugs.action." + serializedName + ".hint";
    }

    public boolean isTimingRing() {
        return this == TIMING_RING;
    }

    public boolean canBeRandomlySelected() {
        return switch (this) {
            case SNEAK, JUMP, RIGHT_CLICK_AIR, WALK_RING, LOOK_AT_CORE, TIMING_RING, STAND_STILL, REOPEN_GUI -> true;
            default -> false;
        };
    }

    public static List<PsyMixerRitualAction> defaultRandomPool() {
        return Arrays.stream(values())
                .filter(PsyMixerRitualAction::canBeRandomlySelected)
                .toList();
    }

    public static PsyMixerRitualAction byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }

    public static PsyMixerRitualAction bySerializedName(String name) {
        if (name == null) {
            return NONE;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (PsyMixerRitualAction action : values()) {
            if (action.serializedName.equals(normalized)) {
                return action;
            }
        }
        return NONE;
    }

    private static void encode(ByteBuf buf, PsyMixerRitualAction action) {
        ByteBufCodecs.VAR_INT.encode(buf, action.id);
    }

    private static PsyMixerRitualAction decode(ByteBuf buf) {
        return byId(ByteBufCodecs.VAR_INT.decode(buf));
    }
}
