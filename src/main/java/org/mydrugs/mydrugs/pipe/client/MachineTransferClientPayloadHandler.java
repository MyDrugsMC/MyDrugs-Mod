package org.mydrugs.mydrugs.pipe.client;

import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.menu.client.AbstractMachineScreen;
import org.mydrugs.mydrugs.network.MachineTransferConfigSnapshotPayload;

public final class MachineTransferClientPayloadHandler {
    private MachineTransferClientPayloadHandler() {
    }

    public static void handleSnapshot(MachineTransferConfigSnapshotPayload payload, IPayloadContext context) {
        if (Minecraft.getInstance().screen instanceof AbstractMachineScreen<?> screen) {
            screen.applyMachineTransferSnapshot(payload);
        }
    }
}
