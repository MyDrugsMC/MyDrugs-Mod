package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class VoidPulseShader extends AnimatedShader {
    public static VoidPulseShader INSTANCE = new VoidPulseShader();

    float pulseSpeed = 10.0F;
    float distortionStrength = 0.008F;
    float darkness = 0.45F;
    float depthBoost = 1.0F;
    float glowR = 0.5F;
    float glowG = 0.1F;
    float glowB = 0.8F;

    protected VoidPulseShader() {
        super("void_pulse");
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc
                .putFloat()
                .putFloat()
                .putFloat()
                .putFloat()
                .putVec3();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(pulseSpeed)
                .putFloat(distortionStrength)
                .putFloat(darkness)
                .putFloat(depthBoost)
                .putVec3(glowR, glowG, glowB);
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
