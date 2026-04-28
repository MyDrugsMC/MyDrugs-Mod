package org.mydrugs.mydrugs.pipe.filter;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum PipeFilterMode {
    ALLOW_LIST,
    DENY_LIST;

    public static final Codec<PipeFilterMode> CODEC = Codec.STRING.xmap(PipeFilterMode::valueOf, PipeFilterMode::name);

    public String serializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
