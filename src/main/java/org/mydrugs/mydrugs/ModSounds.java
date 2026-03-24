package org.mydrugs.mydrugs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MyDrugs.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT =
            SOUND_EVENTS.register("heartbeat",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "heartbeat")
                    )
            );

    public static SoundEvent fromEffectType(EffectType effectType) {
        if (effectType.getCategory() != EffectCategory.SOUND_EFFECT) return null;
        return switch (effectType) {
            case HEARTBEAT -> HEARTBEAT.get();
            default -> null;
        };
    }
}
