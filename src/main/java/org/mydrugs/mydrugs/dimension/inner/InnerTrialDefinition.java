package org.mydrugs.mydrugs.dimension.inner;

import net.minecraft.core.BlockPos;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.integration.CuratedDrugChain;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Static, non-persisted descriptor for a drug's Inner Dimension trial: which drug it belongs to and
 * its localized title/hint keys, plus the fixed interaction-node geometry each trial type uses.
 *
 * <p>There is intentionally no single "interaction radius" here: the trials use heterogeneous
 * engagement models (a stand-still pad, a planting reach, exact node offsets, a sprint route), and
 * the genuine per-trial proximity radii live in {@link InnerTrialManager} where they are actually
 * applied. A former uniform {@code interactionRadius} field was unused dead data and was removed so
 * the balancing surface is truthful.
 */
public record InnerTrialDefinition(
        DrugId drug,
        String titleKey,
        String hintKey
) {
    private static final Map<DrugId, InnerTrialDefinition> DEFINITIONS = definitions();

    private static final List<BlockPos> FOUR_CARDINAL = List.of(
            new BlockPos(0, 0, -6),
            new BlockPos(6, 0, 0),
            new BlockPos(0, 0, 6),
            new BlockPos(-6, 0, 0)
    );
    private static final List<BlockPos> FIVE_PRISM = List.of(
            new BlockPos(0, 0, -7),
            new BlockPos(7, 0, -2),
            new BlockPos(4, 0, 6),
            new BlockPos(-4, 0, 6),
            new BlockPos(-7, 0, -2)
    );
    private static final List<BlockPos> THREE_FAULT = List.of(
            new BlockPos(-7, 0, -4),
            new BlockPos(7, 0, -4),
            new BlockPos(0, 0, 7)
    );

    public static InnerTrialDefinition forDrug(DrugId drug) {
        return DEFINITIONS.get(drug);
    }

    public static List<BlockPos> tobaccoOrder(int centerX, int centerZ) {
        return rotated(FOUR_CARDINAL, deterministicIndex(centerX, centerZ, DrugId.TOBACCO, FOUR_CARDINAL.size()));
    }

    public static List<BlockPos> hashSockets() {
        return FOUR_CARDINAL;
    }

    public static List<BlockPos> lsdNodes() {
        return FIVE_PRISM;
    }

    public static int lsdTrueNodeIndex(int centerX, int centerZ) {
        return deterministicIndex(centerX, centerZ, DrugId.LSD, FIVE_PRISM.size());
    }

    public static List<BlockPos> methNodes() {
        return THREE_FAULT;
    }

    public static List<BlockPos> mushroomRoots() {
        return FOUR_CARDINAL;
    }

    static int horizontalIndex(BlockPos clicked, BlockPos center, List<BlockPos> offsets) {
        for (int i = 0; i < offsets.size(); i++) {
            BlockPos offset = offsets.get(i);
            if (clicked.getX() == center.getX() + offset.getX()
                    && clicked.getZ() == center.getZ() + offset.getZ()) {
                return i;
            }
        }
        return -1;
    }

    private static int deterministicIndex(int centerX, int centerZ, DrugId drug, int bound) {
        long hash = InnerNoise.mix64(
                InnerTerrain.seedForSlot(centerX, centerZ)
                        ^ ((long) drug.networkId() * 0x9E37_79B9L)
                        ^ 0x5452_4941_4C53L
        );
        return Math.floorMod((int) hash, bound);
    }

    private static List<BlockPos> rotated(List<BlockPos> values, int offset) {
        return java.util.stream.IntStream.range(0, values.size())
                .mapToObj(index -> values.get((index + offset) % values.size()))
                .toList();
    }

    private static Map<DrugId, InnerTrialDefinition> definitions() {
        EnumMap<DrugId, InnerTrialDefinition> definitions = new EnumMap<>(DrugId.class);
        for (DrugId drug : CuratedDrugChain.ORDER) {
            String name = drug.serializedName();
            definitions.put(drug, new InnerTrialDefinition(
                    drug,
                    "message.mydrugs.inner_trial." + name + ".title",
                    "message.mydrugs.inner_trial." + name + ".hint"
            ));
        }
        return Map.copyOf(definitions);
    }
}
