package org.mydrugs.mydrugs.effects.addiction.dose;

/**
 * Per-category intoxication state derived from the active dose in the player's body.
 * Alcohol path climbs NORMAL -> DRUNK -> VERY_DRUNK -> ETHYLIC_COMA.
 * Drug path climbs NORMAL -> HIGH -> VERY_HIGH (bad trip) -> OVERDOSE (starts death timer).
 */
public enum DoseState {
    NORMAL,
    // Alcohol path
    DRUNK,
    VERY_DRUNK,
    ETHYLIC_COMA,
    // Drug path
    HIGH,
    VERY_HIGH,
    OVERDOSE
}
