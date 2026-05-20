package org.mydrugs.mydrugs.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.PersonalMusicDiscData;
import org.mydrugs.mydrugs.recovery.block.ModRecoveryBlocks;
import org.mydrugs.mydrugs.recovery.item.ModRecoveryItems;

public record ScribePersonalDiscPayload(
        BlockPos scriberPos,
        String trackId,
        String title,
        String artist,
        int durationMs,
        boolean liked
) implements CustomPacketPayload {
    private static final int MAX_TRACK_ID_LENGTH = 128;
    private static final int MAX_TITLE_LENGTH = 96;
    private static final int MAX_ARTIST_LENGTH = 96;
    private static final int MAX_DURATION_MS = 24 * 60 * 60 * 1000;

    public static final Type<ScribePersonalDiscPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "scribe_personal_disc"));

    public static final StreamCodec<ByteBuf, ScribePersonalDiscPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                BlockPos.STREAM_CODEC.encode(buf, payload.scriberPos());
                ByteBufCodecs.stringUtf8(MAX_TRACK_ID_LENGTH).encode(buf, payload.trackId());
                ByteBufCodecs.stringUtf8(MAX_TITLE_LENGTH).encode(buf, payload.title());
                ByteBufCodecs.stringUtf8(MAX_ARTIST_LENGTH).encode(buf, payload.artist());
                ByteBufCodecs.VAR_INT.encode(buf, payload.durationMs());
                ByteBufCodecs.BOOL.encode(buf, payload.liked());
            },
            buf -> new ScribePersonalDiscPayload(
                    BlockPos.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.stringUtf8(MAX_TRACK_ID_LENGTH).decode(buf),
                    ByteBufCodecs.stringUtf8(MAX_TITLE_LENGTH).decode(buf),
                    ByteBufCodecs.stringUtf8(MAX_ARTIST_LENGTH).decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handleOnServer(ScribePersonalDiscPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.SCRIBE_PERSONAL_DISC)) {
            return;
        }
        if (!player.level().getBlockState(payload.scriberPos()).is(ModRecoveryBlocks.DISC_SCRIBER.get())
                || !player.blockPosition().closerThan(payload.scriberPos(), 8.0D)) {
            player.displayClientMessage(Component.translatable("message.mydrugs.music.scribe_too_far"), true);
            return;
        }

        String trackId = clean(payload.trackId(), MAX_TRACK_ID_LENGTH);
        if (trackId.isBlank()) {
            player.displayClientMessage(Component.translatable("message.mydrugs.music.no_track_selected"), true);
            return;
        }

        int blankSlot = findBlankDisc(player);
        if (blankSlot < 0) {
            player.displayClientMessage(Component.translatable("message.mydrugs.music.no_blank_disc"), true);
            return;
        }

        String title = clean(payload.title(), MAX_TITLE_LENGTH);
        String artist = clean(payload.artist(), MAX_ARTIST_LENGTH);
        int duration = Math.max(0, Math.min(MAX_DURATION_MS, payload.durationMs()));
        if (title.isBlank()) {
            title = "Personal Track";
        }

        ItemStack blank = player.getInventory().getItem(blankSlot);
        blank.shrink(1);

        ItemStack disc = new ItemStack(ModRecoveryItems.PERSONAL_MUSIC_DISC.get());
        disc.set(ModDataComponents.PERSONAL_MUSIC_DISC.get(), new PersonalMusicDiscData(trackId, title, artist, duration, payload.liked()));
        disc.set(DataComponents.CUSTOM_NAME, Component.literal(title));

        if (!player.getInventory().add(disc)) {
            player.drop(disc, false);
        }
        player.getInventory().setChanged();
        player.displayClientMessage(Component.translatable("message.mydrugs.music.disc_created", title), true);
    }

    private static int findBlankDisc(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModRecoveryItems.BLANK_MUSIC_DISC.get())) {
                return i;
            }
        }
        return -1;
    }

    private static String clean(String value, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.length() <= maxLength) {
            return trimmed;
        }
        return trimmed.substring(0, maxLength);
    }
}
