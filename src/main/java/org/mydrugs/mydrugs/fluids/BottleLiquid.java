package org.mydrugs.mydrugs.fluids;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record BottleLiquid(String id) {
    public static final Codec<BottleLiquid> CODEC =
            Codec.STRING.xmap(BottleLiquid::new, BottleLiquid::id);

    public static final StreamCodec<ByteBuf, BottleLiquid> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, BottleLiquid::id,
                    BottleLiquid::new
            );

    public boolean is(String other) {
        return this.id.equals(other);
    }
}