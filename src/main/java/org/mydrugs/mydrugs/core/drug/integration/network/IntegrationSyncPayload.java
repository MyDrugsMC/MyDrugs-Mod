package org.mydrugs.mydrugs.core.drug.integration.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.List;

/** Server -> client: the player's full set of unlocked integrated trait ids. */
public record IntegrationSyncPayload(List<String> traitIds) implements CustomPacketPayload {
    public static final Type<IntegrationSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "integration_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, IntegrationSyncPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), IntegrationSyncPayload::traitIds,
                    IntegrationSyncPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
