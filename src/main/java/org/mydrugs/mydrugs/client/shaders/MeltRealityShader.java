package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class MeltRealityShader extends AnimatedShader {
    public static final MeltRealityShader INSTANCE = new MeltRealityShader();

    private static final float BASE_FLOW   = 0.06F;
    private static final float BASE_CHROMA = 0.0025F;
    private static final float BASE_SOFT   = 0.0035F;
    private static final float BASE_PULSE  = 0.18F;

    public float speed = 0.8F;
    public float flowStrength = BASE_FLOW;
    public float flowScale = 3.0F;
    public float chromaAmount = BASE_CHROMA;
    public float softness     = BASE_SOFT;
    public float pulseAmount  = BASE_PULSE;
    public float tintR = 1.15F;
    public float tintG = 1.02F;
    public float tintB = 1.20F;
    public float tintA = 1.0F;

    protected MeltRealityShader() {
        super("melt_reality");
    }

    @Override
    public void setStrength(float dose) {
        this.flowStrength = BASE_FLOW   * dose;
        this.chromaAmount = BASE_CHROMA * dose;
        this.softness     = BASE_SOFT   * dose;
        this.pulseAmount  = BASE_PULSE  * dose;
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
