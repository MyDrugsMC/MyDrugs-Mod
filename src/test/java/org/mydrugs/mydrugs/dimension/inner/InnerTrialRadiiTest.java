package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

/**
 * Truthfulness guards for trial balancing data. The former uniform {@code interactionRadius} on
 * {@link InnerTrialDefinition} was removed as dead data; the genuine proximity radii live in
 * {@link InnerTrialManager}. These tests lock that those radii are sane (positive, bounded) and
 * that the announce radius is never tighter than an interaction radius, so a player is always told
 * about a trial before they can act on it.
 */
class InnerTrialRadiiTest {

    @Test
    void everyTrialDefinitionExistsWithLocalizationKeys() {
        for (DrugId drug : CuratedDrugChain.ORDER) {
            InnerTrialDefinition definition = InnerTrialDefinition.forDrug(drug);
            assertNotNull(definition, () -> "missing trial definition for " + drug);
            assertTrue(definition.titleKey().startsWith("message.mydrugs.inner_trial."),
                    () -> drug + " title key is not a trial localization key: " + definition.titleKey());
            assertTrue(definition.hintKey().startsWith("message.mydrugs.inner_trial."),
                    () -> drug + " hint key is not a trial localization key: " + definition.hintKey());
        }
    }

    @Test
    void allInteractionRadiiArePositiveAndSane() {
        double[] radii = {
                InnerTrialManager.coffeeRadiusForTest(),
                InnerTrialManager.weedTrialRadiusForTest(),
                InnerTrialManager.alcoholTrialRadiusForTest(),
                InnerTrialManager.cocainePadRadiusForTest(),
                InnerTrialManager.landmarkNoticeRadiusForTest(),
        };
        for (double radius : radii) {
            assertTrue(radius > 0.0D, () -> "trial radius must be positive but was " + radius);
            // A trial fixture never spans more than its island; a radius beyond that would be a bug.
            assertTrue(radius <= InnerDimensionConstants.ISLAND_RADIUS,
                    () -> "trial radius " + radius + " exceeds ISLAND_RADIUS");
        }
    }

    @Test
    void noticeRadiusIsNeverTighterThanAnInteractionRadius() {
        double notice = InnerTrialManager.landmarkNoticeRadiusForTest();
        assertTrue(notice >= InnerTrialManager.weedTrialRadiusForTest(),
                "the trial hint must announce at least as far as the weed planting reach");
        assertTrue(notice >= InnerTrialManager.alcoholTrialRadiusForTest(),
                "the trial hint must announce at least as far as the alcohol interaction reach");
        assertTrue(notice >= InnerTrialManager.coffeeRadiusForTest(),
                "the trial hint must announce at least as far as the coffee stand-still pad");
        assertTrue(notice >= InnerTrialManager.cocainePadRadiusForTest(),
                "the trial hint must announce at least as far as the cocaine pad");
    }
}
