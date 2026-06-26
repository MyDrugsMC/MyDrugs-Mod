package org.mydrugs.mydrugs;

import java.util.Locale;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

@EventBusSubscriber(modid = MyDrugs.MODID)
public class Config {
    public static final Client CLIENT;
    public static final ModConfigSpec CLIENT_SPEC;
    public static final Server SERVER;
    public static final ModConfigSpec SERVER_SPEC;
    public static final Worldgen WORLDGEN;
    public static final ModConfigSpec WORLDGEN_SPEC;

    static {
        ModConfigSpec.Builder clientBuilder = new ModConfigSpec.Builder();
        CLIENT = new Client(clientBuilder);
        CLIENT_SPEC = clientBuilder.build();

        ModConfigSpec.Builder serverBuilder = new ModConfigSpec.Builder();
        SERVER = new Server(serverBuilder);
        SERVER_SPEC = serverBuilder.build();

        ModConfigSpec.Builder worldgenBuilder = new ModConfigSpec.Builder();
        WORLDGEN = new Worldgen(worldgenBuilder);
        WORLDGEN_SPEC = worldgenBuilder.build();
    }

    public static final class Client {
        public static final String PRESET_FULL_EXPERIENCE = "full_experience";
        public static final String PRESET_REDUCED_MOTION = "reduced_motion";
        public static final String PRESET_LOW_INTENSITY = "low_intensity";
        public static final String PRESET_MINIMAL_EFFECTS = "minimal_effects";
        public static final String PRESET_CUSTOM = "custom";
        public static final String HUD_ANCHOR_LEFT = "left";
        public static final String HUD_ANCHOR_RIGHT = "right";
        public static final String HUD_ANCHOR_TOP_LEFT = "top_left";
        public static final String HUD_ANCHOR_TOP_RIGHT = "top_right";
        public static final String HUD_ANCHOR_BOTTOM_LEFT = "bottom_left";
        public static final String HUD_ANCHOR_BOTTOM_RIGHT = "bottom_right";

        public final ModConfigSpec.ConfigValue<String> accessibilityPreset;
        public final ModConfigSpec.BooleanValue enableDrugShaders;
        public final ModConfigSpec.BooleanValue enablePsychedelicShaders;
        public final ModConfigSpec.DoubleValue shaderIntensity;
        public final ModConfigSpec.DoubleValue overlayIntensity;
        public final ModConfigSpec.DoubleValue colorShiftIntensity;
        public final ModConfigSpec.DoubleValue blurIntensity;
        public final ModConfigSpec.DoubleValue staticNoiseIntensity;
        public final ModConfigSpec.DoubleValue maxFlashBrightness;
        public final ModConfigSpec.BooleanValue disableFullScreenFlashing;
        public final ModConfigSpec.BooleanValue colorblindSafeMode;
        public final ModConfigSpec.DoubleValue particleDensityMultiplier;
        public final ModConfigSpec.DoubleValue dimensionFogIntensity;
        public final ModConfigSpec.DoubleValue oreAuraIntensity;
        public final ModConfigSpec.BooleanValue enableCameraShake;
        public final ModConfigSpec.DoubleValue cameraShakeIntensity;
        public final ModConfigSpec.DoubleValue cameraSwayIntensity;
        public final ModConfigSpec.DoubleValue fovPulseIntensity;
        public final ModConfigSpec.BooleanValue disableForcedCameraMovement;
        public final ModConfigSpec.BooleanValue disableScreenRoll;
        public final ModConfigSpec.BooleanValue enableHallucinations;
        public final ModConfigSpec.DoubleValue hallucinationIntensity;
        public final ModConfigSpec.BooleanValue hallucinationSilhouetteOnly;
        public final ModConfigSpec.BooleanValue smoothHallucinationTransitions;
        public final ModConfigSpec.DoubleValue hallucinationSpawnDistanceScale;
        public final ModConfigSpec.BooleanValue enableHeartbeatSounds;
        public final ModConfigSpec.DoubleValue heartbeatVolume;
        public final ModConfigSpec.BooleanValue enableDrugSounds;
        public final ModConfigSpec.DoubleValue hallucinationSoundVolume;
        public final ModConfigSpec.DoubleValue machineBreathingVolume;
        public final ModConfigSpec.DoubleValue badTripVoiceVolume;
        public final ModConfigSpec.BooleanValue disableSuddenLoudSounds;
        public final ModConfigSpec.BooleanValue showAddictionHud;
        public final ModConfigSpec.BooleanValue compactAddictionHud;
        public final ModConfigSpec.ConfigValue<String> addictionHudAnchor;
        public final ModConfigSpec.DoubleValue addictionHudScale;
        public final ModConfigSpec.BooleanValue addictionHudTextLabels;
        public final ModConfigSpec.IntValue addictionHudSafeArea;
        public final ModConfigSpec.BooleanValue reducedMotionMode;
        public final ModConfigSpec.BooleanValue enableBadTripScreamers;
        public final ModConfigSpec.DoubleValue screamerIntensity;
        public final ModConfigSpec.DoubleValue screamerVolumeCap;
        public final ModConfigSpec.BooleanValue muteScreamers;
        public final ModConfigSpec.BooleanValue replaceInputFailWithHudWarningOnly;
        public final ModConfigSpec.BooleanValue showActiveEffectExplanations;
        public final ModConfigSpec.BooleanValue showMachineMoodStatusText;
        public final ModConfigSpec.BooleanValue showGeneratorCravingsAsText;
        public final ModConfigSpec.BooleanValue innerPostProcessing;
        public final ModConfigSpec.DoubleValue innerAmbientVolume;

