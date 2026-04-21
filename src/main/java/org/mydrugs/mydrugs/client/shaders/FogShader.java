package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import net.minecraft.client.Minecraft;

import java.util.List;

public final class FogShader extends AnimatedShader {
    public static final FogShader INSTANCE = new FogShader();
    private static final float BASE_FOG_STRENGTH = 1.0F;
    private float fogStrength = BASE_FOG_STRENGTH;
    private final float fogScale = 2.5F;
    private final float fogR = 0.78F;
    private final float fogG = 0.78F;
    private final float fogB = 0.78F;
    private final float fogSpeed = 1.0F;

    private FogShader() {
        super("fog");
    }

    @Override
    public void setStrength(float dose) {
        this.fogStrength = BASE_FOG_STRENGTH * dose;
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc.putFloat() // FogStrength
                .putFloat() // FogScale
                .putFloat() // FogSpeed
                .putVec3(); // FogColor
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder.putFloat(fogStrength).putFloat(fogScale).putFloat(fogSpeed).putVec3(fogR, fogG, fogB);
    }

    @Override
    protected List<String> extraSamplerNames() {
        return List.of("DepthSampler");
    }

    @Override
    protected void bindExtraSamplers(RenderPass pass, RenderTarget mainTarget, RenderTarget inputTarget) {
        pass.bindSampler("DepthSampler", mainTarget.getDepthTextureView());
    }

    @Override
    protected boolean useCopiedInputTarget() {
        return true;
    }
}