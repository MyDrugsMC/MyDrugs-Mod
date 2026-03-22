package org.mydrugs.mydrugs.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public record BloodSample(String sourceKind, String sourceId, String sourceName) {
    public static final String PLAYER = "player";
    public static final String ENTITY_TYPE = "entity_type";

    public static final Codec<BloodSample> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("source_kind").forGetter(BloodSample::sourceKind),
            Codec.STRING.fieldOf("source_id").forGetter(BloodSample::sourceId),
            Codec.STRING.fieldOf("source_name").forGetter(BloodSample::sourceName)
    ).apply(instance, BloodSample::new));

    public static final StreamCodec<ByteBuf, BloodSample> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, BloodSample::sourceKind,
            ByteBufCodecs.STRING_UTF8, BloodSample::sourceId,
            ByteBufCodecs.STRING_UTF8, BloodSample::sourceName,
            BloodSample::new
    );

    public static BloodSample fromEntity(LivingEntity entity) {
        if (entity instanceof Player player) {
            return new BloodSample(
                    PLAYER,
                    player.getStringUUID(),
                    player.getGameProfile().name()
            );
        }

        String entityTypeId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
        return new BloodSample(
                ENTITY_TYPE,
                entityTypeId,
                entity.getName().getString()
        );
    }

    public boolean isPlayerSource() {
        return PLAYER.equals(this.sourceKind);
    }
}