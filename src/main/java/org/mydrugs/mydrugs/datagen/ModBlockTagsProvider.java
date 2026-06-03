package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MyDrugs.MODID);
    }

    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get())
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get())
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get())
                .add(ModBlocks.PHOSPHATE_ORE.get())
                .add(ModBlocks.DEEPSLATE_PHOSPHATE_ORE.get())
                .add(ModBlocks.RAW_PLATINUM_BLOCK.get())
                .add(ModBlocks.PLATINUM_BLOCK.get())
                .add(ModBlocks.RAW_ALUMINIUM_BLOCK.get())
                .add(ModBlocks.ALUMINIUM_BLOCK.get());

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get())
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get())
                .add(ModBlocks.PHOSPHATE_ORE.get())
                .add(ModBlocks.DEEPSLATE_PHOSPHATE_ORE.get())
                .add(ModBlocks.RAW_ALUMINIUM_BLOCK.get())
                .add(ModBlocks.ALUMINIUM_BLOCK.get());

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get())
                .add(ModBlocks.RAW_PLATINUM_BLOCK.get())
                .add(ModBlocks.PLATINUM_BLOCK.get());

        // c:ores aggregate + per-material sub-tags.
        tag(Tags.Blocks.ORES)
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get())
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get())
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get())
                .add(ModBlocks.PHOSPHATE_ORE.get())
                .add(ModBlocks.DEEPSLATE_PHOSPHATE_ORE.get());

        tag(common("ores/sulfur"))
                .add(ModBlocks.SULFUR_ORE.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE.get());
        tag(common("ores/platinum"))
                .add(ModBlocks.PLATINUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE.get());
        tag(common("ores/aluminium"))
                .add(ModBlocks.ALUMINIUM_ORE.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get());
        tag(common("ores/phosphate"))
                .add(ModBlocks.PHOSPHATE_ORE.get())
                .add(ModBlocks.DEEPSLATE_PHOSPHATE_ORE.get());

        // c:storage_blocks per-material sub-tags.
        tag(common("storage_blocks/platinum")).add(ModBlocks.PLATINUM_BLOCK.get());
        tag(common("storage_blocks/raw_platinum")).add(ModBlocks.RAW_PLATINUM_BLOCK.get());
        tag(common("storage_blocks/aluminium")).add(ModBlocks.ALUMINIUM_BLOCK.get());
        tag(common("storage_blocks/raw_aluminium")).add(ModBlocks.RAW_ALUMINIUM_BLOCK.get());
    }

    @Override
    public String getName() {
        return "MyDrugs Block Tags";
    }
}
