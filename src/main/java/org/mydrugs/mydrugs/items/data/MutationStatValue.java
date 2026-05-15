package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.mutation.GeneticProfileGenerator;

public record MutationStatValue(String statId, float value, float improbabilityScore) {
    public static final Codec<MutationStatValue> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("stat_id").forGetter(MutationStatValue::statId),
            Codec.FLOAT.fieldOf("value").forGetter(MutationStatValue::value),
            Codec.FLOAT.fieldOf("improbability_score").forGetter(MutationStatValue::improbabilityScore)
    ).apply(instance, MutationStatValue::new));

    public static final StreamCodec<ByteBuf, MutationStatValue> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MutationStatValue::statId,
            ByteBufCodecs.FLOAT, MutationStatValue::value,
            ByteBufCodecs.FLOAT, MutationStatValue::improbabilityScore,
            MutationStatValue::new
    );

    public MutationStatValue {
        value = GeneticProfileGenerator.clampValue(value);
        improbabilityScore = Math.clamp(improbabilityScore, 0.0F, 1.0F);
    }
}
