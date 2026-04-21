package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class OpalWaveShader extends AnimatedShader {
    public static final OpalWaveShader INSTANCE = new OpalWaveShader();

    private static final float BASE_DISTORTION = 0.0025F;
    private static final float BASE_SHEEN      = 0.24F;
    private static final float BASE_DISPERSION = 0.0015F;
    private static final float BASE_SOFT       = 0.0035F;

    public float speed = 0.70F;
    public float flowScale   = 3.8F;
    public float distortion  = BASE_DISTORTION;
    public float sheenAmount = BASE_SHEEN;
    public float dispersion  = BASE_DISPERSION;
    public float softness    = BASE_SOFT;
    public float tintAR = 1.05F;
    public float tintAG = 1.12F;
    public float tintAB = 1.20F;
    public float tintAA = 1.0F;
    public float tintBR = 1.18F;
    public float tintBG = 1.05F;
    public float tintBB = 1.16F;
    public float tintBA = 1.0F;

    protected OpalWaveShader() {
        super("opal_wave");
    }

    @Override
    public void setStrength(float dose) {
        this.distortion  = BASE_DISTORTION * dose;
        this.sheenAmount = BASE_SHEEN      * dose;
        this.dispersion  = BASE_DISPERSION * dose;
        this.softness    = BASE_SOFT       * dose;
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
                .putVec4()
                .putVec4();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(speed)
                .putFloat(flowScale)
                .putFloat(distortion)
                .putFloat(sheenAmount)
                .putFloat(dispersion)
                .putFloat(softness)
                .putVec4(tintAR, tintAG, tintAB, tintAA)
                .putVec4(tintBR, tintBG, tintBB, tintBA);
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
