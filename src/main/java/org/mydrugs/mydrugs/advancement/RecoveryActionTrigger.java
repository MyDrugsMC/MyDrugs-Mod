package org.mydrugs.mydrugs.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class RecoveryActionTrigger extends SimpleCriterionTrigger<RecoveryActionTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, String action, String category) {
        trigger(player, instance -> instance.matches(action, category));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<String> action,
            Optional<String> category
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("action").forGetter(Instance::action),
                Codec.STRING.optionalFieldOf("category").forGetter(Instance::category)
        ).apply(instance, Instance::new));

        boolean matches(String action, String category) {
            return StringCriteria.matches(this.action, action)
                    && StringCriteria.matches(this.category, category);
        }
    }
}
