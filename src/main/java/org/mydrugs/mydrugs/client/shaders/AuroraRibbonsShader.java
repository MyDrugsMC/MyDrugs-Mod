package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class AuroraRibbonsShader extends AnimatedShader {
    public static final AuroraRibbonsShader INSTANCE = new AuroraRibbonsShader();

    public float speed = 0.78F;
    public float ribbonStrength = 0.85F;
    public float ribbonScale = 3.2F;
    public float driftAmount = 0.0023F;
    public float glowAmount = 0.0035F;
    public float chromaAmount = 0.0017F;
    public float colorAR = 0.95F;
    public float colorAG = 1.12F;
    public float colorAB = 1.30F;
    public float colorAA = 1.0F;
    public float colorBR = 1.22F;
    public float colorBG = 0.96F;
    public float colorBB = 1.16F;
    public float colorBA = 1.0F;

    protected AuroraRibbonsShader() {
        super("aurora_ribbons");
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
                .putFloat(ribbonStrength)
                .putFloat(ribbonScale)
                .putFloat(driftAmount)
                .putFloat(glowAmount)
                .putFloat(chromaAmount)
                .putVec4(colorAR, colorAG, colorAB, colorAA)
                .putVec4(colorBR, colorBG, colorBB, colorBA);
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
