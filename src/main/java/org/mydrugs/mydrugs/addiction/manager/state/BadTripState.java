package org.mydrugs.mydrugs.addiction.manager.state;

import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;

public final class BadTripState {
    public boolean active;
    public float threshold;
    public float severity;
    public int ticksActive;
    public int nextSymptomReroll;
    public int nextDemonSpawnAttempt;
    public int firstDemonSpawnDelay = -1;
    public int demonRemainsDropped;
    public float symptomIntensity;
    public @Nullable DrugId sourceDrug;
    public DrugCategory sourceCategory = DrugCategory.OTHER;
    public boolean violentDemonHook;
    public boolean reachedViolentSeverity;

    int lastSyncedBand = -1;

    public void reset() {
        this.active = false;
        this.threshold = 0.0F;
        this.severity = 0.0F;
        this.ticksActive = 0;
        this.nextSymptomReroll = 0;
        this.nextDemonSpawnAttempt = 0;
        this.firstDemonSpawnDelay = -1;
        this.demonRemainsDropped = 0;
        this.symptomIntensity = 0.0F;
        this.sourceDrug = null;
        this.sourceCategory = DrugCategory.OTHER;
        this.violentDemonHook = false;
        this.reachedViolentSeverity = false;
        this.lastSyncedBand = -1;
    }
}
