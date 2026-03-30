package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.fluids.FluidEntry;
import org.mydrugs.mydrugs.fluids.ModFluidTags;
import org.mydrugs.mydrugs.fluids.ModFluids;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagProvider extends FluidTagsProvider {
    public ModFluidTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider
    ) {
        super(output, lookupProvider, MyDrugs.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        TagAppender<Fluid, Fluid> tag = this.tag(ModFluidTags.BOTTLABLE);
        for (FluidEntry entry : ModFluids.ALL.values()) {
            tag.add(entry.source().get());
        }
        tag.add(Fluids.WATER);

        // Optional examples:
        // this.tag(Tags.Fluids.WATER).add(ModFluids.SOME_WATERLIKE_FLUID.source().get());
        // this.tag(Tags.Fluids.POTION).add(ModFluids.MY_POTION_FLUID.source().get());
    }
}