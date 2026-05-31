package org.mydrugs.mydrugs.core.drug;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

class ProductionEffectMappingTest {

    @BeforeAll
    static void registerDrugs() {
        if (DrugRegistry.getDrug(DrugId.COFFEE) == null) {
            DrugRegistry.registerDrugs();
        }
    }

    @Test
    void everyCuratedDrugHasProductionUsefulEffectsFromThePlan() {
        Map<DrugId, Set<EffectType>> expected = new EnumMap<>(DrugId.class);
        expected.put(DrugId.COFFEE, EnumSet.of(EffectType.FOCUS, EffectType.MANUAL_WORK_SPEED));
        expected.put(DrugId.TOBACCO, EnumSet.of(EffectType.MINING_SPEED));
        expected.put(DrugId.WEED, EnumSet.of(EffectType.STRESS_RELIEF, EffectType.MOVEMENT_SLOWDOWN));
        expected.put(DrugId.HASH, EnumSet.of(EffectType.TREMOR_REDUCTION, EffectType.PRECISION));
        expected.put(DrugId.ALCOHOL, EnumSet.of(EffectType.DAMAGE_RESISTANCE));
        expected.put(DrugId.COCAINE, EnumSet.of(EffectType.MOVEMENT_SPEED, EffectType.ATTACK_SPEED));
        expected.put(DrugId.LSD, EnumSet.of(EffectType.ORE_FORTUNE, EffectType.ORE_AURA));
        expected.put(DrugId.METH, EnumSet.of(EffectType.MINING_SPEED, EffectType.ADRENALINE_SURGE));
        expected.put(DrugId.MUSHROOMS, EnumSet.of(EffectType.MULTIBLOCK_VISION, EffectType.LOW_LIGHT_VISION));

        for (DrugId drug : CuratedDrugChain.ORDER) {
            DrugModel model = DrugRegistry.getDrug(drug);
            assertNotNull(model, drug + " must be registered");

            Set<EffectType> actual = model.getDrugEffects().stream()
                    .map(effect -> effect.getEffectType())
                    .collect(Collectors.toCollection(() -> EnumSet.noneOf(EffectType.class)));

            assertTrue(actual.containsAll(expected.get(drug)), drug + " is missing " + expected.get(drug));
        }
    }
}
