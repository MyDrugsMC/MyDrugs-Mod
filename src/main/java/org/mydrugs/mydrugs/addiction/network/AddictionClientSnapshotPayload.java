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
        int overdoseTicksRemaining,
        int primaryDangerReason,
        int suggestedAction,
        int withdrawalPhase,
        float dominantTolerance,
        float dominantDose
) implements CustomPacketPayload {
    public static final int RECOVERY_SAFE_ZONE = 1 << 0;
    public static final int RECOVERY_DIARY = 1 << 1;
    public static final int RECOVERY_HEADPHONES = 1 << 2;
    public static final int RECOVERY_SLEEP_BONUS = 1 << 3;
    public static final int RECOVERY_CALMING_MIXTURE = 1 << 4;
    public static final int RECOVERY_PREPARED_TEA = 1 << 5;

    public static final Type<AddictionClientSnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "addiction_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddictionClientSnapshotPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.FLOAT.encode(buf, payload.globalSeverity());
                        ByteBufCodecs.FLOAT.encode(buf, payload.stressLevel());
                        ByteBufCodecs.STRING_UTF8.encode(buf, payload.dominantDrugId());
                        ByteBufCodecs.STRING_UTF8.encode(buf, payload.dominantCategory());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.symptomFlags());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.insomniaTicksRemaining());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.recoveryFlags());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.overdoseTicksRemaining());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.primaryDangerReason());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.suggestedAction());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.withdrawalPhase());
                        ByteBufCodecs.FLOAT.encode(buf, payload.dominantTolerance());
                        ByteBufCodecs.FLOAT.encode(buf, payload.dominantDose());
                    },
                    buf -> new AddictionClientSnapshotPayload(
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf),
                            ByteBufCodecs.FLOAT.decode(buf)
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
