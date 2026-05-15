package org.mydrugs.mydrugs.mutation;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.items.data.MutationPayloadData;
import org.mydrugs.mydrugs.items.data.MutationStatValue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class PlayerMutationsAttachment implements ValueIOSerializable {
    private final Map<String, ActiveMutationStat> activeStats = new LinkedHashMap<>();
    private final InfectionState infection = new InfectionState();

    public List<MutationStatValue> stats() {
        List<MutationStatValue> stats = new ArrayList<>();
        for (ActiveMutationStat stat : this.activeStats.values()) {
            if (stat.currentValue() > 0.0F) {
                stats.add(stat.asCurrentValue());
            }
        }
        return List.copyOf(stats);
    }

    public List<ActiveMutationStat> activeStats() {
        return List.copyOf(this.activeStats.values());
    }

    public InfectionState infection() {
        return this.infection;
    }

    public boolean isEmpty() {
        return this.activeStats.isEmpty();
    }

    public void replaceStats(List<MutationStatValue> values) {
        this.activeStats.clear();
        for (MutationStatValue value : values) {
            if (MutationStat.bySerializedNameOrNull(value.statId()) == null) {
                continue;
            }
            this.activeStats.put(value.statId(), new ActiveMutationStat(
                    value.statId(),
                    value.value(),
                    value.value(),
                    1.0F,
                    1.0F,
                    List.of(),
                    List.of()
            ));
        }
    }

    public void injectPayload(MutationPayloadData payload, float geneticStability) {
        for (MutationStatValue incoming : payload.stats()) {
            if (MutationStat.bySerializedNameOrNull(incoming.statId()) == null) {
                continue;
            }
            ActiveMutationStat existing = this.activeStats.get(incoming.statId());
            if (existing == null) {
                this.activeStats.put(incoming.statId(), new ActiveMutationStat(
                        incoming.statId(),
                        0.0F,
                        incoming.value(),
                        0.0F,
                        payload.assimilationDifficulty(),
                        payload.sourceUuids(),
                        payload.sourceNames()
                ));
            } else {
                existing.mergeTarget(incoming.value(), payload.assimilationDifficulty(), geneticStability, payload.sourceUuids(), payload.sourceNames());
            }
        }
    }

    public boolean tickAssimilation() {
        return tickAssimilation(1.0F);
    }

    public boolean tickAssimilation(float speedMultiplier) {
        float infectionSlowdown = this.infection.active() && this.infection.stage() >= 2 ? 0.35F : 1.0F;
        boolean changed = false;
        for (ActiveMutationStat stat : this.activeStats.values()) {
            changed |= stat.tickAssimilation(infectionSlowdown, speedMultiplier);
        }
        return changed;
    }

    public List<String> drainCompletedAssimilations() {
        List<String> completed = new ArrayList<>();
        for (ActiveMutationStat stat : this.activeStats.values()) {
            if (stat.consumeJustCompleted()) {
                completed.add(stat.statId());
            }
        }
        return completed;
    }

    public Map<String, Float> snapshotCurrent() {
        Map<String, Float> snapshot = new LinkedHashMap<>();
        for (ActiveMutationStat stat : this.activeStats.values()) {
            if (stat.currentValue() > 0.0F) {
                snapshot.put(stat.statId(), stat.currentValue());
            }
        }
        return snapshot;
    }

    public boolean decayMutations(float amount) {
        boolean changed = false;
        List<String> empty = new ArrayList<>();
        for (ActiveMutationStat stat : this.activeStats.values()) {
            changed |= stat.decay(amount);
            if (stat.targetValue() <= 0.0F) {
                empty.add(stat.statId());
            }
        }
        for (String statId : empty) {
            this.activeStats.remove(statId);
            changed = true;
        }
        return changed;
    }

    public boolean clearMutations() {
        if (this.activeStats.isEmpty()) {
            return false;
        }
        this.activeStats.clear();
        return true;
    }

    @Override
    public void serialize(ValueOutput output) {
        ValueOutput.ValueOutputList list = output.childrenList("stats");
        for (ActiveMutationStat stat : this.activeStats.values()) {
            ValueOutput child = list.addChild();
            child.putString("stat_id", stat.statId());
            child.putFloat("current_value", stat.currentValue());
            child.putFloat("target_value", stat.targetValue());
            child.putFloat("assimilation_progress", stat.assimilationProgress());
            child.putFloat("assimilation_difficulty", stat.assimilationDifficulty());
            ValueOutput.ValueOutputList uuids = child.childrenList("source_uuids");
            for (String uuid : stat.sourceUuids()) {
                uuids.addChild().putString("value", uuid);
            }
            if (uuids.isEmpty()) {
                child.discard("source_uuids");
            }
            ValueOutput.ValueOutputList names = child.childrenList("source_names");
            for (String name : stat.sourceNames()) {
                names.addChild().putString("value", name);
            }
            if (names.isEmpty()) {
                child.discard("source_names");
            }
        }
        if (list.isEmpty()) {
            output.discard("stats");
        }

        ValueOutput infectionOutput = output.child("infection");
        infectionOutput.putBoolean("active", this.infection.active());
        infectionOutput.putInt("ticks", this.infection.ticks());
        infectionOutput.putFloat("severity", this.infection.severity());
        infectionOutput.putInt("last_message_stage", this.infection.lastMessageStage());
    }

    @Override
    public void deserialize(ValueInput input) {
        this.activeStats.clear();
        for (ValueInput child : input.childrenListOrEmpty("stats")) {
            String statId = child.getStringOr("stat_id", "");
            if (MutationStat.bySerializedNameOrNull(statId) == null) {
                continue;
            }
            List<String> sourceUuids = readStringList(child, "source_uuids");
            List<String> sourceNames = readStringList(child, "source_names");
            float current = child.getFloatOr("current_value", child.getFloatOr("value", 0.0F));
            float target = child.getFloatOr("target_value", current);
            this.activeStats.put(statId, new ActiveMutationStat(
                    statId,
                    current,
                    target,
                    child.getFloatOr("assimilation_progress", target <= 0.0F ? 0.0F : current / target),
                    child.getFloatOr("assimilation_difficulty", 1.0F),
                    sourceUuids,
                    sourceNames
            ));
        }

        ValueInput infectionInput = input.childOrEmpty("infection");
        this.infection.load(
                infectionInput.getBooleanOr("active", false),
                infectionInput.getIntOr("ticks", 0),
                infectionInput.getFloatOr("severity", 0.0F),
                infectionInput.getIntOr("last_message_stage", 0)
        );
    }

    private static List<String> readStringList(ValueInput input, String key) {
        List<String> result = new ArrayList<>();
        for (ValueInput child : input.childrenListOrEmpty(key)) {
            String value = child.getStringOr("value", "");
            if (!value.isBlank()) {
                result.add(value);
            }
        }
        return List.copyOf(result);
    }
}
