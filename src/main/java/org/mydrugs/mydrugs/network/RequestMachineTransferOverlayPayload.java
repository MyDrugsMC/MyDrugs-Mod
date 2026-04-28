package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferMenuAccess;

public record RequestMachineTransferOverlayPayload(int menuId) implements CustomPacketPayload {
    public static final Type<RequestMachineTransferOverlayPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "request_machine_transfer_overlay"));

    public static final StreamCodec<ByteBuf, RequestMachineTransferOverlayPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, RequestMachineTransferOverlayPayload::menuId,
                    RequestMachineTransferOverlayPayload::new
            );

    public static void handleOnServer(RequestMachineTransferOverlayPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer) || player.containerMenu.containerId != payload.menuId()) {
            return;
        }

        if (!(player.containerMenu instanceof MachineTransferMenuAccess access)) {
            return;
        }

        BlockEntity target = access.getMachineTransferTarget(player);
        if (target == null
                || !player.containerMenu.stillValid(player)
                || !MachineTransferAttachments.isSupported(target)
                || !MachineTransferAttachments.hasTransferUpgrade(target)) {
            return;
        }

        PacketDistributor.sendToPlayer(serverPlayer, MachineTransferConfigSnapshotPayload.from(target, payload.menuId()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
