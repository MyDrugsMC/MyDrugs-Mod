package org.mydrugs.mydrugs.effects.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public record DrugVisualPayload(@Nullable EffectType effectType, int duration, float intensity)
        implements CustomPacketPayload {

    public static final Type<DrugVisualPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drug_visual"));

    public static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    EffectType::bySerializedNameOrNull,
                    EffectType::serializedName
            );

    public static final StreamCodec<ByteBuf, DrugVisualPayload> STREAM_CODEC =
            StreamCodec.composite(
                    EFFECT_TYPE_CODEC,
                    DrugVisualPayload::effectType,
                    ByteBufCodecs.VAR_INT,
                    DrugVisualPayload::duration,
                    ByteBufCodecs.FLOAT,
                    DrugVisualPayload::intensity,
                    DrugVisualPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
