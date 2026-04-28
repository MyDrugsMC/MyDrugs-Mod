package org.mydrugs.mydrugs.pipe.machine;

import com.mojang.serialization.Codec;

public enum MachineTransferSideRule {
    DISABLED,
    INPUT,
    OUTPUT;

    public static final Codec<MachineTransferSideRule> CODEC =
            Codec.STRING.xmap(MachineTransferSideRule::valueOf, MachineTransferSideRule::name);
}
