package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.SieveBlockEntity;
import org.mydrugs.mydrugs.menu.SieveMenu;

public record SieveShakePayload(int menuId, float impulse) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<SieveShakePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "sieve_shake"));

    public static final StreamCodec<ByteBuf, SieveShakePayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, SieveShakePayload::menuId,
                    ByteBufCodecs.FLOAT, SieveShakePayload::impulse,
                    SieveShakePayload::new
            );

    public static void handleOnServer(SieveShakePayload payload, IPayloadContext context) {
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
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.SIEVE_SHAKE)) {
            return;
        }
        float impulse = PayloadValidation.clampNonNegative(payload.impulse(), 4.0F);
        sieve.addShakeImpulse(impulse, player);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}