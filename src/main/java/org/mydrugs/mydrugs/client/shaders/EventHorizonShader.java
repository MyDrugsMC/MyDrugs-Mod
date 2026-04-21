package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class EventHorizonShader extends AnimatedShader {
    public static final EventHorizonShader INSTANCE = new EventHorizonShader();

    private static final float BASE_CENTER_PULL = 0.012F;
    private static final float BASE_LENSING     = 0.0050F;
    private static final float BASE_CHROMA      = 0.0022F;
    private static final float BASE_GLOW        = 0.25F;

    public float speed = 0.65F;
    public float centerPull  = BASE_CENTER_PULL;
    public float ringRadius = 0.24F;
    public float ringWidth = 0.09F;
    public float lensing      = BASE_LENSING;
    public float chromaAmount = BASE_CHROMA;
    public float glowAmount   = BASE_GLOW;
    public float tintR = 1.18F;
    public float tintG = 1.06F;
    public float tintB = 1.22F;
    public float tintA = 1.0F;

    protected EventHorizonShader() {
        super("event_horizon");
    }

    @Override
    public void setStrength(float dose) {
        this.centerPull  = BASE_CENTER_PULL * dose;
        this.lensing     = BASE_LENSING     * dose;
        this.chromaAmount = BASE_CHROMA     * dose;
        this.glowAmount  = BASE_GLOW        * dose;
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc
                .putFloat()
                .putFloat()
                .putFloat()
                .putFloat()
                .putFloat()
                .putFloat()
                .putFloat()
                .putVec4();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(speed)
                .putFloat(centerPull)
                .putFloat(ringRadius)
                .putFloat(ringWidth)
                .putFloat(lensing)
                .putFloat(chromaAmount)
                .putFloat(glowAmount)
                .putVec4(tintR, tintG, tintB, tintA);
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
