package org.mydrugs.mydrugs.pipe;

import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Locale;

public enum PipeResourceKind {
    ITEM,
    FLUID,
    GAS;

    public static final Codec<PipeResourceKind> CODEC = Codec.STRING.xmap(PipeResourceKind::bySerializedName, PipeResourceKind::serializedName);
    public static final StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, PipeResourceKind> STREAM_CODEC = StreamCodec.of(
            (buf, kind) -> ByteBufCodecs.STRING_UTF8.encode(buf, kind.serializedName()),
            buf -> bySerializedName(ByteBufCodecs.STRING_UTF8.decode(buf))
    );

    public String serializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public int networkId() {
        return switch (this) {
            case ITEM -> 0;
            case FLUID -> 1;
            case GAS -> 2;
        };
    }

    public static PipeResourceKind byNetworkId(int id) {
        return switch (id) {
            case 1 -> FLUID;
            case 2 -> GAS;
            default -> ITEM;
        };
    }

    public static PipeResourceKind bySerializedName(String raw) {
        if (raw == null) {
            return ITEM;
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        for (PipeResourceKind kind : values()) {
            if (kind.serializedName().equals(normalized) || kind.name().equalsIgnoreCase(raw)) {
                return kind;
            }
        }
        return ITEM;
    }
}
