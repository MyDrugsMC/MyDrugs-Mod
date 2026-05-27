package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.diary.DiaryBlocker;
import org.mydrugs.mydrugs.diary.DiaryBreadcrumb;
import org.mydrugs.mydrugs.diary.DiaryClaritySnapshot;
import org.mydrugs.mydrugs.diary.DiaryDrugStatDto;
import org.mydrugs.mydrugs.diary.DiaryEntryDto;
import org.mydrugs.mydrugs.diary.DiaryIntegrationProgressDto;
import org.mydrugs.mydrugs.diary.DiaryMasteryStatDto;
import org.mydrugs.mydrugs.diary.DiaryMemory;
import org.mydrugs.mydrugs.diary.DiaryPlayerStateDto;
import org.mydrugs.mydrugs.diary.DiarySpoilerLevel;
import org.mydrugs.mydrugs.diary.DiaryThought;
import org.mydrugs.mydrugs.diary.DiaryWarning;
import org.mydrugs.mydrugs.psyche.PsycheMapNodeDto;

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
        int cooldownTicksRemaining,
        List<PsycheMapNodeDto> psycheNodes,
        DiaryClaritySnapshot clarity,
        List<DiaryIntegrationProgressDto> integrationProgress
) implements CustomPacketPayload {

    public PersonalDiarySnapshotPayload {
        entries = List.copyOf(entries);
        drugStats = List.copyOf(drugStats);
        masteryStats = List.copyOf(masteryStats);
        psycheNodes = List.copyOf(psycheNodes);
        integrationProgress = integrationProgress == null ? List.of() : List.copyOf(integrationProgress);
        if (clarity == null) {
            clarity = DiaryClaritySnapshot.EMPTY;
        }
    }

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
                            ByteBufCodecs.STRING_UTF8.encode(buf, m.displayName());
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

                        ByteBufCodecs.VAR_INT.encode(buf, payload.psycheNodes().size());
                        for (PsycheMapNodeDto n : payload.psycheNodes()) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, n.nodeId());
                            ByteBufCodecs.VAR_LONG.encode(buf, n.unlockedAtGameTime());
                            ByteBufCodecs.VAR_LONG.encode(buf, n.unlockedDay());
                            ByteBufCodecs.STRING_UTF8.encode(buf, n.trigger());
                            ByteBufCodecs.STRING_UTF8.encode(buf, n.dominantDrugId());
                        }

                        encodeClarity(buf, payload.clarity());

                        ByteBufCodecs.VAR_INT.encode(buf, payload.integrationProgress().size());
                        for (DiaryIntegrationProgressDto p : payload.integrationProgress()) {
                            encodeIntegrationProgress(buf, p);
                        }
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

                        int psycheCount = ByteBufCodecs.VAR_INT.decode(buf);
                        List<PsycheMapNodeDto> psycheNodes = new ArrayList<>(psycheCount);
                        for (int i = 0; i < psycheCount; i++) {
                            psycheNodes.add(new PsycheMapNodeDto(
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.VAR_LONG.decode(buf),
                                    ByteBufCodecs.VAR_LONG.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf)
                            ));
                        }
                        DiaryClaritySnapshot clarity = decodeClarity(buf);
                        int integrationCount = ByteBufCodecs.VAR_INT.decode(buf);
                        List<DiaryIntegrationProgressDto> integrationProgress = new ArrayList<>(integrationCount);
                        for (int i = 0; i < integrationCount; i++) {
                            integrationProgress.add(decodeIntegrationProgress(buf));
                        }
                        return new PersonalDiarySnapshotPayload(entries, drugStats, masteryStats, state, currentDay, cooldown, psycheNodes, clarity, integrationProgress);
                    }
            );

    private static void encodeIntegrationProgress(RegistryFriendlyByteBuf buf, DiaryIntegrationProgressDto p) {
        ByteBufCodecs.STRING_UTF8.encode(buf, p.drugId());
        ByteBufCodecs.STRING_UTF8.encode(buf, p.traitKey());
        ByteBufCodecs.STRING_UTF8.encode(buf, p.rewardKey());
        ByteBufCodecs.STRING_UTF8.encode(buf, p.roleplayKey());
        ByteBufCodecs.STRING_UTF8.encode(buf, p.materialItemId());
        ByteBufCodecs.STRING_UTF8.encode(buf, p.requirementType());
        ByteBufCodecs.BOOL.encode(buf, p.knowledgeUnlocked());
        ByteBufCodecs.BOOL.encode(buf, p.peakMet());
        ByteBufCodecs.BOOL.encode(buf, p.lowAddictionMet());
        ByteBufCodecs.BOOL.encode(buf, p.recoveryMet());
        ByteBufCodecs.BOOL.encode(buf, p.lifetimeDoseMet());
        ByteBufCodecs.BOOL.encode(buf, p.cleanDoseStreakMet());
        ByteBufCodecs.BOOL.encode(buf, p.diaryContext());
        ByteBufCodecs.BOOL.encode(buf, p.recoveryRoom());
        ByteBufCodecs.BOOL.encode(buf, p.materialInInventory());
        ByteBufCodecs.BOOL.encode(buf, p.integrationCoreInInventory());
        ByteBufCodecs.BOOL.encode(buf, p.alreadyIntegrated());
        ByteBufCodecs.FLOAT.encode(buf, p.peakCurrent());
        ByteBufCodecs.FLOAT.encode(buf, p.peakRequired());
        ByteBufCodecs.FLOAT.encode(buf, p.addictionCurrent());
        ByteBufCodecs.FLOAT.encode(buf, p.addictionMax());
        ByteBufCodecs.FLOAT.encode(buf, p.recoveryProgress());
        ByteBufCodecs.FLOAT.encode(buf, p.recoveryRequired());
        ByteBufCodecs.FLOAT.encode(buf, p.lifetimeDose());
        ByteBufCodecs.FLOAT.encode(buf, p.lifetimeDoseRequired());
        ByteBufCodecs.VAR_INT.encode(buf, p.cleanDoseStreak());
        ByteBufCodecs.VAR_INT.encode(buf, p.cleanDoseStreakRequired());
    }

    private static DiaryIntegrationProgressDto decodeIntegrationProgress(RegistryFriendlyByteBuf buf) {
        return new DiaryIntegrationProgressDto(
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.STRING_UTF8.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.BOOL.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.FLOAT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf),
                ByteBufCodecs.VAR_INT.decode(buf)
        );
    }

    private static void encodeClarity(RegistryFriendlyByteBuf buf, DiaryClaritySnapshot clarity) {
        DiaryClaritySnapshot safe = clarity == null ? DiaryClaritySnapshot.EMPTY : clarity;
        ByteBufCodecs.STRING_UTF8.encode(buf, safe.thought().textKey());

        ByteBufCodecs.VAR_INT.encode(buf, safe.breadcrumbs().size());
        for (DiaryBreadcrumb breadcrumb : safe.breadcrumbs()) {
            ByteBufCodecs.STRING_UTF8.encode(buf, breadcrumb.textKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, breadcrumb.clearHintKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, breadcrumb.explicitHintKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, breadcrumb.guideTarget());
            ByteBufCodecs.STRING_UTF8.encode(buf, breadcrumb.iconItemId());
            ByteBufCodecs.STRING_UTF8.encode(buf, breadcrumb.spoilerLevel().serializedName());
        }

        ByteBufCodecs.VAR_INT.encode(buf, safe.blockers().size());
        for (DiaryBlocker blocker : safe.blockers()) {
            ByteBufCodecs.STRING_UTF8.encode(buf, blocker.type());
            ByteBufCodecs.STRING_UTF8.encode(buf, blocker.textKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, blocker.clearHintKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, blocker.explicitHintKey());
            ByteBufCodecs.VAR_INT.encode(buf, blocker.count());
            ByteBufCodecs.VAR_LONG.encode(buf, blocker.lastSeenGameTime());
            ByteBufCodecs.STRING_UTF8.encode(buf, blocker.spoilerLevel().serializedName());
        }

        ByteBufCodecs.VAR_INT.encode(buf, safe.memories().size());
        for (DiaryMemory memory : safe.memories()) {
            ByteBufCodecs.VAR_LONG.encode(buf, memory.day());
            ByteBufCodecs.STRING_UTF8.encode(buf, memory.textKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, memory.titleKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, memory.targetNodeId());
            ByteBufCodecs.STRING_UTF8.encode(buf, memory.guideTarget());
            ByteBufCodecs.STRING_UTF8.encode(buf, memory.iconItemId());
        }

        ByteBufCodecs.VAR_INT.encode(buf, safe.warnings().size());
        for (DiaryWarning warning : safe.warnings()) {
            ByteBufCodecs.STRING_UTF8.encode(buf, warning.textKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, warning.clearHintKey());
            ByteBufCodecs.STRING_UTF8.encode(buf, warning.iconItemId());
            ByteBufCodecs.VAR_INT.encode(buf, warning.severity());
        }

        ByteBufCodecs.BOOL.encode(buf, safe.diagnosticMode());
    }

    private static DiaryClaritySnapshot decodeClarity(RegistryFriendlyByteBuf buf) {
        DiaryThought thought = new DiaryThought(ByteBufCodecs.STRING_UTF8.decode(buf));

        int breadcrumbCount = ByteBufCodecs.VAR_INT.decode(buf);
        List<DiaryBreadcrumb> breadcrumbs = new ArrayList<>(breadcrumbCount);
        for (int i = 0; i < breadcrumbCount; i++) {
            breadcrumbs.add(new DiaryBreadcrumb(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    DiarySpoilerLevel.bySerializedName(ByteBufCodecs.STRING_UTF8.decode(buf))
            ));
        }

        int blockerCount = ByteBufCodecs.VAR_INT.decode(buf);
        List<DiaryBlocker> blockers = new ArrayList<>(blockerCount);
        for (int i = 0; i < blockerCount; i++) {
            blockers.add(new DiaryBlocker(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf),
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    DiarySpoilerLevel.bySerializedName(ByteBufCodecs.STRING_UTF8.decode(buf))
            ));
        }

        int memoryCount = ByteBufCodecs.VAR_INT.decode(buf);
        List<DiaryMemory> memories = new ArrayList<>(memoryCount);
        for (int i = 0; i < memoryCount; i++) {
            memories.add(new DiaryMemory(
                    ByteBufCodecs.VAR_LONG.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf)
            ));
        }

        int warningCount = ByteBufCodecs.VAR_INT.decode(buf);
        List<DiaryWarning> warnings = new ArrayList<>(warningCount);
        for (int i = 0; i < warningCount; i++) {
            warnings.add(new DiaryWarning(
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.STRING_UTF8.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
            ));
        }

        boolean diagnosticMode = ByteBufCodecs.BOOL.decode(buf);
        return new DiaryClaritySnapshot(thought, breadcrumbs, blockers, memories, warnings, diagnosticMode);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
