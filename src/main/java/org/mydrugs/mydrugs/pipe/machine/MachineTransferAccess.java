package org.mydrugs.mydrugs.pipe.machine;

public enum MachineTransferAccess {
    INPUT_ONLY,
    OUTPUT_ONLY,
    BIDIRECTIONAL;

    public boolean allows(MachineTransferSideRule rule) {
        return switch (rule) {
            case DISABLED -> true;
            case INPUT -> this == INPUT_ONLY || this == BIDIRECTIONAL;
            case OUTPUT -> this == OUTPUT_ONLY || this == BIDIRECTIONAL;
        };
    }

    public MachineTransferSideRule next(MachineTransferSideRule current) {
        return switch (this) {
            case INPUT_ONLY -> current == MachineTransferSideRule.DISABLED
                    ? MachineTransferSideRule.INPUT
                    : MachineTransferSideRule.DISABLED;
            case OUTPUT_ONLY -> current == MachineTransferSideRule.DISABLED
                    ? MachineTransferSideRule.OUTPUT
                    : MachineTransferSideRule.DISABLED;
            case BIDIRECTIONAL -> switch (current) {
                case DISABLED -> MachineTransferSideRule.INPUT;
                case INPUT -> MachineTransferSideRule.OUTPUT;
                case OUTPUT -> MachineTransferSideRule.DISABLED;
            };
        };
    }
}
