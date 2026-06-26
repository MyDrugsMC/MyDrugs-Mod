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
 * Server -> client: tell tracking clients a Psychotrope Engine just pushed Psy Current to a set of
 * targets. The client animates a brief trail along each engine->target line, color-modulated by
 * the strain bucket so the player can feel the engine getting jagged as it approaches overload.
 */
public record DistillateEnginePulsePayload(
        BlockPos enginePos,
        List<BlockPos> targets,
        int strainBucket
) implements CustomPacketPayload {
    public static final Type<DistillateEnginePulsePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "distillate_engine_pulse"));

    private static final StreamCodec<RegistryFriendlyByteBuf, List<BlockPos>> POS_LIST_CODEC =
            BlockPos.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().apply(ByteBufCodecs.list(64));

    public static final StreamCodec<RegistryFriendlyByteBuf, DistillateEnginePulsePayload> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, DistillateEnginePulsePayload::enginePos,
                    POS_LIST_CODEC, DistillateEnginePulsePayload::targets,
                    ByteBufCodecs.VAR_INT, DistillateEnginePulsePayload::strainBucket,
                    DistillateEnginePulsePayload::new
            );

    public DistillateEnginePulsePayload {
        targets = List.copyOf(targets);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
