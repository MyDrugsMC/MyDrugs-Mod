package org.mydrugs.mydrugs.pipe;

import com.mojang.serialization.Codec;

public enum PipeConnectionMode {
    DISABLED,
    PIPE,
    INPUT,
    OUTPUT;

    public static final Codec<PipeConnectionMode> CODEC =
            Codec.STRING.xmap(PipeConnectionMode::valueOf, PipeConnectionMode::name);

    public PipeConnectionMode next() {
        PipeConnectionMode[] values = values();
        return values[(this.ordinal() + 1) % values.length];
    }
}
