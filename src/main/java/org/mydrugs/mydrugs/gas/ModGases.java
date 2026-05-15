package org.mydrugs.mydrugs.gas;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.*;

public final class ModGases {
    private static final Map<ResourceLocation, GasType> BY_ID = new LinkedHashMap<>();
    private static final List<GasType> SYNC_ORDER = new ArrayList<>();

    public static final GasType CHLORINE = register(new GasSpec("chlorine", 0xFFD6E86A, true, false));
    public static final GasType HYDROGEN_CHLORIDE = register(new GasSpec("hydrogen_chloride", 0xFFD6E86A, true, false));
    public static final GasType SULFUR_DIOXIDE = register(new GasSpec("sulfur_dioxide", 0xFFD6E86A, true, false));
    public static final GasType SULFUR_TRIOXIDE = register(new GasSpec("sulfur_trioxide", 0xFFD6E86A, true, false));
    public static final GasType SULFUR_VAPOR = register(new GasSpec("sulfur_vapor", 0xFFD6E86A, true, false));
    public static final GasType AIR = register(new GasSpec("air", 0xFFFFFFFF, false, false));
    public static final GasType METHANE = register(new GasSpec("methane", 0xFFA8E6FF, false, true));
    public static final GasType ETHYLENE = register(new GasSpec("ethylene", 0xFFB3F2FF, false, true));
    public static final GasType PROPYLENE = register(new GasSpec("propylene", 0xFFC8E5FF, false, true));
    public static final GasType CRUDE_C4_MIX = register(new GasSpec("crude_c4_mix", 0xFFFFC266, false, true));
    public static final GasType SODIUM_HYDROXIDE = register(new GasSpec("sodium_hydroxide", 0xFFFFFFFF, true, true));
    public static final GasType OXYGEN = register(new GasSpec("oxygen", 0xFFFFFFFF, false, true));
    public static final GasType HYDROGEN = register(new GasSpec("hydrogen", 0xFFFFFFFF, false, true));

    private ModGases() {
    }

    private static GasType register(GasSpec spec) {
        GasType gas = new GasType(
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, spec.path()),
                spec.tint(),
                spec.toxic(),
                spec.flammable()
        );
        BY_ID.put(gas.id(), gas);
        SYNC_ORDER.add(gas);
        return gas;
    }

    public static @Nullable GasType get(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static int getSyncId(@Nullable GasType gas) {
        return gas == null ? -1 : SYNC_ORDER.indexOf(gas);
    }

    public static @Nullable GasType bySyncId(int id) {
        return id < 0 || id >= SYNC_ORDER.size() ? null : SYNC_ORDER.get(id);
    }

    public static Collection<GasType> values() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }

    public static @Nullable GasType getNullable(String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return get(ResourceLocation.parse(id));
    }
}
