package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;

public record MutationPayloadData(
        List<String> sourceUuids,
        List<String> sourceEntityTypes,
        List<String> sourceNames,
        String geneticSignature,
        List<MutationStatValue> stats,
        float assimilationDifficulty,
        float rejectionRisk
) {
    public static final Codec<MutationPayloadData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.listOf().fieldOf("source_uuids").forGetter(MutationPayloadData::sourceUuids),
            Codec.STRING.listOf().fieldOf("source_entity_types").forGetter(MutationPayloadData::sourceEntityTypes),
            Codec.STRING.listOf().fieldOf("source_names").forGetter(MutationPayloadData::sourceNames),
            Codec.STRING.fieldOf("genetic_signature").forGetter(MutationPayloadData::geneticSignature),
            MutationStatValue.CODEC.listOf().fieldOf("stats").forGetter(MutationPayloadData::stats),
            Codec.FLOAT.fieldOf("assimilation_difficulty").forGetter(MutationPayloadData::assimilationDifficulty),
            Codec.FLOAT.fieldOf("rejection_risk").forGetter(MutationPayloadData::rejectionRisk)
    ).apply(instance, MutationPayloadData::new));

    public static final StreamCodec<ByteBuf, MutationPayloadData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MutationPayloadData::sourceUuids,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MutationPayloadData::sourceEntityTypes,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), MutationPayloadData::sourceNames,
            ByteBufCodecs.STRING_UTF8, MutationPayloadData::geneticSignature,
            MutationStatValue.STREAM_CODEC.apply(ByteBufCodecs.list()), MutationPayloadData::stats,
            ByteBufCodecs.FLOAT, MutationPayloadData::assimilationDifficulty,
            ByteBufCodecs.FLOAT, MutationPayloadData::rejectionRisk,
            MutationPayloadData::new
    );

    public MutationPayloadData {
        sourceUuids = List.copyOf(sourceUuids);
        sourceEntityTypes = List.copyOf(sourceEntityTypes);
        sourceNames = List.copyOf(sourceNames);
        stats = List.copyOf(stats);
        assimilationDifficulty = Math.clamp(assimilationDifficulty, 0.20F, 1.50F);
        rejectionRisk = Math.clamp(rejectionRisk, 0.02F, 0.35F);
    }

    public static MutationPayloadData fromGene(AdnGeneData gene) {
        List<MutationStatValue> stats = gene.stats();
        float avg = 0.0F;
        float max = 0.0F;
        for (MutationStatValue stat : stats) {
            avg += stat.value();
            max = Math.max(max, stat.value());
        }
        if (!stats.isEmpty()) {
            avg /= stats.size();
        }

        int count = stats.size();
        float assimilationDifficulty = Math.clamp(0.20F + avg * 0.50F + count * 0.08F + max * 0.20F, 0.20F, 1.50F);
        float rejectionRisk = Math.clamp(0.02F + avg * 0.08F + max * 0.08F + Math.max(0, count - 2) * 0.04F, 0.02F, 0.35F);

        return new MutationPayloadData(
                gene.sourceUuids(),
                gene.sourceEntityTypes(),
                gene.sourceNames(),
                gene.geneticSignature(),
                stats,
                assimilationDifficulty,
                rejectionRisk
        );
    }
}
