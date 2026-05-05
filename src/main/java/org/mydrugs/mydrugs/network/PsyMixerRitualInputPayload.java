package org.mydrugs.mydrugs.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.menu.PsyMixerMenu;

public record PsyMixerRitualInputPayload(int menuId, float clientPhase) implements CustomPacketPayload {
    public static final Type<PsyMixerRitualInputPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_mixer_ritual_input")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PsyMixerRitualInputPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PsyMixerRitualInputPayload::menuId,
            ByteBufCodecs.FLOAT, PsyMixerRitualInputPayload::clientPhase,
            PsyMixerRitualInputPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(PsyMixerRitualInputPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!(player.containerMenu instanceof PsyMixerMenu menu)) return;
        if (menu.getMenuId() != payload.menuId()) return;
        if (!menu.stillValid(player)) return;

        if (player.level().getBlockEntity(menu.getCorePos()) instanceof FormedPsyMixerCoreBlockEntity core) {
            core.handleRhythmInput(player);
        }
    }
}
