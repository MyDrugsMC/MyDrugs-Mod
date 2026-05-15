package org.mydrugs.mydrugs.client.shaders;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.ClientStateHolder;
import org.mydrugs.mydrugs.core.client.ClientState;
import org.mydrugs.mydrugs.core.client.shader.ClientShaderManager;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectCategory;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.effects.addiction.dose.DosePath;
import org.mydrugs.mydrugs.effects.addiction.network.DoseSyncPayload;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public final class ShaderManager extends ClientShaderManager<AnimatedShader> {
    public static final ShaderManager INSTANCE = new ShaderManager(ClientStateHolder.get());
    private static final int DOSE_SUSTAIN_TICKS = 40;
    private boolean shadersRegistered = false;

    public ShaderManager(ClientState clientState) {
        super(clientState);
    }

    public void registerPipelines(RegisterRenderPipelinesEvent event) {
        registerShaders();
        for (AnimatedShader shader : getShaders()) {
            MyDrugs.getLOGGER().info("registering shader {}...", shader.getClass());
            shader.buildPipeline();
            event.registerPipeline(shader.getRenderPipeline());
        }
        WithdrawalTunnelShader.INSTANCE.buildPipeline();
        event.registerPipeline(WithdrawalTunnelShader.INSTANCE.getRenderPipeline());
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getActiveShaders()) {
            shader.tick(mc);
        }

        if (mc.player != null) {
            INSTANCE.tick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderLevelPostShaders(RenderLevelStageEvent.AfterLevel event) {

        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getActiveShaders()) {
            shader.render(mc);
        }
    }

    public void registerShaders() {
        if (shadersRegistered) {
            return;
        }
        shadersRegistered = true;
        register(EffectType.FOG, FogShader.INSTANCE);
        register(EffectType.ACID_WARP, AcidWarpShader.INSTANCE);
        register(EffectType.CHROMATIC_DREAM, ChromaticDreamShader.INSTANCE);
        register(EffectType.VOID_PULSE, VoidPulseShader.INSTANCE);
        register(EffectType.LUCID_DREAM, LucidDreamShader.INSTANCE);
        register(EffectType.MELT_REALITY, MeltRealityShader.INSTANCE);
        register(EffectType.EVENT_HORIZON, EventHorizonShader.INSTANCE);
        register(EffectType.OPAL_WAVE, OpalWaveShader.INSTANCE);
        register(EffectType.QUANTUM_FLOWER, QuantumFlowerShader.INSTANCE);
        register(EffectType.AURORA_RIBBONS, AuroraRibbonsShader.INSTANCE);
        register(EffectType.SPECTRAL_POSTER, SpectralPosterShader.INSTANCE);
        register(EffectType.DRUNK_VISION, DrunkVisionShader.INSTANCE);
        register(EffectType.GAMMA_BOOST, GammaBoostShader.INSTANCE);
    }

    // -------------------------------------------------------------------------
    // Dose-driven shader management
    // -------------------------------------------------------------------------

    /** Maps each dose-tracked DrugCategory to the set of SHADER EffectTypes it uses. */
    private final Map<DrugCategory, Set<EffectType>> categoryToShaders = new HashMap<>();
    private final Map<EffectType, Integer> directDurations = new HashMap<>();
    private final Map<EffectType, Float> directBaseStrengths = new HashMap<>();
    private final Map<EffectType, Integer> directFadeTicks = new HashMap<>();
    private final Map<EffectType, Integer> directFadeDurations = new HashMap<>();
    private final Map<EffectType, Float> doseStrengths = new HashMap<>();
    private final Map<EffectType, Float> continuousStrengths = new HashMap<>();
    private boolean categoryMapBuilt = false;

    /**
     * Lazily builds the category → shader mapping from DrugRegistry.
     * Called on first dose sync so DrugRegistry is guaranteed to be populated.
     */
    private void ensureCategoryMap() {
        if (categoryMapBuilt) return;
        categoryMapBuilt = true;
        for (DrugId id : DrugId.values()) {
            DrugModel model = DrugRegistry.getDrug(id);
            if (model == null) continue;
            if (DosePath.of(model.getDrugCategory()) == DosePath.NONE) continue;
            for (DrugEffect effect : model.getDrugEffects()) {
                if (effect.getEffectType().getCategory() == EffectCategory.SHADER) {
                    categoryToShaders
                            .computeIfAbsent(model.getDrugCategory(), k -> new HashSet<>())
                            .add(effect.getEffectType());
                }
            }
        }
    }

    /**
     * Called from {@link org.mydrugs.mydrugs.effects.addiction.client.network.ClientPayloadHandler}
     * when a {@link DoseSyncPayload} arrives.
     * <ul>
     *   <li>dose > 0 → activate all shaders for that category and call
     *       {@code setStrength(dose)} on each.</li>
     *   <li>dose = 0 → deactivate all shaders for that category.</li>
     * </ul>
     * Shaders are sustained with a 40-tick window refreshed every 20 ticks so they
     * never expire while dose is active.
     */
    public void updateDoses(DoseSyncPayload payload) {
        ensureCategoryMap();
        if (!Config.CLIENT.enableDrugShaders.get()) {
            doseStrengths.clear();
            rebuildActiveShaders();
            return;
        }

        for (Map.Entry<DrugCategory, Set<EffectType>> entry : categoryToShaders.entrySet()) {
            DrugCategory category = entry.getKey();
            Set<EffectType> types = entry.getValue();
            float dose = payload.get(category);

            if (dose > 0.001f) {
                for (EffectType type : types) {
                    doseStrengths.put(type, dose);
                }
            } else {
                for (EffectType type : types) {
                    doseStrengths.remove(type);
                }
            }
        }
        rebuildActiveShaders();
    }

    public void addDirect(int durationTicks, EffectType type, float intensity) {
        addDirect(durationTicks, type, intensity, 0, 0);
    }

    public void addDirect(int durationTicks, EffectType type, float intensity, int fadeTicksRemaining, int fadeDurationTicks) {
        if (durationTicks <= 0 || type == null || intensity <= 0.0F) {
            return;
        }
        if (getRegisteredShader(type) == null) {
            MyDrugs.getLOGGER().warn("Shader {} is not initialized!", type.name());
            return;
        }

        directDurations.put(type, durationTicks);
        directBaseStrengths.put(type, intensity);
        if (fadeDurationTicks > 0) {
            directFadeDurations.put(type, Math.max(1, fadeDurationTicks));
            if (fadeTicksRemaining > 0) {
                directFadeTicks.put(type, Math.min(fadeTicksRemaining, durationTicks));
            } else {
                directFadeTicks.remove(type);
            }
        } else {
            directFadeTicks.remove(type);
            directFadeDurations.remove(type);
        }
        rebuildActiveShaders();
    }

    public void setContinuous(EffectType type, float intensity) {
        if (type == null) {
            return;
        }
        float clamped = Math.max(0.0F, intensity);
        Float previous = continuousStrengths.get(type);
        if (clamped <= 0.002F) {
            if (previous != null) {
                continuousStrengths.remove(type);
                rebuildActiveShaders();
            }
            return;
        }

        if (getRegisteredShader(type) == null) {
            return;
        }
        if (previous == null || Math.abs(previous - clamped) > 0.01F) {
            continuousStrengths.put(type, clamped);
            rebuildActiveShaders();
        }
    }

    @Override
    public void add(int durationTicks, EffectType type) {
        addDirect(durationTicks, type, 1.0F);
    }

    @Override
    public void remove(EffectType type) {
        directDurations.remove(type);
        directBaseStrengths.remove(type);
        directFadeTicks.remove(type);
        directFadeDurations.remove(type);
        doseStrengths.remove(type);
        continuousStrengths.remove(type);
        rebuildActiveShaders();
    }

    @Override
    public void clearActive() {
        directDurations.clear();
        directBaseStrengths.clear();
        directFadeTicks.clear();
        directFadeDurations.clear();
        doseStrengths.clear();
        continuousStrengths.clear();
        super.clearActive();
    }

    @Override
    public void tick() {
        if (!directDurations.isEmpty()) {
            java.util.Iterator<Map.Entry<EffectType, Integer>> iterator = directDurations.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<EffectType, Integer> entry = iterator.next();
                EffectType type = entry.getKey();
                int remaining = entry.getValue() - 1;
                directFadeTicks.computeIfPresent(type, (ignored, fade) -> Math.max(0, fade - 1));
                if (remaining <= 0) {
                    boolean fadeStarted = directFadeTicks.containsKey(type);
                    int fadeDuration = directFadeDurations.getOrDefault(type, 0);
                    if (!fadeStarted && fadeDuration > 0) {
                        directFadeTicks.put(type, fadeDuration);
                        entry.setValue(fadeDuration);
                    } else {
                        directBaseStrengths.remove(type);
                        directFadeTicks.remove(type);
                        directFadeDurations.remove(type);
                        iterator.remove();
                    }
                } else {
                    entry.setValue(remaining);
                }
            }
            rebuildActiveShaders();
        } else if (doseStrengths.isEmpty() && continuousStrengths.isEmpty() && hasActiveShaders()) {
            super.clearActive();
        }
    }

    private void rebuildActiveShaders() {
        Set<EffectType> activeTypes = new HashSet<>();
        activeTypes.addAll(directDurations.keySet());
        activeTypes.addAll(doseStrengths.keySet());
        activeTypes.addAll(continuousStrengths.keySet());

        super.clearActive();
        if (activeTypes.isEmpty()) {
            return;
        }

        float configScale = Config.CLIENT.shaderIntensity.get().floatValue();
        for (EffectType type : activeTypes) {
            AnimatedShader shader = getRegisteredShader(type);
            if (shader == null) {
                continue;
            }

            float directStrength = directStrength(type);
            float doseStrength = doseStrengths.getOrDefault(type, 0.0F);
            float continuousStrength = continuousStrengths.getOrDefault(type, 0.0F);
            shader.setStrength(Math.max(Math.max(directStrength, doseStrength), continuousStrength) * configScale);

            int duration = Math.max(
                    directDurations.getOrDefault(type, 0),
                    (doseStrength > 0.001F || continuousStrength > 0.001F) ? DOSE_SUSTAIN_TICKS : 0
            );
            if (duration > 0) {
                super.add(duration, type);
            }
        }
    }

    private float directStrength(EffectType type) {
        float base = directBaseStrengths.getOrDefault(type, 0.0F);
        int fade = directFadeTicks.getOrDefault(type, 0);
        int fadeDuration = directFadeDurations.getOrDefault(type, 0);
        if (fade <= 0 || fadeDuration <= 0) {
            return base;
        }
        return base * Math.clamp(fade / (float) fadeDuration, 0.0F, 1.0F);
    }
}