        private Client(ModConfigSpec.Builder builder) {
            builder.push("accessibility");
            accessibilityPreset = builder
                    .comment("Accessibility preset. Valid values: full_experience, reduced_motion, low_intensity, minimal_effects, custom.")
                    .define("accessibilityPreset", PRESET_FULL_EXPERIENCE);

            builder.push("visual");
            enableDrugShaders = builder
                    .comment("Legacy master switch for MyDrugs full-screen shader effects. Kept for existing configs.")
                    .define("enableDrugShaders", true);
            enablePsychedelicShaders = builder
                    .comment("Enable psychedelic/full-screen shader presentation. Client-only; gameplay effects still run on the server.")
                    .define("enablePsychedelicShaders", true);
            shaderIntensity = builder
                    .comment("Global shader intensity multiplier. 0 disables shader strength, 1 is default, 2 is extra strong.")
                    .defineInRange("shaderIntensity", 1.0D, 0.0D, 2.0D);
            overlayIntensity = builder
                    .comment("Full-screen overlay opacity multiplier. Client-only.")
                    .defineInRange("overlayIntensity", 1.0D, 0.0D, 1.0D);
            colorShiftIntensity = builder
                    .comment("Color shift/chromatic presentation intensity. Client-only.")
                    .defineInRange("colorShiftIntensity", 1.0D, 0.0D, 1.0D);
            blurIntensity = builder
                    .comment("Blur/nausea presentation intensity. Client-only.")
                    .defineInRange("blurIntensity", 1.0D, 0.0D, 1.0D);
            staticNoiseIntensity = builder
                    .comment("Static/noise presentation intensity. Client-only.")
                    .defineInRange("staticNoiseIntensity", 1.0D, 0.0D, 1.0D);
            maxFlashBrightness = builder
                    .comment("Maximum brightness/opacity used by flash-like overlays. Client-only.")
                    .defineInRange("maxFlashBrightness", 1.0D, 0.0D, 1.0D);
            disableFullScreenFlashing = builder
                    .comment("Disable high-intensity full-screen flashing. Bad-trip mechanics still happen.")
                    .define("disableFullScreenFlashing", false);
            colorblindSafeMode = builder
                    .comment("Prefer higher-contrast, less hue-dependent overlay colors where supported.")
                    .define("colorblindSafeMode", false);
            particleDensityMultiplier = builder
                    .comment("Client visual particle density multiplier for MyDrugs effects.")
                    .defineInRange("particleDensityMultiplier", 1.0D, 0.0D, 1.0D);
            dimensionFogIntensity = builder
                    .comment("Dimension fog presentation intensity. Client-only.")
                    .defineInRange("dimensionFogIntensity", 1.0D, 0.0D, 1.0D);
            oreAuraIntensity = builder
                    .comment("Ore aura presentation intensity. 0 hides the client aura only; server mining effects are unchanged.")
                    .defineInRange("oreAuraIntensity", 1.0D, 0.0D, 1.0D);
            builder.pop();

            builder.push("motion");
            enableCameraShake = builder
                    .comment("Legacy master switch for camera movement effects.")
                    .define("enableCameraShake", true);
            cameraShakeIntensity = builder
                    .comment("Camera tremor/shake intensity multiplier.")
                    .defineInRange("cameraShakeIntensity", 1.0D, 0.0D, 2.0D);
            cameraSwayIntensity = builder
                    .comment("Slow camera sway intensity multiplier.")
                    .defineInRange("cameraSwayIntensity", 1.0D, 0.0D, 1.0D);
            fovPulseIntensity = builder
                    .comment("FOV pulse intensity multiplier.")
                    .defineInRange("fovPulseIntensity", 1.0D, 0.0D, 1.0D);
            disableForcedCameraMovement = builder
                    .comment("Disable forced camera movement and large view kicks. Does not disable server mechanics.")
                    .define("disableForcedCameraMovement", false);
            disableScreenRoll = builder
                    .comment("Disable camera roll while keeping yaw/pitch sway if enabled.")
                    .define("disableScreenRoll", false);
            reducedMotionMode = builder
                    .comment("Legacy reduced-motion flag used by older client effects.")
                    .define("reducedMotionMode", false);
            builder.pop();

            builder.push("audio");
            enableHeartbeatSounds = builder
                    .comment("Enable heartbeat sounds. Visual HUD pulses may still show if disabled.")
                    .define("enableHeartbeatSounds", true);
            heartbeatVolume = builder
                    .comment("Heartbeat sound volume multiplier.")
                    .defineInRange("heartbeatVolume", 1.0D, 0.0D, 1.0D);
            enableDrugSounds = builder
                    .comment("Enable MyDrugs client-side drug sound loops.")
                    .define("enableDrugSounds", true);
            hallucinationSoundVolume = builder
                    .comment("Hallucination cue volume multiplier.")
                    .defineInRange("hallucinationSoundVolume", 1.0D, 0.0D, 1.0D);
            machineBreathingVolume = builder
                    .comment("Future machine breathing/ambient machine audio volume multiplier.")
                    .defineInRange("machineBreathingVolume", 1.0D, 0.0D, 1.0D);
            badTripVoiceVolume = builder
                    .comment("Bad-trip voice/intrusive whisper volume multiplier.")
                    .defineInRange("badTripVoiceVolume", 1.0D, 0.0D, 1.0D);
            disableSuddenLoudSounds = builder
                    .comment("Soften sudden MyDrugs audio spikes where client-side control is possible.")
                    .define("disableSuddenLoudSounds", false);
            screamerVolumeCap = builder
                    .comment("Maximum volume cap for screamer audio. 0 is silent, 1 is default cap.")
                    .defineInRange("screamerVolumeCap", 1.0D, 0.0D, 1.0D);
            muteScreamers = builder
                    .comment("Mute screamer audio and use text/HUD alternatives.")
                    .define("muteScreamers", false);
            builder.pop();

            builder.push("presentation");
            enableHallucinations = builder
                    .comment("Enable client hallucination entities and hallucination presentation.")
                    .define("enableHallucinations", true);
            hallucinationIntensity = builder
                    .comment("Hallucination spawn/opacity intensity multiplier.")
                    .defineInRange("hallucinationIntensity", 1.0D, 0.0D, 1.0D);
            hallucinationSilhouetteOnly = builder
                    .comment("Render hallucinations as silhouettes without glowing eyes.")
                    .define("hallucinationSilhouetteOnly", false);
            smoothHallucinationTransitions = builder
                    .comment("Use longer hallucination fades and strongly reduced bob/sway motion.")
                    .define("smoothHallucinationTransitions", false);
            hallucinationSpawnDistanceScale = builder
                    .comment("Scales the client-only hallucination spawn distance.")
                    .defineInRange("hallucinationSpawnDistanceScale", 1.0D, 0.5D, 2.0D);
            showAddictionHud = builder
                    .comment("Show MyDrugs addiction/effect HUD indicators.")
                    .define("showAddictionHud", true);
            compactAddictionHud = builder
                    .comment("Use compact static HUD indicators with fewer animated icons.")
                    .define("compactAddictionHud", false);
            addictionHudAnchor = builder
                    .comment("Addiction effect icon anchor. Valid values: left, right, top_left, top_right, bottom_left, bottom_right.")
                    .define("addictionHudAnchor", HUD_ANCHOR_LEFT);
            addictionHudScale = builder
                    .comment("Scale of addiction effect icons and optional labels.")
                    .defineInRange("addictionHudScale", 1.0D, 0.65D, 1.75D);
            addictionHudTextLabels = builder
                    .comment("Show readable text labels beside addiction HUD effect icons.")
                    .define("addictionHudTextLabels", false);
            addictionHudSafeArea = builder
                    .comment("Distance in GUI pixels between the addiction HUD and the selected screen edge.")
                    .defineInRange("addictionHudSafeArea", 8, 0, 48);
            enableBadTripScreamers = builder
                    .comment("Enable bad-trip screamer image overlays. Bad-trip mechanics still happen when disabled.")
                    .define("enableBadTripScreamers", true);
            screamerIntensity = builder
                    .comment("Legacy bad-trip screamer visual intensity multiplier.")
                    .defineInRange("screamerIntensity", 1.0D, 0.0D, 2.0D);
            replaceInputFailWithHudWarningOnly = builder
                    .comment("Request HUD-only input-fail presentation. Server policy may keep input disruption enabled.")
                    .define("replaceInputFailWithHudWarningOnly", false);
            showActiveEffectExplanations = builder
                    .comment("Show text alternatives/explanations for active custom effects where supported.")
                    .define("showActiveEffectExplanations", true);
            showMachineMoodStatusText = builder
                    .comment("Show machine mood/status as text where supported.")
                    .define("showMachineMoodStatusText", true);
            showGeneratorCravingsAsText = builder
                    .comment("Show psychotrope generator cravings as text instead of audio-only cues where supported.")
                    .define("showGeneratorCravingsAsText", true);
            builder.pop();

            builder.push("inner_dimension");
            innerPostProcessing = builder
                    .comment("Enable the Inner Dimension atmospheric post-processing pass (bloom / god-rays / vignette).",
                            "Client-only and forced off under reduced motion. Disable if the post pass misbehaves on your GPU.")
                    .define("innerPostProcessing", true);
            innerAmbientVolume = builder
                    .comment("Master volume for the Inner Dimension ambient soundscape (region drones, heartbeat bed, one-shots).",
                            "0 disables the soundscape entirely.")
                    .defineInRange("innerAmbientVolume", 1.0D, 0.0D, 1.0D);
            builder.pop();
            builder.pop();
        }

