package org.mydrugs.mydrugs.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualAction;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualJudgement;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;

public record PsyMixerRitualSyncPayload(
        BlockPos corePos,
        boolean running,
        String formulaName,
        int actionIndex,
        int actionCount,
        PsyMixerRitualAction action,
        PsyMixerRitualQuality quality,
        int mistakes,
        int maxMistakes,
        PsyMixerRitualJudgement lastJudgement,
        int feedbackTicks,
        int ritualTick,
        int ritualMaxTime,
        int actionTick,
        int actionTimeout,
        float targetPhase,
        float timingWindow,
        boolean completionAnimation,
        int completionTick,
        int completionDuration,
        int completionReunionTick,
        ItemStack completionPreviewStack
) implements CustomPacketPayload {
    public static final Type<PsyMixerRitualSyncPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_mixer_ritual_sync")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PsyMixerRitualSyncPayload> STREAM_CODEC = StreamCodec.of(
            PsyMixerRitualSyncPayload::encode,
            PsyMixerRitualSyncPayload::decode
    );

    private static void encode(RegistryFriendlyByteBuf buf, PsyMixerRitualSyncPayload payload) {
        BlockPos.STREAM_CODEC.encode(buf, payload.corePos);
        ByteBufCodecs.BOOL.encode(buf, payload.running);
        ByteBufCodecs.STRING_UTF8.encode(buf, payload.formulaName);
        ByteBufCodecs.VAR_INT.encode(buf, payload.actionIndex);
        ByteBufCodecs.VAR_INT.encode(buf, payload.actionCount);
        PsyMixerRitualAction.STREAM_CODEC.encode(buf, payload.action);
        PsyMixerRitualQuality.STREAM_CODEC.encode(buf, payload.quality);
        ByteBufCodecs.VAR_INT.encode(buf, payload.mistakes);
        ByteBufCodecs.VAR_INT.encode(buf, payload.maxMistakes);
        ByteBufCodecs.VAR_INT.encode(buf, payload.lastJudgement.id());
        ByteBufCodecs.VAR_INT.encode(buf, payload.feedbackTicks);
        ByteBufCodecs.VAR_INT.encode(buf, payload.ritualTick);
        ByteBufCodecs.VAR_INT.encode(buf, payload.ritualMaxTime);
        ByteBufCodecs.VAR_INT.encode(buf, payload.actionTick);
        ByteBufCodecs.VAR_INT.encode(buf, payload.actionTimeout);
        ByteBufCodecs.FLOAT.encode(buf, payload.targetPhase);
        ByteBufCodecs.FLOAT.encode(buf, payload.timingWindow);
        ByteBufCodecs.BOOL.encode(buf, payload.completionAnimation);
        ByteBufCodecs.VAR_INT.encode(buf, payload.completionTick);
        ByteBufCodecs.VAR_INT.encode(buf, payload.completionDuration);
        ByteBufCodecs.VAR_INT.encode(buf, payload.completionReunionTick);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.completionPreviewStack);
    }

    private static PsyMixerRitualSyncPayload decode(RegistryFriendlyByteBuf buf) {
        return new PsyMixerRitualSyncPayload(
                BlockPos.STREAM_CODEC.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                PsyMixerRitualAction.STREAM_CODEC.decode(buf),
                PsyMixerRitualQuality.STREAM_CODEC.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                PsyMixerRitualJudgement.byId(ByteBufCodecs.VAR_INT.decode(buf)),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ItemStack.OPTIONAL_STREAM_CODEC.decode(buf)
        );
    }

    public PsyMixerRitualSyncPayload {
        completionPreviewStack = completionPreviewStack.copy();
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
