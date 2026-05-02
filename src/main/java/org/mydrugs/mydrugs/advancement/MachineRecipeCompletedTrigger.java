package org.mydrugs.mydrugs.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class MachineRecipeCompletedTrigger extends SimpleCriterionTrigger<MachineRecipeCompletedTrigger.Instance> {
    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, Event event) {
        trigger(player, instance -> instance.matches(event));
    }

    public record Event(
            ResourceLocation machine,
            Optional<ResourceLocation> recipe,
            Optional<ResourceLocation> resultItem,
            Optional<ResourceLocation> resultFluid,
            Optional<String> resultGas,
            Optional<String> tier
    ) {
    }

    public record Instance(
            Optional<ContextAwarePredicate> player,
            Optional<String> machine,
            Optional<String> recipe,
            Optional<String> resultItem,
            Optional<String> resultFluid,
            Optional<String> resultGas,
            Optional<String> tier
    ) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
                Codec.STRING.optionalFieldOf("machine").forGetter(Instance::machine),
                Codec.STRING.optionalFieldOf("recipe").forGetter(Instance::recipe),
                Codec.STRING.optionalFieldOf("result_item").forGetter(Instance::resultItem),
                Codec.STRING.optionalFieldOf("result_fluid").forGetter(Instance::resultFluid),
                Codec.STRING.optionalFieldOf("result_gas").forGetter(Instance::resultGas),
                Codec.STRING.optionalFieldOf("tier").forGetter(Instance::tier)
        ).apply(instance, Instance::new));

        boolean matches(Event event) {
            return StringCriteria.matches(machine, event.machine().toString())
                    && matchesLocation(recipe, event.recipe())
                    && matchesLocation(resultItem, event.resultItem())
                    && matchesLocation(resultFluid, event.resultFluid())
                    && StringCriteria.matches(resultGas, event.resultGas().orElse(""))
                    && StringCriteria.matches(tier, event.tier().orElse(""));
        }

        private static boolean matchesLocation(Optional<String> expected, Optional<ResourceLocation> actual) {
            return expected.isEmpty() || actual.map(ResourceLocation::toString).filter(value -> StringCriteria.matches(expected, value)).isPresent();
        }
    }
}
