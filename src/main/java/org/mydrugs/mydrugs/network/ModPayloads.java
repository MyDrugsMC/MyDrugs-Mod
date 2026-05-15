package org.mydrugs.mydrugs.network;

import net.minecraft.server.level.ServerPlayer;
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
        PayloadRegistrar registrar = event.registrar(MyDrugs.NETWORK_VERSION);
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
                CoffeePulperDragPayload.TYPE,
                CoffeePulperDragPayload.STREAM_CODEC,
                CoffeePulperDragPayload::handleOnServer
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
        registrar.playToServer(
                PsyMixerStartRitualPayload.TYPE,
                PsyMixerStartRitualPayload.STREAM_CODEC,
                PsyMixerStartRitualPayload::handleOnServer
        );
        registrar.playToServer(
                PsyMixerRitualInputPayload.TYPE,
                PsyMixerRitualInputPayload.STREAM_CODEC,
                PsyMixerRitualInputPayload::handleOnServer
        );
        registrar.playToClient(
                BiomeFinderOpenScreenPayload.TYPE,
                BiomeFinderOpenScreenPayload.STREAM_CODEC
        );
        registrar.playToServer(
                BiomeFinderSelectPayload.TYPE,
                BiomeFinderSelectPayload.STREAM_CODEC,
                BiomeFinderSelectPayload::handleOnServer
        );
        registrar.playToServer(
                StimulantDashPayload.TYPE,
                StimulantDashPayload.STREAM_CODEC,
                StimulantDashPayload::handleOnServer
        );
        registrar.playToClient(
                OpenDrugFormulaNamingPayload.TYPE,
                OpenDrugFormulaNamingPayload.STREAM_CODEC
        );
        registrar.playToServer(
                SubmitDrugFormulaNamePayload.TYPE,
                SubmitDrugFormulaNamePayload.STREAM_CODEC,
                SubmitDrugFormulaNamePayload::handleOnServer
        );
    }

    private static void handleSieveShake(SieveShakePayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
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
        float impulse = PayloadValidation.clampNonNegative(payload.impulse(), 4.0F);
        sieve.addShakeImpulse(impulse, player);
    }
}
