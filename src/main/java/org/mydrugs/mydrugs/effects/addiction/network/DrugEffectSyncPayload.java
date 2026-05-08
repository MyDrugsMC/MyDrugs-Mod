package org.mydrugs.mydrugs.effects.addiction.network;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.List;

public record DrugEffectSyncPayload(List<Entry> effects) implements CustomPacketPayload {
    public static final Type<DrugEffectSyncPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "drug_effect_sync"));

    public record Entry(@Nullable EffectType type, float intensity, int remainingTicks, int fadeTicksRemaining, int fadeDurationTicks) {
        public float effectiveIntensity() {
            if (fadeTicksRemaining <= 0 || fadeDurationTicks <= 0) {
                return intensity;
            }
            return intensity * Math.clamp(fadeTicksRemaining / (float) fadeDurationTicks, 0.0F, 1.0F);
        }
    }

    public static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_CODEC =
            ByteBufCodecs.STRING_UTF8.map(EffectType::bySerializedNameOrNull, EffectType::serializedName);

    public static final StreamCodec<ByteBuf, Entry> ENTRY_CODEC = StreamCodec.composite(
            EFFECT_TYPE_CODEC, Entry::type,
            ByteBufCodecs.FLOAT, Entry::intensity,
            ByteBufCodecs.VAR_INT, Entry::remainingTicks,
            ByteBufCodecs.VAR_INT, Entry::fadeTicksRemaining,
            ByteBufCodecs.VAR_INT, Entry::fadeDurationTicks,
            Entry::new
    );

    public static final StreamCodec<ByteBuf, DrugEffectSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.effects().size());
                for (Entry entry : payload.effects()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
            },
            buf -> {
                int count = ByteBufCodecs.VAR_INT.decode(buf);
                java.util.ArrayList<Entry> entries = new java.util.ArrayList<>(Math.min(count, 32));
                for (int i = 0; i < count; i++) {
                    entries.add(ENTRY_CODEC.decode(buf));
                }
                return new DrugEffectSyncPayload(entries);
            }
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
