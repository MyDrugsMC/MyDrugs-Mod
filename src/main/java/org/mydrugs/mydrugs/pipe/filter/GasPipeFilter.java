package org.mydrugs.mydrugs.pipe.filter;

import org.mydrugs.mydrugs.gas.GasStack;

public final class GasPipeFilter {
    private GasPipeFilter() {
    }

    public static boolean allows(PipeFilterConfig config, GasStack stack) {
        if (stack == null || stack.isEmpty() || stack.type() == null) {
            return false;
        }

        return config.allows(stack.type().id());
    }
}
