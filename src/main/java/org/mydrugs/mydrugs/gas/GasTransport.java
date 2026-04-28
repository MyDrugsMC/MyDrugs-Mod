package org.mydrugs.mydrugs.gas;

public final class GasTransport {
    private GasTransport() {
    }

    public static long move(IGasHandler from, IGasHandler to, long maxAmount) {
        if (from == null || to == null || maxAmount <= 0) {
            return 0;
        }

        for (int tank = 0; tank < from.getTanks(); tank++) {
            GasStack available = from.getGasInTank(tank);
            if (available.isEmpty()) {
                continue;
            }

            GasStack offer = available.withAmount(Math.min(maxAmount, available.amount()));
            long accepted = to.fill(offer, true);
            if (accepted <= 0) {
                continue;
            }

            GasStack drained = from.drain(tank, accepted, false);
            if (drained.isEmpty()) {
                continue;
            }

            return to.fill(drained, false);
        }

        return 0;
    }
}
