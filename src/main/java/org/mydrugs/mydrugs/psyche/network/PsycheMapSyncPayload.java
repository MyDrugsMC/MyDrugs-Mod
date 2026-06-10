package org.mydrugs.mydrugs.psyche.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeDto;

import java.util.ArrayList;
import java.util.List;

/** Server-to-client snapshot of the active Psyche Map integration nodes. */
public record PsycheMapSyncPayload(List<PsycheMapNodeDto> nodes) implements CustomPacketPayload {
    private static final int MAX_NODES = 64;

    public static final Type<PsycheMapSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psyche_map_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PsycheMapSyncPayload> STREAM_CODEC =
            StreamCodec.of(PsycheMapSyncPayload::encode, PsycheMapSyncPayload::decode);

    public PsycheMapSyncPayload {
        nodes = nodes == null ? List.of() : List.copyOf(nodes);
    }

    private static void encode(RegistryFriendlyByteBuf buf, PsycheMapSyncPayload payload) {
        ByteBufCodecs.VAR_INT.encode(buf, payload.nodes.size());
        for (PsycheMapNodeDto node : payload.nodes) {
            ByteBufCodecs.STRING_UTF8.encode(buf, node.nodeId());
            ByteBufCodecs.VAR_LONG.encode(buf, node.unlockedAtGameTime());
            ByteBufCodecs.VAR_LONG.encode(buf, node.unlockedDay());
            ByteBufCodecs.STRING_UTF8.encode(buf, node.trigger());
            ByteBufCodecs.STRING_UTF8.encode(buf, node.dominantDrugId());
        }
    }

    private static PsycheMapSyncPayload decode(RegistryFriendlyByteBuf buf) {
        int count = ByteBufCodecs.VAR_INT.decode(buf);
        if (count < 0 || count > MAX_NODES) {
            throw new IllegalArgumentException("Invalid Psyche Map node count: " + count);
        }
        List<PsycheMapNodeDto> nodes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            nodes.add(new PsycheMapNodeDto(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            ));
        }
        return new PsycheMapSyncPayload(nodes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
