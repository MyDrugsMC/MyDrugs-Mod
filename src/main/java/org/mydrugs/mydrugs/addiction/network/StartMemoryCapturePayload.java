package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record StartMemoryCapturePayload(
        String nodeId,
        String titleKey,
        String mood,
        long gameTime,
        String dominantDrugId
) implements CustomPacketPayload {

    public static final Type<StartMemoryCapturePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "start_memory_capture"));

    public static final StreamCodec<RegistryFriendlyByteBuf, StartMemoryCapturePayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        ByteBufCodecs.STRING_UTF8.encode(buf, p.nodeId());
                        ByteBufCodecs.STRING_UTF8.encode(buf, p.titleKey());
                        ByteBufCodecs.STRING_UTF8.encode(buf, p.mood());
                        ByteBufCodecs.VAR_LONG.encode(buf, p.gameTime());
                        ByteBufCodecs.STRING_UTF8.encode(buf, p.dominantDrugId());
                    },
                    buf -> new StartMemoryCapturePayload(
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_LONG.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
