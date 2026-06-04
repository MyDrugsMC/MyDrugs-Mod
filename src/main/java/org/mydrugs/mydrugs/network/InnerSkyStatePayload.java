package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -&gt; client: the player's Inner Dimension sky state. Drives the constellation map
 * (one fixed pattern per integrated {@link org.mydrugs.mydrugs.core.drug.DrugId}) and the
 * core beacon brightness (scales with {@code integratedCount}). Sent on dimension entry and
 * whenever a new drug is integrated, so the sky reflects healing progress immediately.
 *
 * <p>Drug ids are transmitted by {@link org.mydrugs.mydrugs.core.drug.DrugId#networkId()} so the
 * payload stays compact and version-stable.
 */
public record InnerSkyStatePayload(List<Integer> integratedDrugNetworkIds) implements CustomPacketPayload {
    public static final Type<InnerSkyStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "inner_sky_state"));

    public static final StreamCodec<ByteBuf, InnerSkyStatePayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                int count = Math.min(64, payload.integratedDrugNetworkIds().size());
                ByteBufCodecs.VAR_INT.encode(buf, count);
                for (int i = 0; i < count; i++) {
                    ByteBufCodecs.VAR_INT.encode(buf, payload.integratedDrugNetworkIds().get(i));
                }
            },
            buf -> {
                int count = Math.min(64, ByteBufCodecs.VAR_INT.decode(buf));
                List<Integer> ids = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    ids.add(ByteBufCodecs.VAR_INT.decode(buf));
                }
                return new InnerSkyStatePayload(ids);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
