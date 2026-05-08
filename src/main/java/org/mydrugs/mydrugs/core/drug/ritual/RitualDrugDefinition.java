package org.mydrugs.mydrugs.core.drug.ritual;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record RitualDrugDefinition(
        ResourceLocation id,
        DrugId baseDrug,
        Supplier<? extends Item> outputItem,
        List<DrugEffect> additionalEffects,
        Map<EffectType, Float> effectModifiers,
        float doseMultiplier,
        float addictionMultiplier,
        float instabilityModifier,
        float ritualStabilityBonus,
        float machineSpeedBonus
) {
    public static Builder builder(String path) {
        return new Builder(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path));
    }

    public static final class Builder {
        private final ResourceLocation id;
        private DrugId baseDrug = DrugId.WEED;
        private Supplier<? extends Item> outputItem = () -> net.minecraft.world.item.Items.AIR;
        private final List<DrugEffect> additionalEffects = new ArrayList<>();
        private final EnumMap<EffectType, Float> effectModifiers = new EnumMap<>(EffectType.class);
        private float doseMultiplier = 1.0F;
        private float addictionMultiplier = 1.0F;
        private float instabilityModifier = 0.0F;
        private float ritualStabilityBonus = 0.0F;
        private float machineSpeedBonus = 0.0F;

        private Builder(ResourceLocation id) {
            this.id = id;
        }

        public Builder baseDrug(DrugId baseDrug) {
            this.baseDrug = baseDrug;
            return this;
        }

        public Builder outputItem(Supplier<? extends Item> outputItem) {
            this.outputItem = outputItem;
            return this;
        }

        public Builder addEffect(DrugEffect effect) {
            this.additionalEffects.add(effect);
            return this;
        }

        public Builder effectModifier(EffectType type, float amount) {
            this.effectModifiers.put(type, amount);
            return this;
        }

        public Builder doseMultiplier(float doseMultiplier) {
            this.doseMultiplier = doseMultiplier;
            return this;
        }

        public Builder addictionMultiplier(float addictionMultiplier) {
            this.addictionMultiplier = addictionMultiplier;
            return this;
        }

        public Builder instabilityModifier(float instabilityModifier) {
            this.instabilityModifier = instabilityModifier;
            return this;
        }

        public Builder ritualStabilityBonus(float ritualStabilityBonus) {
            this.ritualStabilityBonus = ritualStabilityBonus;
            return this;
        }

        public Builder machineSpeedBonus(float machineSpeedBonus) {
            this.machineSpeedBonus = machineSpeedBonus;
            return this;
        }

        public RitualDrugDefinition build() {
            return new RitualDrugDefinition(
                    id,
                    baseDrug,
                    outputItem,
                    List.copyOf(additionalEffects),
                    Map.copyOf(effectModifiers),
                    doseMultiplier,
                    addictionMultiplier,
                    instabilityModifier,
                    ritualStabilityBonus,
                    machineSpeedBonus
            );
        }
    }
}
