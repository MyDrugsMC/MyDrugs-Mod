package org.mydrugs.mydrugs.pipe.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.pipe.PipeResourceKind;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public record PipeFilterConfig(PipeResourceKind kind, PipeFilterMode mode, List<ResourceLocation> entries) {
    public static final int MAX_ENTRIES = 9;

    public static final Codec<PipeFilterConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PipeResourceKind.CODEC.fieldOf("kind").forGetter(PipeFilterConfig::kind),
            PipeFilterMode.CODEC.fieldOf("mode").forGetter(PipeFilterConfig::mode),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entries", List.of()).forGetter(PipeFilterConfig::entries)
    ).apply(instance, PipeFilterConfig::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeFilterConfig> STREAM_CODEC = StreamCodec.of(
            PipeFilterConfig::encode,
            PipeFilterConfig::decode
    );

    public PipeFilterConfig {
        if (kind == null) {
            kind = PipeResourceKind.ITEM;
        }
        if (mode == null) {
            mode = PipeFilterMode.DENY_LIST;
        }
        Set<ResourceLocation> normalized = new LinkedHashSet<>();
        if (entries != null) {
            for (ResourceLocation entry : entries) {
                if (entry != null && normalized.size() < MAX_ENTRIES) {
                    normalized.add(entry);
                }
            }
        }
        entries = List.copyOf(normalized);
    }

    public static PipeFilterConfig empty(PipeResourceKind kind) {
        return new PipeFilterConfig(kind, PipeFilterMode.DENY_LIST, List.of());
    }

    public PipeFilterConfig withKind(PipeResourceKind newKind) {
        return new PipeFilterConfig(newKind, this.mode, List.of());
    }

    public PipeFilterConfig withMode(PipeFilterMode newMode) {
        return new PipeFilterConfig(this.kind, newMode, this.entries);
    }

    public PipeFilterConfig withEntry(int slot, ResourceLocation entry) {
        if (slot < 0 || slot >= MAX_ENTRIES || entry == null) {
            return this;
        }
        List<ResourceLocation> next = new ArrayList<>(this.entries);
        if (next.contains(entry)) {
            return this;
        }
        if (slot < next.size()) {
            next.set(slot, entry);
        } else if (next.size() < MAX_ENTRIES) {
            next.add(entry);
        }
        return new PipeFilterConfig(this.kind, this.mode, next);
    }

    public PipeFilterConfig withoutEntry(int slot) {
        if (slot < 0 || slot >= this.entries.size()) {
            return this;
        }
        List<ResourceLocation> next = new ArrayList<>(this.entries);
        next.remove(slot);
        return new PipeFilterConfig(this.kind, this.mode, next);
    }

    public boolean isNoop() {
        return this.mode == PipeFilterMode.DENY_LIST && this.entries.isEmpty();
    }

    public PipeFilterConfig pruneInvalidEntries() {
        List<ResourceLocation> validEntries = this.entries.stream()
                .filter(entry -> PipeFilterEntryResolver.exists(this.kind, entry))
                .toList();
        return validEntries.size() == this.entries.size() ? this : new PipeFilterConfig(this.kind, this.mode, validEntries);
    }

    public boolean allows(ResourceLocation id) {
        boolean contains = this.entries.contains(id);
        return this.mode == PipeFilterMode.ALLOW_LIST ? contains : !contains;
    }

    private static void encode(RegistryFriendlyByteBuf buf, PipeFilterConfig config) {
        PipeResourceKind.STREAM_CODEC.encode(buf, config.kind());
        PipeFilterMode.STREAM_CODEC.encode(buf, config.mode());
        // The canonical constructor already caps entries at MAX_ENTRIES, so this count matches the loop.
        ByteBufCodecs.VAR_INT.encode(buf, config.entries().size());
        for (ResourceLocation entry : config.entries()) {
            ResourceLocation.STREAM_CODEC.encode(buf, entry);
        }
    }

    private static PipeFilterConfig decode(RegistryFriendlyByteBuf buf) {
        PipeResourceKind kind = PipeResourceKind.STREAM_CODEC.decode(buf);
        PipeFilterMode mode = PipeFilterMode.STREAM_CODEC.decode(buf);
        int count = ByteBufCodecs.VAR_INT.decode(buf);
        // Reject hostile/out-of-range counts up front rather than clamping; clamping would read fewer
        // entries than were written and leave unread element bytes in the buffer (desync/corruption).
        if (count < 0 || count > MAX_ENTRIES) {
            throw new DecoderException("PipeFilterConfig entry count out of range [0, " + MAX_ENTRIES + "]: " + count);
        }
        List<ResourceLocation> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(ResourceLocation.STREAM_CODEC.decode(buf));
        }
        return new PipeFilterConfig(kind, mode, entries);
    }
}
