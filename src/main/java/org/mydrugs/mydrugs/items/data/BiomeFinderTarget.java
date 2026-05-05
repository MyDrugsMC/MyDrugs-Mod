package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public record BiomeFinderTarget(
        Optional<ResourceLocation> selectedBiome,
        Optional<BlockPos> cachedPos,
        Optional<ResourceLocation> cachedDimension,
        long lastSearchTick
) {
    public static final BiomeFinderTarget EMPTY = new BiomeFinderTarget(
            Optional.empty(), Optional.empty(), Optional.empty(), 0L
    );

    public static final Codec<BiomeFinderTarget> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.optionalFieldOf("selected_biome").forGetter(BiomeFinderTarget::selectedBiome),
            BlockPos.CODEC.optionalFieldOf("cached_pos").forGetter(BiomeFinderTarget::cachedPos),
            ResourceLocation.CODEC.optionalFieldOf("cached_dimension").forGetter(BiomeFinderTarget::cachedDimension),
            Codec.LONG.optionalFieldOf("last_search_tick", 0L).forGetter(BiomeFinderTarget::lastSearchTick)
    ).apply(instance, BiomeFinderTarget::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BiomeFinderTarget> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), BiomeFinderTarget::selectedBiome,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), BiomeFinderTarget::cachedPos,
            ByteBufCodecs.optional(ResourceLocation.STREAM_CODEC), BiomeFinderTarget::cachedDimension,
            ByteBufCodecs.VAR_LONG, BiomeFinderTarget::lastSearchTick,
            BiomeFinderTarget::new
    );

    public BiomeFinderTarget withSelected(ResourceLocation biome) {
        return new BiomeFinderTarget(Optional.of(biome), Optional.empty(), Optional.empty(), 0L);
    }

    public BiomeFinderTarget withCachedPos(BlockPos pos, ResourceLocation dimension, long tick) {
        return new BiomeFinderTarget(this.selectedBiome, Optional.of(pos), Optional.of(dimension), tick);
    }

    public BiomeFinderTarget cleared() {
        return new BiomeFinderTarget(this.selectedBiome, Optional.empty(), Optional.empty(), 0L);
    }
}
