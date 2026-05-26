package org.mydrugs.mydrugs.energy.psycurrent;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.energy.PsyCurrentConstants;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class DistillateFuelRegistry {
    private static final Map<Item, DistillateFuel> FUELS = new LinkedHashMap<>();
    private static boolean initialized;

    private DistillateFuelRegistry() {
    }

    public static Optional<DistillateFuel> get(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        ensureInitialized();
        return Optional.ofNullable(FUELS.get(stack.getItem()));
    }

    public static boolean isFuel(ItemStack stack) {
        return get(stack).isPresent();
    }

    public static boolean isRejectedPsychedelicMaterial(ItemStack stack) {
        if (stack.isEmpty() || Config.SERVER.allowDreamResidueAsEngineFuel.get()) {
            return false;
        }
        return stack.is(ModItems.DREAM_RESIDUE.get()) || stack.is(ModItems.MYCELIAL_INSIGHT.get());
    }

    public static Map<Item, DistillateFuel> all() {
        ensureInitialized();
        return Map.copyOf(FUELS);
    }

    private static void ensureInitialized() {
        if (initialized) {
            return;
        }

        register(ModItems.LUCID_EXTRACT.get(), DistillateFuelType.LUCID,
                PsyCurrentConstants.LUCID_EXTRACT_CURRENT,
                PsyCurrentConstants.LUCID_EXTRACT_DURATION_TICKS,
                PsyCurrentConstants.LUCID_EXTRACT_STRAIN_ON_START,
                PsyCurrentConstants.LUCID_EXTRACT_STRAIN_PER_SECOND,
                false);
        register(ModItems.CALMING_RESIN.get(), DistillateFuelType.CALMING,
                PsyCurrentConstants.CALMING_RESIN_CURRENT,
                PsyCurrentConstants.CALMING_RESIN_DURATION_TICKS,
                PsyCurrentConstants.CALMING_RESIN_STRAIN_ON_START,
                PsyCurrentConstants.CALMING_RESIN_STRAIN_PER_SECOND,
                false);
        register(ModItems.REDLINE_FUEL.get(), DistillateFuelType.REDLINE,
                PsyCurrentConstants.REDLINE_FUEL_CURRENT,
                PsyCurrentConstants.REDLINE_FUEL_DURATION_TICKS,
                PsyCurrentConstants.REDLINE_FUEL_STRAIN_ON_START,
                PsyCurrentConstants.REDLINE_FUEL_STRAIN_PER_SECOND,
                false);
        register(ModItems.OVERDRIVE_FUEL.get(), DistillateFuelType.OVERDRIVE,
                PsyCurrentConstants.OVERDRIVE_FUEL_CURRENT,
                PsyCurrentConstants.OVERDRIVE_FUEL_DURATION_TICKS,
                PsyCurrentConstants.OVERDRIVE_FUEL_STRAIN_ON_START,
                PsyCurrentConstants.OVERDRIVE_FUEL_STRAIN_PER_SECOND,
                false);
        register(ModItems.UNSTABLE_ESSENCE.get(), DistillateFuelType.UNSTABLE,
                PsyCurrentConstants.UNSTABLE_ESSENCE_CURRENT,
                PsyCurrentConstants.UNSTABLE_ESSENCE_DURATION_TICKS,
                PsyCurrentConstants.UNSTABLE_ESSENCE_STRAIN_ON_START,
                PsyCurrentConstants.UNSTABLE_ESSENCE_STRAIN_PER_SECOND,
                true);

        if (Config.SERVER.allowDreamResidueAsEngineFuel.get()) {
            register(ModItems.DREAM_RESIDUE.get(), DistillateFuelType.UNSTABLE,
                    PsyCurrentConstants.DREAM_RESIDUE_DEBUG_FUEL_CURRENT,
                    PsyCurrentConstants.DREAM_RESIDUE_DEBUG_FUEL_DURATION_TICKS,
                    PsyCurrentConstants.DREAM_RESIDUE_DEBUG_FUEL_STRAIN_ON_START,
                    PsyCurrentConstants.DREAM_RESIDUE_DEBUG_FUEL_STRAIN_PER_SECOND,
                    true);
            register(ModItems.MYCELIAL_INSIGHT.get(), DistillateFuelType.CALMING,
                    PsyCurrentConstants.MYCELIAL_INSIGHT_DEBUG_FUEL_CURRENT,
                    PsyCurrentConstants.MYCELIAL_INSIGHT_DEBUG_FUEL_DURATION_TICKS,
                    PsyCurrentConstants.MYCELIAL_INSIGHT_DEBUG_FUEL_STRAIN_ON_START,
                    PsyCurrentConstants.MYCELIAL_INSIGHT_DEBUG_FUEL_STRAIN_PER_SECOND,
                    false);
        }

        initialized = true;
    }

    private static void register(
            Item item,
            DistillateFuelType type,
            int totalCurrent,
            int durationTicks,
            int strainOnStart,
            int strainPerSecond,
            boolean pulsing
    ) {
        FUELS.put(item, new DistillateFuel(item, type, totalCurrent, durationTicks, strainOnStart, strainPerSecond, pulsing));
    }
}
