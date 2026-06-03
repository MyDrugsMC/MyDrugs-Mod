package org.mydrugs.mydrugs.blocks;

import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;

/**
 * Intentional descriptor table for machines that already have systems. Acts as a foundation for
 * future polish and as the source of truth for the machine-consistency audit ({@link #validateAll()}).
 *
 * <p>Holders left {@code null} encode a deliberate absence (no menu / no recipe), so audits can
 * separate intentional gaps from genuine wiring bugs.</p>
 */
public final class ModMachineContent {
    private ModMachineContent() {
    }

    private static MachineContentDescriptor m(
            String id,
            java.util.function.Supplier<? extends net.minecraft.world.level.block.Block> block,
            java.util.function.Supplier<? extends net.minecraft.world.level.block.entity.BlockEntityType<?>> blockEntity,
            java.util.function.Supplier<? extends net.minecraft.world.inventory.MenuType<?>> menu,
            net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.crafting.RecipeType<?>, ? extends net.minecraft.world.item.crafting.RecipeType<?>> recipeType,
            net.neoforged.neoforge.registries.DeferredHolder<net.minecraft.world.item.crafting.RecipeSerializer<?>, ? extends net.minecraft.world.item.crafting.RecipeSerializer<?>> recipeSerializer) {
        return new MachineContentDescriptor(id, block, blockEntity, menu, recipeType, recipeSerializer,
                "block.mydrugs." + id);
    }

