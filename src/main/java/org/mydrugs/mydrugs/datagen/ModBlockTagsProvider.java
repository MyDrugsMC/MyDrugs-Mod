package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MyDrugs.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get())
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get())
                .add(ModBlocks.RAW_PLATINUM_BLOCK.get())
                .add(ModBlocks.PLATINUM_BLOCK.get())
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get())
                .add(ModBlocks.RAW_ALUMINIUM_BLOCK.get())
                .add(ModBlocks.ALUMINIUM_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get())
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get())
                .add(ModBlocks.RAW_ALUMINIUM_BLOCK.get())
                .add(ModBlocks.ALUMINIUM_BLOCK.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get())
                .add(ModBlocks.RAW_PLATINUM_BLOCK.get())
                .add(ModBlocks.PLATINUM_BLOCK.get());

        tag(Tags.Blocks.ORES)
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get())
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get())
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get());
    }

    @Override
    public String getName() {
        return "MyDrugs Block Tags";
    }
}
