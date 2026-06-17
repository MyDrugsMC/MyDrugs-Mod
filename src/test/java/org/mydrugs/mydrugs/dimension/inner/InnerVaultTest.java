package org.mydrugs.mydrugs.dimension.inner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.HashSet;
import java.util.Set;

/** Memory vault guarantees (B1): presence, determinism, suitable ground, tiered loot. */
class InnerVaultTest {

    @Test
    void islandHostsSeveralVaultsOnCalmOpenGround() {
        Set<String> markers = new HashSet<>();
        int cellars = 0;
        int shrines = 0;
        for (int chunkX = -80; chunkX <= 80; chunkX += 1) {
            for (int chunkZ = -80; chunkZ <= 80; chunkZ += 1) {
                for (InnerVaults.Vault vault : InnerVaults.vaultsTouchingChunk(0, 0, chunkX << 4, chunkZ << 4)) {
                    if (!markers.add(vault.marker())) {
                        continue;
                    }
                    if (vault.cellar()) {
                        cellars++;
                    } else {
                        shrines++;
                    }
                    InnerTerrain.Sample centre = InnerTerrain.sample(0, 0, vault.x(), vault.z());
                    assertTrue(centre.land(), "vault on void at " + vault.marker());
                    assertTrue(!centre.lake() && !centre.hole(), "vault in water/hole at " + vault.marker());
                    assertTrue(centre.pathStrength() <= 0.30D, "vault on a path at " + vault.marker());
                }
            }
        }
        assertTrue(markers.size() >= 4, "expected several vaults, found " + markers.size());
        assertTrue(cellars > 0 || shrines > 0, "expected both styles possible");
    }

    @Test
    void vaultPositionsAreDeterministic() {
        var a = InnerVaults.vaultsTouchingChunk(0, 0, 320, 320);
        var b = InnerVaults.vaultsTouchingChunk(0, 0, 320, 320);
        assertEquals(a, b);
    }

    @Test
    void lootTiersCoverAllRegions() {
        assertEquals(InnerVaults.VAULT_DANGER, InnerVaults.lootTableFor(DrugId.METH));
        assertEquals(InnerVaults.VAULT_DANGER, InnerVaults.lootTableFor(DrugId.COCAINE));
        assertEquals(InnerVaults.VAULT_DEEP, InnerVaults.lootTableFor(DrugId.LSD));
        assertEquals(InnerVaults.VAULT_DEEP, InnerVaults.lootTableFor(DrugId.MUSHROOMS));
        assertEquals(InnerVaults.VAULT_CALM, InnerVaults.lootTableFor(DrugId.COFFEE));
        assertEquals(InnerVaults.VAULT_CALM, InnerVaults.lootTableFor(DrugId.WEED));
    }

    @Test
    void chestSitsOnThePedestalOrInTheCellar() {
        InnerVaults.Vault shrine = new InnerVaults.Vault(100, 100, false, 1L, DrugId.COFFEE);
        InnerVaults.Vault cellar = new InnerVaults.Vault(100, 100, true, 1L, DrugId.COFFEE);
        assertEquals(72, InnerVaults.chestY(shrine, 70));
        assertEquals(68, InnerVaults.chestY(cellar, 70));
    }
}
