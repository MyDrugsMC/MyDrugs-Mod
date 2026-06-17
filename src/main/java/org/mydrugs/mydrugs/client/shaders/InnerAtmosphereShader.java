package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.client.InnerAtmosphereClient;
import org.mydrugs.mydrugs.dimension.inner.InnerAtmosphere;

/**
 * Phase 2 inner-dimension atmosphere post-pass. A full-screen effect, active only inside
 * {@code INNER_LEVEL}, parameterised by the cached {@link InnerAtmosphere} sample:
 *
 * <ul>
 *   <li><b>bloom</b> strength from {@code glowBias} — the emissive blocks/lakes actually glow;</li>
 *   <li><b>god-rays</b> approximated as a screen-space radial blur of bright regions, strongest
 *       when {@code glowBias} is high;</li>
 *   <li><b>vignette + desaturation</b> from {@code danger} — scar fields feel oppressive;</li>
 *   <li><b>warmth</b> from {@code calm} — clearings feel radiant.</li>
 * </ul>
 *
 * <p>All parameters are smoothed toward their target each tick, which crossfades the look when the
 * player crosses a region boundary (the boundary blend is widened by {@code transitionStrength}) so
 * the look eases rather than snaps. The pass is hard-off under reduced motion or when the
 * {@code innerPostProcessing} config is disabled, and {@link AnimatedShader#render} already disables
 * the pass (logging once) if the pipeline fails to build — it never crashes the game.
 *
 * <p>Structure copied from {@link WithdrawalTunnelShader}: full-screen triangle, copied input
 * target, std140 UBO whose field order matches {@code inner_atmosphere.fsh}.
 */
public final class InnerAtmosphereShader extends AnimatedShader {
    public static final InnerAtmosphereShader INSTANCE = new InnerAtmosphereShader();

    private float strength;
    private float bloom;
    private float vignette;
    private float desat;
    private float warmth;
    private float godray;
    /** Transient bloom/strength spike fired by a region crossing (Phase 3); decays each tick. */
    private float crossingPulse;

    private InnerAtmosphereShader() {
        super("inner_atmosphere");
    }

    @Override
    public void tick(Minecraft mc) {
        super.tick(mc);

        float targetStrength = 0.0F;
        float targetBloom = 0.0F;
        float targetVignette = 0.0F;
        float targetDesat = 0.0F;
        float targetWarmth = 0.0F;
        float targetGodray = 0.0F;
        // Boundary blend: ease faster near a region seam so the crossfade tracks the visuals.
        float ease = 0.06F;

        if (enabled() && InnerAtmosphereClient.inInnerDimension(mc)) {
            InnerAtmosphere.Sample sample = InnerAtmosphereClient.current(mc);
            if (sample != null) {
                float glow = (float) Mth.clamp(sample.glowBias(), 0.0D, 1.0D);
                float danger = (float) Mth.clamp(sample.danger(), 0.0D, 1.0D);
                float calm = (float) Mth.clamp(sample.calm(), 0.0D, 1.0D);
                float transition = (float) Mth.clamp(sample.transitionStrength(), 0.0D, 1.0D);

                targetStrength = 1.0F;
                targetBloom = glow;
                targetGodray = glow * (0.4F + glow * 0.6F);
                targetVignette = danger;
                targetDesat = danger * 0.7F;
                targetWarmth = calm;
                ease = 0.045F + transition * 0.08F;
            }
        }

        strength += (targetStrength - strength) * ease;
        bloom += (targetBloom - bloom) * ease;
        vignette += (targetVignette - vignette) * ease;
        desat += (targetDesat - desat) * ease;
        warmth += (targetWarmth - warmth) * ease;
        godray += (targetGodray - godray) * ease;
        // ~0.8s decay (0.92^16 ≈ 0.26) of the crossing spike.
        crossingPulse *= 0.92F;
        if (crossingPulse < 0.002F) {
            crossingPulse = 0.0F;
        }
    }

    /** Phase 3 region-crossing hook: spike bloom/strength briefly. Amount already scaled by caller. */
    public void addCrossingPulse(float amount) {
        crossingPulse = Mth.clamp(crossingPulse + amount, 0.0F, 1.0F);
    }

    /** Resets all eased atmosphere parameters, safe to call outside the Inner Dimension. */
    public void clear() {
        strength = 0.0F;
        bloom = 0.0F;
        vignette = 0.0F;
        desat = 0.0F;
        warmth = 0.0F;
        godray = 0.0F;
        crossingPulse = 0.0F;
    }

    public boolean shouldRender() {
        return enabled() && (strength > 0.004F || crossingPulse > 0.004F);
    }

    private static boolean enabled() {
        try {
            if (Config.CLIENT.reducedMotionMode.get()) {
                return false;
            }
            return Config.CLIENT.innerPostProcessing.get();
        } catch (IllegalStateException ignored) {
            return false;
        }
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc
                .putFloat()  // Strength
                .putFloat()  // Bloom
                .putFloat()  // Vignette
                .putFloat()  // Desat
                .putFloat()  // Warmth
                .putFloat(); // GodRay
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        // The crossing pulse rides on top of the steady strength/bloom so the spike is visible even
        // in a calm region with little baseline glow.
        float effStrength = Mth.clamp(strength + crossingPulse, 0.0F, 1.0F);
        float effBloom = Mth.clamp(bloom + crossingPulse, 0.0F, 1.0F);
        builder
                .putFloat(effStrength)
                .putFloat(effBloom)
                .putFloat(Mth.clamp(vignette, 0.0F, 1.0F))
                .putFloat(Mth.clamp(desat, 0.0F, 1.0F))
                .putFloat(Mth.clamp(warmth, 0.0F, 1.0F))
                .putFloat(Mth.clamp(godray, 0.0F, 1.0F));
    }

    @Override
    protected boolean useFullscreenTriangle() {
        return true;
    }

    @Override
    protected boolean useCopiedInputTarget() {
        return true;
    }
}
