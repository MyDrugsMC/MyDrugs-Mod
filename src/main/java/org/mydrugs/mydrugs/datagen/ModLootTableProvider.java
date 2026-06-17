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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.AlternativesEntry;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.AloeVeraBushBlock;
import org.mydrugs.mydrugs.blocks.crops.CannabisCropBlock;
import org.mydrugs.mydrugs.blocks.crops.CoffeeCropBlock;
import org.mydrugs.mydrugs.blocks.crops.MaltCropBlock;
import org.mydrugs.mydrugs.blocks.crops.ModCrops;
import org.mydrugs.mydrugs.blocks.crops.RyeCropBlock;
import org.mydrugs.mydrugs.dimension.ModInnerDimensionBlocks;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModLootTableProvider extends LootTableProvider {
    public ModLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(
                output,
                Set.of(),
                List.of(
                        new SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK),
                        new SubProviderEntry(ModChestLoot::new, LootContextParamSets.CHEST)
                ),
                lookupProvider
        );
    }

    /** Inner dimension memory-vault chests (B1): three tiers keyed by region danger. */
    private record ModChestLoot(HolderLookup.Provider registries)
            implements net.minecraft.data.loot.LootTableSubProvider {
        @Override
        public void generate(java.util.function.BiConsumer<net.minecraft.resources.ResourceKey<LootTable>, LootTable.Builder> output) {
            output.accept(org.mydrugs.mydrugs.dimension.inner.InnerVaults.VAULT_CALM, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(3.0F, 5.0F))
                            .add(LootItem.lootTableItem(ModItems.PSYCHOTROPIC_PIGMENT.get()).setWeight(8)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                            .add(LootItem.lootTableItem(ModItems.CANNABIS_LEAF.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 6.0F))))
                            .add(LootItem.lootTableItem(ModItems.DRIED_TOBACCO_LEAF.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 6.0F))))
                            .add(LootItem.lootTableItem(ModItems.MALT.get()).setWeight(5)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                            .add(LootItem.lootTableItem(net.minecraft.world.item.Items.AMETHYST_SHARD).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                            .add(LootItem.lootTableItem(ModItems.PSYCHOTROPE_LENS.get()).setWeight(1)))
            );
            output.accept(org.mydrugs.mydrugs.dimension.inner.InnerVaults.VAULT_DEEP, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(3.0F, 6.0F))
                            .add(LootItem.lootTableItem(ModItems.ERGOT.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                            .add(LootItem.lootTableItem(ModItems.FUNGAL_CULTURE.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                            .add(LootItem.lootTableItem(ModItems.MAGIC_MUSHROOM_POWDER.get()).setWeight(5)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 4.0F))))
                            .add(LootItem.lootTableItem(ModItems.MYCELIAL_INSIGHT.get()).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                            .add(LootItem.lootTableItem(ModItems.PSYCHOTROPIC_PIGMENT.get()).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                            .add(LootItem.lootTableItem(ModItems.LSD_DROP.get()).setWeight(2))
                            .add(LootItem.lootTableItem(ModItems.PSYCHOTROPE_LENS.get()).setWeight(2)))
            );
            output.accept(org.mydrugs.mydrugs.dimension.inner.InnerVaults.VAULT_DANGER, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(4.0F, 6.0F))
                            .add(LootItem.lootTableItem(ModItems.METH_SHARD.get()).setWeight(5)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                            .add(LootItem.lootTableItem(net.minecraft.world.item.Items.REDSTONE).setWeight(5)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 8.0F))))
                            .add(LootItem.lootTableItem(ModItems.PSYCHOTROPIC_PIGMENT.get()).setWeight(4)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F))))
                            .add(LootItem.lootTableItem(net.minecraft.world.item.Items.OBSIDIAN).setWeight(3)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                            .add(LootItem.lootTableItem(ModItems.PSYCHOTROPE_LENS.get()).setWeight(3))
                            .add(LootItem.lootTableItem(ModItems.MYCELIAL_INSIGHT.get()).setWeight(2)))
            );
            output.accept(org.mydrugs.mydrugs.dimension.inner.InnerSkyShardLootBuilder.SKY_SHRINE, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(2.0F, 4.0F))
                            .add(LootItem.lootTableItem(ModItems.DREAM_RESIDUE.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                            .add(LootItem.lootTableItem(ModItems.MYCELIAL_INSIGHT.get()).setWeight(5)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.DREAM_ORCHID.get()).setWeight(4))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.PRISM_LOTUS.get()).setWeight(3))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.SPORE_BLOOM.get()).setWeight(3))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get()).setWeight(1)))
            );
            output.accept(org.mydrugs.mydrugs.dimension.inner.InnerSpiralCourtBuilder.SPIRAL_COURT_REWARD, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .setRolls(UniformGenerator.between(7.0F, 9.0F))
                            .add(LootItem.lootTableItem(ModItems.DREAM_RESIDUE.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 5.0F))))
                            .add(LootItem.lootTableItem(ModItems.MYCELIAL_INSIGHT.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                            .add(LootItem.lootTableItem(ModItems.PRESSED_CALM.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                            .add(LootItem.lootTableItem(ModItems.FERMENTED_MEMORY.get()).setWeight(6)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 4.0F))))
                            .add(LootItem.lootTableItem(ModItems.CALMING_SPORES.get()).setWeight(5)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 6.0F))))
                            .add(LootItem.lootTableItem(ModItems.PRISTINE_INTEGRATION_CORE.get()).setWeight(1))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.LUCID_ECHO_NODE.get()).setWeight(2))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.DREAM_RESIDUE_GEODE.get()).setWeight(2))
                            .add(LootItem.lootTableItem(ModInnerDimensionBlocks.MYCELIAL_INSIGHT_NODE.get()).setWeight(2)))
            );
        }
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
            dropSelf(ModBlocks.AUTOCLAVE.get());
            dropSelf(ModBlocks.BASIC_FLUID_PIPE.get());
            dropSelf(ModBlocks.BASIC_GAS_PIPE.get());
            dropSelf(ModBlocks.BASIC_ITEM_PIPE.get());
            dropSelf(ModBlocks.BACTERIAL_INCUBATOR.get());
            dropSelf(ModBlocks.BIOCHEMICAL_REACTOR.get());
            dropSelf(ModBlocks.BTX_FRACTIONATION_TOWER.get());
            dropSelf(ModBlocks.CATALYTIC_REFORMER.get());
            dropSelf(ModBlocks.CENTRIFUGE.get());
            dropSelf(ModBlocks.CHEMICAL_REACTOR.get());
            dropSelf(ModBlocks.CLAY_VAT.get());
            dropSelf(ModBlocks.DISTILLER.get());
            dropSelf(ModBlocks.PSYCHOTROPE_DISTILLERY.get());
            dropSelf(ModBlocks.DISTILLATE_ENGINE.get());
            dropSelf(ModBlocks.PSYCHOTROPE_RESONATOR.get());
            dropSelf(ModBlocks.DRYING_RACK.get());
            dropSelf(ModBlocks.ELECTROLYZER.get());
            dropSelf(ModBlocks.FAST_FLUID_PIPE.get());
            dropSelf(ModBlocks.FAST_GAS_PIPE.get());
            dropSelf(ModBlocks.FAST_ITEM_PIPE.get());
            dropSelf(ModBlocks.FLUID_FILTERER.get());
            dropSelf(ModBlocks.FLUID_PUMP.get());
            dropSelf(ModBlocks.GASIFIER.get());
            dropSelf(ModBlocks.GAS_PUMP.get());
            dropSelf(ModBlocks.GENE_EXTRACTOR.get());
            dropSelf(ModBlocks.HEMOGENIC_INFUSER.get());
            dropSelf(ModBlocks.GROWTH_CHAMBER.get());
            dropSelf(ModBlocks.CRISPR_CAS9_COMBINATOR.get());
            dropSelf(ModBlocks.MIXING_VAT.get());
            dropSelf(ModBlocks.GRINDING_BOWL.get());
            dropSelf(ModBlocks.REDUCTION_STILL.get());
            dropSelf(ModBlocks.MECHANICAL_FRAME.get());
            dropSelf(ModBlocks.RECOVERY_ANCHOR.get());
            dropSelf(ModBlocks.DISC_SCRIBER.get());
            dropSelf(ModBlocks.RECOVERY_JUKEBOX.get());
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
            dropSelf(ModBlocks.BITTER_NUT_BUSH.get());
            dropSelf(ModBlocks.BREATH_GRASS.get());
            dropSelf(ModBlocks.CALMING_FERN.get());
            dropSelf(ModBlocks.MEMORY_REEDS.get());
            dropSelf(ModBlocks.REDLINE_THORN.get());
            dropSelf(ModBlocks.MYCELIAL_ROOT.get());
            dropSelf(ModInnerDimensionBlocks.LUCID_CLOVER.get());
            dropSelf(ModInnerDimensionBlocks.ASH_GRASS.get());
            dropSelf(ModInnerDimensionBlocks.MOSS_BREATH_CARPET.get());
            dropSelf(ModInnerDimensionBlocks.QUARTZ_NEEDLEGRASS.get());
            dropSelf(ModInnerDimensionBlocks.MYCELIAL_THREADS.get());
            dropSelf(ModInnerDimensionBlocks.DREAM_ORCHID.get());
            dropSelf(ModInnerDimensionBlocks.SPORE_BLOOM.get());
            dropSelf(ModInnerDimensionBlocks.BITTER_SPROUT.get());
            dropSelf(ModInnerDimensionBlocks.REDLINE_SPARK_BLOOM.get());
            dropSelf(ModInnerDimensionBlocks.CALMING_BUSH.get());
            dropSelf(ModInnerDimensionBlocks.MEMORY_SEDGE.get());
            dropSelf(ModInnerDimensionBlocks.REDLINE_BRAMBLE.get());
            dropSelf(ModInnerDimensionBlocks.CRYSTAL_SHRUB.get());
            dropSelf(ModInnerDimensionBlocks.FERMENTED_SHRUB.get());
            dropSelf(ModInnerDimensionBlocks.MUD_REEDS.get());
            dropSelf(ModInnerDimensionBlocks.MEMORY_LOTUS.get());
            dropSelf(ModInnerDimensionBlocks.BREATH_LILY.get());
            dropSelf(ModInnerDimensionBlocks.PRISM_LOTUS.get());
            aloeBush(ModBlocks.ALOE_VERA_BUSH.get());

            crop(ModCrops.ALOE_VERA_CROP.get(), ModItems.ALOE_VERA.get(), ModCrops.ALOE_VERA_SEEDS.get());
            crop(ModCrops.TOBACCO_CROP.get(), ModItems.TOBACCO_LEAF.get(), ModCrops.TOBACCO_SEEDS.get());
            crop(ModCrops.COCA_CROP.get(), ModItems.COCA_LEAF.get(), ModCrops.COCA_SEEDS.get());
            crop(ModCrops.OPIUM_POPPY_CROP.get(), ModCrops.OPIUM_POPPY_SEEDS.get(), ModCrops.OPIUM_POPPY_SEEDS.get());
            crop(ModCrops.LAVENDER_CROP.get(), ModItems.LAVENDER.get(), ModCrops.LAVENDER_SEEDS.get());
            crop(ModCrops.VALERIAN_CROP.get(), ModItems.VALERIAN_ROOT.get(), ModCrops.VALERIAN_SEEDS.get());
            crop(ModCrops.EPHEDRA_CROP.get(), ModCrops.EPHEDRA_CUTTINGS.get(), ModCrops.EPHEDRA_CUTTINGS.get());
            tallCrop(ModCrops.CANNABIS_CROP.get(), ModItems.CANNABIS_LEAF.get(), ModCrops.CANNABIS_SEEDS.get(), CannabisCropBlock.HALF);
            tallCrop(ModCrops.COFFEE_CROP.get(), ModItems.COFFEE_CHERRIES.get(), ModCrops.COFFEE_SEEDS.get(), CoffeeCropBlock.HALF);
            tallCrop(ModCrops.MALT_CROP.get(), ModItems.MALT.get(), ModCrops.MALT_SEEDS.get(), MaltCropBlock.HALF);
            tallCrop(ModCrops.RYE_CROP.get(), ModItems.RYE.get(), ModCrops.RYE_SEEDS.get(), RyeCropBlock.HALF);

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

            add(
                    ModBlocks.PHOSPHATE_ORE.get(),
                    block -> createOreDrop(block, ModItems.RAW_PHOSPHORUS.get())
            );

            add(
                    ModBlocks.DEEPSLATE_PHOSPHATE_ORE.get(),
                    block -> createOreDrop(block, ModItems.RAW_PHOSPHORUS.get())
            );

            add(ModBlocks.LUCID_ECHO_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.LUCID_EXTRACT.get()));
            add(ModBlocks.BITTER_ECHO_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.BITTER_RESIDUE.get()));
            add(ModBlocks.CALMING_ECHO_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.CALMING_RESIN.get()));
            add(ModBlocks.PRESSED_CALM_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.PRESSED_CALM.get()));
            add(ModBlocks.FERMENTED_MEMORY_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.FERMENTED_MEMORY.get()));
            add(ModBlocks.REDLINE_CRYSTAL_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.REDLINE_FUEL.get()));
            add(ModBlocks.DREAM_RESIDUE_GEODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.DREAM_RESIDUE.get()));
            add(ModBlocks.OVERDRIVE_SLAG.get(),
                    block -> symbolicNodeDrop(block, ModItems.OVERDRIVE_FUEL.get()));
            add(ModBlocks.MYCELIAL_INSIGHT_NODE.get(),
                    block -> symbolicNodeDrop(block, ModItems.MYCELIAL_INSIGHT.get()));

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

        private LootTable.Builder symbolicNodeDrop(Block block, ItemLike drop) {
            return applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(drop)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F))))));
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

        private void aloeBush(Block block) {
            add(block, applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(ModItems.ALOE_VERA.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(AloeVeraBushBlock.AGE, AloeVeraBushBlock.MAX_AGE)))
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))))
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(ModCrops.ALOE_VERA_SEEDS.get())
                                    .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(AloeVeraBushBlock.AGE, AloeVeraBushBlock.MAX_AGE)))))));
        }

        private void tallCrop(Block block, Item crop, Item seeds, EnumProperty<DoubleBlockHalf> halfProperty) {
            add(block, cropDrops(
                    block,
                    crop,
                    seeds,
                    () -> LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(CropBlock.AGE, 7)),
                    () -> LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(halfProperty, DoubleBlockHalf.LOWER))
            ));
        }

        private LootTable.Builder cropDrops(
                Block block,
                ItemLike crop,
                ItemLike seeds,
                Supplier<LootItemCondition.Builder> matureCondition,
                Supplier<LootItemCondition.Builder> lowerHalfCondition
        ) {
            var fortune = this.registries
                    .lookupOrThrow(Registries.ENCHANTMENT)
                    .getOrThrow(Enchantments.FORTUNE);

            return applyExplosionDecay(block, LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                            .add(AlternativesEntry.alternatives(
                                    LootItem.lootTableItem(crop)
                                            .when(lowerHalfCondition.get())
                                            .when(matureCondition.get()),
                                    LootItem.lootTableItem(seeds)
                                            .when(lowerHalfCondition.get())
                            )))
                    .withPool(LootPool.lootPool()
                            .add(LootItem.lootTableItem(seeds)
                                    .when(lowerHalfCondition.get())
                                    .when(matureCondition.get())
                                    .apply(ApplyBonusCount.addBonusBinomialDistributionCount(fortune, 0.5714286F, 3)))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(
                    ModBlocks.ADVANCED_FURNACE.get(),
                    ModBlocks.ADVANCED_MIXING_VAT.get(),
                    ModBlocks.AROMATIC_EXTRACTOR.get(),
                    ModBlocks.AUTOCLAVE.get(),
                    ModBlocks.BASIC_FLUID_PIPE.get(),
                    ModBlocks.BASIC_GAS_PIPE.get(),
                    ModBlocks.BASIC_ITEM_PIPE.get(),
                    ModBlocks.BACTERIAL_INCUBATOR.get(),
                    ModBlocks.BIOCHEMICAL_REACTOR.get(),
                    ModBlocks.BTX_FRACTIONATION_TOWER.get(),
                    ModBlocks.CATALYTIC_REFORMER.get(),
                    ModBlocks.CENTRIFUGE.get(),
                    ModBlocks.CHEMICAL_REACTOR.get(),
                    ModBlocks.CLAY_VAT.get(),
                    ModBlocks.DISTILLER.get(),
                    ModBlocks.PSYCHOTROPE_DISTILLERY.get(),
                    ModBlocks.DISTILLATE_ENGINE.get(),
                    ModBlocks.PSYCHOTROPE_RESONATOR.get(),
                    ModBlocks.DRYING_RACK.get(),
                    ModBlocks.ELECTROLYZER.get(),
                    ModBlocks.FAST_FLUID_PIPE.get(),
                    ModBlocks.FAST_GAS_PIPE.get(),
                    ModBlocks.FAST_ITEM_PIPE.get(),
                    ModBlocks.FLUID_FILTERER.get(),
                    ModBlocks.FLUID_PUMP.get(),
                    ModBlocks.GASIFIER.get(),
                    ModBlocks.GAS_PUMP.get(),
                    ModBlocks.GENE_EXTRACTOR.get(),
                    ModBlocks.HEMOGENIC_INFUSER.get(),
                    ModBlocks.GROWTH_CHAMBER.get(),
                    ModBlocks.CRISPR_CAS9_COMBINATOR.get(),
                    ModBlocks.MIXING_VAT.get(),
                    ModBlocks.GRINDING_BOWL.get(),
                    ModBlocks.REDUCTION_STILL.get(),
                    ModBlocks.MECHANICAL_FRAME.get(),
                    ModBlocks.RECOVERY_ANCHOR.get(),
                    ModBlocks.DISC_SCRIBER.get(),
                    ModBlocks.RECOVERY_JUKEBOX.get(),
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
                    ModBlocks.BITTER_NUT_BUSH.get(),
                    ModBlocks.ALOE_VERA_BUSH.get(),
                    ModBlocks.LUCID_ECHO_NODE.get(),
                    ModBlocks.BITTER_ECHO_NODE.get(),
                    ModBlocks.CALMING_ECHO_NODE.get(),
                    ModBlocks.PRESSED_CALM_NODE.get(),
                    ModBlocks.FERMENTED_MEMORY_NODE.get(),
                    ModBlocks.REDLINE_CRYSTAL_NODE.get(),
                    ModBlocks.DREAM_RESIDUE_GEODE.get(),
                    ModBlocks.OVERDRIVE_SLAG.get(),
                    ModBlocks.MYCELIAL_INSIGHT_NODE.get(),
                    ModBlocks.BREATH_GRASS.get(),
                    ModBlocks.CALMING_FERN.get(),
                    ModBlocks.MEMORY_REEDS.get(),
                    ModBlocks.REDLINE_THORN.get(),
                    ModBlocks.MYCELIAL_ROOT.get(),
                    ModInnerDimensionBlocks.LUCID_CLOVER.get(),
                    ModInnerDimensionBlocks.ASH_GRASS.get(),
                    ModInnerDimensionBlocks.MOSS_BREATH_CARPET.get(),
                    ModInnerDimensionBlocks.QUARTZ_NEEDLEGRASS.get(),
                    ModInnerDimensionBlocks.MYCELIAL_THREADS.get(),
                    ModInnerDimensionBlocks.DREAM_ORCHID.get(),
                    ModInnerDimensionBlocks.SPORE_BLOOM.get(),
                    ModInnerDimensionBlocks.BITTER_SPROUT.get(),
                    ModInnerDimensionBlocks.REDLINE_SPARK_BLOOM.get(),
                    ModInnerDimensionBlocks.CALMING_BUSH.get(),
                    ModInnerDimensionBlocks.MEMORY_SEDGE.get(),
                    ModInnerDimensionBlocks.REDLINE_BRAMBLE.get(),
                    ModInnerDimensionBlocks.CRYSTAL_SHRUB.get(),
                    ModInnerDimensionBlocks.FERMENTED_SHRUB.get(),
                    ModInnerDimensionBlocks.MUD_REEDS.get(),
                    ModInnerDimensionBlocks.MEMORY_LOTUS.get(),
                    ModInnerDimensionBlocks.BREATH_LILY.get(),
                    ModInnerDimensionBlocks.PRISM_LOTUS.get(),

                    ModCrops.ALOE_VERA_CROP.get(),
                    ModCrops.TOBACCO_CROP.get(),
                    ModCrops.COCA_CROP.get(),
                    ModCrops.OPIUM_POPPY_CROP.get(),
                    ModCrops.LAVENDER_CROP.get(),
                    ModCrops.VALERIAN_CROP.get(),
                    ModCrops.CANNABIS_CROP.get(),
                    ModCrops.COFFEE_CROP.get(),
                    ModCrops.MALT_CROP.get(),
                    ModCrops.RYE_CROP.get(),
                    ModCrops.EPHEDRA_CROP.get(),

                    ModBlocks.SALT_BLOCK.get(),

                    ModBlocks.SULFUR_ORE.get(),
                    ModBlocks.DEEPSLATE_SULFUR_ORE.get(),

                    ModBlocks.PLATINUM_ORE.get(),
                    ModBlocks.DEEPSLATE_PLATINUM_ORE.get(),
                    ModBlocks.ALUMINIUM_ORE.get(),
                    ModBlocks.DEEPSLATE_ALUMINIUM_ORE.get(),
                    ModBlocks.PHOSPHATE_ORE.get(),
                    ModBlocks.DEEPSLATE_PHOSPHATE_ORE.get(),
                    ModBlocks.RAW_PLATINUM_BLOCK.get(),
                    ModBlocks.PLATINUM_BLOCK.get(),
                    ModBlocks.RAW_ALUMINIUM_BLOCK.get(),
                    ModBlocks.ALUMINIUM_BLOCK.get()
            );
        }
    }
}
