package org.mydrugs.mydrugs.dimension.inner;

import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Geometry of the nine colossal region set pieces (A2) — one walk-in "mega-form" per drug
 * sector, placed at mid-radius just off the spoke so the path to the shrine passes within
 * sight of it. Pure deterministic math (slot seed + drug id), shared by:
 *
 * <ul>
 *   <li>{@code InnerTerrain.computeSample} — flattens/carves the ground plinth each form sits in
 *       and calms competing features (lakes, holes, spikes, groves) inside the footprint;</li>
 *   <li>{@link InnerMegaFormBuilder} — renders each form's structure chunk-slice by chunk-slice;</li>
 *   <li>server systems (storm, guardians) — via {@link #formFor} positions.</li>
 * </ul>
 */
public final class InnerMegaForms {
    private static final long FORM_SALT = 0x4D45_4741L;
    /** Widest form footprint; used for chunk-intersection culling. */
    public static final int MAX_RADIUS = 26;
    /** Apron width over which the plinth eases back into natural terrain. */
    static final double APRON = 20.0D;

    private static final Map<Long, EnumMap<DrugId, Form>> FORMS = new ConcurrentHashMap<>();

    private InnerMegaForms() {
    }

    /**
     * @param x world x of the form centre
     * @param z world z of the form centre
     * @param radius footprint radius (structure stays inside it)
     * @param baseY plinth ground level
     * @param orientation radial angle from the island centre toward the form (canyon/hall axis)
     * @param hash per-form deterministic style bits
     */
    public record Form(DrugId drug, int x, int z, int radius, int baseY, double orientation, long hash) {
        /** Signed distance along the radial axis through the form centre. */
        double along(int worldX, int worldZ) {
            return Math.cos(orientation) * (worldX - x) + Math.sin(orientation) * (worldZ - z);
        }

        /** Unsigned distance from the radial axis (the canyon / hall cross direction). */
        double perp(int worldX, int worldZ) {
            return Math.abs(-Math.sin(orientation) * (worldX - x) + Math.cos(orientation) * (worldZ - z));
        }

        public double distance(int worldX, int worldZ) {
            return Math.hypot(worldX - x, worldZ - z);
        }
    }

    public static Form formFor(int centerX, int centerZ, DrugId drug) {
        long seed = InnerTerrain.seedForSlot(centerX, centerZ);
        EnumMap<DrugId, Form> bySlot = FORMS.computeIfAbsent(seed, s -> {
            EnumMap<DrugId, Form> map = new EnumMap<>(DrugId.class);
            for (DrugId d : CuratedDrugChain.ORDER) {
                map.put(d, computeForm(s, centerX, centerZ, d));
            }
            return map;
        });
        Form form = bySlot.get(drug);
        return form != null ? form : computeForm(seed, centerX, centerZ, drug);
    }

    private static Form computeForm(long seed, int centerX, int centerZ, DrugId drug) {
        long hash = InnerNoise.mix64(seed + FORM_SALT + (long) drug.networkId() * 0x9E37_79B9L);
        // Off the spoke by 0.14-0.20 rad so the path passes in sight but never through.
        double side = (hash & 1L) == 0L ? 1.0D : -1.0D;
        double angle = InnerRegionMap.angleFor(drug)
                + side * (0.14D + ((hash >>> 8) & 63L) / 63.0D * 0.06D);
        double radial = InnerRegionMap.landmarkRadiusFor(drug) * 0.62D;
        int x = centerX + (int) Math.round(Math.cos(angle) * radial);
        int z = centerZ + (int) Math.round(Math.sin(angle) * radial);
        return new Form(drug, x, z, radiusFor(drug), InnerDimensionConstants.BASE_Y + 6, angle, hash);
    }

    static int radiusFor(DrugId drug) {
        return switch (drug) {
            case LSD, METH, MUSHROOMS -> 26;
            case WEED, ALCOHOL -> 24;
            case TOBACCO, COCAINE -> 22;
            case COFFEE -> 20;
            case HASH -> 18;
            default -> 20;
        };
    }

    /**
     * Ground level the plinth shapes the terrain to at a column. Most forms sit on a flat
     * platform; some carve their relief straight into the ground (crater bowl, canyon, fault).
     */
    static int plinthY(Form form, int worldX, int worldZ) {
        int base = form.baseY();
        double d = form.distance(worldX, worldZ);
        double r = form.radius();
        return switch (form.drug()) {
            case WEED -> {
                // Terraced crater bowl, deepest at the centre.
                double depth = Math.max(0.0D, (1.0D - d / r)) * 9.0D;
                yield base - (int) (Math.floor(depth / 2.0D) * 2.0D);
            }
            case TOBACCO -> base - (int) Math.round(Math.max(0.0D, 1.0D - d / r) * 3.0D);
            case ALCOHOL -> base - 2;
            case LSD -> form.perp(worldX, worldZ) < 4.5D ? base - 10 : base;
            case METH -> form.perp(worldX, worldZ) < 5.5D ? base - 12 : base;
            default -> base;
        };
    }

    /** Features are silenced inside a form so lakes/holes/groves never tear up a set piece. */
    static InnerFeatureSample calm(InnerFeatureSample features) {
        return new InnerFeatureSample(
                false, 0.0D, 0.0D, 0.0D, 0.0D, InnerLakeType.NONE, 0, 0, false, false,
                false, 0.0D, InnerHoleType.NONE,
                false, 0.0D,
                false, 0.0D,
                false, 0.0D,
                features.cliffStrength(),
                features.slopeHint()
        );
    }

    /** Lightning targets for the METH Fault storm (B3): the spire tips along the rift edges. */
    public static int[][] faultSpireOffsets(Form form) {
        int[][] tips = new int[6][2];
        for (int i = 0; i < 6; i++) {
            double along = -15.0D + i * 6.0D;
            double perpSide = (i & 1) == 0 ? 7.0D : -7.0D;
            tips[i][0] = form.x() + (int) Math.round(Math.cos(form.orientation()) * along
                    - Math.sin(form.orientation()) * perpSide);
            tips[i][1] = form.z() + (int) Math.round(Math.sin(form.orientation()) * along
                    + Math.cos(form.orientation()) * perpSide);
        }
        return tips;
    }
}
