package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.items.ModItems;

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
            dropSelf(ModBlocks.PSY_ANVIL.get());
            dropSelf(ModBlocks.PSYCHEDELIC_MYCELIUM.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM_BLOCK.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM_STEM.get());

            add(ModBlocks.SALT_BLOCK.get(),
                    block -> oreDropWithCount(block, ModItems.SALT_POWDER.get(), 2.0F, 5.0F));

            add(
                    ModBlocks.SULFUR_ORE.get(),
                    block -> oreDropWithCount(block, ModItems.SULFUR_POWDER.get(), 2.0F, 5.0F)
            );

            add(
                    ModBlocks.DEEPSLATE_SULFUR_ORE.get(),
                    block -> oreDropWithCount(block, ModItems.SULFUR_POWDER.get(), 2.0F, 5.0F)
            );

            add(
                    ModBlocks.PLATINUM_ORE.get(),
                    block -> createOreDrop(block, ModItems.RAW_PLATINUM.get())
            );

            add(
                    ModBlocks.DEEPSLATE_PLATINUM_ORE.get(),
                    block -> createOreDrop(block, ModItems.RAW_PLATINUM.get())
            );

            add(
                    ModBlocks.ALUMINIUM_ORE.get(),
                    block -> createOreDrop(block, ModItems.RAW_ALUMINIUM.get())
            );

            add(
                    ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get(),
                    block -> createOreDrop(block, ModItems.RAW_ALUMINIUM.get())
            );

            dropSelf(ModBlocks.RAW_PLATINUM_BLOCK.get());
            dropSelf(ModBlocks.PLATINUM_BLOCK.get());
            dropSelf(ModBlocks.RAW_ALUMINIUM_BLOCK.get());
            dropSelf(ModBlocks.ALUMINIUM_BLOCK.get());
        }

        private LootTable.Builder oreDropWithCount(Block block, ItemLike drop, float min, float max) {
            var fortune = this.registries
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.FORTUNE);

            return createSilkTouchDispatchTable(
                    block,
                    applyExplosionDecay(
                            block,
                            LootItem.lootTableItem(drop)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)))
                                    .apply(ApplyBonusCount.addOreBonusCount(fortune))
                    )
            );
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    ModBlocks.ADVANCED_FURNACE.get(),
                    ModBlocks.MIXING_VAT.get(),
                    ModBlocks.GRINDING_BOWL.get(),
                    ModBlocks.STOMP_CRAFTER.get(),
                    ModBlocks.PSY_ANVIL.get(),
                    ModBlocks.PSYCHEDELIC_MYCELIUM.get(),
                    ModBlocks.MAGIC_MUSHROOM_BLOCK.get(),
                    ModBlocks.MAGIC_MUSHROOM_STEM.get(),

                    ModBlocks.SALT_BLOCK.get(),

                    ModBlocks.SULFUR_ORE.get(),
                    ModBlocks.DEEPSLATE_SULFUR_ORE.get(),

                    ModBlocks.PLATINUM_ORE.get(),
                    ModBlocks.DEEPSLATE_PLATINUM_ORE.get(),
                    ModBlocks.ALUMINIUM_ORE.get(),
                    ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get(),
                    ModBlocks.RAW_PLATINUM_BLOCK.get(),
                    ModBlocks.PLATINUM_BLOCK.get(),
                    ModBlocks.RAW_ALUMINIUM_BLOCK.get(),
                    ModBlocks.ALUMINIUM_BLOCK.get()
            );
        }
    }
}
