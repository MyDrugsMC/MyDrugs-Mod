package org.mydrugs.mydrugs.core.drug.ritual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.items.data.ComponentCodecs;

import java.util.Comparator;

public record RitualDrugEffectData(EffectType type, int duration, float intensity) {
    public static final int MAX_DURATION = 1_000_000;
    public static final float MAX_INTENSITY = 1024.0F;
    public static final Comparator<RitualDrugEffectData> CANONICAL_ORDER =
            Comparator.comparing((RitualDrugEffectData effect) -> effect.type().serializedName())
                    .thenComparingInt(RitualDrugEffectData::duration)
                    .thenComparingDouble(effect -> effect.intensity());

    public static final Codec<EffectType> EFFECT_TYPE_CODEC = Codec.STRING.comapFlatMap(
            name -> EffectType.bySerializedName(name)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown effect type: " + name)),
            EffectType::serializedName
    );

    public static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    name -> EffectType.bySerializedName(name).orElse(EffectType.CONFUSION),
                    EffectType::serializedName
            );

    public static final Codec<RitualDrugEffectData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            EFFECT_TYPE_CODEC.fieldOf("type").forGetter(RitualDrugEffectData::type),
            ComponentCodecs.clampedInt(0, MAX_DURATION).fieldOf("duration").forGetter(RitualDrugEffectData::duration),
            ComponentCodecs.clampedFloat(0.0F, MAX_INTENSITY).fieldOf("intensity").forGetter(RitualDrugEffectData::intensity)
    ).apply(instance, RitualDrugEffectData::new));

    public static final StreamCodec<ByteBuf, RitualDrugEffectData> STREAM_CODEC = StreamCodec.composite(
            EFFECT_TYPE_STREAM_CODEC, RitualDrugEffectData::type,
            ComponentCodecs.clampedVarInt(0, MAX_DURATION), RitualDrugEffectData::duration,
            ComponentCodecs.clampedFloatStream(0.0F, MAX_INTENSITY), RitualDrugEffectData::intensity,
            RitualDrugEffectData::new
    );

    public static RitualDrugEffectData from(DrugEffect effect) {
        return new RitualDrugEffectData(effect.getEffectType(), effect.getBaseDuration(), effect.getBaseIntensity());
    }

    public DrugEffect toDrugEffect() {
        return new DrugEffect(this.type, this.duration, this.intensity);
    }

    public String canonicalPart() {
        return this.type.serializedName() + ":" + this.duration + ":" + String.format(java.util.Locale.ROOT, "%.4f", this.intensity);
    }
}
