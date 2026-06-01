package org.mydrugs.mydrugs.addiction.explain;

public enum AddictionDangerReason {
    NONE(0),
    OVERDOSE(1),
    BAD_TRIP(2),
    VERY_HIGH_DOSE(3),
    HIGH_DOSE(4),
    HUNGER(5),
    LOW_HEALTH(6),
    WITHDRAWAL_PEAK(7),
    HIGH_STRESS(8),
    SLEEP_BLOCKED(9),
    ISOLATION(10),
    UNSAFE_PLACE(11);

    private final int networkId;

    AddictionDangerReason(int networkId) {
        this.networkId = networkId;
    }

    public int networkId() {
        return networkId;
    }

    public static AddictionDangerReason byNetworkId(int networkId) {
        for (AddictionDangerReason reason : values()) {
            if (reason.networkId == networkId) {
                return reason;
            }
        }
        return NONE;
    }
}
