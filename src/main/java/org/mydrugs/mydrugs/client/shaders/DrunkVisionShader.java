package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class DrunkVisionShader extends AnimatedShader {
    public static DrunkVisionShader INSTANCE = new DrunkVisionShader();

    float speed = 0.82F;
    private static final float BASE_SWAY      = 0.018F;
    private static final float BASE_BARREL    = 0.080F;
    private static final float BASE_SPIN      = 0.055F;
    private static final float BASE_JITTER    = 0.0017F;
    private static final float BASE_BLUR      = 0.0027F;
    private static final float BASE_CHROMA    = 0.0016F;
    private static final float BASE_ECHO      = 0.0024F;
    private static final float BASE_PULSE     = 0.16F;
    float swayAmount    = BASE_SWAY;
    float barrelAmount  = BASE_BARREL;
    float spinAmount    = BASE_SPIN;
    float radiusStart   = 0.22F;
    float jitterAmount  = BASE_JITTER;
    float blurAmount    = BASE_BLUR;
    float chromaAmount  = BASE_CHROMA;
    float echoAmount    = BASE_ECHO;
    float pulseAmount   = BASE_PULSE;
    float tintR = 1.08F;
    float tintG = 1.03F;
    float tintB = 0.96F;

    protected DrunkVisionShader() {
        super("drunk_vision");
    }

    @Override
    public void setStrength(float dose) {
        this.swayAmount   = BASE_SWAY    * dose;
        this.barrelAmount = BASE_BARREL  * dose;
        this.spinAmount   = BASE_SPIN    * dose;
        this.jitterAmount = BASE_JITTER  * dose;
        this.blurAmount   = BASE_BLUR    * dose;
        this.chromaAmount = BASE_CHROMA  * dose;
        this.echoAmount   = BASE_ECHO    * dose;
        this.pulseAmount  = BASE_PULSE   * dose;
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
