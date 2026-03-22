package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class QuantumFlowerShader extends AnimatedShader {
    public static final QuantumFlowerShader INSTANCE = new QuantumFlowerShader();

    public float speed = 0.75F;
    public float petals = 7.0F;
    public float warpAmount = 0.0040F;
    public float bloomAmount = 0.0035F;
    public float chromaAmount = 0.0018F;
    public float iridescence = 0.22F;
    public float tintR = 1.14F;
    public float tintG = 1.04F;
    public float tintB = 1.18F;
    public float tintA = 1.0F;

    protected QuantumFlowerShader() {
        super("quantum_flower");
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
