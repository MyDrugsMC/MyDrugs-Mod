package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.menu.ManualCoffeePulperMenu;

public record CoffeePulperDragPayload(int menuId, float amount) implements CustomPacketPayload {
    public static final Type<CoffeePulperDragPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "coffee_pulper_drag"));

    public static final StreamCodec<ByteBuf, CoffeePulperDragPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, CoffeePulperDragPayload::menuId,
            ByteBufCodecs.FLOAT, CoffeePulperDragPayload::amount,
            CoffeePulperDragPayload::new
    );

    public static void handleOnServer(CoffeePulperDragPayload payload, IPayloadContext context) {
        var player = context.player();
        if (player == null || !(player.containerMenu instanceof ManualCoffeePulperMenu menu)) return;
        if (menu.getMenuId() != payload.menuId()) return;
        menu.addPulperWork(player, Math.max(0.0F, Math.min(payload.amount(), 8.0F)));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
