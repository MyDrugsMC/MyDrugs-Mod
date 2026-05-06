package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public final class GammaBoostShader extends AnimatedShader {
    public static final GammaBoostShader INSTANCE = new GammaBoostShader();

    private float strength;

    private GammaBoostShader() {
        super("gamma_boost");
    }

    @Override
    public void setStrength(float strength) {
        this.strength = Math.min(2.0F, Math.max(0.0F, strength));
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc.putFloat();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder.putFloat(strength);
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