        public boolean psychedelicShadersEnabled() {
            return enableDrugShaders.get() && enablePsychedelicShaders.get();
        }

        public float shaderScale() {
            return shaderIntensity.get().floatValue();
        }

        public float visualScale() {
            return overlayIntensity.get().floatValue();
        }

        public float flashCap() {
            return maxFlashBrightness.get().floatValue();
        }

        public float suddenSoundCap(float volume) {
            float capped = disableSuddenLoudSounds.get() ? Math.min(volume, 0.35F) : volume;
            return Math.max(0.0F, Math.min(1.0F, capped));
        }

        public static String normalizePreset(String preset) {
            String value = preset == null ? PRESET_CUSTOM : preset.trim().toLowerCase(Locale.ROOT);
            return switch (value) {
                case PRESET_FULL_EXPERIENCE, PRESET_REDUCED_MOTION, PRESET_LOW_INTENSITY,
                        PRESET_MINIMAL_EFFECTS, PRESET_CUSTOM -> value;
                default -> PRESET_CUSTOM;
            };
        }

        public static String normalizeAddictionHudAnchor(String anchor) {
            String value = anchor == null ? HUD_ANCHOR_LEFT : anchor.trim().toLowerCase(Locale.ROOT);
            return switch (value) {
                case HUD_ANCHOR_LEFT, HUD_ANCHOR_RIGHT, HUD_ANCHOR_TOP_LEFT, HUD_ANCHOR_TOP_RIGHT,
                        HUD_ANCHOR_BOTTOM_LEFT, HUD_ANCHOR_BOTTOM_RIGHT -> value;
                default -> HUD_ANCHOR_LEFT;
            };
        }
    }

