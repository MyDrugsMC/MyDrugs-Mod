package org.mydrugs.mydrugs.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualAction;

public record PsyMixerRitualActionPayload(BlockPos corePos, PsyMixerRitualAction action, float clientPhase) implements CustomPacketPayload {
    public static final Type<PsyMixerRitualActionPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_mixer_ritual_action")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PsyMixerRitualActionPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PsyMixerRitualActionPayload::corePos,
            PsyMixerRitualAction.STREAM_CODEC, PsyMixerRitualActionPayload::action,
            ByteBufCodecs.FLOAT, PsyMixerRitualActionPayload::clientPhase,
            PsyMixerRitualActionPayload::new
    );

    public static void handleOnServer(PsyMixerRitualActionPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.PSY_MIXER_RITUAL_INPUT)) return;
        if (player.distanceToSqr(payload.corePos().getX() + 0.5, payload.corePos().getY() + 0.5, payload.corePos().getZ() + 0.5) > 100.0) return;
        if (player.level().getBlockEntity(payload.corePos()) instanceof FormedPsyMixerCoreBlockEntity core) {
            core.handleRitualAction(player, payload.action());
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
