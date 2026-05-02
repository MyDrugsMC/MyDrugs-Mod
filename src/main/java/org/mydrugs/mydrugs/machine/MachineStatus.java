package org.mydrugs.mydrugs.machine;

public enum MachineStatus {
    IDLE,
    RUNNING,
    MISSING_INPUT_ITEM,
    MISSING_INPUT_FLUID,
    MISSING_INPUT_GAS,
    MISSING_CATALYST,
    NO_MATCHING_RECIPE,
    OUTPUT_SLOT_FULL,
    OUTPUT_TANK_FULL,
    OUTPUT_GAS_TANK_FULL,
    MISSING_CONTAINER,
    NOT_ENOUGH_ENERGY,
    NOT_ENOUGH_HEAT,
    BLOCKED_BY_TRANSFER,
    INVALID_MULTIBLOCK,
    PAUSED,
    UNKNOWN_ERROR;

    public String translationKey() {
        return "machine_status.mydrugs." + name().toLowerCase();
    }

    public int networkId() {
        return switch (this) {
            case IDLE -> 0;
            case RUNNING -> 1;
            case MISSING_INPUT_ITEM -> 2;
            case MISSING_INPUT_FLUID -> 3;
            case MISSING_INPUT_GAS -> 4;
            case MISSING_CATALYST -> 5;
            case NO_MATCHING_RECIPE -> 6;
            case OUTPUT_SLOT_FULL -> 7;
            case OUTPUT_TANK_FULL -> 8;
            case OUTPUT_GAS_TANK_FULL -> 9;
            case MISSING_CONTAINER -> 10;
            case NOT_ENOUGH_ENERGY -> 11;
            case NOT_ENOUGH_HEAT -> 12;
            case BLOCKED_BY_TRANSFER -> 13;
            case INVALID_MULTIBLOCK -> 14;
            case PAUSED -> 15;
            case UNKNOWN_ERROR -> 16;
        };
    }

    public static MachineStatus byNetworkId(int id) {
        for (MachineStatus status : values()) {
            if (status.networkId() == id) {
                return status;
            }
        }
        return UNKNOWN_ERROR;
    }
}
