package org.mydrugs.mydrugs.pipe.machine;

import java.util.List;

public interface TransferUpgradeableBlockEntity {
    boolean hasTransferUpgrade();

    void installTransferUpgrade();

    MachineTransferConfig transferConfig();

    List<MachineTransferPortSpec> transferPorts();
}
