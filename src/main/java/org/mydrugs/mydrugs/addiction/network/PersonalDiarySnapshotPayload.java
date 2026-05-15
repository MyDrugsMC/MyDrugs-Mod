package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.diary.DiaryDrugStatDto;
import org.mydrugs.mydrugs.diary.DiaryEntryDto;
import org.mydrugs.mydrugs.diary.DiaryMasteryStatDto;
import org.mydrugs.mydrugs.diary.DiaryPlayerStateDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Server -> client: open the Personal Diary screen with a full snapshot of data the screen needs
 * to render two stats pages + all day pages without further server queries.
 */
public record PersonalDiarySnapshotPayload(
        List<DiaryEntryDto> entries,
        List<DiaryDrugStatDto> drugStats,
        List<DiaryMasteryStatDto> masteryStats,
        DiaryPlayerStateDto playerState,
        long currentDay,
        int cooldownTicksRemaining
) implements CustomPacketPayload {

    public static final Type<PersonalDiarySnapshotPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "personal_diary_snapshot"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PersonalDiarySnapshotPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.VAR_INT.encode(buf, payload.entries().size());
                        for (DiaryEntryDto e : payload.entries()) {
                            ByteBufCodecs.VAR_LONG.encode(buf, e.day());
                            ByteBufCodecs.VAR_LONG.encode(buf, e.createdGameTime());
                            ByteBufCodecs.STRING_UTF8.encode(buf, e.type());
                            ByteBufCodecs.STRING_UTF8.encode(buf, e.content());
                            ByteBufCodecs.STRING_UTF8.encode(buf, e.sourceKey());
                            ByteBufCodecs.STRING_UTF8.encode(buf, e.dominantDrugId());
                        }
                        ByteBufCodecs.VAR_INT.encode(buf, payload.drugStats().size());
                        for (DiaryDrugStatDto d : payload.drugStats()) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, d.drugId());
                            ByteBufCodecs.FLOAT.encode(buf, d.lifetimeDose());
                            ByteBufCodecs.FLOAT.encode(buf, d.addictionValue());
                            ByteBufCodecs.FLOAT.encode(buf, d.withdrawalMeter());
                            ByteBufCodecs.FLOAT.encode(buf, d.tolerance());
                            ByteBufCodecs.FLOAT.encode(buf, d.peakHistoricalAddiction());
                            ByteBufCodecs.FLOAT.encode(buf, d.currentDose());
                        }
                        ByteBufCodecs.VAR_INT.encode(buf, payload.masteryStats().size());
                        for (DiaryMasteryStatDto m : payload.masteryStats()) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, m.recipeId());
                            ByteBufCodecs.VAR_INT.encode(buf, m.completed());
                            ByteBufCodecs.VAR_INT.encode(buf, m.failed());
                            ByteBufCodecs.FLOAT.encode(buf, m.speedMultiplier());
                            ByteBufCodecs.FLOAT.encode(buf, m.instabilityReduction());
                        }
                        DiaryPlayerStateDto s = payload.playerState();
                        ByteBufCodecs.FLOAT.encode(buf, s.stress());
                        ByteBufCodecs.FLOAT.encode(buf, s.globalSeverity());
                        ByteBufCodecs.STRING_UTF8.encode(buf, s.dominantDrugId());
                        ByteBufCodecs.STRING_UTF8.encode(buf, s.dominantCategory());
                        ByteBufCodecs.STRING_UTF8.encode(buf, s.doseState());
                        ByteBufCodecs.BOOL.encode(buf, s.badTripActive());
                        ByteBufCodecs.FLOAT.encode(buf, s.badTripSeverity());
                        ByteBufCodecs.VAR_INT.encode(buf, s.overdoseTimerTicks());
                        ByteBufCodecs.VAR_INT.encode(buf, s.symptomFlags());
                        ByteBufCodecs.VAR_INT.encode(buf, s.recoveryFlags());
                        ByteBufCodecs.BOOL.encode(buf, s.sleepBlocked());

                        ByteBufCodecs.VAR_LONG.encode(buf, payload.currentDay());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.cooldownTicksRemaining());
                    },
                    buf -> {
                        int entryCount = ByteBufCodecs.VAR_INT.decode(buf);
                        List<DiaryEntryDto> entries = new ArrayList<>(entryCount);
                        for (int i = 0; i < entryCount; i++) {
                            entries.add(new DiaryEntryDto(
                                    ByteBufCodecs.VAR_LONG.decode(buf),
                                    ByteBufCodecs.VAR_LONG.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf)
                            ));
                        }
                        int drugCount = ByteBufCodecs.VAR_INT.decode(buf);
                        List<DiaryDrugStatDto> drugStats = new ArrayList<>(drugCount);
                        for (int i = 0; i < drugCount; i++) {
                            drugStats.add(new DiaryDrugStatDto(
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf)
                            ));
                        }
                        int masteryCount = ByteBufCodecs.VAR_INT.decode(buf);
                        List<DiaryMasteryStatDto> masteryStats = new ArrayList<>(masteryCount);
                        for (int i = 0; i < masteryCount; i++) {
                            masteryStats.add(new DiaryMasteryStatDto(
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.VAR_INT.decode(buf),
                                    ByteBufCodecs.VAR_INT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf)
                            ));
                        }
                        DiaryPlayerStateDto state = new DiaryPlayerStateDto(
                                ByteBufCodecs.FLOAT.decode(buf),
                                ByteBufCodecs.FLOAT.decode(buf),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                ByteBufCodecs.STRING_UTF8.decode(buf),
                                ByteBufCodecs.BOOL.decode(buf),
                                ByteBufCodecs.FLOAT.decode(buf),
                                ByteBufCodecs.VAR_INT.decode(buf),
                                ByteBufCodecs.VAR_INT.decode(buf),
                                ByteBufCodecs.VAR_INT.decode(buf),
                                ByteBufCodecs.BOOL.decode(buf)
                        );
                        long currentDay = ByteBufCodecs.VAR_LONG.decode(buf);
                        int cooldown = ByteBufCodecs.VAR_INT.decode(buf);
                        return new PersonalDiarySnapshotPayload(entries, drugStats, masteryStats, state, currentDay, cooldown);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
