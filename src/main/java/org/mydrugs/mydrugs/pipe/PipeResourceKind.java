package org.mydrugs.mydrugs.pipe;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum PipeResourceKind {
    ITEM,
    FLUID,
    GAS;

    public static final Codec<PipeResourceKind> CODEC = Codec.STRING.xmap(PipeResourceKind::valueOf, PipeResourceKind::name);

    public String serializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
