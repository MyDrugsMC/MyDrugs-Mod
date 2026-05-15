package org.mydrugs.mydrugs.effects.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;

public record AddictionDebugActionPayload(int action, boolean value) implements CustomPacketPayload {
    public static final int RESET_STATS = 0;
    public static final int SET_SYMPTOM_IMMUNITY = 1;

    public static final Type<AddictionDebugActionPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "addiction_debug_action"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddictionDebugActionPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.VAR_INT, AddictionDebugActionPayload::action,
                    ByteBufCodecs.BOOL, AddictionDebugActionPayload::value,
                    AddictionDebugActionPayload::new
            );

    public static void handleOnServer(AddictionDebugActionPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        // Debug mutation: require permission level 2 (op) AND a server config opt-in.
        // Creative mode alone is not authorization on a server you do not own.
        if (!Config.SERVER.allowDebugActionPayloads.get() || !player.hasPermissions(2)) {
            return;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        if (payload.action() == RESET_STATS) {
            boolean immune = stats.addictionSymptomsImmune;
            stats.perDrug.clear();
            stats.stressLevel = AddictionConstants.STRESS_BASELINE;
            stats.overdoseDeathTimer = -1;
            stats.addictionSymptomsImmune = immune;
        } else if (payload.action() == SET_SYMPTOM_IMMUNITY) {
            stats.addictionSymptomsImmune = payload.value();
        }

        PacketDistributor.sendToPlayer(player, AddictionDebugOpenPayload.from(player));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
