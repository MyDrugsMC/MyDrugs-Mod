package org.mydrugs.mydrugs.psyche;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.network.StartMemoryCapturePayload;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;

public final class PsycheMapManager {
    private PsycheMapManager() {
    }

    public static boolean unlock(ServerPlayer player, ResourceLocation nodeId, String trigger) {
        PlayerPsycheMapAttachment map = player.getData(ModAttachments.PLAYER_PSYCHE_MAP.get());
        if (map.has(nodeId)) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        long day = PlayerDiaryAttachment.currentDay(gameTime);
        String dominant = dominantDrugId(player);

        boolean stored = map.unlock(nodeId, gameTime, day, trigger == null ? "" : trigger, dominant);
        if (!stored) {
            return false;
        }

        String titleKey = "knowledge." + nodeId.getNamespace() + "." + nodeId.getPath();
        StartMemoryCapturePayload payload = new StartMemoryCapturePayload(
                nodeId.toString(),
                titleKey,
                "",
                gameTime,
                dominant
        );
        PacketDistributor.sendToPlayer(player, payload);
        return true;
    }

    private static String dominantDrugId(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugId best = null;
        float bestScore = -1.0F;
        for (DrugId id : stats.getTrackedDrugIds()) {
            DrugAddictionStats ds = stats.getDrugStats(id);
            if (ds == null) continue;
            float score = ds.currentDose() * 2.0F + ds.baseWithdrawalMeter * 1.2F + ds.addictionValue * 0.6F;
            if (score > bestScore) {
                bestScore = score;
                best = id;
            }
        }
        return best == null ? "" : best.serializedName();
    }
}