    public static final class Server {
        public final ModConfigSpec.BooleanValue addictionEnabled;
        public final ModConfigSpec.BooleanValue enableCookHazards;
        public final ModConfigSpec.DoubleValue cookHazardIntensity;
        public final ModConfigSpec.BooleanValue overdoseEnabled;
        public final ModConfigSpec.BooleanValue withdrawalEnabled;
        public final ModConfigSpec.DoubleValue addictionGainMultiplier;
        public final ModConfigSpec.DoubleValue toleranceGainMultiplier;
        public final ModConfigSpec.DoubleValue toleranceDecayMultiplier;
        public final ModConfigSpec.DoubleValue withdrawalSeverityMultiplier;
        public final ModConfigSpec.DoubleValue overdoseThresholdMultiplier;
        public final ModConfigSpec.DoubleValue safeZoneRecoveryMultiplier;
        public final ModConfigSpec.IntValue recoveryRoomScanRadius;
        public final ModConfigSpec.IntValue recoveryEnvironmentCacheTicks;
        public final ModConfigSpec.IntValue socialReliefCacheTicks;
        public final ModConfigSpec.IntValue headphonesInventoryCacheTicks;
        public final ModConfigSpec.IntValue recoveryRoomMaxVolume;
        public final ModConfigSpec.IntValue recoveryRoomFragileThreshold;
        public final ModConfigSpec.IntValue recoveryRoomRestingThreshold;
        public final ModConfigSpec.IntValue recoveryRoomSafeThreshold;
        public final ModConfigSpec.IntValue recoveryRoomSanctuaryThreshold;
        public final ModConfigSpec.DoubleValue therapyCooldownMultiplier;
        public final ModConfigSpec.BooleanValue allowClientInputFailHudOnly;
        public final ModConfigSpec.BooleanValue enableRecoveryCommands;
        public final ModConfigSpec.BooleanValue enableSurvivalRecoveryRecipes;
        public final ModConfigSpec.IntValue recoveryRitualMaxLevels;
        public final ModConfigSpec.IntValue crudeWireOutputCount;
        public final ModConfigSpec.BooleanValue allowBrokenReceptacle;
        public final ModConfigSpec.BooleanValue allowDreamResidueAsEngineFuel;
        public final ModConfigSpec.BooleanValue allowDebugActionPayloads;
        public final ModConfigSpec.BooleanValue enablePipeDebugCounters;
        public final ModConfigSpec.IntValue pipeTickInterval;

