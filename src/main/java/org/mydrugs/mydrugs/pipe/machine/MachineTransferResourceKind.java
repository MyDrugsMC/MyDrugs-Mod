package org.mydrugs.mydrugs.pipe.machine;

import org.mydrugs.mydrugs.pipe.PipeResourceKind;

public enum MachineTransferResourceKind {
    ITEM,
    FLUID,
    GAS;

    public PipeResourceKind toPipeKind() {
        return switch (this) {
            case ITEM -> PipeResourceKind.ITEM;
            case FLUID -> PipeResourceKind.FLUID;
            case GAS -> PipeResourceKind.GAS;
        };
    }
}
