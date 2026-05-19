package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.manager.ItemEffectHandler;
import org.mydrugs.mydrugs.items.ModItems;

public record HeadphonesControlPayload(Action action, String trackId, float volume) implements CustomPacketPayload {
    public enum Action {
        TOGGLE_PLAY(0),
        NEXT(1),
        PREVIOUS(2),
        SELECT_TRACK(3),
        LIKE_TRACK(4),
        SET_VOLUME(5),
        OPEN_GUI(6);

        private final int id;

        Action(int id) {
            this.id = id;
        }

        public int id() {
            return id;
        }

        public static Action byId(int id) {
            for (Action action : values()) {
                if (action.id == id) {
                    return action;
                }
            }
            return TOGGLE_PLAY;
        }
    }

    public static final Type<HeadphonesControlPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "headphones_control"));

    public static final StreamCodec<ByteBuf, HeadphonesControlPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.action().id());
                ByteBufCodecs.STRING_UTF8.encode(buf, payload.trackId());
                ByteBufCodecs.FLOAT.encode(buf, payload.volume());
            },
            buf -> new HeadphonesControlPayload(
                    Action.byId(ByteBufCodecs.VAR_INT.decode(buf)),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(HeadphonesControlPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || !hasHeadphones(player)) {
            return;
        }

        switch (payload.action()) {
            case TOGGLE_PLAY -> ItemEffectHandler.toggleHeadphones(player);
            case NEXT, PREVIOUS -> ItemEffectHandler.cycleHeadphonesTrack(player);
            case SELECT_TRACK -> ItemEffectHandler.setHeadphonesPlaying(player, true);
            case LIKE_TRACK, SET_VOLUME, OPEN_GUI -> {
                // Client-local library state is intentionally not persisted on the server.
            }
        }
    }

    private static boolean hasHeadphones(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.HEADPHONES.get())) {
                return true;
            }
        }
        return false;
    }
}
