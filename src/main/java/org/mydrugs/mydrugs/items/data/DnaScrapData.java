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
    public static final Codec<DnaScrapData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("source_uuid").forGetter(DnaScrapData::sourceUuid),
            Codec.STRING.fieldOf("source_entity_type").forGetter(DnaScrapData::sourceEntityType),
            Codec.STRING.fieldOf("source_name").forGetter(DnaScrapData::sourceName),
            Codec.STRING.fieldOf("source_kind").forGetter(DnaScrapData::sourceKind),
            Codec.STRING.fieldOf("genetic_signature").forGetter(DnaScrapData::geneticSignature),
            Codec.STRING.fieldOf("rarity_tier").forGetter(DnaScrapData::rarityTier),
            MutationStatValue.CODEC.listOf().fieldOf("stats").forGetter(DnaScrapData::stats)
    ).apply(instance, DnaScrapData::new));

    public static final StreamCodec<ByteBuf, DnaScrapData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DnaScrapData::sourceUuid,
            ByteBufCodecs.STRING_UTF8, DnaScrapData::sourceEntityType,
            ByteBufCodecs.STRING_UTF8, DnaScrapData::sourceName,
            ByteBufCodecs.STRING_UTF8, DnaScrapData::sourceKind,
            ByteBufCodecs.STRING_UTF8, DnaScrapData::geneticSignature,
            ByteBufCodecs.STRING_UTF8, DnaScrapData::rarityTier,
            MutationStatValue.STREAM_CODEC.apply(ByteBufCodecs.list()), DnaScrapData::stats,
            DnaScrapData::new
    );

    public DnaScrapData {
        stats = List.copyOf(stats);
    }

    public boolean isPlayerSource() {
        return "player".equals(this.sourceKind);
    }
}
