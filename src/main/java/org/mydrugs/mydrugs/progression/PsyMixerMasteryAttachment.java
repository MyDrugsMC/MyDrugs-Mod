package org.mydrugs.mydrugs.progression;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class PsyMixerMasteryAttachment implements ValueIOSerializable {
    private final Map<ResourceLocation, Integer> completed = new LinkedHashMap<>();
    private final Map<ResourceLocation, Integer> failed = new LinkedHashMap<>();

    public int getCompleted(ResourceLocation recipeId) {
        return completed.getOrDefault(recipeId, 0);
    }

    public int getFailed(ResourceLocation recipeId) {
        return failed.getOrDefault(recipeId, 0);
    }

    public void incrementCompleted(ResourceLocation recipeId) {
        completed.merge(recipeId, 1, Integer::sum);
    }

    public void incrementFailed(ResourceLocation recipeId) {
        failed.merge(recipeId, 1, Integer::sum);
    }

    public float getSpeedMultiplier(ResourceLocation recipeId) {
        return 1.0F;
    }

    public float getInstabilityReduction(ResourceLocation recipeId) {
        return 0.0F;
    }

    public float getTimingWindowBonus(ResourceLocation recipeId) {
        return 0.0F;
    }

    public int getRemovedActionCount(ResourceLocation formulaId) {
        return Math.max(0, getCompleted(formulaId) / 10);
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
    }
}
