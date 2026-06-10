package org.mydrugs.mydrugs.psyche;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.attachment.PlayerIntegrationAttachment;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.network.StartMemoryCapturePayload;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.IntegratedTrait;
import org.mydrugs.mydrugs.diary.DiaryEntry;
import org.mydrugs.mydrugs.diary.DiaryEntryType;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.psyche.network.PsycheMapSyncPayload;

import java.util.ArrayList;
import java.util.List;

public final class PsycheMapManager {
    public static final String INTEGRATION_TRIGGER = "psychotrope_resonator_integration";

    private PsycheMapManager() {
    }

    public static boolean unlockIntegration(ServerPlayer player, DrugId drugId) {
        PsycheMapNodeCatalog.Entry entry = PsycheMapNodeCatalog.byDrug(drugId);
        if (entry == null) {
            return false;
        }
        return unlock(player, entry.idAsResourceLocation(), INTEGRATION_TRIGGER, drugId.serializedName(), true);
    }

    /** Existing knowledge and milestone callers retain their original generic unlock path. */
    public static boolean unlock(ServerPlayer player, ResourceLocation nodeId, String trigger) {
        PsycheMapNodeCatalog.Entry entry = nodeId == null ? null : PsycheMapNodeCatalog.byId(nodeId.toString());
        if (entry == null) {
            return false;
        }
        return unlock(player, nodeId, trigger, dominantDrugId(player), true);
    }

    private static boolean unlock(
            ServerPlayer player,
            ResourceLocation nodeId,
            String trigger,
            String integratedDrugId,
            boolean captureMemory
    ) {
        if (player == null || nodeId == null || PsycheMapNodeCatalog.byId(nodeId.toString()) == null) {
            return false;
        }
        PlayerPsycheMapAttachment map = player.getData(ModAttachments.PLAYER_PSYCHE_MAP.get());
        if (map.has(nodeId)) {
            return false;
        }

        long gameTime = player.level().getGameTime();
        long day = PlayerDiaryAttachment.currentDay(gameTime);
        boolean stored = map.unlock(
                nodeId,
                gameTime,
                day,
                trigger == null ? "" : trigger,
                integratedDrugId == null ? "" : integratedDrugId
        );
        if (!stored) {
            return false;
        }

        PsycheMapNodeCatalog.Entry entry = PsycheMapNodeCatalog.byId(nodeId.toString());
        if (captureMemory && entry != null) {
            PacketDistributor.sendToPlayer(player, new StartMemoryCapturePayload(
                    nodeId.toString(),
                    entry.titleKey,
                    "",
                    gameTime,
                    integratedDrugId
            ));
            appendMemoryDiaryEntry(player, entry, day, gameTime, integratedDrugId);
        }

        syncToClient(player);
        return true;
    }

    private static void appendMemoryDiaryEntry(
            ServerPlayer player,
            PsycheMapNodeCatalog.Entry entry,
            long day,
            long gameTime,
            String integratedDrugId
    ) {
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY);
        Component contentComponent = entry.isIntegrationNode()
                ? Component.translatable(
                        "diary.mydrugs.psyche.integration_memory",
                        Component.translatable(entry.drugTranslationKey())
                )
                : Component.translatable(
                        "diary.mydrugs.psyche.memory",
                        Component.translatable(entry.titleKey)
                );
        String content = PlayerDiaryAttachment.sanitizeCustomContent(contentComponent.getString());
        if (content == null || content.isEmpty()) {
            return;
        }
        diary.append(new DiaryEntry(
                day,
                gameTime,
                DiaryEntryType.AUTO,
                content,
                "memory:" + entry.nodeId,
                integratedDrugId
        ));
    }

    public static List<PsycheMapNodeDto> snapshot(ServerPlayer player) {
        List<PsycheMapNodeDto> nodes = new ArrayList<>();
        if (player == null) {
            return nodes;
        }
        PlayerPsycheMapAttachment map = player.getData(ModAttachments.PLAYER_PSYCHE_MAP.get());
        for (PlayerPsycheMapAttachment.Node node : map.getNodes()) {
            if (PsycheMapNodeCatalog.byId(node.nodeId) != null) {
                nodes.add(new PsycheMapNodeDto(
                        node.nodeId,
                        node.unlockedAtGameTime,
                        node.unlockedDay,
                        node.trigger,
                        node.dominantDrugId
                ));
            }
        }
        return List.copyOf(nodes);
    }

    public static void syncToClient(ServerPlayer player) {
        if (player != null) {
            PacketDistributor.sendToPlayer(player, new PsycheMapSyncPayload(snapshot(player)));
        }
    }

    public static void reconcileIntegratedNodes(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PlayerIntegrationAttachment integration = player.getData(ModAttachments.PLAYER_INTEGRATION.get());
        for (IntegratedTrait trait : integration.all()) {
            PsycheMapNodeCatalog.Entry entry = PsycheMapNodeCatalog.byDrug(trait.source());
            if (entry != null) {
                unlock(
                        player,
                        entry.idAsResourceLocation(),
                        "integration_reconcile",
                        trait.source().serializedName(),
                        false
                );
            }
        }
        syncToClient(player);
    }

    private static String dominantDrugId(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugId best = null;
        float bestScore = -1.0F;
        for (DrugId id : stats.getTrackedDrugIds()) {
            DrugAddictionStats drugStats = stats.getDrugStats(id);
            if (drugStats == null) {
                continue;
            }
            float score = drugStats.currentDose() * 2.0F
                    + drugStats.baseWithdrawalMeter * 1.2F
                    + drugStats.addictionValue * 0.6F;
            if (score > bestScore) {
                bestScore = score;
                best = id;
            }
        }
        return best == null ? "" : best.serializedName();
    }
}
