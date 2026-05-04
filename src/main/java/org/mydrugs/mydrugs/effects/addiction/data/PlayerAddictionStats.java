package org.mydrugs.mydrugs.effects.addiction.data;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class PlayerAddictionStats implements ValueIOSerializable {
    public final EnumMap<DrugId, DrugAddictionStats> perDrug = new EnumMap<>(DrugId.class);
    public float geneticFactor;
    public float resilience;
    public float stressLevel;
    public TemporaryRecoveryEffects temporaryEffects = new TemporaryRecoveryEffects();

    public long lastTherapyDay = -1L;
    public long sleepBlockedUntil = 0L;
    public long lastGeneralHintTick = 0L;
    public long lastSleepHintTick = 0L;
    public long lastSafeZoneHintTick = 0L;
    public long lastFoodHintTick = 0L;
    public long lastHealthHintTick = 0L;
    public long lastHintTick = 0L;
    public long lastAnchorHintTick = 0L;
    public long lastSocialHintTick = 0L;

    public double lastHintX = 0.0D;
    public double lastHintY = 0.0D;
    public double lastHintZ = 0.0D;

    public String lastHintTopicId = "";
    public int lastHintVariantIndex = -1;

    public boolean wasInSafeZoneLastTick = false;
    public boolean anchorHintShownThisVisit = false;

    public long lastDiaryHintTick = 0L;
    public long lastHeadphonesHintTick = 0L;
    public long lastDrugHintTick = 0L;

    public int overdoseDeathTimer = -1;
    public boolean addictionSymptomsImmune = false;

    public PlayerAddictionStats() {
        RandomSource random = RandomSource.create();
        rerollLifeTraits(random);
    }

    public void rerollLifeTraits(RandomSource random) {
        geneticFactor = 0.85F + random.nextFloat() * 0.30F;
        resilience = 0.05F + random.nextFloat() * 0.10F;
        stressLevel = 0.15F;
    }

    public DrugAddictionStats getOrCreateDrugStats(DrugId drugId) {
        return perDrug.computeIfAbsent(drugId, ignored -> new DrugAddictionStats());
    }

    public @Nullable DrugAddictionStats getDrugStats(DrugId drugId) {
        return perDrug.get(drugId);
    }

    public Map<DrugId, DrugAddictionStats> getAllDrugStats() {
        return Collections.unmodifiableMap(perDrug);
    }

    public List<DrugId> getTrackedDrugIds() {
        return new ArrayList<>(perDrug.keySet());
    }

    public boolean removeDrugStatsIfEmpty(DrugId drugId) {
        DrugAddictionStats stats = perDrug.get(drugId);
        if (stats == null || !stats.isEmpty()) {
            return false;
        }

        perDrug.remove(drugId);
        return true;
    }

    public float getCategoryAddictionValue(DrugCategory category) {
        float total = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) == category) {
                total += entry.getValue().addictionValue;
            }
        }
        return total;
    }

    public float getCategoryMaxAddictionValue(DrugCategory category) {
        float max = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) == category) {
                max = Math.max(max, entry.getValue().addictionValue);
            }
        }
        return max;
    }

    public float getCategoryTolerance(DrugCategory category) {
        float total = 0.0F;
        int count = 0;

        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) == category) {
                total += entry.getValue().tolerance;
                count++;
            }
        }

        return count == 0 ? 0.0F : total / count;
    }

    public float getCategoryCurrentDose(DrugCategory category) {
        float total = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) == category) {
                total += entry.getValue().currentDose();
            }
        }
        return total;
    }

    public float getCategoryWithdrawalMeter(DrugCategory category) {
        float max = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) == category) {
                max = Math.max(max, entry.getValue().baseWithdrawalMeter);
            }
        }
        return max;
    }

    public float getMaxWithdrawalMeter() {
        float max = 0.0F;
        for (DrugAddictionStats stats : perDrug.values()) {
            max = Math.max(max, stats.baseWithdrawalMeter);
        }
        return max;
    }

    public @Nullable DrugId getMostWithdrawingDrugId() {
        DrugId best = null;
        float bestMeter = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            float meter = entry.getValue().baseWithdrawalMeter;
            if (meter > bestMeter) {
                bestMeter = meter;
                best = entry.getKey();
            }
        }
        return best;
    }

    public boolean hasActiveCategory(DrugCategory category) {
        return getDominantDrugInCategory(category) != null;
    }

    public @Nullable DrugId getDominantDrugInCategory(DrugCategory category) {
        DrugId bestId = null;
        float bestScore = -1.0F;
        float bestAddiction = -1.0F;

        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) != category) {
                continue;
            }

            DrugAddictionStats stats = entry.getValue();
            float score = stats.baseWithdrawalMeter;
            if (score > bestScore || (score == bestScore && stats.addictionValue > bestAddiction)) {
                bestScore = score;
                bestAddiction = stats.addictionValue;
                bestId = entry.getKey();
            }
        }

        return bestId;
    }

    public @Nullable DrugAddictionStats getDominantDrugStatsInCategory(DrugCategory category) {
        DrugId drugId = getDominantDrugInCategory(category);
        return drugId != null ? perDrug.get(drugId) : null;
    }

    public void addWithdrawalToCategory(DrugCategory category, float amount) {
        if (amount <= 0.0F) {
            return;
        }

        List<Map.Entry<DrugId, DrugAddictionStats>> entries = getEntriesForCategory(category);
        if (entries.isEmpty()) {
            return;
        }

        float totalWeight = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
            totalWeight += Math.max(0.001F, entry.getValue().addictionValue);
        }

        for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
            DrugAddictionStats stats = entry.getValue();
            float weight = Math.max(0.001F, stats.addictionValue);
            float share = amount * (weight / totalWeight);
            stats.baseWithdrawalMeter = Math.min(100.0F, stats.baseWithdrawalMeter + share);
        }
    }

    public void reduceWithdrawalInCategory(DrugCategory category, float amount) {
        if (amount <= 0.0F) {
            return;
        }

        List<Map.Entry<DrugId, DrugAddictionStats>> entries = getEntriesForCategory(category);
        if (entries.isEmpty()) {
            return;
        }

        float totalWeight = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
            totalWeight += Math.max(0.001F, entry.getValue().baseWithdrawalMeter);
        }

        if (totalWeight <= 0.0F) {
            float equalShare = amount / entries.size();
            for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
                DrugAddictionStats stats = entry.getValue();
                stats.baseWithdrawalMeter = Math.max(0.0F, stats.baseWithdrawalMeter - equalShare);
            }
            return;
        }

        for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
            DrugAddictionStats stats = entry.getValue();
            float weight = Math.max(0.001F, stats.baseWithdrawalMeter);
            float share = amount * (weight / totalWeight);
            stats.baseWithdrawalMeter = Math.max(0.0F, stats.baseWithdrawalMeter - share);
        }
    }

    public void reduceAddictionInCategory(DrugCategory category, float amount) {
        if (amount <= 0.0F) {
            return;
        }

        List<Map.Entry<DrugId, DrugAddictionStats>> entries = getEntriesForCategory(category);
        if (entries.isEmpty()) {
            return;
        }

        float totalWeight = 0.0F;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
            totalWeight += Math.max(0.001F, entry.getValue().addictionValue);
        }

        for (Map.Entry<DrugId, DrugAddictionStats> entry : entries) {
            DrugAddictionStats stats = entry.getValue();
            float weight = Math.max(0.001F, stats.addictionValue);
            float share = amount * (weight / totalWeight);
            stats.addictionValue = Math.max(0.0F, stats.addictionValue - share);
        }
    }

    private List<Map.Entry<DrugId, DrugAddictionStats>> getEntriesForCategory(DrugCategory category) {
        List<Map.Entry<DrugId, DrugAddictionStats>> result = new ArrayList<>();
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            if (DrugRegistry.getCategory(entry.getKey()) == category) {
                result.add(entry);
            }
        }
        return result;
    }

    public void copyFrom(PlayerAddictionStats other, boolean wasDeath, RandomSource random) {
        perDrug.clear();
        for (Map.Entry<DrugId, DrugAddictionStats> entry : other.perDrug.entrySet()) {
            perDrug.put(entry.getKey(), entry.getValue().copy());
        }

        temporaryEffects = other.temporaryEffects.copy();
        lastTherapyDay = other.lastTherapyDay;
        sleepBlockedUntil = other.sleepBlockedUntil;
        resilience = other.resilience;
        stressLevel = other.stressLevel;
        overdoseDeathTimer = wasDeath ? -1 : other.overdoseDeathTimer;

        if (wasDeath) {
            rerollLifeTraits(random);
            resilience = other.resilience;
            stressLevel = Math.min(0.35F, other.stressLevel * 0.50F);
        } else {
            geneticFactor = other.geneticFactor;
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putFloat("genetic_factor", geneticFactor);
        output.putFloat("resilience", resilience);
        output.putFloat("stress_level", stressLevel);
        output.putLong("last_therapy_day", lastTherapyDay);
        output.putLong("sleep_blocked_until", sleepBlockedUntil);
        output.putInt("overdose_death_timer", overdoseDeathTimer);
        output.putBoolean("addiction_symptoms_immune", addictionSymptomsImmune);

        ValueOutput effects = output.child("temporary_effects");
        temporaryEffects.serialize(effects);

        ValueOutput drugs = output.child("drug_stats");
        drugs.putInt("count", perDrug.size());

        int index = 0;
        for (Map.Entry<DrugId, DrugAddictionStats> entry : perDrug.entrySet()) {
            ValueOutput child = drugs.child("entry_" + index++);
            child.putString("drug_id", entry.getKey().serializedName());
            entry.getValue().serialize(child);
        }
    }

    @Override
    public void deserialize(ValueInput input) {
        geneticFactor = input.getFloatOr("genetic_factor", 1.0F);
        resilience = input.getFloatOr("resilience", 0.05F);
        stressLevel = input.getFloatOr("stress_level", 0.15F);
        lastTherapyDay = input.getLongOr("last_therapy_day", -1L);
        sleepBlockedUntil = input.getLongOr("sleep_blocked_until", 0L);
        overdoseDeathTimer = input.getIntOr("overdose_death_timer", -1);
        addictionSymptomsImmune = input.getBooleanOr("addiction_symptoms_immune", false);

        temporaryEffects = new TemporaryRecoveryEffects();
        temporaryEffects.deserialize(input.childOrEmpty("temporary_effects"));

        perDrug.clear();
        ValueInput drugs = input.childOrEmpty("drug_stats");
        int count = drugs.getIntOr("count", -1);

        if (count >= 0) {
            for (int i = 0; i < count; i++) {
                ValueInput child = drugs.childOrEmpty("entry_" + i);
                String drugIdName = child.getStringOr("drug_id", "");
                if (drugIdName.isEmpty()) {
                    continue;
                }

                DrugId drugId = DrugId.bySerializedName(drugIdName).orElse(null);
                if (drugId == null) {
                    continue;
                }

                DrugAddictionStats stats = new DrugAddictionStats();
                stats.deserialize(child);

                if (!stats.isEmpty() || stats.lastUseTime > 0L) {
                    perDrug.put(drugId, stats);
                }
            }
            return;
        }

        migrateLegacyCategoryBuckets(drugs);
    }

    private void migrateLegacyCategoryBuckets(ValueInput drugs) {
        for (DrugCategory category : DrugCategory.values()) {
            ValueInput child = drugs.childOrEmpty(category.name().toLowerCase(Locale.ROOT));
            DrugAddictionStats stats = new DrugAddictionStats();
            stats.deserialize(child);

            if (stats.isEmpty() && stats.lastUseTime <= 0L) {
                continue;
            }

            DrugId representative = DrugRegistry.getRepresentativeDrugId(category);
            if (representative == null) {
                continue;
            }

            perDrug.put(representative, stats);
        }
    }
}
