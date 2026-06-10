package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.mydrugs.mydrugs.items.data.ComponentCodecs;

public record PersonalMusicDiscData(
        String trackId,
        String serverTrackId,
        String audioHash,
        String title,
        String artist,
        int durationMs,
        boolean liked,
        boolean serverHosted
) {
    public static final int MAX_TRACK_ID_LENGTH = 256;
    public static final int MAX_TEXT_LENGTH = 256;
    public static final int MAX_DURATION_MS = 24 * 60 * 60 * 1000; // 24h sanity ceiling

    public static final PersonalMusicDiscData EMPTY = new PersonalMusicDiscData("", "", "", "", "", 0, false, false);

    public PersonalMusicDiscData(String trackId, String title, String artist, int durationMs, boolean liked) {
        this(trackId, "", "", title, artist, durationMs, liked, false);
    }

    public static final Codec<PersonalMusicDiscData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ComponentCodecs.boundedString(MAX_TRACK_ID_LENGTH).optionalFieldOf("track_id", "").forGetter(PersonalMusicDiscData::trackId),
            ComponentCodecs.boundedString(MAX_TRACK_ID_LENGTH).optionalFieldOf("server_track_id", "").forGetter(PersonalMusicDiscData::serverTrackId),
            ComponentCodecs.boundedString(64).optionalFieldOf("audio_hash", "").forGetter(PersonalMusicDiscData::audioHash),
            ComponentCodecs.boundedString(MAX_TEXT_LENGTH).optionalFieldOf("title", "").forGetter(PersonalMusicDiscData::title),
            ComponentCodecs.boundedString(MAX_TEXT_LENGTH).optionalFieldOf("artist", "").forGetter(PersonalMusicDiscData::artist),
            ComponentCodecs.clampedInt(0, MAX_DURATION_MS).optionalFieldOf("duration_ms", 0).forGetter(PersonalMusicDiscData::durationMs),
            Codec.BOOL.optionalFieldOf("liked", false).forGetter(PersonalMusicDiscData::liked),
            Codec.BOOL.optionalFieldOf("server_hosted", false).forGetter(PersonalMusicDiscData::serverHosted)
    ).apply(instance, PersonalMusicDiscData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PersonalMusicDiscData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ComponentCodecs.boundedStringStream(MAX_TRACK_ID_LENGTH).encode(buf, data.trackId());
                ComponentCodecs.boundedStringStream(MAX_TRACK_ID_LENGTH).encode(buf, data.serverTrackId());
                ComponentCodecs.boundedStringStream(64).encode(buf, data.audioHash());
                ComponentCodecs.boundedStringStream(MAX_TEXT_LENGTH).encode(buf, data.title());
                ComponentCodecs.boundedStringStream(MAX_TEXT_LENGTH).encode(buf, data.artist());
                ComponentCodecs.clampedVarInt(0, MAX_DURATION_MS).encode(buf, data.durationMs());
                ByteBufCodecs.BOOL.encode(buf, data.liked());
                ByteBufCodecs.BOOL.encode(buf, data.serverHosted());
            },
            buf -> new PersonalMusicDiscData(
                    ComponentCodecs.boundedStringStream(MAX_TRACK_ID_LENGTH).decode(buf),
                    ComponentCodecs.boundedStringStream(MAX_TRACK_ID_LENGTH).decode(buf),
                    ComponentCodecs.boundedStringStream(64).decode(buf),
                    ComponentCodecs.boundedStringStream(MAX_TEXT_LENGTH).decode(buf),
                    ComponentCodecs.boundedStringStream(MAX_TEXT_LENGTH).decode(buf),
                    ComponentCodecs.clampedVarInt(0, MAX_DURATION_MS).decode(buf),
                    ByteBufCodecs.BOOL.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );
}
