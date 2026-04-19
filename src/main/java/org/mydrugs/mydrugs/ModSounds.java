package org.mydrugs.mydrugs;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, MyDrugs.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT =
            SOUND_EVENTS.register("heartbeat",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "heartbeat")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> SINGLE_HEARTBEAT =
            SOUND_EVENTS.register("single_heartbeat",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "single_heartbeat")
                    )
            );

    public static final DeferredHolder<SoundEvent, SoundEvent> SMOKE =
            SOUND_EVENTS.register("smoke",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "smoke")
                    )
            );

    public static final Supplier<SoundEvent> INTRUSIVE_WHISPER =
            SOUND_EVENTS.register("intrusive_whisper",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "intrusive_whisper")));

    public static final Supplier<SoundEvent> HALLUCINATION_CUE =
            SOUND_EVENTS.register("hallucination_cue",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "hallucination_cue")));

    public static final Supplier<SoundEvent> GOODVIBES_MUSIC =
            SOUND_EVENTS.register("goodvibes_music",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "goodvibes_music")));

    public static final Supplier<SoundEvent> WRITE =
            SOUND_EVENTS.register("write",
                    () -> SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "write")));

    public static SoundEvent fromEffectType(EffectType effectType) {
        if (effectType.getCategory() != EffectCategory.SOUND_EFFECT) return null;
        return switch (effectType) {
            case HEARTBEAT -> HEARTBEAT.get();
            default -> null;
        };
    }
}
