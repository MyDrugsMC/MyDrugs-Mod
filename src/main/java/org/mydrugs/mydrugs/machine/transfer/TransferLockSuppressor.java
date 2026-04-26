package org.mydrugs.mydrugs.machine.transfer;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public final class TransferLockSuppressor {
    private TransferLockSuppressor() {
    }

    public static boolean run(Consumer<Boolean> suppressor, BooleanSupplier action) {
        suppressor.accept(true);
        try {
            return action.getAsBoolean();
        } finally {
            suppressor.accept(false);
        }
    }
}
