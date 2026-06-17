package org.mydrugs.mydrugs.client;

import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.network.InnerSkyStatePayload;

import java.util.EnumSet;
import java.util.Set;

/**
 * Client mirror of the player's Inner Dimension sky progress, fed by
 * {@link InnerSkyStatePayload}. Holds the integrated-drug set (which constellations are lit) and a
 * smoothed beacon brightness that eases toward the integrated count so the core orb fades up as the
 * island grows rather than popping.
 *
 * <p>Pure client visual state — cleared on logout via
 * {@link org.mydrugs.mydrugs.client.effects.ClientEventHandler}.
 */
public final class InnerSkyClientState {
    private static final Set<DrugId> INTEGRATED = EnumSet.noneOf(DrugId.class);
    private static float beaconBrightness;
    private static float targetBeacon;

    private InnerSkyClientState() {
    }

    public static void accept(InnerSkyStatePayload payload) {
        INTEGRATED.clear();
        for (int networkId : payload.integratedDrugNetworkIds()) {
            DrugId id = DrugId.byNetworkId(networkId);
            if (id != null) {
                INTEGRATED.add(id);
            }
        }
        // Beacon brightness ramps with progress but saturates so a fully healed island still reads.
        targetBeacon = INTEGRATED.isEmpty() ? 0.0F : Mth.clamp(0.28F + INTEGRATED.size() * 0.12F, 0.0F, 1.0F);
    }

    /** Smooths the beacon toward its target. Call once per client tick. */
    public static void tick() {
        beaconBrightness += (targetBeacon - beaconBrightness) * 0.06F;
    }

    public static Set<DrugId> integratedDrugs() {
        return Set.copyOf(INTEGRATED);
    }

    public static int integratedCount() {
        return INTEGRATED.size();
    }

    public static boolean isIntegrated(DrugId id) {
        return INTEGRATED.contains(id);
    }

    public static float beaconBrightness() {
        return beaconBrightness;
    }

    public static void clear() {
        INTEGRATED.clear();
        beaconBrightness = 0.0F;
        targetBeacon = 0.0F;
    }
}
