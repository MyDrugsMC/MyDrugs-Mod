package org.mydrugs.mydrugs.pipe.machine;

import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

public record MachineTransferPortId(ResourceLocation id) {
    public static final Codec<MachineTransferPortId> CODEC =
            ResourceLocation.CODEC.xmap(MachineTransferPortId::new, MachineTransferPortId::id);

    public static MachineTransferPortId of(String namespace, String path) {
        return new MachineTransferPortId(ResourceLocation.fromNamespaceAndPath(namespace, path));
    }
}
