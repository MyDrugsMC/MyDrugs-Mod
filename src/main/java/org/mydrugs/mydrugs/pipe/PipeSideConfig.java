package org.mydrugs.mydrugs.pipe;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterConfig;

public final class PipeSideConfig {
    private PipeConnectionMode mode;
    @Nullable
    private PipeFilterConfig filter;

    public PipeSideConfig() {
        this(PipeConnectionMode.DISABLED, null);
    }

    public PipeSideConfig(PipeConnectionMode mode, @Nullable PipeFilterConfig filter) {
        this.mode = mode;
        this.filter = filter;
    }

    public PipeConnectionMode mode() {
        return this.mode;
    }

    public void setMode(PipeConnectionMode mode) {
        this.mode = mode;
    }

    @Nullable
    public PipeFilterConfig filter() {
        return this.filter;
    }

    public void setFilter(@Nullable PipeFilterConfig filter) {
        this.filter = filter;
    }

    public PipeSideConfig copy() {
        return new PipeSideConfig(this.mode, this.filter);
    }
}
