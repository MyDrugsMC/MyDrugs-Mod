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
    public static final Codec<DnaGeneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("source_uuids").forGetter(DnaGeneData::sourceUuids),
            Codec.STRING.listOf().fieldOf("source_entity_types").forGetter(DnaGeneData::sourceEntityTypes),
            Codec.STRING.listOf().fieldOf("source_names").forGetter(DnaGeneData::sourceNames),
            Codec.STRING.fieldOf("genetic_signature").forGetter(DnaGeneData::geneticSignature),
            Codec.BOOL.fieldOf("broken").forGetter(DnaGeneData::broken),
            MutationStatValue.CODEC.listOf().fieldOf("stats").forGetter(DnaGeneData::stats)
    ).apply(instance, DnaGeneData::new));

    public static final StreamCodec<ByteBuf, DnaGeneData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DnaGeneData::sourceUuids,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DnaGeneData::sourceEntityTypes,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), DnaGeneData::sourceNames,
            ByteBufCodecs.STRING_UTF8, DnaGeneData::geneticSignature,
            ByteBufCodecs.BOOL, DnaGeneData::broken,
            MutationStatValue.STREAM_CODEC.apply(ByteBufCodecs.list()), DnaGeneData::stats,
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
