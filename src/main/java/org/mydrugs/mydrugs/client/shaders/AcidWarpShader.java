package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class AcidWarpShader extends AnimatedShader {
    public static AcidWarpShader INSTANCE = new AcidWarpShader();

    private static final float BASE_STRENGTH = 0.01F;
    float strength = BASE_STRENGTH;
    float speed = 1.2F;
    float frequency = 6.0F;
    float tintR = 1.2F;
    float tintG = 0.9F;
    float tintB = 1.3F;

    protected AcidWarpShader() {
        super("acid_warp");
    }

    @Override
    public void setStrength(float dose) {
        this.strength = BASE_STRENGTH * dose;
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc
                .putFloat()
                .putFloat()
                .putFloat()
                .putVec3();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(strength)
                .putFloat(speed)
                .putFloat(frequency)
                .putVec3(tintR, tintG, tintB);
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
