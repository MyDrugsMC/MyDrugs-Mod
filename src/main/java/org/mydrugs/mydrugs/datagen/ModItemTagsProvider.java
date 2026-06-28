package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.items.ModItemTags;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output,
                               CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, MyDrugs.MODID);
    }

    private static TagKey<Item> common(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Ingots.
        tag(common("ingots/platinum")).add(ModItems.PLATINUM_INGOT.get());
        tag(common("ingots/aluminium")).add(ModItems.ALUMINIUM_INGOT.get());
        tag(Tags.Items.INGOTS)
                .add(ModItems.PLATINUM_INGOT.get())
                .add(ModItems.ALUMINIUM_INGOT.get())
                .add(ModItems.STEEL_INGOT.get());

        // Raw materials.
        tag(common("raw_materials/platinum")).add(ModItems.RAW_PLATINUM.get());
        tag(common("raw_materials/aluminium")).add(ModItems.RAW_ALUMINIUM.get());
        tag(common("raw_materials/phosphorus")).add(ModItems.RAW_PHOSPHORUS.get());
        tag(Tags.Items.RAW_MATERIALS)
                .add(ModItems.RAW_PLATINUM.get())
                .add(ModItems.RAW_ALUMINIUM.get())
                .add(ModItems.RAW_PHOSPHORUS.get());

        // Dusts: this repo used neither c:dusts nor c:powders previously; follow NeoForge's c:dusts.
        tag(common("dusts/sulfur")).add(ModItems.SULFUR_POWDER.get());
        tag(Tags.Items.DUSTS).add(ModItems.SULFUR_POWDER.get());

        // Item mirrors of the ore block tags.
        tag(common("ores/sulfur"))
                .add(ModBlocks.SULFUR_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE_ITEM.get());
        tag(common("ores/platinum"))
                .add(ModBlocks.PLATINUM_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE_ITEM.get());
        tag(common("ores/aluminium"))
                .add(ModBlocks.ALUMINIUM_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE_ITEM.get());
        tag(common("ores/phosphate"))
                .add(ModBlocks.PHOSPHATE_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_PHOSPHATE_ORE_ITEM.get());
        tag(Tags.Items.ORES)
                .add(ModBlocks.SULFUR_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_SULFUR_ORE_ITEM.get())
                .add(ModBlocks.PLATINUM_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_PLATINUM_ORE_ITEM.get())
                .add(ModBlocks.ALUMINIUM_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_ALUMINIUM_ORE_ITEM.get())
                .add(ModBlocks.PHOSPHATE_ORE_ITEM.get())
                .add(ModBlocks.DEEPSLATE_PHOSPHATE_ORE_ITEM.get());

        // Item mirrors of the storage block tags.
        tag(common("storage_blocks/platinum")).add(ModBlocks.PLATINUM_BLOCK_ITEM.get());
        tag(common("storage_blocks/raw_platinum")).add(ModBlocks.RAW_PLATINUM_BLOCK_ITEM.get());
        tag(common("storage_blocks/aluminium")).add(ModBlocks.ALUMINIUM_BLOCK_ITEM.get());
        tag(common("storage_blocks/raw_aluminium")).add(ModBlocks.RAW_ALUMINIUM_BLOCK_ITEM.get());

        tag(ModItemTags.PSY_ANVIL_CORES)
                .add(Items.ENDER_PEARL)
                .add(ModItems.UNSTABLE_PEARL.get());
        tag(ModItemTags.PSY_MIXER_AWAKENING_CORES)
                .add(Items.ENDER_PEARL)
                .add(ModItems.UNSTABLE_PEARL.get());
        tag(ModItemTags.RESONANCE_SHARDS)
                .add(Items.AMETHYST_SHARD)
                .add(ModItems.ATTUNED_SHARD.get());
        tag(ModItemTags.RITUAL_FIBERS)
                .add(Items.VINE)
                .add(Items.STRING)
                .add(ModItems.PLANT_BIOMASS.get());
    }

    @Override
    public String getName() {
        return "MyDrugs Item Tags";
    }
}
