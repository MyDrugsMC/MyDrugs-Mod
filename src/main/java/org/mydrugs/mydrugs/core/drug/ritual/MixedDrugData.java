package org.mydrugs.mydrugs.core.drug.ritual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.data.ComponentCodecs;

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
        String canonicalSignature,
        PsyMixerRitualQuality quality
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

    public static final int MAX_NAME_LENGTH = 256;
    public static final int MAX_ID_LENGTH = 256;
    public static final int MAX_SIGNATURE_LENGTH = 2048;
    public static final int MAX_EFFECTS = 64;

    public static final Codec<MixedDrugData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentCodecs.boundedString(MAX_ID_LENGTH).fieldOf("formula_id").forGetter(MixedDrugData::formulaId),
            ComponentCodecs.boundedString(MAX_NAME_LENGTH).fieldOf("display_name").forGetter(MixedDrugData::displayName),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("author_uuid").forGetter(MixedDrugData::authorUuid),
            ComponentCodecs.boundedString(MAX_NAME_LENGTH).fieldOf("author_name").forGetter(MixedDrugData::authorName),
            DRUG_ID_CODEC.fieldOf("base_drug").forGetter(MixedDrugData::baseDrug),
            ComponentCodecs.boundedList(RitualDrugEffectData.CODEC, MAX_EFFECTS).fieldOf("base_effects_snapshot").forGetter(MixedDrugData::baseEffectsSnapshot),
            ComponentCodecs.boundedList(RitualDrugEffectData.CODEC, MAX_EFFECTS).fieldOf("added_effects").forGetter(MixedDrugData::addedEffects),
            ComponentCodecs.boundedString(MAX_SIGNATURE_LENGTH).fieldOf("canonical_signature").forGetter(MixedDrugData::canonicalSignature),
            PsyMixerRitualQuality.CODEC.optionalFieldOf("quality", PsyMixerRitualQuality.BASE).forGetter(MixedDrugData::quality)
    ).apply(instance, MixedDrugData::new));

    public static final StreamCodec<ByteBuf, MixedDrugData> STREAM_CODEC = StreamCodec.of(
            MixedDrugData::encode,
            MixedDrugData::decode
    );

    public MixedDrugData {
        baseEffectsSnapshot = List.copyOf(baseEffectsSnapshot);
        addedEffects = List.copyOf(addedEffects);
        if (quality == null) {
            quality = PsyMixerRitualQuality.BASE;
        }
    }

    public static MixedDrugData pending(RitualDrugFormula formula) {
        return pending(formula, PsyMixerRitualQuality.BASE);
    }

    public static MixedDrugData pending(RitualDrugFormula formula, PsyMixerRitualQuality quality) {
        return new MixedDrugData(
                formula.formulaId(),
                "",
                new UUID(0L, 0L),
                "",
                formula.baseDrug(),
                formula.baseEffectsSnapshot(),
                formula.addedEffects(),
                formula.canonicalSignature(),
                quality
        );
    }

    public MixedDrugData withQuality(PsyMixerRitualQuality quality) {
        return new MixedDrugData(
                formulaId,
                displayName,
                authorUuid,
                authorName,
                baseDrug,
                baseEffectsSnapshot,
                addedEffects,
                canonicalSignature,
                quality
        );
    }

    public MixedDrugData withDisplayName(String displayName) {
        return new MixedDrugData(
                formulaId,
                displayName,
                authorUuid,
                authorName,
                baseDrug,
                baseEffectsSnapshot,
                addedEffects,
                canonicalSignature,
                quality
        );
    }

    private static void encode(ByteBuf buf, MixedDrugData data) {
        ComponentCodecs.boundedStringStream(MAX_ID_LENGTH).encode(buf, data.formulaId);
        ComponentCodecs.boundedStringStream(MAX_NAME_LENGTH).encode(buf, data.displayName);
        ByteBufCodecs.STRING_UTF8.encode(buf, data.authorUuid.toString());
        ComponentCodecs.boundedStringStream(MAX_NAME_LENGTH).encode(buf, data.authorName);
        DRUG_ID_STREAM_CODEC.encode(buf, data.baseDrug);
        ComponentCodecs.boundedListStream(RitualDrugEffectData.STREAM_CODEC, MAX_EFFECTS).encode(buf, data.baseEffectsSnapshot);
        ComponentCodecs.boundedListStream(RitualDrugEffectData.STREAM_CODEC, MAX_EFFECTS).encode(buf, data.addedEffects);
        ComponentCodecs.boundedStringStream(MAX_SIGNATURE_LENGTH).encode(buf, data.canonicalSignature);
        PsyMixerRitualQuality.STREAM_CODEC.encode(buf, data.quality);
    }

    private static MixedDrugData decode(ByteBuf buf) {
        return new MixedDrugData(
                ComponentCodecs.boundedStringStream(MAX_ID_LENGTH).decode(buf),
                ComponentCodecs.boundedStringStream(MAX_NAME_LENGTH).decode(buf),
                UUID.fromString(ByteBufCodecs.STRING_UTF8.decode(buf)),
                ComponentCodecs.boundedStringStream(MAX_NAME_LENGTH).decode(buf),
                DRUG_ID_STREAM_CODEC.decode(buf),
                ComponentCodecs.boundedListStream(RitualDrugEffectData.STREAM_CODEC, MAX_EFFECTS).decode(buf),
                ComponentCodecs.boundedListStream(RitualDrugEffectData.STREAM_CODEC, MAX_EFFECTS).decode(buf),
                ComponentCodecs.boundedStringStream(MAX_SIGNATURE_LENGTH).decode(buf),
                PsyMixerRitualQuality.STREAM_CODEC.decode(buf)
        );
    }
}
