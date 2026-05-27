package org.mydrugs.mydrugs.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.List;

/**
 * Server -> client: show the Distillate Engine's distribution area for {@code durationTicks}.
 *
 * The server emits this once when the player presses the "show area" button. The client renders
 * a particle outline around {@code enginePos} at the requested radius for the requested duration.
 */
public record DistillateEnginePreviewPayload(
        BlockPos enginePos,
        int radius,
        int durationTicks,
        List<BlockPos> validTargets,
        List<BlockPos> fullTargets,
        List<BlockPos> incompatibleTargets
) implements CustomPacketPayload {
    public static final Type<DistillateEnginePreviewPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "distillate_engine_preview"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<BlockPos>> POS_LIST_CODEC =
            BlockPos.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().apply(ByteBufCodecs.list(256));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistillateEnginePreviewPayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, DistillateEnginePreviewPayload::enginePos,
                    ByteBufCodecs.VAR_INT, DistillateEnginePreviewPayload::radius,
                    ByteBufCodecs.VAR_INT, DistillateEnginePreviewPayload::durationTicks,
                    POS_LIST_CODEC, DistillateEnginePreviewPayload::validTargets,
                    POS_LIST_CODEC, DistillateEnginePreviewPayload::fullTargets,
                    POS_LIST_CODEC, DistillateEnginePreviewPayload::incompatibleTargets,
                    DistillateEnginePreviewPayload::new
            );

    public DistillateEnginePreviewPayload {
        validTargets = List.copyOf(validTargets);
        fullTargets = List.copyOf(fullTargets);
        incompatibleTargets = List.copyOf(incompatibleTargets);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
