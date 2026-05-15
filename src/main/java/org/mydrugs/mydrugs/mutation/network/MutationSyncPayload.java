package org.mydrugs.mydrugs.mutation.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.List;

public record MutationSyncPayload(List<Entry> entries) implements CustomPacketPayload {
    public static final Type<MutationSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "mutation_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, MutationSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), MutationSyncPayload::entries,
                    MutationSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Entry(String statId, float value) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.STRING_UTF8, Entry::statId,
                        ByteBufCodecs.FLOAT, Entry::value,
                        Entry::new
                );
    }
}
