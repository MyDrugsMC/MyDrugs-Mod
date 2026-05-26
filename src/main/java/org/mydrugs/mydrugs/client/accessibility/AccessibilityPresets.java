package org.mydrugs.mydrugs.client.accessibility;

import java.util.List;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.mydrugs.mydrugs.Config;

/**
 * Applies the named accessibility presets by writing the individual {@link Config.Client} values.
 * Presets are a starting point: once the player edits any single control the screen switches the
 * stored preset to {@code custom}. All values written here are client-only presentation settings;
 * server gameplay mechanics are never touched.
 */
public final class AccessibilityPresets {
    private AccessibilityPresets() {
    }

    /** Every client value a preset or the reset button is allowed to rewrite. */
    private static List<ModConfigSpec.ConfigValue<?>> managedValues() {
        Config.Client c = Config.CLIENT;
        return List.of(
                c.enableDrugShaders, c.enablePsychedelicShaders, c.shaderIntensity, c.overlayIntensity,
                c.colorShiftIntensity, c.blurIntensity, c.staticNoiseIntensity, c.maxFlashBrightness,
                c.disableFullScreenFlashing, c.colorblindSafeMode, c.particleDensityMultiplier,
                c.dimensionFogIntensity, c.oreAuraIntensity, c.enableCameraShake, c.cameraShakeIntensity,
                c.cameraSwayIntensity, c.fovPulseIntensity, c.disableForcedCameraMovement,
                c.disableScreenRoll, c.reducedMotionMode, c.enableHallucinations, c.hallucinationIntensity,
                c.enableHeartbeatSounds, c.heartbeatVolume, c.enableDrugSounds, c.hallucinationSoundVolume,
                c.machineBreathingVolume, c.badTripVoiceVolume, c.disableSuddenLoudSounds,
                c.showAddictionHud, c.compactAddictionHud, c.enableBadTripScreamers, c.screamerIntensity,
                c.screamerVolumeCap, c.muteScreamers, c.replaceInputFailWithHudWarningOnly,
                c.showActiveEffectExplanations, c.showMachineMoodStatusText, c.showGeneratorCravingsAsText);
    }

    @SuppressWarnings("unchecked")
    private static <T> void resetToDefault(ModConfigSpec.ConfigValue<?> value) {
        ModConfigSpec.ConfigValue<T> typed = (ModConfigSpec.ConfigValue<T>) value;
        typed.set(typed.getDefault());
    }

    /** Resets every managed control to its spec default and selects the Full Experience preset. */
    public static void restoreDefaults() {
        for (ModConfigSpec.ConfigValue<?> value : managedValues()) {
            resetToDefault(value);
        }
        Config.CLIENT.accessibilityPreset.set(Config.Client.PRESET_FULL_EXPERIENCE);
    }

    /** Applies a named preset. Unknown or {@code custom} values fall back to Full Experience. */
    public static void apply(String presetId) {
        String preset = Config.Client.normalizePreset(presetId);
        restoreDefaults();
        switch (preset) {
            case Config.Client.PRESET_REDUCED_MOTION -> applyReducedMotion();
            case Config.Client.PRESET_LOW_INTENSITY -> applyLowIntensity();
            case Config.Client.PRESET_MINIMAL_EFFECTS -> applyMinimalEffects();
            default -> {
                // full_experience and custom both leave the spec defaults in place.
            }
        }
        Config.CLIENT.accessibilityPreset.set(
                preset.equals(Config.Client.PRESET_CUSTOM) ? Config.Client.PRESET_FULL_EXPERIENCE : preset);
    }

    private static void applyReducedMotion() {
        Config.Client c = Config.CLIENT;
        c.enableCameraShake.set(false);
        c.cameraShakeIntensity.set(0.0D);
        c.cameraSwayIntensity.set(0.25D);
        c.fovPulseIntensity.set(0.25D);
        c.disableForcedCameraMovement.set(true);
        c.disableScreenRoll.set(true);
        c.reducedMotionMode.set(true);
        c.overlayIntensity.set(0.7D);
        c.blurIntensity.set(0.6D);
    }

    private static void applyLowIntensity() {
        Config.Client c = Config.CLIENT;
        c.shaderIntensity.set(0.5D);
        c.overlayIntensity.set(0.6D);
        c.colorShiftIntensity.set(0.6D);
        c.blurIntensity.set(0.6D);
        c.staticNoiseIntensity.set(0.5D);
        c.maxFlashBrightness.set(0.6D);
        c.disableFullScreenFlashing.set(true);
        c.particleDensityMultiplier.set(0.5D);
        c.hallucinationIntensity.set(0.6D);
        c.cameraShakeIntensity.set(0.5D);
        c.heartbeatVolume.set(0.5D);
        c.badTripVoiceVolume.set(0.6D);
        c.screamerVolumeCap.set(0.4D);
        c.enableBadTripScreamers.set(false);
        c.screamerIntensity.set(0.0D);
    }

    private static void applyMinimalEffects() {
        Config.Client c = Config.CLIENT;
        c.enablePsychedelicShaders.set(false);
        c.shaderIntensity.set(0.0D);
        c.overlayIntensity.set(0.3D);
        c.colorShiftIntensity.set(0.0D);
        c.blurIntensity.set(0.0D);
        c.staticNoiseIntensity.set(0.0D);
        c.maxFlashBrightness.set(0.2D);
        c.disableFullScreenFlashing.set(true);
        c.particleDensityMultiplier.set(0.2D);
        c.dimensionFogIntensity.set(0.3D);
        c.oreAuraIntensity.set(0.3D);
        c.enableHallucinations.set(false);
        c.hallucinationIntensity.set(0.0D);
        c.enableCameraShake.set(false);
        c.cameraShakeIntensity.set(0.0D);
        c.cameraSwayIntensity.set(0.0D);
        c.fovPulseIntensity.set(0.0D);
        c.disableForcedCameraMovement.set(true);
        c.disableScreenRoll.set(true);
        c.reducedMotionMode.set(true);
        c.heartbeatVolume.set(0.4D);
        c.hallucinationSoundVolume.set(0.0D);
        c.machineBreathingVolume.set(0.3D);
        c.badTripVoiceVolume.set(0.0D);
        c.disableSuddenLoudSounds.set(true);
        c.screamerVolumeCap.set(0.0D);
        c.muteScreamers.set(true);
        c.enableBadTripScreamers.set(false);
        c.screamerIntensity.set(0.0D);
        c.compactAddictionHud.set(true);
        c.replaceInputFailWithHudWarningOnly.set(true);
        c.showActiveEffectExplanations.set(true);
        c.showMachineMoodStatusText.set(true);
        c.showGeneratorCravingsAsText.set(true);
    }
}
