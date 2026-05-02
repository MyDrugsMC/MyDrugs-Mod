package org.mydrugs.mydrugs.effects.addiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugCategory;

/**
 * Sent server → client every 20 ticks.
 * Contains one float dose value per {@link DrugCategory} stable network id.
 */
public record DoseSyncPayload(float[] doses) implements CustomPacketPayload {

    public static final Type<DoseSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "dose_sync"));

    public static final StreamCodec<ByteBuf, DoseSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.doses().length);
                for (float f : payload.doses()) {
                    buf.writeFloat(f);
                }
            },
            buf -> {
                int len = ByteBufCodecs.VAR_INT.decode(buf);
                float[] doses = new float[len];
                for (int i = 0; i < len; i++) {
                    doses[i] = buf.readFloat();
                }
                return new DoseSyncPayload(doses);
            }
    );

    /** Convenience: get the dose for a specific category by stable network id. */
    public float get(DrugCategory category) {
        int id = category.networkId();
        return (id < doses.length) ? doses[id] : 0f;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
