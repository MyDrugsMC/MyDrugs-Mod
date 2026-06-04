package org.mydrugs.mydrugs.addiction.network;

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

    /** Upper bound on payload entries: one per distinct {@link EffectType}. */
    public static final int MAX_ENTRIES = EffectType.values().length;

    public record Entry(@Nullable EffectType type, float intensity, float riskPressure, int remainingTicks,
                        int activeTicks, int ageTicks, int baselineDurationTicks, int fadeTicksRemaining,
                        int fadeDurationTicks, int onsetTicks, int peakTicks, int comedownTicks,
                        float routeIntensityMultiplier, float routeDurationMultiplier, float routeDoseMultiplier,
                        float routeRiskMultiplier) {
        public float effectiveIntensity() {
            return intensity * activePhaseScale() * fadeScale();
        }

        public boolean fading() {
            return activeTicks <= 0 && fadeTicksRemaining > 0;
        }

        public boolean comedown() {
            if (fading() || !hasPhaseCurve() || activeTicks <= 0) {
                return false;
            }
            int total = activeDurationTotal();
            int onset = Math.min(onsetTicks, Math.max(0, total - 1));
            return ageTicks >= comedownStart(total, onset);
        }

        private boolean hasPhaseCurve() {
            return onsetTicks > 0 || peakTicks > 0 || comedownTicks > 0;
        }

        private float activePhaseScale() {
            if (!hasPhaseCurve() || activeTicks <= 0) {
                return 1.0F;
            }

            int total = activeDurationTotal();
            int onset = Math.min(onsetTicks, Math.max(0, total - 1));
            if (onset > 0 && ageTicks < onset) {
                return Math.clamp(ageTicks / (float) onset, 0.0F, 1.0F);
            }

            int comedownStart = comedownStart(total, onset);
            if (ageTicks >= comedownStart && total > comedownStart) {
                return Math.clamp((total - ageTicks) / (float) (total - comedownStart), 0.0F, 1.0F);
            }

            return 1.0F;
        }

        private float fadeScale() {
            if (activeTicks > 0 || fadeDurationTicks <= 0) {
                return 1.0F;
            }
            return Math.clamp(fadeTicksRemaining / (float) fadeDurationTicks, 0.0F, 1.0F);
        }

        private int activeDurationTotal() {
            return Math.max(1, Math.max(baselineDurationTicks, ageTicks + activeTicks));
        }

        private int comedownStart(int total, int onset) {
            int remainingAfterOnset = Math.max(0, total - onset);
            int comedown = Math.min(comedownTicks, remainingAfterOnset);
            int peak = Math.min(peakTicks, Math.max(0, remainingAfterOnset - comedown));
            return Math.clamp(Math.max(onset + peak, total - comedown), onset, total);
        }
    }

    public static final StreamCodec<ByteBuf, EffectType> EFFECT_TYPE_CODEC =
            ByteBufCodecs.STRING_UTF8.map(EffectType::bySerializedNameOrNull, EffectType::serializedName);

    public static final StreamCodec<ByteBuf, Entry> ENTRY_CODEC = StreamCodec.of(
            (buf, entry) -> {
                EFFECT_TYPE_CODEC.encode(buf, entry.type());
                ByteBufCodecs.FLOAT.encode(buf, entry.intensity());
                ByteBufCodecs.FLOAT.encode(buf, entry.riskPressure());
                ByteBufCodecs.VAR_INT.encode(buf, entry.remainingTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.activeTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.ageTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.baselineDurationTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.fadeTicksRemaining());
                ByteBufCodecs.VAR_INT.encode(buf, entry.fadeDurationTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.onsetTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.peakTicks());
                ByteBufCodecs.VAR_INT.encode(buf, entry.comedownTicks());
                ByteBufCodecs.FLOAT.encode(buf, entry.routeIntensityMultiplier());
                ByteBufCodecs.FLOAT.encode(buf, entry.routeDurationMultiplier());
                ByteBufCodecs.FLOAT.encode(buf, entry.routeDoseMultiplier());
                ByteBufCodecs.FLOAT.encode(buf, entry.routeRiskMultiplier());
            },
            buf -> new Entry(
                    EFFECT_TYPE_CODEC.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf),
                    ByteBufCodecs.FLOAT.decode(buf)
            )
    );

    public static final StreamCodec<ByteBuf, DrugEffectSyncPayload> STREAM_CODEC = StreamCodec.of(
            (buf, payload) -> {
                ByteBufCodecs.VAR_INT.encode(buf, payload.effects().size());
                for (Entry entry : payload.effects()) {
                    ENTRY_CODEC.encode(buf, entry);
                }
            },
            buf -> {
                // Entry count is bounded by the number of distinct EffectType values: a payload can
                // never legitimately carry more, so reject anything larger as corrupt/hostile.
                int count = ByteBufCodecs.VAR_INT.decode(buf);
                if (count < 0 || count > MAX_ENTRIES) {
                    throw new io.netty.handler.codec.DecoderException(
                            "DrugEffectSyncPayload entry count out of bounds: " + count);
                }
                java.util.ArrayList<Entry> entries = new java.util.ArrayList<>(count);
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
