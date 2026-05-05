package org.mydrugs.mydrugs.progression;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

import java.util.LinkedHashMap;
import java.util.Map;

public final class PsyMixerMasteryAttachment implements ValueIOSerializable {
    private static final float TIME_REDUCTION_PER_COMPLETION = 0.04F;
    private static final float MIN_TIME_MULTIPLIER = 0.35F;
    private static final float INSTABILITY_REDUCTION_PER_COMPLETION = 0.015F;
    private static final float MAX_INSTABILITY_REDUCTION = 0.25F;

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
        int c = getCompleted(recipeId);
        return Math.max(MIN_TIME_MULTIPLIER, 1.0F - c * TIME_REDUCTION_PER_COMPLETION);
    }

    public float getInstabilityReduction(ResourceLocation recipeId) {
        int c = getCompleted(recipeId);
        return Math.min(MAX_INSTABILITY_REDUCTION, c * INSTABILITY_REDUCTION_PER_COMPLETION);
    }

    public float getTimingWindowBonus(ResourceLocation recipeId) {
        int c = getCompleted(recipeId);
        return Math.min(0.20F, c * 0.01F);
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
