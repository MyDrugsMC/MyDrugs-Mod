package org.mydrugs.mydrugs.core.drug.ritual;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.drugs.CannabisPowderItem;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RitualDrugRegistry {
    private static final Map<ResourceLocation, RitualDrugDefinition> DEFINITIONS = new LinkedHashMap<>();

    private RitualDrugRegistry() {
    }

    public static void registerDefaults() {
        if (!DEFINITIONS.isEmpty()) {
            return;
        }

        register(RitualDrugDefinition.builder("brightened_cannabis")
                .baseDrug(DrugId.WEED)
                .outputItem(ModItems.BRIGHTENED_CANNABIS_POWDER)
                .addEffect(new DrugEffect(EffectType.GAMMA_BOOST, 30 * 20, 0.45F))
                .effectModifier(EffectType.GAMMA_BOOST, 0.25F)
                .ritualStabilityBonus(0.10F)
                .addictionMultiplier(1.05F)
                .machineSpeedBonus(0.05F)
                .build());
    }

    public static RitualDrugDefinition register(RitualDrugDefinition definition) {
        DEFINITIONS.put(definition.id(), definition);
        return definition;
    }

    public static Collection<RitualDrugDefinition> all() {
        return DEFINITIONS.values();
    }

    public static @Nullable RitualDrugDefinition byOutput(Item item) {
        for (RitualDrugDefinition definition : DEFINITIONS.values()) {
            if (definition.outputItem().get() == item) {
                return definition;
            }
        }
        return null;
    }

    public static boolean isBrightenedCannabis(Item item) {
        RitualDrugDefinition definition = byOutput(item);
        return definition != null && definition.outputItem().get() == ModItems.BRIGHTENED_CANNABIS_POWDER.get();
    }

    public static Collection<DrugEffect> brightenedCannabisEffects() {
        RitualDrugDefinition definition = byOutput(ModItems.BRIGHTENED_CANNABIS_POWDER.get());
        return definition == null ? CannabisPowderItem.BRIGHTENED_EFFECTS : definition.additionalEffects();
    }
}