    public static final List<MachineContentDescriptor> DESCRIPTORS = List.of(
            m("advanced_furnace", ModBlocks.ADVANCED_FURNACE, ModBlockEntities.ADVANCED_FURNACE, ModMenus.ADVANCED_FURNACE, ModRecipeTypes.ADVANCED_FURNACE, ModRecipeSerializers.ADVANCED_FURNACE),
            m("distiller", ModBlocks.DISTILLER, ModBlockEntities.DISTILLER, ModMenus.DISTILLER, ModRecipeTypes.DISTILLER, ModRecipeSerializers.DISTILLER),
            // Mixing vat has a recipe type but no dedicated menu (interaction-driven).
            m("mixing_vat", ModBlocks.MIXING_VAT, ModBlockEntities.MIXING_VAT, null, ModRecipeTypes.MIXING_VAT, ModRecipeSerializers.MIXING_VAT),
            m("sieve", ModBlocks.SIEVE, ModBlockEntities.SIEVE, ModMenus.SIEVE, ModRecipeTypes.SIEVING, ModRecipeSerializers.SIEVING),
            m("fluid_filterer", ModBlocks.FLUID_FILTERER, ModBlockEntities.FLUID_FILTERER, ModMenus.FLUID_FILTERER, ModRecipeTypes.FLUID_FILTERING, ModRecipeSerializers.FLUID_FILTERING),
            // Evaporation tray is passive: recipe type, no menu.
            m("evaporation_tray", ModBlocks.EVAPORATION_TRAY, ModBlockEntities.EVAPORATION_TRAY, null, ModRecipeTypes.EVAPORATION_TRAY, ModRecipeSerializers.EVAPORATION_TRAY),
            m("centrifuge", ModBlocks.CENTRIFUGE, ModBlockEntities.CENTRIFUGE, ModMenus.CENTRIFUGE, ModRecipeTypes.CENTRIFUGE, ModRecipeSerializers.CENTRIFUGE),
            // BTX tower has a menu but no dedicated recipe type yet.
            m("btx_fractionation_tower", ModBlocks.BTX_FRACTIONATION_TOWER, ModBlockEntities.BTX_FRACTIONATION_TOWER, ModMenus.BTX_FRACTIONATION_TOWER, null, null),
            m("aromatic_extractor", ModBlocks.AROMATIC_EXTRACTOR, ModBlockEntities.AROMATIC_EXTRACTOR, ModMenus.AROMATIC_EXTRACTOR, ModRecipeTypes.AROMATIC_EXTRACTOR, ModRecipeSerializers.AROMATIC_EXTRACTOR),
            m("electrolyzer", ModBlocks.ELECTROLYZER, ModBlockEntities.ELECTROLYZER, ModMenus.ELECTROLYZER, ModRecipeTypes.ELECTROLYZER, ModRecipeSerializers.ELECTROLYZER),
            m("growth_chamber", ModBlocks.GROWTH_CHAMBER, ModBlockEntities.GROWTH_CHAMBER, ModMenus.GROWTH_CHAMBER, ModRecipeTypes.GROWTH_CHAMBER, ModRecipeSerializers.GROWTH_CHAMBER),
            // Gene/biotech machines have menus but no dedicated recipe type.
            m("gene_extractor", ModBlocks.GENE_EXTRACTOR, ModBlockEntities.GENE_EXTRACTOR, ModMenus.GENE_EXTRACTOR, null, null),
            m("crispr_cas9_combinator", ModBlocks.CRISPR_CAS9_COMBINATOR, ModBlockEntities.CRISPR_CAS9_COMBINATOR, ModMenus.CRISPR_CAS9_COMBINATOR, null, null),
            m("bacterial_incubator", ModBlocks.BACTERIAL_INCUBATOR, ModBlockEntities.BACTERIAL_INCUBATOR, ModMenus.BACTERIAL_INCUBATOR, null, null),
            m("hemogenic_infuser", ModBlocks.HEMOGENIC_INFUSER, ModBlockEntities.HEMOGENIC_INFUSER, ModMenus.HEMOGENIC_INFUSER, null, null),
            m("autoclave", ModBlocks.AUTOCLAVE, ModBlockEntities.AUTOCLAVE, ModMenus.AUTOCLAVE, null, null),
            m("biochemical_reactor", ModBlocks.BIOCHEMICAL_REACTOR, ModBlockEntities.BIOCHEMICAL_REACTOR, ModMenus.BIOCHEMICAL_REACTOR, ModRecipeTypes.BIOCHEMICAL_REACTOR, ModRecipeSerializers.BIOCHEMICAL_REACTOR),
            m("gasifier", ModBlocks.GASIFIER, ModBlockEntities.GASIFIER, ModMenus.GASIFIER, ModRecipeTypes.GASIFIER, ModRecipeSerializers.GASIFIER),
            m("chemical_reactor", ModBlocks.CHEMICAL_REACTOR, ModBlockEntities.CHEMICAL_REACTOR, ModMenus.CHEMICAL_REACTOR, ModRecipeTypes.CHEMICAL_REACTOR, ModRecipeSerializers.CHEMICAL_REACTOR),
            m("advanced_mixing_vat", ModBlocks.ADVANCED_MIXING_VAT, ModBlockEntities.ADVANCED_MIXING_VAT_BE, ModMenus.ADVANCED_MIXING_VAT, ModRecipeTypes.ADVANCED_MIXING_VAT, ModRecipeSerializers.ADVANCED_MIXING_VAT),
            m("catalytic_reformer", ModBlocks.CATALYTIC_REFORMER, ModBlockEntities.CATALYTIC_REFORMER, ModMenus.CATALYTIC_REFORMER, ModRecipeTypes.CATALYTIC_REFORMER, ModRecipeSerializers.CATALYTIC_REFORMER),
            m("steam_cracker", ModBlocks.STEAM_CRACKER, ModBlockEntities.STEAM_CRACKER, ModMenus.STEAM_CRACKER, ModRecipeTypes.STEAM_CRACKER, ModRecipeSerializers.STEAM_CRACKER),
            m("psychotrope_distillery", ModBlocks.PSYCHOTROPE_DISTILLERY, ModBlockEntities.PSYCHOTROPE_DISTILLERY, ModMenus.PSYCHOTROPE_DISTILLERY, ModRecipeTypes.PSYCHOTROPE_DISTILLERY, ModRecipeSerializers.PSYCHOTROPE_DISTILLERY),
            // Psy anvil drives its recipe without a screen menu.
            m("psy_anvil", ModBlocks.PSY_ANVIL, ModBlockEntities.PSY_ANVIL, null, ModRecipeTypes.PSY_ANVIL, ModRecipeSerializers.PSY_ANVIL),
            // Psy mixer is a multiblock formed from the core block.
            new MachineContentDescriptor("psy_mixer", ModBlocks.FORMED_PSY_MIXER_CORE, ModBlockEntities.FORMED_PSY_MIXER_CORE,
                    ModMenus.PSY_MIXER, ModRecipeTypes.PSY_MIXER, ModRecipeSerializers.PSY_MIXER,
                    "block.mydrugs.formed_psy_mixer_core")
    );

    /**
     * Runtime audit: confirms each machine's expected systems actually resolve, and that any recipe
     * type/serializer pair shares an id. Returns human-readable problems; empty means consistent.
     */
    public static List<String> validateAll() {
        List<String> issues = new ArrayList<>();
        for (MachineContentDescriptor d : DESCRIPTORS) {
            if (d.block().get() == null) {
                issues.add(d.id() + ": block missing");
            }
            if (d.expectsBlockEntity() && d.blockEntity().get() == null) {
                issues.add(d.id() + ": block entity expected but missing");
            }
            if (d.expectsMenu() && d.menu().get() == null) {
                issues.add(d.id() + ": menu expected but missing");
            }
            if (d.expectsRecipe()) {
                if (d.recipeType().get() == null) {
                    issues.add(d.id() + ": recipe type expected but missing");
                }
                if (d.recipeSerializer() == null || d.recipeSerializer().get() == null) {
                    issues.add(d.id() + ": recipe serializer expected but missing");
                } else if (!d.recipeType().getId().equals(d.recipeSerializer().getId())) {
                    issues.add(d.id() + ": recipe type id " + d.recipeType().getId()
                            + " != serializer id " + d.recipeSerializer().getId());
                }
            }
        }
        return issues;
    }
}
