package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public enum PsyMixerTier {
    DORMANT("dormant", 0),
    AWAKENED("awakened", 1);

    public static final Codec<PsyMixerTier> CODEC = Codec.STRING.xmap(
            PsyMixerTier::bySerializedName,
            PsyMixerTier::serializedName
    );
    public static final StreamCodec<io.netty.buffer.ByteBuf, PsyMixerTier> STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(PsyMixerTier::bySerializedName, PsyMixerTier::serializedName);

    private final String serializedName;
    private final int id;

    PsyMixerTier(String serializedName, int id) {
        this.serializedName = serializedName;
        this.id = id;
    }

    public String serializedName() {
        return serializedName;
    }

    public int id() {
        return id;
    }

    public boolean canRun(PsyMixerTier required) {
        return this.id >= (required == null ? AWAKENED : required).id;
    }

    public String translationKey() {
        return "screen.mydrugs.psy_mixer.tier." + serializedName;
    }

    public static PsyMixerTier byId(int id) {
        for (PsyMixerTier tier : values()) {
            if (tier.id == id) {
                return tier;
            }
        }
        return AWAKENED;
    }

    public static PsyMixerTier bySerializedName(String name) {
        if (name == null) {
            return AWAKENED;
        }
        String normalized = name.trim().toLowerCase(Locale.ROOT);
        for (PsyMixerTier tier : values()) {
            if (tier.serializedName.equals(normalized)) {
                return tier;
            }
        }
        return AWAKENED;
    }
}
