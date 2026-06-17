package org.mydrugs.mydrugs.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class InnerDimensionMilestoneTrigger
        extends SimpleCriterionTrigger<InnerDimensionMilestoneTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, String milestone) {
        trigger(player, instance -> instance.matches(milestone));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<String> milestone
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("milestone").forGetter(Instance::milestone)
        ).apply(instance, Instance::new));

        boolean matches(String milestone) {
            return StringCriteria.matches(this.milestone, milestone);
        }
    }
}
