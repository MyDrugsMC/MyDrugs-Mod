package org.mydrugs.mydrugs.gas;

public final class GasTransport {
    private GasTransport() {
    }

    public static long move(IGasHandler from, IGasHandler to, long maxAmount) {
        if (from == null || to == null || maxAmount <= 0) {
            return 0;
        }

        GasStack available = from.getGasInTank(0);
        if (available.isEmpty()) {
            return 0;
        }

        GasStack offer = available.withAmount(Math.min(maxAmount, available.amount()));
        long accepted = to.fill(offer, true);
        if (accepted <= 0) {
            return 0;
        }

        GasStack drained = from.drain(accepted, false);
        if (drained.isEmpty()) {
            return 0;
        }

        return to.fill(drained, false);
    }
}