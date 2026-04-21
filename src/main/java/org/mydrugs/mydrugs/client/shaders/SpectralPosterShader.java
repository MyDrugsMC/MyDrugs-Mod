package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class SpectralPosterShader extends AnimatedShader {
    public static final SpectralPosterShader INSTANCE = new SpectralPosterShader();

    private static final float BASE_DRIFT   = 0.0012F;
    private static final float BASE_RAINBOW = 0.72F;
    private static final float BASE_GRAIN   = 0.012F;

    public float speed = 0.55F;
    public float posterLevels = 5.0F;
    public float edgeAmount    = 1.0F;
    public float driftAmount   = BASE_DRIFT;
    public float rainbowAmount = BASE_RAINBOW;
    public float grainAmount   = BASE_GRAIN;
    public float accentR = 1.16F;
    public float accentG = 1.04F;
    public float accentB = 1.20F;
    public float accentA = 1.0F;

    protected SpectralPosterShader() {
        super("spectral_poster");
    }

    @Override
    public void setStrength(float dose) {
        this.driftAmount   = BASE_DRIFT   * dose;
        this.rainbowAmount = BASE_RAINBOW * dose;
        this.grainAmount   = BASE_GRAIN   * dose;
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
                .putVec4();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(speed)
                .putFloat(posterLevels)
                .putFloat(edgeAmount)
                .putFloat(driftAmount)
                .putFloat(rainbowAmount)
                .putFloat(grainAmount)
                .putVec4(accentR, accentG, accentB, accentA);
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
