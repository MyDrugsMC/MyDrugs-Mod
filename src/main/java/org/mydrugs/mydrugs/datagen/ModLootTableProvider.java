package org.mydrugs.mydrugs.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.crops.ModCrops;
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
            dropSelf(ModBlocks.ADVANCED_MIXING_VAT.get());
            dropSelf(ModBlocks.AROMATIC_EXTRACTOR.get());
            dropSelf(ModBlocks.BASIC_FLUID_PIPE.get());
            dropSelf(ModBlocks.BASIC_GAS_PIPE.get());
            dropSelf(ModBlocks.BASIC_ITEM_PIPE.get());
            dropSelf(ModBlocks.BIOCHEMICAL_REACTOR.get());
            dropSelf(ModBlocks.BTX_FRACTIONATION_TOWER.get());
            dropSelf(ModBlocks.CATALYTIC_REFORMER.get());
            dropSelf(ModBlocks.CENTRIFUGE.get());
            dropSelf(ModBlocks.CHEMICAL_REACTOR.get());
            dropSelf(ModBlocks.CLAY_VAT.get());
            dropSelf(ModBlocks.DISTILLER.get());
            dropSelf(ModBlocks.DRYING_RACK.get());
            dropSelf(ModBlocks.ELECTROLYZER.get());
            dropSelf(ModBlocks.FAST_FLUID_PIPE.get());
            dropSelf(ModBlocks.FAST_GAS_PIPE.get());
            dropSelf(ModBlocks.FAST_ITEM_PIPE.get());
            dropSelf(ModBlocks.FLUID_FILTERER.get());
            dropSelf(ModBlocks.FLUID_PUMP.get());
            dropSelf(ModBlocks.GASIFIER.get());
            dropSelf(ModBlocks.GAS_PUMP.get());
            dropSelf(ModBlocks.GROWTH_CHAMBER.get());
            dropSelf(ModBlocks.MIXING_VAT.get());
            dropSelf(ModBlocks.GRINDING_BOWL.get());
            dropSelf(ModBlocks.MECHANICAL_FRAME.get());
            dropSelf(ModBlocks.RECOVERY_ANCHOR.get());
            dropSelf(ModBlocks.SIEVE.get());
            dropSelf(ModBlocks.STEAM_CRACKER.get());
            dropSelf(ModBlocks.STOMP_CRAFTER.get());
            dropSelf(ModBlocks.STOMP_PLATE_BLOCK.get());
            dropSelf(ModBlocks.THERAPIST_DESK.get());
            dropSelf(ModBlocks.TREATED_PLANKS.get());
            dropSelf(ModBlocks.PSY_ANVIL.get());
            dropSelf(ModBlocks.PSYCHEDELIC_MYCELIUM.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM_BLOCK.get());
            dropSelf(ModBlocks.MAGIC_MUSHROOM_STEM.get());
            dropSelf(ModBlocks.EVAPORATION_TRAY.get());

            crop(ModCrops.ALOE_VERA_CROP.get(), ModItems.ALOE_VERA.get(), ModItems.ALOE_VERA.get());
            crop(ModCrops.OPIUM_POPPY_CROP.get(), ModCrops.OPIUM_POPPY_SEEDS.get(), ModCrops.OPIUM_POPPY_SEEDS.get());

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

        private void crop(Block block, Item crop, Item seeds) {
            add(block, createCropDrops(
                    block,
                    crop,
                    seeds,
                    LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7))
            ));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    ModBlocks.ADVANCED_FURNACE.get(),
                    ModBlocks.ADVANCED_MIXING_VAT.get(),
                    ModBlocks.AROMATIC_EXTRACTOR.get(),
                    ModBlocks.BASIC_FLUID_PIPE.get(),
                    ModBlocks.BASIC_GAS_PIPE.get(),
                    ModBlocks.BASIC_ITEM_PIPE.get(),
                    ModBlocks.BIOCHEMICAL_REACTOR.get(),
                    ModBlocks.BTX_FRACTIONATION_TOWER.get(),
                    ModBlocks.CATALYTIC_REFORMER.get(),
                    ModBlocks.CENTRIFUGE.get(),
                    ModBlocks.CHEMICAL_REACTOR.get(),
                    ModBlocks.CLAY_VAT.get(),
                    ModBlocks.DISTILLER.get(),
                    ModBlocks.DRYING_RACK.get(),
                    ModBlocks.ELECTROLYZER.get(),
                    ModBlocks.FAST_FLUID_PIPE.get(),
                    ModBlocks.FAST_GAS_PIPE.get(),
                    ModBlocks.FAST_ITEM_PIPE.get(),
                    ModBlocks.FLUID_FILTERER.get(),
                    ModBlocks.FLUID_PUMP.get(),
                    ModBlocks.GASIFIER.get(),
                    ModBlocks.GAS_PUMP.get(),
                    ModBlocks.GROWTH_CHAMBER.get(),
                    ModBlocks.MIXING_VAT.get(),
                    ModBlocks.GRINDING_BOWL.get(),
                    ModBlocks.MECHANICAL_FRAME.get(),
                    ModBlocks.RECOVERY_ANCHOR.get(),
                    ModBlocks.SIEVE.get(),
                    ModBlocks.STEAM_CRACKER.get(),
                    ModBlocks.STOMP_CRAFTER.get(),
                    ModBlocks.STOMP_PLATE_BLOCK.get(),
                    ModBlocks.THERAPIST_DESK.get(),
                    ModBlocks.TREATED_PLANKS.get(),
                    ModBlocks.PSY_ANVIL.get(),
                    ModBlocks.PSYCHEDELIC_MYCELIUM.get(),
                    ModBlocks.MAGIC_MUSHROOM.get(),
                    ModBlocks.MAGIC_MUSHROOM_BLOCK.get(),
                    ModBlocks.MAGIC_MUSHROOM_STEM.get(),
                    ModBlocks.EVAPORATION_TRAY.get(),

                    ModCrops.ALOE_VERA_CROP.get(),
                    ModCrops.OPIUM_POPPY_CROP.get(),

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
