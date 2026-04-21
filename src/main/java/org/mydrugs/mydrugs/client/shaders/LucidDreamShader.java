package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class LucidDreamShader extends AnimatedShader {
    public static final LucidDreamShader INSTANCE = new LucidDreamShader();

    private static final float BASE_BREATH = 0.025F;
    private static final float BASE_LENS   = 0.003F;
    private static final float BASE_ECHO   = 0.0018F;
    private static final float BASE_CHROMA = 0.0022F;
    private static final float BASE_DIFF   = 0.003F;

    public float speed = 0.85F;
    public float breathAmount = BASE_BREATH;
    public float lensWarp     = BASE_LENS;
    public float echoAmount   = BASE_ECHO;
    public float chromaAmount = BASE_CHROMA;
    public float diffusion    = BASE_DIFF;
    public float tintR = 1.10F;
    public float tintG = 1.04F;
    public float tintB = 1.16F;
    public float tintA = 1.0F;

    protected LucidDreamShader() {
        super("lucid_dream");
    }

    @Override
    public void setStrength(float dose) {
        this.breathAmount = BASE_BREATH * dose;
        this.lensWarp     = BASE_LENS   * dose;
        this.echoAmount   = BASE_ECHO   * dose;
        this.chromaAmount = BASE_CHROMA * dose;
        this.diffusion    = BASE_DIFF   * dose;
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
