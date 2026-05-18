package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugEffectData;

import java.util.Locale;

public enum PsyMixerRitualQuality {
    CRUDE(0, "crude", 0.80F, 1.20F),
    BASE(1, "base", 1.00F, 1.00F),
    PERFECT(2, "perfect", 1.20F, 0.80F),
    MASTERWORK(3, "masterwork", 1.40F, 0.60F);

    private static final PsyMixerRitualQuality[] BY_ID = values();

    public static final Codec<PsyMixerRitualQuality> CODEC = Codec.STRING.xmap(
            PsyMixerRitualQuality::bySerializedName,
            PsyMixerRitualQuality::serializedName
    );

    public static final StreamCodec<ByteBuf, PsyMixerRitualQuality> STREAM_CODEC = StreamCodec.of(
            PsyMixerRitualQuality::encode,
            PsyMixerRitualQuality::decode
    );

    private final int id;
    private final String serializedName;
    private final float positiveMultiplier;
    private final float negativeMultiplier;

    PsyMixerRitualQuality(int id, String serializedName, float positiveMultiplier, float negativeMultiplier) {
        this.id = id;
        this.serializedName = serializedName;
        this.positiveMultiplier = positiveMultiplier;
        this.negativeMultiplier = negativeMultiplier;
    }

    public int id() {
        return id;
    }

    public String serializedName() {
        return serializedName;
    }

    public String translationKey() {
        return "screen.mydrugs.psy_mixer.quality." + serializedName;
    }

    public float positiveMultiplier() {
        return positiveMultiplier;
    }

    public float negativeMultiplier() {
        return negativeMultiplier;
    }

    public int positivePercent() {
        return Math.round(positiveMultiplier * 100.0F);
    }

    public int negativePercent() {
        return Math.round(negativeMultiplier * 100.0F);
    }

    public RitualDrugEffectData applyTo(RitualDrugEffectData effect) {
        float multiplier = isNegative(effect.type()) ? negativeMultiplier : positiveMultiplier;
        float intensity = clamp(effect.intensity() * multiplier, 0.0F, 1.0F);
        return new RitualDrugEffectData(effect.type(), effect.duration(), intensity);
    }

    public static PsyMixerRitualQuality byId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : BASE;
    }

    public static PsyMixerRitualQuality bySerializedName(String name) {
        if (name == null) {
            return BASE;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (PsyMixerRitualQuality quality : values()) {
            if (quality.serializedName.equals(normalized)) {
                return quality;
            }
        }
        return BASE;
    }

    private static boolean isNegative(EffectType type) {
        return switch (type) {
            case CUSTOM_NAUSEA,
                    MOVEMENT_SLOWDOWN,
                    HP_DECREASE,
                    CAMERA_SWAY,
                    TREMOR,
                    STUMBLE,
                    INPUT_FAIL,
                    VOMIT,
                    CONFUSION,
                    NAUSEA,
                    SLOWNESS,
                    HEARTBEAT -> true;
            default -> false;
        };
    }

    private static void encode(ByteBuf buf, PsyMixerRitualQuality quality) {
        ByteBufCodecs.VAR_INT.encode(buf, quality.id);
    }

    private static PsyMixerRitualQuality decode(ByteBuf buf) {
        return byId(ByteBufCodecs.VAR_INT.decode(buf));
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
