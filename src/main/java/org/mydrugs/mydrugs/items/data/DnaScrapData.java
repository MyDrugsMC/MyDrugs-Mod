package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record DnaScrapData(
        String sourceUuid,
        String sourceEntityType,
        String sourceName,
        String sourceKind,
        String geneticSignature,
        String rarityTier,
        List<MutationStatValue> stats
) {
    public static final int MAX_STATS = 64;
    public static final int MAX_STRING_LENGTH = 256;

    public static final Codec<DnaScrapData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("source_uuid").forGetter(DnaScrapData::sourceUuid),
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("source_entity_type").forGetter(DnaScrapData::sourceEntityType),
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("source_name").forGetter(DnaScrapData::sourceName),
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("source_kind").forGetter(DnaScrapData::sourceKind),
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("genetic_signature").forGetter(DnaScrapData::geneticSignature),
            ComponentCodecs.boundedString(MAX_STRING_LENGTH).fieldOf("rarity_tier").forGetter(DnaScrapData::rarityTier),
            ComponentCodecs.boundedList(MutationStatValue.CODEC, MAX_STATS).fieldOf("stats").forGetter(DnaScrapData::stats)
    ).apply(instance, DnaScrapData::new));

    public static final StreamCodec<ByteBuf, DnaScrapData> STREAM_CODEC = StreamCodec.composite(
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaScrapData::sourceUuid,
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaScrapData::sourceEntityType,
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaScrapData::sourceName,
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaScrapData::sourceKind,
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaScrapData::geneticSignature,
            ComponentCodecs.boundedStringStream(MAX_STRING_LENGTH), DnaScrapData::rarityTier,
            ComponentCodecs.boundedListStream(MutationStatValue.STREAM_CODEC, MAX_STATS), DnaScrapData::stats,
            DnaScrapData::new
    );

    public DnaScrapData {
        stats = List.copyOf(stats);
    }

    public boolean isPlayerSource() {
        return "player".equals(this.sourceKind);
    }
}
