package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class DrunkVisionShader extends AnimatedShader {
    public static DrunkVisionShader INSTANCE = new DrunkVisionShader();

    float speed = 0.82F;
    float swayAmount = 0.018F;
    float barrelAmount = 0.080F;
    float spinAmount = 0.055F;
    float radiusStart = 0.22F;
    float jitterAmount = 0.0017F;
    float blurAmount = 0.0027F;
    float chromaAmount = 0.0016F;
    float echoAmount = 0.0024F;
    float pulseAmount = 0.16F;
    float tintR = 1.08F;
    float tintG = 1.03F;
    float tintB = 0.96F;

    protected DrunkVisionShader() {
        super("drunk_vision");
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
                .putFloat()
                .putFloat()
                .putFloat()
                .putVec3();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(speed)
                .putFloat(swayAmount)
                .putFloat(barrelAmount)
                .putFloat(spinAmount)
                .putFloat(radiusStart)
                .putFloat(jitterAmount)
                .putFloat(blurAmount)
                .putFloat(chromaAmount)
                .putFloat(echoAmount)
                .putFloat(pulseAmount)
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
