package org.mydrugs.mydrugs.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.SieveBlockEntity;
import org.mydrugs.mydrugs.menu.SieveMenu;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModPayloads {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(
                SieveShakePayload.TYPE,
                SieveShakePayload.STREAM_CODEC,
                ModPayloads::handleSieveShake
        );
        registrar.playToServer(
                RollerDragPayload.TYPE,
                RollerDragPayload.STREAM_CODEC,
                RollerDragPayload::handleOnServer
        );
        registrar.playToServer(
                OpenMachineTransferConfigPayload.TYPE,
                OpenMachineTransferConfigPayload.STREAM_CODEC,
                OpenMachineTransferConfigPayload::handleOnServer
        );
        registrar.playToServer(
                RequestMachineTransferOverlayPayload.TYPE,
                RequestMachineTransferOverlayPayload.STREAM_CODEC,
                RequestMachineTransferOverlayPayload::handleOnServer
        );
        registrar.playToServer(
                CycleMachineTransferSidePayload.TYPE,
                CycleMachineTransferSidePayload.STREAM_CODEC,
                CycleMachineTransferSidePayload::handleOnServer
        );
        registrar.playToClient(
                MachineTransferConfigSnapshotPayload.TYPE,
                MachineTransferConfigSnapshotPayload.STREAM_CODEC
        );
    }

    private static void handleSieveShake(SieveShakePayload payload, IPayloadContext context) {
        Player player = context.player();

        if (!(player.containerMenu instanceof SieveMenu menu)) {
            return;
        }

        if (menu.getMenuId() != payload.menuId()) {
            return;
        }

        if (!(menu.getMachineContainer() instanceof SieveBlockEntity sieve)) {
            return;
        }

        if (!menu.stillValid(player)) {
            return;
        }

        sieve.addShakeImpulse(payload.impulse());
    }
}
