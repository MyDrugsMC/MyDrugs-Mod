package org.mydrugs.mydrugs.effects.addiction.data;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;

import java.util.*;

public final class PlayerAddictionStats implements ValueIOSerializable {
    public float geneticFactor;
    public float resilience;
    public float stressLevel;

    public final EnumMap<DrugCategory, DrugAddictionStats> perDrug = new EnumMap<>(DrugCategory.class);
    public TemporaryRecoveryEffects temporaryEffects = new TemporaryRecoveryEffects();

    public long lastTherapyDay = -1L;
    public long sleepBlockedUntil = 0L;

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