package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.RecoveryRitualLogic.RecoveryKind;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.RecoveryRitualLogic.SlotKind;
import org.mydrugs.mydrugs.core.drug.DrugId;

/**
 * Pure-JVM tests for the soft-lock recovery logic. The key guarantee under test is that a recovery
 * route never requires the very item it is meant to recover.
 */
class RecoveryRitualLogicTest {

    private static List<SlotKind> loadout(SlotKind a, SlotKind b, SlotKind c, SlotKind d, SlotKind e) {
        return List.of(a, b, c, d, e);
    }

    @Test
    void receptacleLoadoutClassifies() {
        assertEquals(RecoveryKind.RECEPTACLE, RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE)));
    }

    @Test
    void wireLoadoutClassifies() {
        assertEquals(RecoveryKind.WIRE, RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT,
                        SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT)));
    }

    @Test
    void integrationCoreLoadoutClassifies() {
        assertEquals(RecoveryKind.INTEGRATION_CORE, RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.SEED, SlotKind.SEED, SlotKind.SEED, SlotKind.SEED)));
    }

    @Test
    void drugPositionDoesNotMatter() {
        assertEquals(RecoveryKind.RECEPTACLE, RecoveryRitualLogic.classify(
                loadout(SlotKind.VINE, SlotKind.VINE, SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE)));
        assertEquals(RecoveryKind.WIRE, RecoveryRitualLogic.classify(
                loadout(SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT,
                        SlotKind.COPPER_INGOT, SlotKind.DRUG)));
    }

    /**
     * The core soft-lock guarantee: putting the locked item (a receptacle or a wire) into any slot
     * makes the loadout invalid, so a recovery ritual can never demand the item it recovers.
     */
    @Test
    void recoveryNeverRequiresTheLockedItem() {
        for (SlotKind locked : List.of(SlotKind.RECEPTACLE, SlotKind.WIRE)) {
            for (int slot = 0; slot < 5; slot++) {
                List<SlotKind> base = new ArrayList<>(loadout(
                        SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE));
                base.set(slot, locked);
                assertNull(RecoveryRitualLogic.classify(base),
                        "locked item " + locked + " in slot " + slot + " must invalidate the loadout");

                List<SlotKind> wireBase = new ArrayList<>(loadout(
                        SlotKind.DRUG, SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT,
                        SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT));
                wireBase.set(slot, locked);
                assertNull(RecoveryRitualLogic.classify(wireBase),
                        "locked item " + locked + " in slot " + slot + " must invalidate the wire loadout");
            }
        }
    }

    @Test
    void invalidLoadoutsAreRejected() {
        // No drug.
        assertNull(RecoveryRitualLogic.classify(
                loadout(SlotKind.VINE, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE)));
        // Two drugs.
        assertNull(RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE)));
        // Mixed fillers.
        assertNull(RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE, SlotKind.COPPER_INGOT, SlotKind.COPPER_INGOT)));
        // An empty slot.
        assertNull(RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE, SlotKind.EMPTY)));
        // An unrelated item.
        assertNull(RecoveryRitualLogic.classify(
                loadout(SlotKind.DRUG, SlotKind.VINE, SlotKind.VINE, SlotKind.VINE, SlotKind.OTHER)));
        // Wrong slot count.
        assertNull(RecoveryRitualLogic.classify(List.of(SlotKind.DRUG, SlotKind.VINE)));
        assertNull(RecoveryRitualLogic.classify(List.of()));
    }

    @Test
    void successChanceScalesWithExperience() {
        // 0 levels: chance comes only from the drug bonus.
        assertEquals(0.05D, RecoveryRitualLogic.successChance(0, 30, RecoveryRitualLogic.drugBonus(DrugId.COFFEE)), 1e-9);
        // Half the level cap contributes a quarter of the odds; weed adds 0.15.
        assertEquals(0.40D, RecoveryRitualLogic.successChance(15, 30, RecoveryRitualLogic.drugBonus(DrugId.WEED)), 1e-9);
        // Full level cap = 50% from XP; hash adds 0.30.
        assertEquals(0.80D, RecoveryRitualLogic.successChance(30, 30, RecoveryRitualLogic.drugBonus(DrugId.HASH)), 1e-9);
    }

    @Test
    void thirtyLevelsWithMethGuaranteesSuccess() {
        double chance = RecoveryRitualLogic.successChance(30, 30, RecoveryRitualLogic.drugBonus(DrugId.METH));
        assertEquals(1.0D, chance, 1e-9);
    }

    @Test
    void successChanceIsClampedToOne() {
        // Far beyond the cap, still never exceeds 1.0.
        assertEquals(1.0D, RecoveryRitualLogic.successChance(999, 30, RecoveryRitualLogic.drugBonus(DrugId.METH)), 1e-9);
    }

    @Test
    void experienceConsumptionIsCappedAtMaxLevels() {
        assertEquals(30, RecoveryRitualLogic.levelsConsumed(50, 30));
        assertEquals(10, RecoveryRitualLogic.levelsConsumed(10, 30));
        assertEquals(0, RecoveryRitualLogic.levelsConsumed(0, 30));
        assertEquals(0, RecoveryRitualLogic.levelsConsumed(-5, 30));
    }

    @Test
    void unknownDrugsUseTheDefaultBonus() {
        assertEquals(RecoveryRitualLogic.DEFAULT_DRUG_BONUS, RecoveryRitualLogic.drugBonus(null), 1e-9);
    }

    @Test
    void drugBonusTableMatchesSpec() {
        assertEquals(0.05D, RecoveryRitualLogic.drugBonus(DrugId.COFFEE), 1e-9);
        assertEquals(0.10D, RecoveryRitualLogic.drugBonus(DrugId.TOBACCO), 1e-9);
        assertEquals(0.15D, RecoveryRitualLogic.drugBonus(DrugId.WEED), 1e-9);
        assertEquals(0.25D, RecoveryRitualLogic.drugBonus(DrugId.ALCOHOL), 1e-9);
        assertEquals(0.30D, RecoveryRitualLogic.drugBonus(DrugId.HASH), 1e-9);
        assertEquals(0.40D, RecoveryRitualLogic.drugBonus(DrugId.COCAINE), 1e-9);
        assertEquals(0.45D, RecoveryRitualLogic.drugBonus(DrugId.LSD), 1e-9);
        assertEquals(0.50D, RecoveryRitualLogic.drugBonus(DrugId.METH), 1e-9);
    }
}
