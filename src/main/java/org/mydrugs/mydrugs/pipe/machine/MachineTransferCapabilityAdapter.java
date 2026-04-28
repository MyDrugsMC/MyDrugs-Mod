package org.mydrugs.mydrugs.pipe.machine;

import net.minecraft.core.Direction;

public final class MachineTransferCapabilityAdapter {
    private MachineTransferCapabilityAdapter() {
    }

    public static boolean allows(
            TransferUpgradeableBlockEntity machine,
            MachineTransferPortSpec port,
            Direction side,
            MachineTransferSideRule requestedRule
    ) {
        if (!machine.hasTransferUpgrade()) {
            return false;
        }

        MachineLocalSide localSide = MachineLocalSide.FRONT;
        return port.supports(requestedRule) && machine.transferConfig().getRule(port.id(), localSide) == requestedRule;
    }
}
