package org.mydrugs.mydrugs.advancement;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public final class DrugKnowledgeAttachment implements ValueIOSerializable {
    private final EnumSet<DrugId> consumedDrugIds = EnumSet.noneOf(DrugId.class);
    private final EnumSet<DrugCategory> consumedCategories = EnumSet.noneOf(DrugCategory.class);
    private final Set<String> consumedRoutes = new HashSet<>();

    public boolean hasDiscovered(DrugId drugId) {
        return consumedDrugIds.contains(drugId);
    }

    public boolean hasDiscoveredCategory(DrugCategory category) {
        return consumedCategories.contains(category);
    }

    public boolean hasDiscoveredRoute(String route) {
        return consumedRoutes.contains(normalize(route));
    }

    public DrugKnowledgeResult markConsumed(DrugId drugId, DrugCategory category, String route,
                                            org.mydrugs.mydrugs.core.drug.use.DrugUseSource source,
                                            float effectiveDose) {
        String normalizedRoute = normalize(route);
        boolean firstDrug = consumedDrugIds.add(drugId);
        boolean firstCategory = consumedCategories.add(category);
        boolean firstRoute = consumedRoutes.add(normalizedRoute);
        return new DrugKnowledgeResult(drugId, category, normalizedRoute, source, effectiveDose, firstDrug, firstCategory, firstRoute);
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putInt("drug_count", consumedDrugIds.size());
        int index = 0;
        for (DrugId drugId : consumedDrugIds) {
            output.putString("drug_" + index++, drugId.serializedName());
        }

        output.putInt("category_count", consumedCategories.size());
        index = 0;
        for (DrugCategory category : consumedCategories) {
            output.putString("category_" + index++, serializeCategory(category));
        }

        output.putInt("route_count", consumedRoutes.size());
        index = 0;
        for (String route : consumedRoutes) {
            output.putString("route_" + index++, route);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        consumedDrugIds.clear();
        consumedCategories.clear();
        consumedRoutes.clear();

        int drugCount = input.getIntOr("drug_count", 0);
        for (int i = 0; i < drugCount; i++) {
            DrugId.bySerializedName(input.getStringOr("drug_" + i, "")).ifPresent(consumedDrugIds::add);
        }

        int categoryCount = input.getIntOr("category_count", 0);
        for (int i = 0; i < categoryCount; i++) {
            DrugCategory category = parseCategory(input.getStringOr("category_" + i, ""));
            if (category != null) {
                consumedCategories.add(category);
            }
        }

        int routeCount = input.getIntOr("route_count", 0);
        for (int i = 0; i < routeCount; i++) {
            String route = normalize(input.getStringOr("route_" + i, ""));
            if (!route.isBlank()) {
                consumedRoutes.add(route);
            }
        }
    }

    static String serializeCategory(DrugCategory category) {
        return category.name().toLowerCase(Locale.ROOT);
    }

    static DrugCategory parseCategory(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return null;
        }
        try {
            return DrugCategory.valueOf(normalized);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static String normalize(String route) {
        return route == null ? "" : route.trim().toLowerCase(Locale.ROOT);
    }
}
