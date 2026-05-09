package org.mydrugs.mydrugs.core.drug.ritual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.List;
import java.util.UUID;

public record MixedDrugData(
        String formulaId,
        String displayName,
        UUID authorUuid,
        String authorName,
        DrugId baseDrug,
        List<RitualDrugEffectData> baseEffectsSnapshot,
        List<RitualDrugEffectData> addedEffects,
        String canonicalSignature
) {
    public static final Codec<DrugId> DRUG_ID_CODEC = Codec.STRING.comapFlatMap(
            name -> DrugId.bySerializedName(name)
                    .map(DataResult::success)
                    .orElseGet(() -> DataResult.error(() -> "Unknown drug id: " + name)),
            DrugId::serializedName
    );

    public static final StreamCodec<ByteBuf, DrugId> DRUG_ID_STREAM_CODEC =
            ByteBufCodecs.STRING_UTF8.map(
                    name -> DrugId.bySerializedName(name).orElse(DrugId.WEED),
                    DrugId::serializedName
            );

    public static final Codec<MixedDrugData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("formula_id").forGetter(MixedDrugData::formulaId),
            Codec.STRING.fieldOf("display_name").forGetter(MixedDrugData::displayName),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("author_uuid").forGetter(MixedDrugData::authorUuid),
            Codec.STRING.fieldOf("author_name").forGetter(MixedDrugData::authorName),
            DRUG_ID_CODEC.fieldOf("base_drug").forGetter(MixedDrugData::baseDrug),
            RitualDrugEffectData.CODEC.listOf().fieldOf("base_effects_snapshot").forGetter(MixedDrugData::baseEffectsSnapshot),
            RitualDrugEffectData.CODEC.listOf().fieldOf("added_effects").forGetter(MixedDrugData::addedEffects),
            Codec.STRING.fieldOf("canonical_signature").forGetter(MixedDrugData::canonicalSignature)
    ).apply(instance, MixedDrugData::new));

    public static final StreamCodec<ByteBuf, MixedDrugData> STREAM_CODEC = StreamCodec.of(
            MixedDrugData::encode,
            MixedDrugData::decode
    );

    public MixedDrugData {
        baseEffectsSnapshot = List.copyOf(baseEffectsSnapshot);
        addedEffects = List.copyOf(addedEffects);
    }

    public static MixedDrugData pending(RitualDrugFormula formula) {
        return new MixedDrugData(
                formula.formulaId(),
                "",
                new UUID(0L, 0L),
                "",
                formula.baseDrug(),
                formula.baseEffectsSnapshot(),
                formula.addedEffects(),
                formula.canonicalSignature()
        );
    }

    private static void encode(ByteBuf buf, MixedDrugData data) {
        ByteBufCodecs.STRING_UTF8.encode(buf, data.formulaId);
        ByteBufCodecs.STRING_UTF8.encode(buf, data.displayName);
        ByteBufCodecs.STRING_UTF8.encode(buf, data.authorUuid.toString());
        ByteBufCodecs.STRING_UTF8.encode(buf, data.authorName);
        DRUG_ID_STREAM_CODEC.encode(buf, data.baseDrug);
        RitualDrugEffectData.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.baseEffectsSnapshot);
        RitualDrugEffectData.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, data.addedEffects);
        ByteBufCodecs.STRING_UTF8.encode(buf, data.canonicalSignature);
    }

    private static MixedDrugData decode(ByteBuf buf) {
        return new MixedDrugData(
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buf)),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                DRUG_ID_STREAM_CODEC.decode(buf),
                RitualDrugEffectData.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                RitualDrugEffectData.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf)
        );
    }
}
