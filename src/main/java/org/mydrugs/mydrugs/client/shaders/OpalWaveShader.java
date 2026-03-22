package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class OpalWaveShader extends AnimatedShader {
    public static final OpalWaveShader INSTANCE = new OpalWaveShader();

    public float speed = 0.70F;
    public float flowScale = 3.8F;
    public float distortion = 0.0025F;
    public float sheenAmount = 0.24F;
    public float dispersion = 0.0015F;
    public float softness = 0.0035F;
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
