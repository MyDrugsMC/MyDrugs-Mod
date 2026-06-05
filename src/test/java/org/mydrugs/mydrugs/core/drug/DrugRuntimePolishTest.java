package org.mydrugs.mydrugs.core.drug;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.runtime.ActiveDrugEffect;
import org.mydrugs.mydrugs.core.drug.strategy.RouteEffectProfile;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DrugRuntimePolishTest {
    private static final Set<String> REMOVED_IDS = Set.of(
            "mdma",
            "salvia",
            "dmt",
            "heroin",
            "morphine",
            "fentanyl",
            "opium",
            "ketamine",
            "pcp",
            "dxm",
            "benzodiazepine",
            "barbiturate",
            "nitrous_oxide"
    );

    @BeforeAll
    static void registerDrugs() {
        if (DrugRegistry.getDrug(DrugId.COFFEE) == null) {
            DrugRegistry.registerDrugs();
        }
    }

    @Test
    void removedDrugIdsDoNotResolve() {
        for (String id : REMOVED_IDS) {
            assertNull(DrugId.bySerializedNameOrNull(id));
        }
        assertNull(DrugId.byNetworkId(11));
        assertNull(DrugId.byNetworkId(23));
    }

    @Test
    void remainingNetworkIdsStayStable() {
        assertEquals(1, DrugId.WEED.networkId());
        assertEquals(5, DrugId.CRACK.networkId());
        assertEquals(7, DrugId.LSD.networkId());
        assertEquals(18, DrugId.ALCOHOL.networkId());
        assertEquals(21, DrugId.TOBACCO.networkId());
        assertEquals(22, DrugId.COFFEE.networkId());
    }

    @Test
    void registryOnlyContainsRemainingDrugIds() {
        Set<DrugId> registered = DrugRegistry.getAllDrugs().stream()
                .map(DrugModel::getId)
                .collect(Collectors.toSet());
        assertEquals(EnumSet.allOf(DrugId.class), registered);
        for (DrugModel model : DrugRegistry.getAllDrugs()) {
            assertTrue(model.hasExplicitAddictionRate(), model.getId() + " must pin addiction tuning");
            assertTrue(model.tuningProfile().isSpecified(), model.getId() + " must pin balance tuning");
        }
    }

    @Test
    void routeCurveRampsPeaksAndComesDown() {
        RouteEffectProfile profile = new RouteEffectProfile(10, 10, 10, 1.0F, 1.0F, 1.0F, 1.0F);
        ActiveDrugEffect effect = new ActiveDrugEffect(EffectType.MOVEMENT_SPEED, 1.0F, 40, profile);

        assertEquals(ActiveDrugEffect.Phase.ONSET, effect.phase());
        assertEquals(0.0F, effect.intensity(), 0.0001F);

        for (int i = 0; i < 10; i++) {
            effect.tick();
        }
        assertEquals(ActiveDrugEffect.Phase.PEAK, effect.phase());
        assertEquals(1.0F, effect.intensity(), 0.0001F);

        for (int i = 0; i < 21; i++) {
            effect.tick();
        }
        assertEquals(ActiveDrugEffect.Phase.COMEDOWN, effect.phase());
        assertTrue(effect.intensity() < 1.0F);
    }

    @Test
    void stackingDiminishesBenefitButIncreasesRiskPressure() {
        ActiveDrugEffect benefit = new ActiveDrugEffect(EffectType.MOVEMENT_SPEED, 1.0F, 200);
        benefit.merge(1.0F, 200, RouteEffectProfile.immediate(), false, 1.0F);
        float secondBenefit = benefit.intensity();
        benefit.merge(1.0F, 200, RouteEffectProfile.immediate(), false, 1.0F);
        assertEquals(secondBenefit, benefit.intensity(), 0.0001F);

        ActiveDrugEffect harmful = new ActiveDrugEffect(EffectType.TREMOR, 1.0F, 200);
        harmful.merge(1.0F, 200, RouteEffectProfile.immediate(), true, 1.0F);
        float secondHarm = harmful.intensity();
        float secondPressure = harmful.riskPressure();
        harmful.merge(1.0F, 200, RouteEffectProfile.immediate(), true, 1.0F);
        assertTrue(harmful.intensity() > secondHarm);
        assertTrue(harmful.riskPressure() > secondPressure);
    }

    @Test
    void durationFormattingIsCompact() {
        assertEquals("<1s", AddictionClientState.formatDuration(0));
        assertEquals("42s", AddictionClientState.formatDuration(42 * 20));
        assertEquals("1:24", AddictionClientState.formatDuration(84 * 20));
    }
}
