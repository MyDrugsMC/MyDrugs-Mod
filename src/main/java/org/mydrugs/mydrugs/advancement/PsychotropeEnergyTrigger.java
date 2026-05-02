package org.mydrugs.mydrugs.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class PsychotropeEnergyTrigger extends SimpleCriterionTrigger<PsychotropeEnergyTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, Event event) {
        trigger(player, instance -> instance.matches(event));
    }

    public record Event(String event, String drug, int amount, int threshold) {
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<String> event,
            Optional<String> drug,
            Optional<Integer> amount,
            Optional<Integer> threshold
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("event").forGetter(Instance::event),
                Codec.STRING.optionalFieldOf("drug").forGetter(Instance::drug),
                Codec.INT.optionalFieldOf("amount").forGetter(Instance::amount),
                Codec.INT.optionalFieldOf("threshold").forGetter(Instance::threshold)
        ).apply(instance, Instance::new));

        boolean matches(Event actual) {
            return StringCriteria.matches(event, actual.event())
                    && StringCriteria.matches(drug, actual.drug())
                    && (amount.isEmpty() || actual.amount() >= amount.get())
                    && (threshold.isEmpty() || actual.threshold() >= threshold.get());
        }
    }
}
