package org.mydrugs.mydrugs.effects.addiction.data;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;

import java.util.EnumMap;

public final class PlayerAddictionStats implements ValueIOSerializable {
    public final EnumMap<DrugCategory, DrugAddictionStats> perDrug = new EnumMap<>(DrugCategory.class);
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

    public int lastHintTopicOrdinal = -1;
    public int lastHintVariantIndex = -1;

    public boolean wasInSafeZoneLastTick = false;
    public boolean anchorHintShownThisVisit = false;

    public long lastDiaryHintTick = 0L;
    public long lastHeadphonesHintTick = 0L;

    /** -1 when no overdose is active; otherwise ticks left until death from overdose. */
    public int overdoseDeathTimer = -1;

    public PlayerAddictionStats() {
        RandomSource random = RandomSource.create();
        rerollLifeTraits(random);
        for (DrugCategory category : DrugCategory.values()) {
            perDrug.put(category, new DrugAddictionStats());
        }
    }

    public void rerollLifeTraits(RandomSource random) {
        geneticFactor = 0.85F + random.nextFloat() * 0.30F;
        resilience = 0.05F + random.nextFloat() * 0.10F;
        stressLevel = 0.15F;
    }

    public DrugAddictionStats get(DrugCategory category) {
        return perDrug.computeIfAbsent(category, c -> new DrugAddictionStats());
    }

    public void copyFrom(PlayerAddictionStats other, boolean wasDeath, RandomSource random) {
        perDrug.clear();
        for (DrugCategory category : DrugCategory.values()) {
            perDrug.put(category, other.get(category).copy());
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

        ValueOutput effects = output.child("temporary_effects");
        temporaryEffects.serialize(effects);

        ValueOutput drugs = output.child("drug_stats");
        for (DrugCategory category : DrugCategory.values()) {
            ValueOutput child = drugs.child(category.name().toLowerCase(java.util.Locale.ROOT));
            get(category).serialize(child);
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

        temporaryEffects = new TemporaryRecoveryEffects();
        temporaryEffects.deserialize(input.childOrEmpty("temporary_effects"));

        perDrug.clear();
        ValueInput drugs = input.childOrEmpty("drug_stats");
        for (DrugCategory category : DrugCategory.values()) {
            DrugAddictionStats stats = new DrugAddictionStats();
            stats.deserialize(drugs.childOrEmpty(category.name().toLowerCase(java.util.Locale.ROOT)));
            perDrug.put(category, stats);
        }
    }
}