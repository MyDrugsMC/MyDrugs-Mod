package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class ChromaticDreamShader extends AnimatedShader {
    public static ChromaticDreamShader INSTANCE = new ChromaticDreamShader();

    float strength = 0.006F;
    float speed = 1.0F;
    float zoom = 0.015F;
    float saturation = 1.35F;
    protected ChromaticDreamShader() {
        super("chromatic_dream");
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
