package org.mydrugs.mydrugs.addiction.data;

public enum WithdrawalPhase {
    NONE(0),
    RISING(1),
    PEAK(2),
    EASING(3),
    SETTLING(4);

    private final int networkId;

    WithdrawalPhase(int networkId) {
        this.networkId = networkId;
    }

    public int networkId() {
        return networkId;
    }

    public static WithdrawalPhase byNetworkId(int networkId) {
        for (WithdrawalPhase phase : values()) {
            if (phase.networkId == networkId) {
                return phase;
            }
        }
        return NONE;
    }
}