        private Server(ModConfigSpec.Builder builder) {
            builder.push("gameplay");
            addictionEnabled = builder.define("addictionEnabled", true);
            enableCookHazards = builder
                    .comment("Toxic fume hazard during street-meth one-pot cooks. Disable for accessibility.")
                    .define("enableCookHazards", true);
            cookHazardIntensity = builder
                    .comment("Scales fume frequency, poison/nausea level, and fire chance. 0 disables, 1 default.")
                    .defineInRange("cookHazardIntensity", 1.0D, 0.0D, 4.0D);
            overdoseEnabled = builder.define("overdoseEnabled", true);
            withdrawalEnabled = builder.define("withdrawalEnabled", true);
            addictionGainMultiplier = builder.defineInRange("addictionGainMultiplier", 1.0D, 0.0D, 100.0D);
            toleranceGainMultiplier = builder.defineInRange("toleranceGainMultiplier", 1.0D, 0.0D, 100.0D);
            toleranceDecayMultiplier = builder.defineInRange("toleranceDecayMultiplier", 1.0D, 0.0D, 100.0D);
            withdrawalSeverityMultiplier = builder.defineInRange("withdrawalSeverityMultiplier", 1.0D, 0.0D, 100.0D);
            overdoseThresholdMultiplier = builder.defineInRange("overdoseThresholdMultiplier", 1.0D, 0.1D, 100.0D);
            safeZoneRecoveryMultiplier = builder.defineInRange("safeZoneRecoveryMultiplier", 1.0D, 0.0D, 100.0D);
            recoveryRoomScanRadius = builder.defineInRange("recoveryRoomScanRadius", 12, 4, 24);
            recoveryEnvironmentCacheTicks = builder
                    .comment("Server ticks to reuse a player's recovery environment scan while they stand still.",
                            "This throttles the expensive recovery-room anchor cube scan; movement or dimension changes still refresh sooner.")
                    .defineInRange("recoveryEnvironmentCacheTicks", 20, 1, 100);
            socialReliefCacheTicks = builder
                    .comment("Server ticks to reuse nearby companion/entity counts for passive recovery calculations.")
                    .defineInRange("socialReliefCacheTicks", 20, 1, 100);
            headphonesInventoryCacheTicks = builder
                    .comment("Server ticks to reuse recovery inventory checks such as headphones/diary/food presence.")
                    .defineInRange("headphonesInventoryCacheTicks", 20, 1, 100);
            recoveryRoomMaxVolume = builder.defineInRange("recoveryRoomMaxVolume", 220, 48, 512);
            recoveryRoomFragileThreshold = builder.defineInRange("recoveryRoomFragileThreshold", 25, 1, 100);
            recoveryRoomRestingThreshold = builder.defineInRange("recoveryRoomRestingThreshold", 45, 1, 100);
            recoveryRoomSafeThreshold = builder.defineInRange("recoveryRoomSafeThreshold", 65, 1, 100);
            recoveryRoomSanctuaryThreshold = builder.defineInRange("recoveryRoomSanctuaryThreshold", 85, 1, 100);
            therapyCooldownMultiplier = builder.defineInRange("therapyCooldownMultiplier", 1.0D, 0.0D, 100.0D);
            allowClientInputFailHudOnly = builder
                    .comment("Deprecated compatibility toggle. Server-side input-fail balance is always authoritative; clients may only reduce local presentation.")
                    .define("allowClientInputFailHudOnly", false);
            builder.pop();

            builder.push("progression_recovery");
            enableRecoveryCommands = builder
                    .comment("Enable the /mydrugs knowledge|recover|progression admin/rescue commands. Disable to hide them entirely.")
                    .define("enableRecoveryCommands", true);
            enableSurvivalRecoveryRecipes = builder
                    .comment("Enable the survival Psy Mixer recovery rituals (rebind a lost Psy Receptacle, re-make Centrifuge Wires).")
                    .define("enableSurvivalRecoveryRecipes", true);
            recoveryRitualMaxLevels = builder
                    .comment("Maximum experience levels a recovery ritual consumes. Levels also drive success chance:",
                            "0 levels contributes 0% and this cap contributes 50% (the rest comes from the drug used).")
                    .defineInRange("recoveryRitualMaxLevels", 30, 1, 1000);
            crudeWireOutputCount = builder
                    .comment("How many wires a successful wire recovery ritual produces.")
                    .defineInRange("crudeWireOutputCount", 4, 1, 64);
            allowBrokenReceptacle = builder
                    .comment("Allow the Psy Receptacle recovery (rebind) ritual. Disable to remove the survival rebind path for the receptacle specifically; the wire ritual stays available.")
                    .define("allowBrokenReceptacle", true);
            allowDreamResidueAsEngineFuel = builder
                    .comment("Allow Dream Residue and Mycelial Insight to be burned as Psychotrope Engine fuel. Default false: psychedelic materials belong to integration, not factory power.")
                    .define("allowDreamResidueAsEngineFuel", false);
            builder.pop();

            builder.push("admin");
            allowDebugActionPayloads = builder
                    .comment("Allow privileged players (permission level >= 2) to send debug action payloads that mutate addiction stats. Default false; keep disabled on public servers.")
                    .define("allowDebugActionPayloads", false);
            enablePipeDebugCounters = builder
                    .comment("Log pipe network transfer counters once per minute. Intended for development and server diagnostics.")
                    .define("enablePipeDebugCounters", false);
            pipeTickInterval = builder
                    .comment("Server tick interval for pipe transfer work. Transfer amounts are scaled by this interval to preserve approximate throughput.")
                    .defineInRange("pipeTickInterval", 4, 1, 20);
            builder.pop();
        }
    }

