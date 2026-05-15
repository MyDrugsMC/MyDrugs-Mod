package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record AdnScrapData(
        String sourceUuid,
        String sourceEntityType,
        String sourceName,
        String sourceKind,
        String geneticSignature,
        String rarityTier,
        List<MutationStatValue> stats
) {
    public static final Codec<AdnScrapData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("source_uuid").forGetter(AdnScrapData::sourceUuid),
            Codec.STRING.fieldOf("source_entity_type").forGetter(AdnScrapData::sourceEntityType),
            Codec.STRING.fieldOf("source_name").forGetter(AdnScrapData::sourceName),
            Codec.STRING.fieldOf("source_kind").forGetter(AdnScrapData::sourceKind),
            Codec.STRING.fieldOf("genetic_signature").forGetter(AdnScrapData::geneticSignature),
            Codec.STRING.fieldOf("rarity_tier").forGetter(AdnScrapData::rarityTier),
            MutationStatValue.CODEC.listOf().fieldOf("stats").forGetter(AdnScrapData::stats)
    ).apply(instance, AdnScrapData::new));

    public static final StreamCodec<ByteBuf, AdnScrapData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, AdnScrapData::sourceUuid,
            ByteBufCodecs.STRING_UTF8, AdnScrapData::sourceEntityType,
            ByteBufCodecs.STRING_UTF8, AdnScrapData::sourceName,
            ByteBufCodecs.STRING_UTF8, AdnScrapData::sourceKind,
            ByteBufCodecs.STRING_UTF8, AdnScrapData::geneticSignature,
            ByteBufCodecs.STRING_UTF8, AdnScrapData::rarityTier,
            MutationStatValue.STREAM_CODEC.apply(ByteBufCodecs.list()), AdnScrapData::stats,
            AdnScrapData::new
    );

    public AdnScrapData {
        stats = List.copyOf(stats);
    }

    public boolean isPlayerSource() {
        return "player".equals(this.sourceKind);
    }
}
