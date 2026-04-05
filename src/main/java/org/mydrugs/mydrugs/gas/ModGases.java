package org.mydrugs.mydrugs.gas;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import javax.annotation.Nullable;
import java.util.*;

public final class ModGases {
    public static final GasType CHLORINE = register("chlorine", 0xFFD6E86A, true, false);
    public static final GasType HYDROCHLORIC = register("hydrochloric", 0xFFD6E86A, true, false);
    public static final GasType REACTIVE = register("reactive", 0xFFD6E86A, true, false);
    public static final GasType SULFUR = register("sulfur", 0xFFD6E86A, true, false);

    private static final Map<ResourceLocation, GasType> BY_ID = new LinkedHashMap<>();
    private static final List<GasType> SYNC_ORDER = new ArrayList<>();

    private ModGases() {}

    public static GasType register(String path, int tint, boolean toxic, boolean flammable) {
        GasType gas = new GasType(
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path),
                tint,
                toxic,
                flammable
        );
        BY_ID.put(gas.id(), gas);
        SYNC_ORDER.add(gas);
        return gas;
    }

    public static @Nullable GasType get(ResourceLocation id) {
        return BY_ID.get(id);
    }

    public static int getSyncId(@Nullable GasType gas) {
        if (gas == null) {
            return -1;
        }
        return SYNC_ORDER.indexOf(gas);
    }

    public static @Nullable GasType bySyncId(int id) {
        if (id < 0 || id >= SYNC_ORDER.size()) {
            return null;
        }
        return SYNC_ORDER.get(id);
    }

    public static Collection<GasType> values() {
        return Collections.unmodifiableCollection(BY_ID.values());
    }
}