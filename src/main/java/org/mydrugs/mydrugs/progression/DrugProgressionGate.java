package org.mydrugs.mydrugs.progression;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;

import java.util.Optional;

public final class DrugProgressionGate {
    private DrugProgressionGate() {
    }

    public static Decision evaluate(ServerPlayer player, DrugModel model) {
        Rule rule = ruleFor(model);
        if (rule == null) {
            return Decision.allowed(Optional.empty(), false);
        }

        if (rule.requiredKnowledge() != null && !PsyKnowledgeManager.has(player, rule.requiredKnowledge())) {
            if (rule.blockConsumption()) {
                return Decision.blocked(rule.mushroomMessage()
                        ? "message.mydrugs.knowledge.blocked.mushroom"
                        : "message.mydrugs.knowledge.blocked.generic");
            }
            return Decision.allowed(Optional.empty(), true);
        }

        return Decision.allowed(Optional.ofNullable(rule.grantedKnowledge()), false);
    }

    public static void notifyBlocked(ServerPlayer player, String messageKey) {
        player.displayClientMessage(Component.translatable(messageKey), true);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.45F, 0.7F);
    }

    private static @Nullable Rule ruleFor(DrugModel model) {
        DrugId id = model.getId();
        DrugCategory category = model.getDrugCategory();

        return switch (id) {
            case COFFEE -> new Rule(null, PsyKnowledgeKey.CAFFEINE, true, false);
            case TOBACCO -> new Rule(null, PsyKnowledgeKey.NICOTINIC, true, false);
            case WEED, HASH -> new Rule(PsyKnowledgeKey.NICOTINIC, PsyKnowledgeKey.CANNABINOID, true, false);
            case ALCOHOL -> new Rule(PsyKnowledgeKey.CANNABINOID, PsyKnowledgeKey.FERMENTED, true, false);
            case COCAINE, CRACK -> new Rule(PsyKnowledgeKey.FERMENTED, PsyKnowledgeKey.STIMULANT, true, false);
            case LSD -> new Rule(PsyKnowledgeKey.STIMULANT, PsyKnowledgeKey.LYSERGIC, true, false);
            case METH -> new Rule(PsyKnowledgeKey.LYSERGIC, PsyKnowledgeKey.OVERCLOCKED, true, false);
            case MUSHROOMS -> new Rule(PsyKnowledgeKey.OVERCLOCKED, PsyKnowledgeKey.MYCELIAL, false, true);
            default -> {
                if (category == DrugCategory.CANNABINOID) {
                    yield new Rule(PsyKnowledgeKey.NICOTINIC, PsyKnowledgeKey.CANNABINOID, true, false);
                }
                if (category == DrugCategory.STIMULANT) {
                    yield new Rule(PsyKnowledgeKey.FERMENTED, PsyKnowledgeKey.STIMULANT, true, false);
                }
                yield null;
            }
        };
    }

    private record Rule(
            @Nullable PsyKnowledgeKey requiredKnowledge,
            @Nullable PsyKnowledgeKey grantedKnowledge,
            boolean blockConsumption,
            boolean mushroomMessage
    ) {
    }

    public record Decision(boolean allowed, Optional<PsyKnowledgeKey> grantedKnowledge, @Nullable String blockedMessageKey, boolean knowledgeDeferred) {
        static Decision allowed(Optional<PsyKnowledgeKey> grantedKnowledge, boolean knowledgeDeferred) {
            return new Decision(true, grantedKnowledge, null, knowledgeDeferred);
        }

        static Decision blocked(String messageKey) {
            return new Decision(false, Optional.empty(), messageKey, false);
        }
    }
}
