package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record PersonalMusicDiscData(
        String trackId,
        String title,
        String artist,
        int durationMs,
        boolean liked
) {
    public static final PersonalMusicDiscData EMPTY = new PersonalMusicDiscData("", "", "", 0, false);

    public static final Codec<PersonalMusicDiscData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("track_id", "").forGetter(PersonalMusicDiscData::trackId),
            Codec.STRING.optionalFieldOf("title", "").forGetter(PersonalMusicDiscData::title),
            Codec.STRING.optionalFieldOf("artist", "").forGetter(PersonalMusicDiscData::artist),
            Codec.INT.optionalFieldOf("duration_ms", 0).forGetter(PersonalMusicDiscData::durationMs),
            Codec.BOOL.optionalFieldOf("liked", false).forGetter(PersonalMusicDiscData::liked)
    ).apply(instance, PersonalMusicDiscData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PersonalMusicDiscData> STREAM_CODEC = StreamCodec.of(
            (buf, data) -> {
                ByteBufCodecs.STRING_UTF8.encode(buf, data.trackId());
                ByteBufCodecs.STRING_UTF8.encode(buf, data.title());
                ByteBufCodecs.STRING_UTF8.encode(buf, data.artist());
                ByteBufCodecs.VAR_INT.encode(buf, data.durationMs());
                ByteBufCodecs.BOOL.encode(buf, data.liked());
            },
            buf -> new PersonalMusicDiscData(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.BOOL.decode(buf)
            )
    );
}
