package org.mydrugs.mydrugs.effects.payloads;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public record IngameEffectPayload(DrugEffect effect) implements CustomPacketPayload {
    public static final Type<IngameEffectPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "ingame_effect"));

    public static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_CODEC =
            ByteBufCodecs.VAR_INT.map(
                    id -> EffectType.values()[id],
                    EffectType::ordinal
            );

    public static final StreamCodec<ByteBuf, DrugEffect> DRUG_EFFECT_CODEC =
            StreamCodec.composite(
                    EFFECT_TYPE_CODEC,
                    DrugEffect::getType,
                    ByteBufCodecs.VAR_INT,
                    DrugEffect::getBaseDuration,
                    ByteBufCodecs.VAR_INT,
                    DrugEffect::getBasePotency,
                    DrugEffect::new
            );

    public static final StreamCodec<ByteBuf, IngameEffectPayload> STREAM_CODEC =
            StreamCodec.composite(
                    DRUG_EFFECT_CODEC,
                    IngameEffectPayload::effect,
                    IngameEffectPayload::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}