package org.mydrugs.mydrugs.fluids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModFluidTags {
    public static final TagKey<Fluid> BOTTLABLE = TagKey.create(
            Registries.FLUID,
            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "bottlable")
    );

    private ModFluidTags() {
    }
}