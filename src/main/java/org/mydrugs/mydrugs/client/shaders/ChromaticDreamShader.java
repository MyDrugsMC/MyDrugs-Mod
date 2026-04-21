package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class ChromaticDreamShader extends AnimatedShader {
    public static ChromaticDreamShader INSTANCE = new ChromaticDreamShader();

    private static final float BASE_STRENGTH = 0.006F;
    private static final float BASE_ZOOM     = 0.015F;
    float strength  = BASE_STRENGTH;
    float speed     = 1.0F;
    float zoom      = BASE_ZOOM;
    float saturation = 1.35F;

    protected ChromaticDreamShader() {
        super("chromatic_dream");
    }

    @Override
    public void setStrength(float dose) {
        this.strength = BASE_STRENGTH * dose;
        this.zoom     = BASE_ZOOM     * dose;
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc
                .putFloat()
                .putFloat()
                .putFloat()
                .putFloat();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(strength)
                .putFloat(speed)
                .putFloat(zoom)
                .putFloat(saturation);
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
