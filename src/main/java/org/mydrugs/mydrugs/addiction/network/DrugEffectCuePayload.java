package org.mydrugs.mydrugs.addiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public record DrugEffectCuePayload(@Nullable EffectType effectType, DrugEffectCueKind kind, float intensity)
        implements CustomPacketPayload {
    public static final Type<DrugEffectCuePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drug_effect_cue"));

    private static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_CODEC =
            ByteBufCodecs.STRING_UTF8.map(EffectType::bySerializedNameOrNull, type -> type == null ? "" : type.serializedName());

    private static final StreamCodec<ByteBuf, DrugEffectCueKind> KIND_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    name -> {
                        DrugEffectCueKind kind = DrugEffectCueKind.bySerializedNameOrNull(name);
                        return kind == null ? DrugEffectCueKind.EFFECT_STARTED : kind;
                    },
                    DrugEffectCueKind::serializedName
            );

    public static final StreamCodec<ByteBuf, DrugEffectCuePayload> STREAM_CODEC = StreamCodec.composite(
            EFFECT_TYPE_CODEC, DrugEffectCuePayload::effectType,
            KIND_CODEC, DrugEffectCuePayload::kind,
            ByteBufCodecs.FLOAT, DrugEffectCuePayload::intensity,
            DrugEffectCuePayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
