package org.mydrugs.mydrugs.client.compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.mydrugs.mydrugs.blocks.entity.BTXFractionationTowerBlockEntity;
import org.mydrugs.mydrugs.fluids.ModFluids;

record BTXFractionationTowerJeiRecipe(
        ResourceLocation inputFluid,
        int inputAmount,
        ResourceLocation benzeneFluid,
        int benzeneAmount,
        ResourceLocation tolueneFluid,
        int tolueneAmount,
        ResourceLocation xyleneFluid,
        int xyleneAmount,
        int processingTime
) {
    static final BTXFractionationTowerJeiRecipe DEFAULT = new BTXFractionationTowerJeiRecipe(
            ModFluids.rl("btx_mix"),
            BTXFractionationTowerBlockEntity.INPUT_PER_BATCH,
            ModFluids.rl(ModFluids.BENZENE.name()),
            BTXFractionationTowerBlockEntity.BENZENE_PER_BATCH,
            ModFluids.rl(ModFluids.TOLUENE.name()),
            BTXFractionationTowerBlockEntity.TOLUENE_PER_BATCH,
            ModFluids.rl(ModFluids.XYLENE.name()),
            BTXFractionationTowerBlockEntity.XYLENE_PER_BATCH,
            BTXFractionationTowerBlockEntity.BASE_TICKS
    );

    Ingredient fuelPreview() {
        return Ingredient.of(Items.COAL, Items.CHARCOAL, Items.BLAZE_ROD, Items.LAVA_BUCKET);
    }
}

