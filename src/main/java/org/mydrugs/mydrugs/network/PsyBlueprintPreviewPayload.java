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

public record PsyBlueprintPreviewPayload(ResourceLocation dimension, BlockPos origin, List<Entry> entries, int durationTicks)
        implements CustomPacketPayload {
    public static final Type<PsyBlueprintPreviewPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_blueprint_preview"));

    public record Entry(BlockPos pos, ResourceLocation blockId, boolean wrongBlock) {
    }

    public static final StreamCodec<ByteBuf, Entry> ENTRY_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            Entry::pos,
            ResourceLocation.STREAM_CODEC,
            Entry::blockId,
            ByteBufCodecs.BOOL,
            Entry::wrongBlock,
            Entry::new
    );

    public static final StreamCodec<ByteBuf, PsyBlueprintPreviewPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ResourceLocation.STREAM_CODEC.encode(buf, payload.dimension());
                BlockPos.STREAM_CODEC.encode(buf, payload.origin());
                ByteBufCodecs.VAR_INT.encode(buf, payload.entries().size());
                for (Entry entry : payload.entries()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
                ByteBufCodecs.VAR_INT.encode(buf, payload.durationTicks());
            },
            buf -> {
                ResourceLocation dimension = ResourceLocation.STREAM_CODEC.decode(buf);
                BlockPos origin = BlockPos.STREAM_CODEC.decode(buf);
                int count = ByteBufCodecs.VAR_INT.decode(buf);
                List<Entry> entries = new ArrayList<>(Math.min(count, 128));
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CODEC.decode(buf));
                }
                return new PsyBlueprintPreviewPayload(dimension, origin, entries, ByteBufCodecs.VAR_INT.decode(buf));
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
