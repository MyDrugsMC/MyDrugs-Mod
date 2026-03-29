package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.world.level.material.Fluids;
import org.mydrugs.mydrugs.MyDrugs;
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
        this.tag(ModFluidTags.BOTTLABLE)
                .add(Fluids.WATER)
                .add(ModFluids.AMMONIAC.source().get())
                .add(ModFluids.BLOOD.source().get())
                .add(ModFluids.VODKA.source().get())
                .add(ModFluids.RAW_ALCOHOL.source().get())
                .add(ModFluids.STARCH_MASH.source().get())
                .add(ModFluids.SWEET_MASH.source().get())
                .add(ModFluids.WILD_YEAST.source().get())
                .add(ModFluids.FERMENTED_MASH.source().get());

        // Optional examples:
        // this.tag(Tags.Fluids.WATER).add(ModFluids.SOME_WATERLIKE_FLUID.source().get());
        // this.tag(Tags.Fluids.POTION).add(ModFluids.MY_POTION_FLUID.source().get());
    }
}