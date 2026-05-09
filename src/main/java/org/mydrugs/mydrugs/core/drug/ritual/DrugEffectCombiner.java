package org.mydrugs.mydrugs.core.drug.ritual;

import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.EnumMap;
import java.util.List;

public final class DrugEffectCombiner {
    private DrugEffectCombiner() {
    }

    public static List<RitualDrugEffectData> combine(List<RitualDrugEffectData> effects) {
        EnumMap<EffectType, RitualDrugEffectData> byType = new EnumMap<>(EffectType.class);
        for (RitualDrugEffectData effect : effects) {
            byType.merge(effect.type(), effect, DrugEffectCombiner::merge);
        }
        return byType.values().stream()
                .sorted(RitualDrugEffectData.CANONICAL_ORDER)
                .toList();
    }

    private static RitualDrugEffectData merge(RitualDrugEffectData left, RitualDrugEffectData right) {
        return new RitualDrugEffectData(
                left.type(),
                Math.max(left.duration(), right.duration()),
                Math.min(2.5F, left.intensity() + right.intensity())
        );
    }
}
