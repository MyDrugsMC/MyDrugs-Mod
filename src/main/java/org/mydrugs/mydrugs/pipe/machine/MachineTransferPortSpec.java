package org.mydrugs.mydrugs.pipe.machine;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public record MachineTransferPortSpec(
        MachineTransferPortId id,
        MachineTransferResourceKind kind,
        MachineTransferAccess access,
        String translationKey,
        List<Integer> itemSlots,
        List<Integer> fluidTanks,
        List<Integer> gasTanks,
        Set<MachineLocalSide> defaultLocalSides,
        boolean allowMultipleSides,
        int sortOrder
) {
    public MachineTransferPortSpec {
        itemSlots = List.copyOf(itemSlots);
        fluidTanks = List.copyOf(fluidTanks);
        gasTanks = List.copyOf(gasTanks);
        defaultLocalSides = defaultLocalSides.isEmpty()
                ? Set.of()
                : Set.copyOf(EnumSet.copyOf(defaultLocalSides));
    }

    public boolean supports(MachineTransferSideRule rule) {
        return this.access.allows(rule);
    }
}
