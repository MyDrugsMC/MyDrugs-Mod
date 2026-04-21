package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class QuantumFlowerShader extends AnimatedShader {
    public static final QuantumFlowerShader INSTANCE = new QuantumFlowerShader();

    private static final float BASE_WARP   = 0.0040F;
    private static final float BASE_BLOOM  = 0.0035F;
    private static final float BASE_CHROMA = 0.0018F;
    private static final float BASE_IRID   = 0.22F;

    public float speed = 0.75F;
    public float petals = 7.0F;
    public float warpAmount   = BASE_WARP;
    public float bloomAmount  = BASE_BLOOM;
    public float chromaAmount = BASE_CHROMA;
    public float iridescence  = BASE_IRID;
    public float tintR = 1.14F;
    public float tintG = 1.04F;
    public float tintB = 1.18F;
    public float tintA = 1.0F;

    protected QuantumFlowerShader() {
        super("quantum_flower");
    }

    @Override
    public void setStrength(float dose) {
        this.warpAmount   = BASE_WARP   * dose;
        this.bloomAmount  = BASE_BLOOM  * dose;
        this.chromaAmount = BASE_CHROMA * dose;
        this.iridescence  = BASE_IRID   * dose;
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
                .putFloat(petals)
                .putFloat(warpAmount)
                .putFloat(bloomAmount)
                .putFloat(chromaAmount)
                .putFloat(iridescence)
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
