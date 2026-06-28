package org.mydrugs.mydrugs.progression;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PsyMixerMasteryAttachment implements ValueIOSerializable {
    private final Map<ResourceLocation, Integer> completed = new LinkedHashMap<>();
    private final Map<ResourceLocation, Integer> failed = new LinkedHashMap<>();
    private final EnumMap<DrugId, Integer> completedByBaseDrug = new EnumMap<>(DrugId.class);
    private final EnumMap<DrugId, Integer> failedByBaseDrug = new EnumMap<>(DrugId.class);

    public int getCompleted(ResourceLocation recipeId) {
        return completed.getOrDefault(recipeId, 0);
    }

    public int getFailed(ResourceLocation recipeId) {
        return failed.getOrDefault(recipeId, 0);
    }

    public int getCompleted(DrugId baseDrug) {
        return completedByBaseDrug.getOrDefault(baseDrug, 0);
    }

    public int getFailed(DrugId baseDrug) {
        return failedByBaseDrug.getOrDefault(baseDrug, 0);
    }

    public void incrementCompleted(ResourceLocation recipeId) {
        completed.merge(recipeId, 1, Integer::sum);
    }

    public void incrementCompleted(ResourceLocation recipeId, DrugId baseDrug) {
        incrementCompleted(recipeId);
        if (baseDrug != null) {
            completedByBaseDrug.merge(baseDrug, 1, Integer::sum);
        }
    }

    public void incrementFailed(ResourceLocation recipeId) {
        failed.merge(recipeId, 1, Integer::sum);
    }

    public void incrementFailed(ResourceLocation recipeId, DrugId baseDrug) {
        incrementFailed(recipeId);
        if (baseDrug != null) {
            failedByBaseDrug.merge(baseDrug, 1, Integer::sum);
        }
    }

    public float getSpeedMultiplier(ResourceLocation recipeId) {
        return 1.0F;
    }

    public float getSpeedMultiplier(ResourceLocation recipeId, DrugId baseDrug) {
        if (baseDrug == DrugId.COFFEE) {
            return 1.0F - Math.min(0.15F, getCompleted(DrugId.COFFEE) * 0.02F);
        }
        return getSpeedMultiplier(recipeId);
    }

    public float getInstabilityReduction(ResourceLocation recipeId) {
        return 0.0F;
    }

    public float getTimingWindowBonus(ResourceLocation recipeId) {
        return Math.min(0.06F, (getCompleted(recipeId) / 3) * 0.01F);
    }

    public float getTimingWindowBonus(ResourceLocation recipeId, DrugId baseDrug) {
        float bonus = getTimingWindowBonus(recipeId);
        if (baseDrug == DrugId.TOBACCO) {
            bonus += Math.min(0.06F, getCompleted(DrugId.TOBACCO) * 0.01F);
        }
        return Math.min(0.12F, bonus);
    }

    public float getActionTimeoutBonus(ResourceLocation recipeId) {
        return Math.min(0.20F, (getCompleted(recipeId) / 5) * 0.05F);
    }

    public int getRemovedActionCount(ResourceLocation formulaId) {
        return Math.min(2, Math.max(0, getCompleted(formulaId) / 10));
    }

    public int getMistakeForgivenessBonus(DrugId baseDrug) {
        if ((baseDrug == DrugId.WEED || baseDrug == DrugId.HASH) && getCompleted(DrugId.WEED) >= 3) {
            return 1;
        }
        return 0;
    }

    public boolean shouldShowExplicitHints(ResourceLocation formulaId) {
        return getFailed(formulaId) >= 3 && getCompleted(formulaId) < 3;
    }

    /** Read-only view of all completed-count entries, in insertion order. */
    public Map<ResourceLocation, Integer> getCompletedEntriesView() {
        return Collections.unmodifiableMap(completed);
    }

    /** Read-only view of all failed-count entries, in insertion order. */
    public Map<ResourceLocation, Integer> getFailedEntriesView() {
        return Collections.unmodifiableMap(failed);
    }

    public int getTotalCompleted() {
        int sum = 0;
        for (int v : completed.values()) sum += v;
        return sum;
    }

    public int getTotalFailed() {
        int sum = 0;
        for (int v : failed.values()) sum += v;
        return sum;
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList completedList = output.childrenList("completed");
        for (Map.Entry<ResourceLocation, Integer> entry : completed.entrySet()) {
            ValueOutput child = completedList.addChild();
            child.putString("id", entry.getKey().toString());
            child.putInt("count", entry.getValue());
        }
        if (completedList.isEmpty()) {
            output.discard("completed");
        }

        ValueOutput.ValueOutputList failedList = output.childrenList("failed");
        for (Map.Entry<ResourceLocation, Integer> entry : failed.entrySet()) {
            ValueOutput child = failedList.addChild();
            child.putString("id", entry.getKey().toString());
            child.putInt("count", entry.getValue());
        }
        if (failedList.isEmpty()) {
            output.discard("failed");
        }

        ValueOutput.ValueOutputList completedByBaseDrugList = output.childrenList("completed_by_base_drug");
        for (Map.Entry<DrugId, Integer> entry : completedByBaseDrug.entrySet()) {
            ValueOutput child = completedByBaseDrugList.addChild();
            child.putString("drug", entry.getKey().serializedName());
            child.putInt("count", entry.getValue());
        }
        if (completedByBaseDrugList.isEmpty()) {
            output.discard("completed_by_base_drug");
        }

        ValueOutput.ValueOutputList failedByBaseDrugList = output.childrenList("failed_by_base_drug");
        for (Map.Entry<DrugId, Integer> entry : failedByBaseDrug.entrySet()) {
            ValueOutput child = failedByBaseDrugList.addChild();
            child.putString("drug", entry.getKey().serializedName());
            child.putInt("count", entry.getValue());
        }
        if (failedByBaseDrugList.isEmpty()) {
            output.discard("failed_by_base_drug");
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        completed.clear();
        for (ValueInput child : input.childrenListOrEmpty("completed")) {
            ResourceLocation id = ResourceLocation.tryParse(child.getStringOr("id", ""));
            int count = child.getIntOr("count", 0);
            if (id != null && count > 0) {
                completed.put(id, count);
            }
        }
        failed.clear();
        for (ValueInput child : input.childrenListOrEmpty("failed")) {
            ResourceLocation id = ResourceLocation.tryParse(child.getStringOr("id", ""));
            int count = child.getIntOr("count", 0);
            if (id != null && count > 0) {
                failed.put(id, count);
            }
        }

        completedByBaseDrug.clear();
        for (ValueInput child : input.childrenListOrEmpty("completed_by_base_drug")) {
            DrugId drug = DrugId.bySerializedNameOrNull(child.getStringOr("drug", ""));
            int count = child.getIntOr("count", 0);
            if (drug != null && count > 0) {
                completedByBaseDrug.put(drug, count);
            }
        }

        failedByBaseDrug.clear();
        for (ValueInput child : input.childrenListOrEmpty("failed_by_base_drug")) {
            DrugId drug = DrugId.bySerializedNameOrNull(child.getStringOr("drug", ""));
            int count = child.getIntOr("count", 0);
            if (drug != null && count > 0) {
                failedByBaseDrug.put(drug, count);
            }
        }
    }
}
