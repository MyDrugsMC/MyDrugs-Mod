package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import org.mydrugs.mydrugs.blocks.ModBlocks;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(
                output,
                Set.of(),
                List.of(new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)),
                lookupProvider
        );
    }

    private static class ModBlockLoot extends BlockLootSubProvider {
        protected ModBlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.ADVANCED_FURNACE.get());
            dropSelf(ModBlocks.MIXING_VAT.get());
            dropSelf(ModBlocks.GRINDING_BOWL.get());
            dropSelf(ModBlocks.STOMP_CRAFTER.get());
            dropSelf(ModBlocks.PSYCHEDELIC_GRASS_BLOCK.get());
            dropSelf(ModBlocks.PSYCHEDELIC_MYCELIUM.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM_BLOCK.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM_STEM.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    ModBlocks.ADVANCED_FURNACE.get(),
                    ModBlocks.MIXING_VAT.get(),
                    ModBlocks.GRINDING_BOWL.get(),
                    ModBlocks.STOMP_CRAFTER.get(),
                    ModBlocks.PSYCHEDELIC_GRASS_BLOCK.get(),
                    ModBlocks.PSYCHEDELIC_MYCELIUM.get(),
                    ModBlocks.MAGIC_MUSHROOM_BLOCK.get(),
                    ModBlocks.MAGIC_MUSHROOM_STEM.get()
            );
        }
    }
}