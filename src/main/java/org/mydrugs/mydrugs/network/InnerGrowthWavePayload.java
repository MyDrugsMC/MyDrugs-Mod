package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

/**
 * Server -&gt; client: the owning player is present in the Inner Dimension while a drug integrates,
 * so the client should play the Phase 8 growth-wave flourish — a one-time post bloom (Phase 2) and a
 * resonant sound swell (Phase 5) — paired with the outward chunk reveal the server is pacing.
 *
 * <p>Carries the integrated drug's network id purely so the flourish can be flavoured; the reveal
 * itself is driven server-side.
 */
public record InnerGrowthWavePayload(int drugNetworkId) implements CustomPacketPayload {
    public static final Type<InnerGrowthWavePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "inner_growth_wave"));

    public static final StreamCodec<ByteBuf, InnerGrowthWavePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> ByteBufCodecs.VAR_INT.encode(buf, payload.drugNetworkId()),
            buf -> new InnerGrowthWavePayload(ByteBufCodecs.VAR_INT.decode(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
