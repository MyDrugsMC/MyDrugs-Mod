package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.ArrayList;
import java.util.List;

public record RecoveryRoomParticlesPayload(
        BlockPos anchorPos,
        BlockPos min,
        BlockPos max,
        List<BlockPos> samples,
        int score,
        int tierId,
        int moduleFlags,
        boolean activeMusic,
        long seed,
        boolean highlight,
        boolean ambient
) implements CustomPacketPayload {
    public static final Type<RecoveryRoomParticlesPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "recovery_room_particles"));

    public static final StreamCodec<ByteBuf, RecoveryRoomParticlesPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.anchorPos());
                BlockPos.STREAM_CODEC.encode(buf, payload.min());
                BlockPos.STREAM_CODEC.encode(buf, payload.max());
                int count = Math.min(128, payload.samples().size());
                ByteBufCodecs.VAR_INT.encode(buf, count);
                for (int i = 0; i < count; i++) {
                    BlockPos.STREAM_CODEC.encode(buf, payload.samples().get(i));
                }
                ByteBufCodecs.VAR_INT.encode(buf, payload.score());
                ByteBufCodecs.VAR_INT.encode(buf, payload.tierId());
                ByteBufCodecs.VAR_INT.encode(buf, payload.moduleFlags());
                ByteBufCodecs.BOOL.encode(buf, payload.activeMusic());
                ByteBufCodecs.VAR_LONG.encode(buf, payload.seed());
                ByteBufCodecs.BOOL.encode(buf, payload.highlight());
                ByteBufCodecs.BOOL.encode(buf, payload.ambient());
            },
            buf -> {
                BlockPos anchorPos = BlockPos.STREAM_CODEC.decode(buf);
                BlockPos min = BlockPos.STREAM_CODEC.decode(buf);
                BlockPos max = BlockPos.STREAM_CODEC.decode(buf);
                int count = Math.min(128, ByteBufCodecs.VAR_INT.decode(buf));
                List<BlockPos> samples = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    samples.add(BlockPos.STREAM_CODEC.decode(buf));
                }
                int score = ByteBufCodecs.VAR_INT.decode(buf);
                int tierId = ByteBufCodecs.VAR_INT.decode(buf);
                int moduleFlags = ByteBufCodecs.VAR_INT.decode(buf);
                boolean activeMusic = ByteBufCodecs.BOOL.decode(buf);
                long seed = ByteBufCodecs.VAR_LONG.decode(buf);
                boolean highlight = ByteBufCodecs.BOOL.decode(buf);
                boolean ambient = ByteBufCodecs.BOOL.decode(buf);
                return new RecoveryRoomParticlesPayload(anchorPos, min, max, samples, score, tierId, moduleFlags, activeMusic, seed, highlight, ambient);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
