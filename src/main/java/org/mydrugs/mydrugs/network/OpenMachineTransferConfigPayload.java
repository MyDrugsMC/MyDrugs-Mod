package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferAttachments;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferConfigOpener;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferMenuAccess;

public record OpenMachineTransferConfigPayload(int menuId) implements CustomPacketPayload {
    public static final Type<OpenMachineTransferConfigPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "open_machine_transfer_config"));

    public static final StreamCodec<ByteBuf, OpenMachineTransferConfigPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, OpenMachineTransferConfigPayload::menuId,
                    OpenMachineTransferConfigPayload::new
            );

    public static void handleOnServer(OpenMachineTransferConfigPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (player.containerMenu.containerId != payload.menuId()) {
            return;
        }

        if (!(player.containerMenu instanceof MachineTransferMenuAccess access)) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.transfer_config.unsupported"), true);
            return;
        }

        BlockEntity target = access.getMachineTransferTarget(player);
        if (target == null || !MachineTransferAttachments.isSupported(target) || !player.containerMenu.stillValid(player)) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.transfer_config.open_failed"), true);
            return;
        }

        if (!MachineTransferAttachments.hasTransferUpgrade(target)) {
            serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.transfer_upgrade.required"), true);
            return;
        }

        MachineTransferConfigOpener.open(serverPlayer, target);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
