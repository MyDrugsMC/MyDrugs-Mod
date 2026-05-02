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
import org.mydrugs.mydrugs.pipe.machine.MachineTransferConfig;
import org.mydrugs.mydrugs.pipe.machine.MachineLocalSide;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferMenuAccess;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferPortSpec;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferSideRule;

import java.util.List;

public record CycleMachineTransferSidePayload(int menuId, int portIndex, String localSideId) implements CustomPacketPayload {
    public static final Type<CycleMachineTransferSidePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "cycle_machine_transfer_side"));

    public static final StreamCodec<ByteBuf, CycleMachineTransferSidePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, CycleMachineTransferSidePayload::menuId,
                    ByteBufCodecs.VAR_INT, CycleMachineTransferSidePayload::portIndex,
                    ByteBufCodecs.STRING_UTF8, CycleMachineTransferSidePayload::localSideId,
                    CycleMachineTransferSidePayload::new
            );

    public static void handleOnServer(CycleMachineTransferSidePayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer) || player.containerMenu.containerId != payload.menuId()) {
            return;
        }

        if (!(player.containerMenu instanceof MachineTransferMenuAccess access)) {
            return;
        }

        BlockEntity target = access.getMachineTransferTarget(player);
        MachineLocalSide side = MachineLocalSide.bySerializedName(payload.localSideId());
        if (target == null
                || side == null
                || !player.containerMenu.stillValid(player)
                || !MachineTransferAttachments.isSupported(target)
                || !MachineTransferAttachments.hasTransferUpgrade(target)) {
            return;
        }

        List<MachineTransferPortSpec> ports = MachineTransferAttachments.ports(target);
        if (payload.portIndex() < 0 || payload.portIndex() >= ports.size()) {
            return;
        }

        MachineTransferPortSpec port = ports.get(payload.portIndex());
        MachineTransferConfig config = MachineTransferAttachments.config(target);
        config.cycleRule(port, side);
        MachineTransferAttachments.markCapabilityChanged(target);
        PacketDistributor.sendToPlayer(serverPlayer, MachineTransferConfigSnapshotPayload.from(target, payload.menuId()));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