    /**
     * Worldgen options. Registered as a STARTUP config because MyDrugs reads them while registering
     * TerraBlender regions/surface rules during common setup — long before per-world SERVER configs
     * are available. STARTUP files ({@code mydrugs-worldgen.toml}) are loaded before mod construction
     * on both physical sides, and worldgen toggles are not meant to change at runtime anyway.
     */
    public static final class Worldgen {
        public static final boolean DEFAULT_REPLACE_MUSHROOM_FIELDS = false;
        public static final boolean DEFAULT_ADD_PSYCHEDELIC_BIOME_SEPARATELY = true;
        public static final int DEFAULT_PSYCHEDELIC_BIOME_WEIGHT = 1;
        public static final String DEFAULT_PSYCHEDELIC_BIOME_CLIMATE_BANDS = "mushroom_only";

        public final ModConfigSpec.BooleanValue enableWorldgen;
        public final ModConfigSpec.BooleanValue enableOverworldBiomes;
        public final ModConfigSpec.BooleanValue enableSkyIslandDimension;
        public final ModConfigSpec.BooleanValue warnWorldgenConfigChangedAfterWorldgen;
        public final ModConfigSpec.BooleanValue replaceMushroomFields;
        public final ModConfigSpec.BooleanValue addPsychedelicBiomeSeparately;
        public final ModConfigSpec.IntValue psychedelicBiomeWeight;
        public final ModConfigSpec.ConfigValue<String> psychedelicBiomeClimateBands;
        public final ModConfigSpec.BooleanValue enableCustomSurfaceRules;
        public final ModConfigSpec.BooleanValue enableSulfurOre;
        public final ModConfigSpec.BooleanValue enablePlatinumOre;
        public final ModConfigSpec.BooleanValue enableAluminiumOre;
        public final ModConfigSpec.BooleanValue enablePetroleumLakes;
        public final ModConfigSpec.IntValue sulfurVeinSize;
        public final ModConfigSpec.IntValue sulfurVeinsPerChunk;
        public final ModConfigSpec.IntValue sulfurMinHeight;
        public final ModConfigSpec.IntValue sulfurMaxHeight;
        public final ModConfigSpec.IntValue platinumVeinSize;
        public final ModConfigSpec.IntValue platinumVeinsPerChunk;
        public final ModConfigSpec.IntValue platinumMinHeight;
        public final ModConfigSpec.IntValue platinumMaxHeight;
        public final ModConfigSpec.IntValue aluminiumVeinSize;
        public final ModConfigSpec.IntValue aluminiumVeinsPerChunk;
        public final ModConfigSpec.IntValue aluminiumMinHeight;
        public final ModConfigSpec.IntValue aluminiumMaxHeight;
        public final ModConfigSpec.BooleanValue enablePhosphateOre;
        public final ModConfigSpec.IntValue phosphateVeinSize;
        public final ModConfigSpec.IntValue phosphateVeinsPerChunk;
        public final ModConfigSpec.IntValue phosphateMinHeight;
        public final ModConfigSpec.IntValue phosphateMaxHeight;
        public final ModConfigSpec.BooleanValue mysticalOreDimensionOnly;
        public final ModConfigSpec.BooleanValue enableWildCannabis;
        public final ModConfigSpec.IntValue wildCannabisSpawnRate;
        public final ModConfigSpec.BooleanValue enableWildCoca;
        public final ModConfigSpec.IntValue wildCocaSpawnRate;
        public final ModConfigSpec.BooleanValue enableWildTobacco;
        public final ModConfigSpec.IntValue wildTobaccoSpawnRate;
        public final ModConfigSpec.BooleanValue enableWildRye;
        public final ModConfigSpec.IntValue wildRyeSpawnRate;
        public final ModConfigSpec.BooleanValue enableWildErgot;
        public final ModConfigSpec.IntValue wildErgotSpawnRate;
        public final ModConfigSpec.BooleanValue enablePsychedelicMushrooms;
        public final ModConfigSpec.IntValue psychedelicMushroomSpawnRate;
        public final ModConfigSpec.BooleanValue enableAloeVera;
        public final ModConfigSpec.IntValue aloeVeraSpawnRate;
        public final ModConfigSpec.BooleanValue enableLavender;
        public final ModConfigSpec.IntValue lavenderSpawnRate;
        public final ModConfigSpec.BooleanValue enableValerian;
        public final ModConfigSpec.IntValue valerianSpawnRate;
        public final ModConfigSpec.BooleanValue enableBitterNutBush;
        public final ModConfigSpec.IntValue bitterNutBushSpawnRate;
        public final ModConfigSpec.BooleanValue enableThirdEyePetal;
        public final ModConfigSpec.IntValue thirdEyePetalSpawnRate;
        public final ModConfigSpec.BooleanValue enableEphedra;
        public final ModConfigSpec.IntValue ephedraSpawnRate;
        public final ModConfigSpec.BooleanValue requireLsdWakeUpEntry;
        public final ModConfigSpec.BooleanValue allowAdminPortalBlock;
        public final ModConfigSpec.IntValue skyIslandDensity;
        public final ModConfigSpec.DoubleValue skyIslandSizeMultiplier;
        public final ModConfigSpec.ConfigValue<String> skyIslandBiomeWeights;
        public final ModConfigSpec.DoubleValue mysticalOreDensity;
        public final ModConfigSpec.IntValue skyIslandStructureFrequency;
        public final ModConfigSpec.DoubleValue skyIslandMobSpawnMultiplier;
        public final ModConfigSpec.IntValue fallingSafetyGraceTicks;
        public final ModConfigSpec.BooleanValue returnAnchorRequired;
        public final ModConfigSpec.BooleanValue enableInnerDimensionDebugLogging;
        public final ModConfigSpec.BooleanValue innerFaultStormEnabled;
        public final ModConfigSpec.BooleanValue innerWildlifeEnabled;

