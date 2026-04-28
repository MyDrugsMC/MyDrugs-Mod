package org.mydrugs.mydrugs.pipe.machine;

import org.mydrugs.mydrugs.pipe.PipeResourceKind;

public record MachineTransferPort(MachineTransferPortId id, PipeResourceKind kind, String translationKey) {
}
