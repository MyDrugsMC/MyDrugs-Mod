package org.mydrugs.mydrugs.pipe.machine;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public record MachineTransferSpec(List<MachineTransferPortSpec> ports) {
    public static final MachineTransferSpec EMPTY = new MachineTransferSpec(List.of());

    public MachineTransferSpec {
        ports = ports.stream()
                .sorted(Comparator.comparingInt(MachineTransferPortSpec::sortOrder))
                .toList();
    }

    public Optional<MachineTransferPortSpec> port(MachineTransferPortId id) {
        return this.ports.stream().filter(port -> port.id().equals(id)).findFirst();
    }

    public List<MachineTransferPortSpec> portsByKind(MachineTransferResourceKind kind) {
        return this.ports.stream().filter(port -> port.kind() == kind).toList();
    }
}
