package org.mydrugs.mydrugs.core.drug.integration;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

/**
 * The Integration Core ladder. Each curated drug requires a core of at least its assigned tier;
 * a higher tier substitutes for any lower one (Pristine works for COFFEE if you've made one).
 *
 * <p>The legacy {@code mydrugs:integration_core} item maps to {@link #CRUDE} so saves from before
 * the ladder existed keep working — that registry id is intentionally not renamed.
 *
 * <p>The actual {@link Item} suppliers are bound at runtime by
 * {@link IntegrationCoreTiers#bind(IntegrationCoreTier, Supplier)} during mod registration to
 * avoid a hard {@code ModItems} dependency from this enum.
 */
public enum IntegrationCoreTier {
    CRUDE(0, "crude"),
    BASIC(1, "basic"),
    ADVANCED(2, "advanced"),
    REFINED(3, "refined"),
    PRISTINE(4, "pristine"),
    PRIME(5, "prime");

    private static final Map<DrugId, IntegrationCoreTier> REQUIRED = new EnumMap<>(DrugId.class);

    static {
        REQUIRED.put(DrugId.COFFEE, CRUDE);
        REQUIRED.put(DrugId.TOBACCO, BASIC);
        REQUIRED.put(DrugId.WEED, BASIC);
        REQUIRED.put(DrugId.HASH, ADVANCED);
        REQUIRED.put(DrugId.ALCOHOL, ADVANCED);
        REQUIRED.put(DrugId.COCAINE, REFINED);
        REQUIRED.put(DrugId.LSD, PRISTINE);
        REQUIRED.put(DrugId.METH, PRISTINE);
        REQUIRED.put(DrugId.MUSHROOMS, PRIME);
    }

    private final int rank;
    private final String id;

    IntegrationCoreTier(int rank, String id) {
        this.rank = rank;
        this.id = id;
    }

    public int rank() {
        return rank;
    }

    public String id() {
        return id;
    }

    /** Translation key for the tier label in tooltips and lang. */
    public String translationKey() {
        return "integration.mydrugs.core_tier." + id;
    }

    public boolean satisfies(IntegrationCoreTier required) {
        return required != null && this.rank >= required.rank;
    }

    /** The minimum tier required to integrate {@code drugId}, or {@code null} for uncurated drugs. */
    public static @Nullable IntegrationCoreTier requiredFor(DrugId drugId) {
        return drugId == null ? null : REQUIRED.get(drugId);
    }

    /** The tier of the core represented by {@code stack}, or {@code null} if not a core item. */
    public static @Nullable IntegrationCoreTier ofStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return null;
        }
        return IntegrationCoreTiers.tierOfItem(stack.getItem());
    }

    public static @Nullable IntegrationCoreTier byId(String id) {
        if (id == null) {
            return null;
        }
        String normalized = id.trim().toLowerCase(Locale.ROOT);
        for (IntegrationCoreTier tier : values()) {
            if (tier.id.equals(normalized)) {
                return tier;
            }
        }
        return null;
    }

    public static @Nullable IntegrationCoreTier byRank(int rank) {
        for (IntegrationCoreTier tier : values()) {
            if (tier.rank == rank) {
                return tier;
            }
        }
        return null;
    }
}
