package org.mydrugs.mydrugs.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;

import java.util.Optional;

public final class PsyKnowledgeUnlockedTrigger extends SimpleCriterionTrigger<PsyKnowledgeUnlockedTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, PsyKnowledgeKey key) {
        trigger(player, instance -> instance.matches(key));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<String> knowledge
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("knowledge").forGetter(Instance::knowledge)
        ).apply(instance, Instance::new));

        boolean matches(PsyKnowledgeKey key) {
            return StringCriteria.matches(this.knowledge, key.id().toString());
        }
    }
}
