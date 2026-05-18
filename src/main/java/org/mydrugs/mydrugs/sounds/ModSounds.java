package org.mydrugs.mydrugs.sounds;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualAction;
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

    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_SNEAK = registerSound("psy_mixer_voice_sneak");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_JUMP = registerSound("psy_mixer_voice_jump");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_RIGHT_CLICK_AIR = registerSound("psy_mixer_voice_right_click_air");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_WALK_RING = registerSound("psy_mixer_voice_walk_ring");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_LOOK_AT_CORE = registerSound("psy_mixer_voice_look_at_core");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_TIMING_RING = registerSound("psy_mixer_voice_timing_ring");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_STAND_STILL = registerSound("psy_mixer_voice_stand_still");
    public static final Supplier<SoundEvent> PSY_MIXER_VOICE_REOPEN_GUI = registerSound("psy_mixer_voice_reopen_gui");

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name,
                () -> SoundEvent.createVariableRangeEvent(
                        ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, name)));
    }

    public static SoundEvent fromEffectType(EffectType effectType) {
        if (effectType.getCategory() != EffectCategory.SOUND_EFFECT) return null;
        return switch (effectType) {
            case HEARTBEAT -> HEARTBEAT.get();
            default -> null;
        };
    }

    public static SoundEvent psyMixerVoice(PsyMixerRitualAction action) {
        return switch (action) {
            case SNEAK -> PSY_MIXER_VOICE_SNEAK.get();
            case JUMP -> PSY_MIXER_VOICE_JUMP.get();
            case RIGHT_CLICK_AIR -> PSY_MIXER_VOICE_RIGHT_CLICK_AIR.get();
            case WALK_RING -> PSY_MIXER_VOICE_WALK_RING.get();
            case LOOK_AT_CORE -> PSY_MIXER_VOICE_LOOK_AT_CORE.get();
            case TIMING_RING -> PSY_MIXER_VOICE_TIMING_RING.get();
            case STAND_STILL -> PSY_MIXER_VOICE_STAND_STILL.get();
            case REOPEN_GUI -> PSY_MIXER_VOICE_REOPEN_GUI.get();
            default -> null;
        };
    }
}
