package org.mydrugs.mydrugs.items;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VanillaBiomeFinderItemTest {

    @Test
    void customPsychedelicValleyIsSelectable() {
        ResourceLocation valley = BiomeFinderSelectableBiomes.PSYCHEDELIC_MUSHROOM_VALLEY;

        assertTrue(BiomeFinderSelectableBiomes.isSelectableBiome(valley));
        assertFalse(BiomeFinderSelectableBiomes.isExcluded(valley));
        assertEquals("Psychedelic Mushroom Valley", BiomeFinderSelectableBiomes.prettyName(valley));
    }

    @Test
    void vanillaMushroomFieldsRemainExcluded() {
        ResourceLocation mushroomFields = ResourceLocation.fromNamespaceAndPath("minecraft", "mushroom_fields");

        assertFalse(BiomeFinderSelectableBiomes.isSelectableBiome(mushroomFields));
        assertTrue(BiomeFinderSelectableBiomes.isExcluded(mushroomFields));
    }

    @Test
    void otherModdedBiomesAreNotIncludedByDefault() {
        ResourceLocation moddedBiome = ResourceLocation.fromNamespaceAndPath("othermod", "glow_forest");

        assertFalse(BiomeFinderSelectableBiomes.isSelectableBiome(moddedBiome));
        assertTrue(BiomeFinderSelectableBiomes.isExcluded(moddedBiome));
    }
}
