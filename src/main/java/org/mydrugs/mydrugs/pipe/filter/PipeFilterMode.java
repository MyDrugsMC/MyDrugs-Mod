package org.mydrugs.mydrugs.pipe.filter;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public enum PipeFilterMode {
    ALLOW_LIST,
    DENY_LIST;

    public static final Codec<PipeFilterMode> CODEC = Codec.STRING.xmap(PipeFilterMode::bySerializedName, PipeFilterMode::serializedName);
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeFilterMode> STREAM_CODEC = StreamCodec.of(
            (buf, mode) -> ByteBufCodecs.STRING_UTF8.encode(buf, mode.serializedName()),
            buf -> bySerializedName(ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    public String serializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public PipeFilterMode toggled() {
        return this == ALLOW_LIST ? DENY_LIST : ALLOW_LIST;
    }

    public int networkId() {
        return this == ALLOW_LIST ? 0 : 1;
    }

    public static PipeFilterMode byNetworkId(int id) {
        return id == 0 ? ALLOW_LIST : DENY_LIST;
    }

    public static PipeFilterMode bySerializedName(String raw) {
        if (raw == null) {
            return DENY_LIST;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (PipeFilterMode mode : values()) {
            if (mode.serializedName().equals(normalized) || mode.name().equalsIgnoreCase(raw)) {
                return mode;
            }
        }
        return DENY_LIST;
    }
}
