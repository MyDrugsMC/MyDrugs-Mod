package org.mydrugs.mydrugs.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlocks;

import java.util.function.Supplier;

public final class ModPoiTypes {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MyDrugs.MODID);

    public static final ResourceKey<PoiType> THERAPIST_POI_KEY =
            ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "therapist"));

    public static final Supplier<PoiType> THERAPIST_POI =
            POI_TYPES.register("therapist", () ->
                    new PoiType(
                            java.util.Set.copyOf(ModBlocks.THERAPIST_DESK.get().getStateDefinition().getPossibleStates()),
                            1,
                            1
                    )
            );

    private ModPoiTypes() {}

    public static void register(IEventBus modBus) {
        POI_TYPES.register(modBus);
    }
}