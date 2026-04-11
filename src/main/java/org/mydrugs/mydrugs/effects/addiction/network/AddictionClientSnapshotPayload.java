package org.mydrugs.mydrugs.effects.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record AddictionClientSnapshotPayload(
        float globalSeverity,
        float stressLevel,
        String dominantCategory,
        int symptomFlags,
        int insomniaTicksRemaining
) implements CustomPacketPayload {
    public static final Type<AddictionClientSnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "addiction_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddictionClientSnapshotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, AddictionClientSnapshotPayload::globalSeverity,
                    ByteBufCodecs.FLOAT, AddictionClientSnapshotPayload::stressLevel,
                    ByteBufCodecs.STRING_UTF8, AddictionClientSnapshotPayload::dominantCategory,
                    ByteBufCodecs.VAR_INT, AddictionClientSnapshotPayload::symptomFlags,
                    ByteBufCodecs.VAR_INT, AddictionClientSnapshotPayload::insomniaTicksRemaining,
                    AddictionClientSnapshotPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}