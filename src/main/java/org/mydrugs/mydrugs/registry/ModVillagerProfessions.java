package org.mydrugs.mydrugs.registry;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

import java.util.function.Supplier;

public final class ModVillagerProfessions {
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, MyDrugs.MODID);

    public static final Supplier<VillagerProfession> THERAPIST =
            PROFESSIONS.register("therapist", () ->
                    new VillagerProfession(
                            Component.literal("therapist"),
                            holder -> holder.is(ModPoiTypes.THERAPIST_POI_KEY),
                            holder -> holder.is(ModPoiTypes.THERAPIST_POI_KEY),
                            ImmutableSet.of(),
                            ImmutableSet.of(),
                            SoundEvents.VILLAGER_WORK_CLERIC
                    )
            );

    private ModVillagerProfessions() {
    }

    public static void register(IEventBus modBus) {
        PROFESSIONS.register(modBus);
    }
}