package org.mydrugs.mydrugs.progression;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;

import java.util.List;
import java.util.Set;

public final class DrugProgressionGate {
    private DrugProgressionGate() {
    }

    public static Decision evaluate(ServerPlayer player, DrugModel model) {
        return evaluateKnownKnowledge(model, PsyKnowledgeManager.getKnown(player));
    }

    public static Decision evaluateKnownKnowledge(DrugModel model, Set<PsyKnowledgeKey> knownKnowledge) {
        Rule rule = ruleFor(model);
        if (rule == null) {
            return Decision.allowed(List.of(), false);
        }

        if (rule.requiredKnowledge() != null && !knownKnowledge.contains(rule.requiredKnowledge())) {
            if (rule.blockConsumption()) {
                return Decision.blocked(rule.mushroomMessage()
                        ? "message.mydrugs.knowledge.blocked.mushroom"
                        : "message.mydrugs.knowledge.blocked.generic");
            }
            return Decision.allowed(List.of(), true);
        }

        return Decision.allowed(rule.grantedKnowledge(), false);
    }

    public static void notifyBlocked(ServerPlayer player, String messageKey) {
        player.displayClientMessage(Component.translatable(messageKey), true);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.45F, 0.7F);
    }

    private static @Nullable Rule ruleFor(DrugModel model) {
        DrugId id = model.getId();
        DrugCategory category = model.getDrugCategory();

        return switch (id) {
            case COFFEE -> new Rule(null, List.of(PsyKnowledgeKey.CAFFEINE), true, false);
            case TOBACCO -> new Rule(null, List.of(PsyKnowledgeKey.NICOTINIC), true, false);
            case WEED -> new Rule(PsyKnowledgeKey.NICOTINIC, List.of(PsyKnowledgeKey.CANNABINOID), true, false);
            case HASH -> new Rule(PsyKnowledgeKey.FERMENTED, List.of(PsyKnowledgeKey.STEEL_PLATING), true, false);
            case ALCOHOL -> new Rule(PsyKnowledgeKey.CANNABINOID, List.of(PsyKnowledgeKey.FERMENTED), true, false);
            case COCAINE, CRACK -> new Rule(PsyKnowledgeKey.STEEL_PLATING, List.of(PsyKnowledgeKey.STIMULANT), true, false);
            case LSD -> new Rule(PsyKnowledgeKey.STIMULANT, List.of(PsyKnowledgeKey.LYSERGIC), true, false);
            case METH -> new Rule(PsyKnowledgeKey.LYSERGIC, List.of(PsyKnowledgeKey.OVERCLOCKED), true, false);
            case MUSHROOMS -> new Rule(PsyKnowledgeKey.OVERCLOCKED, List.of(PsyKnowledgeKey.MYCELIAL), false, true);
            default -> {
                if (category == DrugCategory.CANNABINOID) {
                    yield new Rule(PsyKnowledgeKey.NICOTINIC, List.of(PsyKnowledgeKey.CANNABINOID), true, false);
                }
                if (category == DrugCategory.STIMULANT) {
                    yield new Rule(PsyKnowledgeKey.STEEL_PLATING, List.of(PsyKnowledgeKey.STIMULANT), true, false);
                }
                yield null;
            }
        };
    }

    public static DrugId required(PsyKnowledgeKey key) {
        if (key == PsyKnowledgeKey.CAFFEINE) return DrugId.COFFEE;
        if (key == PsyKnowledgeKey.NICOTINIC) return DrugId.TOBACCO;
        if (key == PsyKnowledgeKey.STEEL_PLATING) return DrugId.HASH;
        if (key == PsyKnowledgeKey.CANNABINOID) return DrugId.WEED;
        if (key == PsyKnowledgeKey.FERMENTED) return DrugId.ALCOHOL;
        if (key == PsyKnowledgeKey.LYSERGIC) return DrugId.LSD;
        if (key == PsyKnowledgeKey.STIMULANT) return DrugId.COCAINE;
        if (key == PsyKnowledgeKey.OVERCLOCKED) return DrugId.METH;
        if (key == PsyKnowledgeKey.MYCELIAL) return DrugId.MUSHROOMS;
        return null;
    }

    private record Rule(
            @Nullable PsyKnowledgeKey requiredKnowledge,
            List<PsyKnowledgeKey> grantedKnowledge,
            boolean blockConsumption,
            boolean mushroomMessage
    ) {
        private Rule {
            grantedKnowledge = List.copyOf(grantedKnowledge);
        }
    }

    public record Decision(boolean allowed, List<PsyKnowledgeKey> grantedKnowledge, @Nullable String blockedMessageKey, boolean knowledgeDeferred) {
        public Decision {
            grantedKnowledge = List.copyOf(grantedKnowledge);
        }

        static Decision allowed(List<PsyKnowledgeKey> grantedKnowledge, boolean knowledgeDeferred) {
            return new Decision(true, grantedKnowledge, null, knowledgeDeferred);
        }

        static Decision blocked(String messageKey) {
            return new Decision(false, List.of(), messageKey, false);
        }
    }
}
