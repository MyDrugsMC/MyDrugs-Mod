package org.mydrugs.mydrugs.worldgen.biomes;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.mydrugs.mydrugs.blocks.ModBlocks;

public final class ModSurfaceRules {
    private static final SurfaceRules.RuleSource DIRT = SurfaceRules.state(Blocks.DIRT.defaultBlockState());
    private static final SurfaceRules.RuleSource PSY_MYCELIUM =
            SurfaceRules.state(ModBlocks.PSYCHEDELIC_MYCELIUM.get().defaultBlockState());

    public static SurfaceRules.RuleSource makeRules() {
        SurfaceRules.ConditionSource atOrAboveWater = SurfaceRules.waterBlockCheck(-1, 0);

        SurfaceRules.RuleSource mushroomSurface =
                SurfaceRules.sequence(SurfaceRules.ifTrue(atOrAboveWater, PSY_MYCELIUM), DIRT);

        return SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ModBiomes.PSYCHEDELIC_MUSHROOM_VALLEY),
                        SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, mushroomSurface))
        );
    }
}