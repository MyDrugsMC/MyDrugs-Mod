package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class LucidDreamShader extends AnimatedShader {
    public static final LucidDreamShader INSTANCE = new LucidDreamShader();

    public float speed = 0.85F;
    public float breathAmount = 0.025F;
    public float lensWarp = 0.003F;
    public float echoAmount = 0.0018F;
    public float chromaAmount = 0.0022F;
    public float diffusion = 0.003F;
    public float tintR = 1.10F;
    public float tintG = 1.04F;
    public float tintB = 1.16F;
    public float tintA = 1.0F;

    protected LucidDreamShader() {
        super("lucid_dream");
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
                .putFloat(breathAmount)
                .putFloat(lensWarp)
                .putFloat(echoAmount)
                .putFloat(chromaAmount)
                .putFloat(diffusion)
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
