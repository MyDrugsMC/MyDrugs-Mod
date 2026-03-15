package org.mydrugs.mydrugs.forge;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.DrugEffect;
import org.mydrugs.mydrugs.core.EffectType;

public record EffectPayload(DrugEffect effect) implements CustomPacketPayload {
    public static final Type<EffectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "effect"));

    private static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_CODEC =
            ByteBufCodecs.VAR_INT.map(
                    id -> EffectType.values()[id],
                    EffectType::ordinal
            );

    public static final StreamCodec<ByteBuf, DrugEffect> DRUG_EFFECT_CODEC =
            StreamCodec.composite(
                    EFFECT_TYPE_CODEC,
                    DrugEffect::getEffectType,
                    DrugEffect::new
            );

    public static final StreamCodec<ByteBuf, EffectPayload> STREAM_CODEC =
            StreamCodec.composite(
                    DRUG_EFFECT_CODEC,
                    EffectPayload::effect,
                    EffectPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}