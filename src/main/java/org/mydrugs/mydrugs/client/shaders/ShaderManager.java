package org.mydrugs.mydrugs.client.shaders;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.mydrugs.mydrugs.MyDrugs;
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
    public static final ShaderManager INSTANCE = new ShaderManager(MyDrugs.CLIENT_STATE);

    public ShaderManager(ClientState clientState) {
        super(clientState);
    }

    @SubscribeEvent
    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        for (AnimatedShader shader : INSTANCE.getShaders()) {
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

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderLevelStageEvent.AfterLevel event) {
        Minecraft mc = Minecraft.getInstance();

        for (AnimatedShader shader : INSTANCE.getActiveShaders()) {
            shader.render(mc);
        }
    }

    public void registerShaders() {
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
    }

    // -------------------------------------------------------------------------
    // Dose-driven shader management
    // -------------------------------------------------------------------------

    /** Maps each dose-tracked DrugCategory to the set of SHADER EffectTypes it uses. */
    private final Map<DrugCategory, Set<EffectType>> categoryToShaders = new HashMap<>();
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
        for (Map.Entry<DrugCategory, Set<EffectType>> entry : categoryToShaders.entrySet()) {
            DrugCategory category = entry.getKey();
            Set<EffectType> types = entry.getValue();
            float dose = payload.get(category);

            if (dose > 0.001f) {
                for (EffectType type : types) {
                    // 40-tick sustain refreshed every 20 ticks keeps the shader alive.
                    add(40, type);
                    AnimatedShader shader = getRegisteredShader(type);
                    if (shader != null) shader.setStrength(dose);
                }
            } else {
                for (EffectType type : types) {
                    remove(type);
                }
            }
        }
    }
}