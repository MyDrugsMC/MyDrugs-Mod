package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record AdnGeneData(
        List<String> sourceUuids,
        List<String> sourceEntityTypes,
        List<String> sourceNames,
        String geneticSignature,
        boolean broken,
        List<MutationStatValue> stats
) {
    public static final Codec<AdnGeneData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("source_uuids").forGetter(AdnGeneData::sourceUuids),
            Codec.STRING.listOf().fieldOf("source_entity_types").forGetter(AdnGeneData::sourceEntityTypes),
            Codec.STRING.listOf().fieldOf("source_names").forGetter(AdnGeneData::sourceNames),
            Codec.STRING.fieldOf("genetic_signature").forGetter(AdnGeneData::geneticSignature),
            Codec.BOOL.fieldOf("broken").forGetter(AdnGeneData::broken),
            MutationStatValue.CODEC.listOf().fieldOf("stats").forGetter(AdnGeneData::stats)
    ).apply(instance, AdnGeneData::new));

    public static final StreamCodec<ByteBuf, AdnGeneData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AdnGeneData::sourceUuids,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AdnGeneData::sourceEntityTypes,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), AdnGeneData::sourceNames,
            ByteBufCodecs.STRING_UTF8, AdnGeneData::geneticSignature,
            ByteBufCodecs.BOOL, AdnGeneData::broken,
            MutationStatValue.STREAM_CODEC.apply(ByteBufCodecs.list()), AdnGeneData::stats,
            AdnGeneData::new
    );

    public AdnGeneData {
        sourceUuids = List.copyOf(sourceUuids);
        sourceEntityTypes = List.copyOf(sourceEntityTypes);
        sourceNames = List.copyOf(sourceNames);
        stats = List.copyOf(stats);
    }

    public static AdnGeneData singleStatFromScrap(AdnScrapData scrap, MutationStatValue stat) {
        return new AdnGeneData(
                List.of(scrap.sourceUuid()),
                List.of(scrap.sourceEntityType()),
                List.of(scrap.sourceName()),
                scrap.geneticSignature(),
                false,
                List.of(stat)
        );
    }
}
