package org.mydrugs.mydrugs.recipes;

import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Intentional, auditable description of the mod's recipe systems: each recipe type paired with its
 * serializer and (optionally) its registered {@link RecipeDisplay} type.
 *
 * <p>This makes recipe-registry consistency explicit rather than accidental. {@link #validateAll()}
 * can be called at runtime (e.g. during common setup) to surface mismatches; bootstrap-free unit
 * tests assert the same invariants by scanning the registration sources.</p>
 */
public final class ModRecipeContent {
    private ModRecipeContent() {
    }

    public record RecipeContentEntry(
            String id,
            DeferredHolder<RecipeType<?>, ? extends RecipeType<?>> type,
            DeferredHolder<RecipeSerializer<?>, ? extends RecipeSerializer<?>> serializer,
            @Nullable Supplier<? extends RecipeDisplay.Type<?>> display
    ) {
        public boolean hasDisplay() {
            return display != null;
        }
    }

    private static RecipeContentEntry of(
            String id,
            DeferredHolder<RecipeType<?>, ? extends RecipeType<?>> type,
            DeferredHolder<RecipeSerializer<?>, ? extends RecipeSerializer<?>> serializer) {
        return new RecipeContentEntry(id, type, serializer, null);
    }

    private static RecipeContentEntry of(
            String id,
            DeferredHolder<RecipeType<?>, ? extends RecipeType<?>> type,
            DeferredHolder<RecipeSerializer<?>, ? extends RecipeSerializer<?>> serializer,
            Supplier<? extends RecipeDisplay.Type<?>> display) {
        return new RecipeContentEntry(id, type, serializer, display);
    }

    public static final List<RecipeContentEntry> ENTRIES = List.of(
            of("grinding", ModRecipeTypes.GRINDING, ModRecipeSerializers.GRINDING),
            of("coffee_pulping", ModRecipeTypes.COFFEE_PULPING, ModRecipeSerializers.COFFEE_PULPING),
            of("reduction_still", ModRecipeTypes.REDUCTION_STILL, ModRecipeSerializers.REDUCTION_STILL),
            of("stomp_crafting", ModRecipeTypes.STOMP_CRAFTING, ModRecipeSerializers.STOMP_CRAFTING),
            of("advanced_furnace", ModRecipeTypes.ADVANCED_FURNACE, ModRecipeSerializers.ADVANCED_FURNACE),
            of("mixing_vat", ModRecipeTypes.MIXING_VAT, ModRecipeSerializers.MIXING_VAT),
            of("distiller", ModRecipeTypes.DISTILLER, ModRecipeSerializers.DISTILLER, ModRecipeDisplays.DISTILLER),
            of("psychotrope_distillery", ModRecipeTypes.PSYCHOTROPE_DISTILLERY, ModRecipeSerializers.PSYCHOTROPE_DISTILLERY),
            of("drying", ModRecipeTypes.DRYING, ModRecipeSerializers.DRYING),
            of("sieving", ModRecipeTypes.SIEVING, ModRecipeSerializers.SIEVING),
            of("fluid_filtering", ModRecipeTypes.FLUID_FILTERING, ModRecipeSerializers.FLUID_FILTERING),
            of("evaporation_tray", ModRecipeTypes.EVAPORATION_TRAY, ModRecipeSerializers.EVAPORATION_TRAY),
            of("centrifuge", ModRecipeTypes.CENTRIFUGE, ModRecipeSerializers.CENTRIFUGE),
            of("electrolyzer", ModRecipeTypes.ELECTROLYZER, ModRecipeSerializers.ELECTROLYZER),
            of("growth_chamber", ModRecipeTypes.GROWTH_CHAMBER, ModRecipeSerializers.GROWTH_CHAMBER),
            of("biochemical_reactor", ModRecipeTypes.BIOCHEMICAL_REACTOR, ModRecipeSerializers.BIOCHEMICAL_REACTOR),
            of("chemical_reactor", ModRecipeTypes.CHEMICAL_REACTOR, ModRecipeSerializers.CHEMICAL_REACTOR),
            of("btx_fractionation", ModRecipeTypes.BTX_FRACTIONATION, ModRecipeSerializers.BTX_FRACTIONATION),
            of("gasifier", ModRecipeTypes.GASIFIER, ModRecipeSerializers.GASIFIER),
            of("advanced_mixing_vat", ModRecipeTypes.ADVANCED_MIXING_VAT, ModRecipeSerializers.ADVANCED_MIXING_VAT),
            of("catalytic_reformer", ModRecipeTypes.CATALYTIC_REFORMER, ModRecipeSerializers.CATALYTIC_REFORMER),
            of("steam_cracker", ModRecipeTypes.STEAM_CRACKER, ModRecipeSerializers.STEAM_CRACKER),
            of("aromatic_extractor", ModRecipeTypes.AROMATIC_EXTRACTOR, ModRecipeSerializers.AROMATIC_EXTRACTOR),
            of("psy_anvil", ModRecipeTypes.PSY_ANVIL, ModRecipeSerializers.PSY_ANVIL),
            of("psy_mixer", ModRecipeTypes.PSY_MIXER, ModRecipeSerializers.PSY_MIXER)
    );

    /**
     * Recipe types intentionally without a {@link RecipeDisplay}. Tracked explicitly so missing
     * displays are a deliberate decision rather than an accident.
     */
    public static final List<String> DISPLAY_EXEMPT = ENTRIES.stream()
            .filter(entry -> !entry.hasDisplay())
            .map(RecipeContentEntry::id)
            .toList();

    /**
     * Validates registry consistency at runtime. Returns a list of human-readable problems; empty
     * means everything is consistent.
     */
    public static List<String> validateAll() {
        List<String> issues = new ArrayList<>();
        for (RecipeContentEntry entry : ENTRIES) {
            if (!entry.type().getId().equals(entry.serializer().getId())) {
                issues.add("Recipe '" + entry.id() + "' type id " + entry.type().getId()
                        + " != serializer id " + entry.serializer().getId());
            }
            if (entry.hasDisplay() && entry.display().get() == null) {
                issues.add("Recipe '" + entry.id() + "' declares a display but it is unregistered");
            }
        }
        return issues;
    }
}
