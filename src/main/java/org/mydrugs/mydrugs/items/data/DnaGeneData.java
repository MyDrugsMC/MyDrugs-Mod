package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record DnaGeneData(
        List<String> sourceUuids,
        List<String> sourceEntityTypes,
        List<String> sourceNames,
        String geneticSignature,
        boolean broken,
        List<MutationStatValue> stats
) {
    public static final int MAX_SOURCES = 64;
    public static final int MAX_STATS = 64;
    public static final int MAX_STRING_LENGTH = 256;

    public static final Codec<DnaGeneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentCodecs.boundedList(ComponentCodecs.boundedString(MAX_STRING_LENGTH), MAX_SOURCES).fieldOf("source_uuids").forGetter(DnaGeneData::sourceUuids),
            ComponentCodecs.boundedList(ComponentCodecs.boundedString(MAX_STRING_LENGTH), MAX_SOURCES).fieldOf("source_entity_types").forGetter(DnaGeneData::sourceEntityTypes),
            ComponentCodecs.boundedList(ComponentCodecs.boundedString(MAX_STRING_LENGTH), MAX_SOURCES).fieldOf("source_names").forGetter(DnaGeneData::sourceNames),
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("genetic_signature").forGetter(DnaGeneData::geneticSignature),
            Codec.BOOL.fieldOf("broken").forGetter(DnaGeneData::broken),
            ComponentCodecs.boundedList(MutationStatValue.CODEC, MAX_STATS).fieldOf("stats").forGetter(DnaGeneData::stats)
    ).apply(instance, DnaGeneData::new));

    public static final StreamCodec<ByteBuf, DnaGeneData> STREAM_CODEC = StreamCodec.composite(
            ComponentCodecs.boundedListStream(ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), MAX_SOURCES), DnaGeneData::sourceUuids,
            ComponentCodecs.boundedListStream(ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), MAX_SOURCES), DnaGeneData::sourceEntityTypes,
            ComponentCodecs.boundedListStream(ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), MAX_SOURCES), DnaGeneData::sourceNames,
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaGeneData::geneticSignature,
            ByteBufCodecs.BOOL, DnaGeneData::broken,
            ComponentCodecs.boundedListStream(MutationStatValue.STREAM_CODEC, MAX_STATS), DnaGeneData::stats,
            DnaGeneData::new
    );

    public DnaGeneData {
        sourceUuids = List.copyOf(sourceUuids);
        sourceEntityTypes = List.copyOf(sourceEntityTypes);
        sourceNames = List.copyOf(sourceNames);
        stats = List.copyOf(stats);
    }

    public static DnaGeneData singleStatFromScrap(DnaScrapData scrap, MutationStatValue stat) {
        return new DnaGeneData(
                List.of(scrap.sourceUuid()),
                List.of(scrap.sourceEntityType()),
                List.of(scrap.sourceName()),
                scrap.geneticSignature(),
                false,
                List.of(stat)
        );
    }
}
