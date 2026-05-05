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
import org.mydrugs.mydrugs.menu.PsyMixerMenu;

public record PsyMixerStartRitualPayload(int menuId, BlockPos corePos) implements CustomPacketPayload {
    public static final Type<PsyMixerStartRitualPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "psy_mixer_start_ritual")
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, PsyMixerStartRitualPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, PsyMixerStartRitualPayload::menuId,
            BlockPos.STREAM_CODEC, PsyMixerStartRitualPayload::corePos,
            PsyMixerStartRitualPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(PsyMixerStartRitualPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;
        if (!(player.containerMenu instanceof PsyMixerMenu menu)) return;
        if (menu.getMenuId() != payload.menuId()) return;
        if (!menu.stillValid(player)) return;
        if (!menu.getCorePos().equals(payload.corePos())) return;

        if (player.level().getBlockEntity(payload.corePos()) instanceof FormedPsyMixerCoreBlockEntity core) {
            core.tryStartRitual(player);
        }
    }
}
