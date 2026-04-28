package org.mydrugs.mydrugs.pipe.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;

import java.util.List;

public record PipeFilterConfig(PipeResourceKind kind, PipeFilterMode mode, List<ResourceLocation> entries) {
    public static final Codec<PipeFilterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PipeResourceKind.CODEC.fieldOf("kind").forGetter(PipeFilterConfig::kind),
            PipeFilterMode.CODEC.fieldOf("mode").forGetter(PipeFilterConfig::mode),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(PipeFilterConfig::entries)
    ).apply(instance, PipeFilterConfig::new));

    public PipeFilterConfig {
        entries = List.copyOf(entries);
    }

    public static PipeFilterConfig empty(PipeResourceKind kind) {
        return new PipeFilterConfig(kind, PipeFilterMode.DENY_LIST, List.of());
    }

    public boolean allows(ResourceLocation id) {
        boolean contains = this.entries.contains(id);
        return this.mode == PipeFilterMode.ALLOW_LIST ? contains : !contains;
    }
}
