package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class MeltRealityShader extends AnimatedShader {
    public static final MeltRealityShader INSTANCE = new MeltRealityShader();

    public float speed = 0.8F;
    public float flowStrength = 0.06F;
    public float flowScale = 3.0F;
    public float chromaAmount = 0.0025F;
    public float softness = 0.0035F;
    public float pulseAmount = 0.18F;
    public float tintR = 1.15F;
    public float tintG = 1.02F;
    public float tintB = 1.20F;
    public float tintA = 1.0F;

    protected MeltRealityShader() {
        super("melt_reality");
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
                .putFloat(flowStrength)
                .putFloat(flowScale)
                .putFloat(chromaAmount)
                .putFloat(softness)
                .putFloat(pulseAmount)
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