        private Worldgen(ModConfigSpec.Builder builder) {
            builder.push("worldgen");
            enableWorldgen = builder
                    .comment("Master worldgen switch for MyDrugs biome modifiers and TerraBlender region registration. Existing placed blocks remain safe.")
                    .define("enableWorldgen", true);
            enableOverworldBiomes = builder
                    .comment("Enable MyDrugs overworld biome injection through TerraBlender.")
                    .define("enableOverworldBiomes", true);
            enableSkyIslandDimension = builder
                    .comment("Sky Island dimension switch. The Sky Island dimension is not registered yet; this is reserved for pack defaults. The Inner Dimension is shipped separately and is not controlled by this flag.")
                    .define("enableSkyIslandDimension", false);
            warnWorldgenConfigChangedAfterWorldgen = builder
                    .comment("Log warnings when invasive worldgen options are disabled after worlds already exist.")
                    .define("warnWorldgenConfigChangedAfterWorldgen", true);

            builder.push("overworld_biome");
            replaceMushroomFields = builder
                    .comment("Legacy/invasive option. Replaces vanilla Mushroom Fields with Psychedelic Mushroom Valley. Disabled by default to preserve vanilla biome identity.")
                    .define("replaceMushroomFields", DEFAULT_REPLACE_MUSHROOM_FIELDS);
            addPsychedelicBiomeSeparately = builder
                    .comment("Adds Psychedelic Mushroom Valley as its own rare overworld biome without replacing vanilla Mushroom Fields. Recommended default.")
                    .define("addPsychedelicBiomeSeparately", DEFAULT_ADD_PSYCHEDELIC_BIOME_SEPARATELY);
            psychedelicBiomeWeight = builder
                    .comment("TerraBlender region weight for Psychedelic Mushroom Valley. Low values keep the biome rare.")
                    .defineInRange("psychedelicBiomeWeight", DEFAULT_PSYCHEDELIC_BIOME_WEIGHT, 0, 100);
            psychedelicBiomeClimateBands = builder
                    .comment("Climate band preset for separate Psychedelic Mushroom Valley injection: mushroom_only, warm_wet, or broad_wet. mushroom_only keeps generation rare and closest to Mushroom Fields behavior.")
                    .define("psychedelicBiomeClimateBands", DEFAULT_PSYCHEDELIC_BIOME_CLIMATE_BANDS);
            enableCustomSurfaceRules = builder
                    .comment("Enable psychedelic mycelium surface rules for the MyDrugs biome.")
                    .define("enableCustomSurfaceRules", true);
            builder.pop();

            builder.push("ores_and_fluids");
            enableSulfurOre = builder.define("enableSulfurOre", true);
            sulfurVeinSize = builder.defineInRange("sulfurVeinSize", 7, 1, 64);
            sulfurVeinsPerChunk = builder.defineInRange("sulfurVeinsPerChunk", 10, 0, 128);
            sulfurMinHeight = builder.defineInRange("sulfurMinHeight", -64, -128, 320);
            sulfurMaxHeight = builder.defineInRange("sulfurMaxHeight", 64, -128, 320);
            enablePlatinumOre = builder.define("enablePlatinumOre", true);
            platinumVeinSize = builder.defineInRange("platinumVeinSize", 4, 1, 64);
            platinumVeinsPerChunk = builder.defineInRange("platinumVeinsPerChunk", 7, 0, 128);
            platinumMinHeight = builder.defineInRange("platinumMinHeight", -80, -128, 320);
            platinumMaxHeight = builder.defineInRange("platinumMaxHeight", 80, -128, 320);
            enableAluminiumOre = builder.define("enableAluminiumOre", true);
            aluminiumVeinSize = builder.defineInRange("aluminiumVeinSize", 4, 1, 64);
            aluminiumVeinsPerChunk = builder.defineInRange("aluminiumVeinsPerChunk", 14, 0, 128);
            aluminiumMinHeight = builder.defineInRange("aluminiumMinHeight", -80, -128, 320);
            aluminiumMaxHeight = builder.defineInRange("aluminiumMaxHeight", 80, -128, 320);
            enablePhosphateOre = builder.define("enablePhosphateOre", true);
            phosphateVeinSize = builder.defineInRange("phosphateVeinSize", 5, 1, 64);
            phosphateVeinsPerChunk = builder.defineInRange("phosphateVeinsPerChunk", 6, 0, 128);
            phosphateMinHeight = builder.defineInRange("phosphateMinHeight", -32, -128, 320);
            phosphateMaxHeight = builder.defineInRange("phosphateMaxHeight", 64, -128, 320);
            enablePetroleumLakes = builder
                    .comment("Enable desert petroleum lake biome modifier. Disable for less invasive overworld fluid generation.")
                    .define("enablePetroleumLakes", true);
            mysticalOreDimensionOnly = builder
                    .comment("Future Mystical Ore toggle. When true, Mystical Ore should generate only in the MyDrugs dimension.")
                    .define("mysticalOreDimensionOnly", true);
            builder.pop();

            builder.push("plants");
            enableWildCannabis = builder.define("enableWildCannabis", true);
            wildCannabisSpawnRate = builder.defineInRange("wildCannabisSpawnRate", 40, 1, 10000);
            enableWildCoca = builder.define("enableWildCoca", true);
            wildCocaSpawnRate = builder.defineInRange("wildCocaSpawnRate", 55, 1, 10000);
            enableWildTobacco = builder.define("enableWildTobacco", true);
            wildTobaccoSpawnRate = builder.defineInRange("wildTobaccoSpawnRate", 45, 1, 10000);
            enableWildRye = builder.define("enableWildRye", true);
            wildRyeSpawnRate = builder.defineInRange("wildRyeSpawnRate", 35, 1, 10000);
            enableWildErgot = builder.define("enableWildErgot", true);
            wildErgotSpawnRate = builder.defineInRange("wildErgotSpawnRate", 120, 1, 10000);
            enablePsychedelicMushrooms = builder.define("enablePsychedelicMushrooms", true);
            psychedelicMushroomSpawnRate = builder.defineInRange("psychedelicMushroomSpawnRate", 24, 1, 10000);
            enableAloeVera = builder.define("enableAloeVera", true);
            aloeVeraSpawnRate = builder.defineInRange("aloeVeraSpawnRate", 42, 1, 10000);
            enableLavender = builder.define("enableLavender", true);
            lavenderSpawnRate = builder.defineInRange("lavenderSpawnRate", 24, 1, 10000);
            enableValerian = builder.define("enableValerian", true);
            valerianSpawnRate = builder.defineInRange("valerianSpawnRate", 34, 1, 10000);
            enableBitterNutBush = builder.define("enableBitterNutBush", true);
            bitterNutBushSpawnRate = builder.defineInRange("bitterNutBushSpawnRate", 28, 1, 10000);
            enableThirdEyePetal = builder.define("enableThirdEyePetal", true);
            thirdEyePetalSpawnRate = builder.defineInRange("thirdEyePetalSpawnRate", 160, 1, 10000);
            enableEphedra = builder.define("enableEphedra", true);
            ephedraSpawnRate = builder.defineInRange("ephedraSpawnRate", 80, 1, 10000);
            builder.pop();

            builder.push("sky_island_dimension_future");
            requireLsdWakeUpEntry = builder.define("requireLsdWakeUpEntry", true);
            allowAdminPortalBlock = builder.define("allowAdminPortalBlock", false);
            skyIslandDensity = builder.defineInRange("islandDensity", 35, 1, 500);
            skyIslandSizeMultiplier = builder.defineInRange("islandSizeMultiplier", 1.0D, 0.1D, 10.0D);
            skyIslandBiomeWeights = builder.define("biomeWeights", "psychedelic_mushroom_valley=1");
            mysticalOreDensity = builder.defineInRange("mysticalOreDensity", 1.0D, 0.0D, 20.0D);
            skyIslandStructureFrequency = builder.defineInRange("structureFrequency", 24, 1, 10000);
            skyIslandMobSpawnMultiplier = builder.defineInRange("mobSpawnMultiplier", 1.0D, 0.0D, 20.0D);
            fallingSafetyGraceTicks = builder.defineInRange("fallingSafetyGraceTicks", 100, 0, 20 * 60);
            returnAnchorRequired = builder.define("returnAnchorRequired", true);
            builder.pop();

            builder.push("inner_dimension");
            enableInnerDimensionDebugLogging = builder
                    .comment("Log verbose Inner Dimension overlay queue diagnostics.")
                    .define("enableInnerDimensionDebugLogging", false);
            innerFaultStormEnabled = builder
                    .comment("Visual-only lightning strikes at the Fault (the METH region set piece) while a player is near.",
                            "Strikes are infrequent and never cause fire or damage. Disable for players sensitive to sky flashes.")
                    .define("innerFaultStormEnabled", true);
            innerWildlifeEnabled = builder
                    .comment("Ambient Inner Dimension inhabitants: Mother Cap guardians and grazing dream-fauna in calm regions.")
                    .define("innerWildlifeEnabled", true);
            builder.pop();
            builder.pop();
        }
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event) {
    }
}
