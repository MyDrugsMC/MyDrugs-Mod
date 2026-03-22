package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;

public class EventHorizonShader extends AnimatedShader {
    public static final EventHorizonShader INSTANCE = new EventHorizonShader();

    public float speed = 0.65F;
    public float centerPull = 0.012F;
    public float ringRadius = 0.24F;
    public float ringWidth = 0.09F;
    public float lensing = 0.0050F;
    public float chromaAmount = 0.0022F;
    public float glowAmount = 0.25F;
    public float tintR = 1.18F;
    public float tintG = 1.06F;
    public float tintB = 1.22F;
    public float tintA = 1.0F;

    protected EventHorizonShader() {
        super("event_horizon");
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
                .putVec4();
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        builder
                .putFloat(speed)
                .putFloat(centerPull)
                .putFloat(ringRadius)
                .putFloat(ringWidth)
                .putFloat(lensing)
                .putFloat(chromaAmount)
                .putFloat(glowAmount)
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
