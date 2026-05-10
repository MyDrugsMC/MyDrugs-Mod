package org.mydrugs.mydrugs.effects.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.AddictionConstants;
import org.mydrugs.mydrugs.effects.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SafeZoneManager;
import org.mydrugs.mydrugs.effects.addiction.manager.recovery.SocialReliefManager;
import org.mydrugs.mydrugs.effects.addiction.manager.state.StressManager;

import java.util.ArrayList;
import java.util.List;

public record AddictionDebugOpenPayload(
        float geneticFactor,
        float resilience,
        float stressLevel,
        float stressTarget,
        boolean symptomsImmune,
        List<DrugStatsRow> rows
) implements CustomPacketPayload {
    public static final Type<AddictionDebugOpenPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "addiction_debug_open"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AddictionDebugOpenPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        ByteBufCodecs.FLOAT.encode(buf, payload.geneticFactor());
                        ByteBufCodecs.FLOAT.encode(buf, payload.resilience());
                        ByteBufCodecs.FLOAT.encode(buf, payload.stressLevel());
                        ByteBufCodecs.FLOAT.encode(buf, payload.stressTarget());
                        ByteBufCodecs.BOOL.encode(buf, payload.symptomsImmune());
                        ByteBufCodecs.VAR_INT.encode(buf, payload.rows().size());
                        for (DrugStatsRow row : payload.rows()) {
                            ByteBufCodecs.STRING_UTF8.encode(buf, row.drugId());
                            ByteBufCodecs.FLOAT.encode(buf, row.addiction());
                            ByteBufCodecs.FLOAT.encode(buf, row.withdrawal());
                            ByteBufCodecs.FLOAT.encode(buf, row.tolerance());
                            ByteBufCodecs.FLOAT.encode(buf, row.currentDose());
                            ByteBufCodecs.FLOAT.encode(buf, row.relapseMemory());
                            ByteBufCodecs.FLOAT.encode(buf, row.peakAddiction());
                            ByteBufCodecs.VAR_LONG.encode(buf, row.lastUseTime());
                            ByteBufCodecs.STRING_UTF8.encode(buf, row.lastDoseState());
                            ByteBufCodecs.VAR_INT.encode(buf, row.activeDoseContributions());
                        }
                    },
                    buf -> {
                        float geneticFactor = ByteBufCodecs.FLOAT.decode(buf);
                        float resilience = ByteBufCodecs.FLOAT.decode(buf);
                        float stressLevel = ByteBufCodecs.FLOAT.decode(buf);
                        float stressTarget = ByteBufCodecs.FLOAT.decode(buf);
                        boolean symptomsImmune = ByteBufCodecs.BOOL.decode(buf);
                        int count = ByteBufCodecs.VAR_INT.decode(buf);
                        List<DrugStatsRow> rows = new ArrayList<>(count);
                        for (int i = 0; i < count; i++) {
                            rows.add(new DrugStatsRow(
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.FLOAT.decode(buf),
                                    ByteBufCodecs.VAR_LONG.decode(buf),
                                    ByteBufCodecs.STRING_UTF8.decode(buf),
                                    ByteBufCodecs.VAR_INT.decode(buf)
                            ));
                        }
                        return new AddictionDebugOpenPayload(geneticFactor, resilience, stressLevel, stressTarget, symptomsImmune, rows);
                    }
            );

    public static AddictionDebugOpenPayload from(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        List<DrugStatsRow> rows = new ArrayList<>();
        for (DrugId drugId : DrugId.values()) {
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            rows.add(new DrugStatsRow(
                    drugId.name(),
                    drugStats == null ? 0.0F : drugStats.addictionValue,
                    drugStats == null ? 0.0F : drugStats.baseWithdrawalMeter,
                    drugStats == null ? 0.0F : drugStats.tolerance,
                    drugStats == null ? 0.0F : drugStats.currentDose(),
                    drugStats == null ? 0.0F : drugStats.relapseMemory,
                    drugStats == null ? 0.0F : drugStats.peakHistoricalAddiction,
                    drugStats == null ? 0L : drugStats.lastUseTime,
                    drugStats == null ? "NORMAL" : drugStats.lastDoseState.name(),
                    drugStats == null ? 0 : drugStats.doseContributions.size()
            ));
        }
        return new AddictionDebugOpenPayload(
                stats.geneticFactor,
                stats.resilience,
                stats.stressLevel,
                currentStressTarget(player, stats),
                stats.addictionSymptomsImmune,
                rows
        );
    }

    private static float currentStressTarget(ServerPlayer player, PlayerAddictionStats stats) {
        boolean inCombat = player.tickCount - player.getLastHurtByMobTimestamp() < AddictionConstants.COMBAT_DETECTION_TICKS;
        int companions = SocialReliefManager.countCompanions(player, AddictionConstants.COMPANION_DETECTION_RADIUS);
        boolean inSafeZone = SafeZoneManager.isInSafeZone(player);
        float globalSeverity = AddictionManager.getGlobalSeverity(player);
        return StressManager.getStressTarget(player, stats, globalSeverity, inCombat, companions, inSafeZone);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record DrugStatsRow(
            String drugId,
            float addiction,
            float withdrawal,
            float tolerance,
            float currentDose,
            float relapseMemory,
            float peakAddiction,
            long lastUseTime,
            String lastDoseState,
            int activeDoseContributions
    ) {
    }
}
