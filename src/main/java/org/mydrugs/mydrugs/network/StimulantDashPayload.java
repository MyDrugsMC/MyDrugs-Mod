package org.mydrugs.mydrugs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.network.DrugEffectCueKind;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.core.drug.runtime.StimulantDashManager;

public record StimulantDashPayload(float forward, float strafe) implements CustomPacketPayload {
    public static final Type<StimulantDashPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "stimulant_dash")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, StimulantDashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, StimulantDashPayload::forward,
            ByteBufCodecs.FLOAT, StimulantDashPayload::strafe,
            StimulantDashPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(StimulantDashPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!PayloadValidation.isFinite(payload.forward()) || !PayloadValidation.isFinite(payload.strafe())) {
            return;
        }
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.STIMULANT_DASH)) {
            return;
        }
        // Vanilla Player.zza/xxa are normalised to [-1, 1]; clamp for safety.
        float forward = PayloadValidation.clamp(payload.forward(), -1.0F, 1.0F);
        float strafe = PayloadValidation.clamp(payload.strafe(), -1.0F, 1.0F);
        StimulantDashManager.DashResult result = StimulantDashManager.tryDash(player, forward, strafe);
        DrugEffectRuntimeManager.sendCue(player, EffectType.DASH_POWER, cueKind(result), result == StimulantDashManager.DashResult.SUCCESS ? 1.0F : 0.6F);
    }

    private static DrugEffectCueKind cueKind(StimulantDashManager.DashResult result) {
        return switch (result) {
            case SUCCESS -> DrugEffectCueKind.DASH_SUCCESS;
            case NO_STIMULANT_EFFECT -> DrugEffectCueKind.DASH_NO_EFFECT;
            case COOLDOWN -> DrugEffectCueKind.DASH_COOLDOWN;
            case INVALID_DIRECTION_STATE -> DrugEffectCueKind.DASH_INVALID;
        };
    }
}
