package org.mydrugs.mydrugs.energy.stillhouse;

import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.Map;
import java.util.Optional;

public record StillhouseBurnerFuel(ResourceLocation fluidId, int currentPerMb) {
    public static final int FUSEL_OIL_PC_PER_MB = 4;
    public static final int RAW_ALCOHOL_PC_PER_MB = 10;
    public static final int ETHANOL_PC_PER_MB = 18;

    private static final Map<ResourceLocation, StillhouseBurnerFuel> FUELS = Map.of(
            id("fusel_oil"), new StillhouseBurnerFuel(id("fusel_oil"), FUSEL_OIL_PC_PER_MB),
            id("raw_alcohol"), new StillhouseBurnerFuel(id("raw_alcohol"), RAW_ALCOHOL_PC_PER_MB),
            id("ethanol"), new StillhouseBurnerFuel(id("ethanol"), ETHANOL_PC_PER_MB)
    );

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, path);
    }

    public static Optional<StillhouseBurnerFuel> get(ResourceLocation fluidId) {
        return Optional.ofNullable(FUELS.get(fluidId));
    }

    public static boolean isFuel(ResourceLocation fluidId) {
        return FUELS.containsKey(fluidId);
    }
}
