package org.mydrugs.mydrugs.recovery;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.items.data.PersonalMusicDiscData;
import org.mydrugs.mydrugs.network.PersonalDiscPlaybackPayload;
import org.mydrugs.mydrugs.recovery.item.PersonalMusicDiscItem;
import org.mydrugs.mydrugs.recovery.music.ServerMusicLibrary;

public final class PersonalDiscPlaybackManager {
    public static final double RANGE = 64.0D;

    private PersonalDiscPlaybackManager() {
    }

    public static void start(ServerLevel level, BlockPos pos, ItemStack disc, PersonalDiscPlaybackPayload.Source source, long startedGameTime) {
        PersonalMusicDiscData data = PersonalMusicDiscItem.getDiscData(disc);
        if (data.trackId() == null || data.trackId().isBlank()) {
            return;
        }
        PersonalDiscPlaybackPayload payload = new PersonalDiscPlaybackPayload(
                PersonalDiscPlaybackPayload.Action.START,
                source,
                pos.immutable(),
                clean(data.trackId()),
                clean(data.title()),
                clean(data.artist()),
                Math.max(0, data.durationMs()),
                startedGameTime,
                clean(data.serverTrackId()),
                clean(data.audioHash()),
                data.serverHosted(),
                0
        );
        sendToNearby(level, pos, payload);
    }

    public static void stop(ServerLevel level, BlockPos pos, PersonalDiscPlaybackPayload.Source source) {
        sendToNearby(level, pos, PersonalDiscPlaybackPayload.stop(source, pos.immutable()));
    }

    private static void sendToNearby(ServerLevel level, BlockPos pos, PersonalDiscPlaybackPayload payload) {
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.5D;
        double z = pos.getZ() + 0.5D;
        double maxDistanceSqr = RANGE * RANGE;
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(x, y, z) <= maxDistanceSqr) {
                if (payload.serverHosted()) {
                    ServerMusicLibrary.authorizeDownload(player, payload.audioHash());
                }
                PacketDistributor.sendToPlayer(player, payload);
            }
        }
    }

    private static String clean(String value) {
        return value == null ? "" : value;
    }
}
