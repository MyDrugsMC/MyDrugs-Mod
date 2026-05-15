package org.mydrugs.mydrugs.advancement;

import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.InjectingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SniffingStrategy;
import org.mydrugs.mydrugs.core.drug.use.ResolvedDrugUse;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;

public final class DrugKnowledge {
    private DrugKnowledge() {
    }

    public static boolean hasDiscovered(ServerPlayer player, DrugId drugId) {
        return player.getData(ModAttachments.DRUG_KNOWLEDGE.get()).hasDiscovered(drugId);
    }

    public static boolean hasDiscoveredCategory(ServerPlayer player, DrugCategory category) {
        return player.getData(ModAttachments.DRUG_KNOWLEDGE.get()).hasDiscoveredCategory(category);
    }

    public static boolean hasDiscoveredRoute(ServerPlayer player, String route) {
        return player.getData(ModAttachments.DRUG_KNOWLEDGE.get()).hasDiscoveredRoute(route);
    }

    public static DrugKnowledgeResult markConsumed(ResolvedDrugUse use) {
        String route = routeName(use.strategy());
        return use.player().getData(ModAttachments.DRUG_KNOWLEDGE.get()).markConsumed(
                use.model().getId(),
                use.model().getDrugCategory(),
                route,
                use.source(),
                use.effectiveDose()
        );
    }

    public static String routeName(ConsumptionStrategy strategy) {
        if (strategy instanceof SmokingStrategy) {
            return "smoked";
        }
        if (strategy instanceof EatingStrategy) {
            return "eaten";
        }
        if (strategy instanceof SniffingStrategy) {
            return "sniffed";
        }
        if (strategy instanceof InjectingStrategy) {
            return "injected";
        }
        return "unknown";
    }
}
