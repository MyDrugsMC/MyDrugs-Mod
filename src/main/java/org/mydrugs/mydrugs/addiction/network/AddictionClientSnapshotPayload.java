package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record AddictionClientSnapshotPayload(
        float globalSeverity,
        float stressLevel,
        String dominantDrugId,
        String dominantCategory,
        int symptomFlags,
        int insomniaTicksRemaining,
        int recoveryFlags,
        int overdoseTicksRemaining
) implements CustomPacketPayload {
    public static final int RECOVERY_SAFE_ZONE = 1 << 0;
    public static final int RECOVERY_DIARY = 1 << 1;
    public static final int RECOVERY_HEADPHONES = 1 << 2;
    public static final int RECOVERY_SLEEP_BONUS = 1 << 3;
    public static final int RECOVERY_CALMING_MIXTURE = 1 << 4;

    public static final Type<AddictionClientSnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "addiction_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddictionClientSnapshotPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, AddictionClientSnapshotPayload::globalSeverity,
                    ByteBufCodecs.FLOAT, AddictionClientSnapshotPayload::stressLevel,
                    ByteBufCodecs.STRING_UTF8, AddictionClientSnapshotPayload::dominantDrugId,
                    ByteBufCodecs.STRING_UTF8, AddictionClientSnapshotPayload::dominantCategory,
                    ByteBufCodecs.VAR_INT, AddictionClientSnapshotPayload::symptomFlags,
                    ByteBufCodecs.VAR_INT, AddictionClientSnapshotPayload::insomniaTicksRemaining,
                    ByteBufCodecs.VAR_INT, AddictionClientSnapshotPayload::recoveryFlags,
                    ByteBufCodecs.VAR_INT, AddictionClientSnapshotPayload::overdoseTicksRemaining,
                    AddictionClientSnapshotPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}