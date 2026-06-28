package org.mydrugs.mydrugs.recipes.sieving;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SieveDeterministicBonusTest {
    @Test
    void fiveTwentyPercentCraftsAwardExactlyOneBonus() {
        float accumulator = 0.0F;
        int bonuses = 0;

        for (int i = 0; i < 5; i++) {
            SieveDeterministicBonus.Result result = SieveDeterministicBonus.apply(accumulator, 0.2F);
            accumulator = result.accumulator();
            bonuses += result.bonusCount();
        }

        assertEquals(1, bonuses);
        assertEquals(0.0F, accumulator, 0.0001F);
    }

    @Test
    void fractionalProgressCarriesAcrossCrafts() {
        SieveDeterministicBonus.Result result = SieveDeterministicBonus.apply(0.35F, 0.2F);

        assertEquals(0, result.bonusCount());
        assertEquals(0.55F, result.accumulator(), 0.0001F);
    }
}
