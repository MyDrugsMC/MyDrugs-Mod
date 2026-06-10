package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

public record PersonalDiarySubmitResultPayload(
        Result result,
        boolean success,
        String messageKey,
        int cooldownTicksRemaining
) implements CustomPacketPayload {
    public enum Result {
        SAVED,
        NO_DIARY,
        COOLDOWN,
        EMPTY_OR_INVALID,
        TOO_LONG_OR_INVALID,
        UNKNOWN_FAILURE;

        private static Result byId(int id) {
            Result[] values = values();
            return id >= 0 && id < values.length ? values[id] : UNKNOWN_FAILURE;
        }
    }

    public static final Type<PersonalDiarySubmitResultPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "personal_diary_submit_result"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PersonalDiarySubmitResultPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, payload.result().ordinal());
                        ByteBufCodecs.BOOL.encode(buf, payload.success());
                        ByteBufCodecs.STRING_UTF8.encode(buf, payload.messageKey());
                        ByteBufCodecs.VAR_INT.encode(buf, Math.max(0, payload.cooldownTicksRemaining()));
                    },
                    buf -> new PersonalDiarySubmitResultPayload(
                            Result.byId(ByteBufCodecs.VAR_INT.decode(buf)),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.STRING_UTF8.decode(buf),
                            Math.max(0, ByteBufCodecs.VAR_INT.decode(buf))
                    )
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
