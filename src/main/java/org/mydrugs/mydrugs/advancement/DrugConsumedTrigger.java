package org.mydrugs.mydrugs.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class DrugConsumedTrigger extends SimpleCriterionTrigger<DrugConsumedTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, DrugKnowledgeResult result) {
        trigger(player, instance -> instance.matches(result));
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<String> drug,
            Optional<String> category,
            Optional<String> route,
            Optional<String> source,
            Optional<Boolean> firstTime,
            Optional<Float> minEffectiveDose
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("drug").forGetter(Instance::drug),
                Codec.STRING.optionalFieldOf("category").forGetter(Instance::category),
                Codec.STRING.optionalFieldOf("route").forGetter(Instance::route),
                Codec.STRING.optionalFieldOf("source").forGetter(Instance::source),
                Codec.BOOL.optionalFieldOf("first_time").forGetter(Instance::firstTime),
                Codec.FLOAT.optionalFieldOf("min_effective_dose").forGetter(Instance::minEffectiveDose)
        ).apply(instance, Instance::new));

        boolean matches(DrugKnowledgeResult result) {
            if (!StringCriteria.matches(drug, result.drugId().serializedName())) {
                return false;
            }
            if (!StringCriteria.matches(category, DrugKnowledgeAttachment.serializeCategory(result.category()))) {
                return false;
            }
            if (!StringCriteria.matches(route, result.route())) {
                return false;
            }
            if (!StringCriteria.matches(source, result.source().name().toLowerCase(java.util.Locale.ROOT))) {
                return false;
            }
            if (firstTime.isPresent() && firstTime.get() != result.anyFirstTime()) {
                return false;
            }
            return minEffectiveDose.isEmpty() || result.effectiveDose() >= minEffectiveDose.get();
        }
    }
}
