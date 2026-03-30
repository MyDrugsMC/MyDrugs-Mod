package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.menu.RollerMenu;

public record RollerDragPayload(int menuId, float amount) implements CustomPacketPayload {
    public static final Type<RollerDragPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "roller_drag"));

    public static final StreamCodec<ByteBuf, RollerDragPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, RollerDragPayload::menuId,
            ByteBufCodecs.FLOAT, RollerDragPayload::amount,
            RollerDragPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(RollerDragPayload payload, IPayloadContext context) {
        var player = context.player();
        if (player == null) {
            return;
        }

        if (!(player.containerMenu instanceof RollerMenu menu)) {
            return;
        }

        if (menu.getMenuId() != payload.menuId()) {
            return;
        }

        float clamped = Math.max(0.0F, Math.min(payload.amount(), 6.0F));
        menu.addRollProgress(clamped);
    }
}