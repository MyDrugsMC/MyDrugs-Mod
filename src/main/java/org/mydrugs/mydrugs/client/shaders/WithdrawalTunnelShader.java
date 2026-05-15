package org.mydrugs.mydrugs.client.shaders;

import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.buffers.Std140SizeCalculator;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.mydrugs.mydrugs.client.effects.AddictionClientState;
import org.mydrugs.mydrugs.client.effects.HeartbeatPulse;
import org.mydrugs.mydrugs.addiction.config.SymptomFlags;
import org.mydrugs.mydrugs.addiction.config.SymptomThresholds;

public final class WithdrawalTunnelShader extends AnimatedShader {
    public static final WithdrawalTunnelShader INSTANCE = new WithdrawalTunnelShader();

    private float smoothedStrength = 0.0F;

    private WithdrawalTunnelShader() {
        super("withdrawal_tunnel");
    }

    @Override
    public void tick(Minecraft mc) {
        super.tick(mc);

        float target = 0.0F;

        if (AddictionClientState.has(SymptomFlags.VISION)) {
            float severity = Mth.clamp(AddictionClientState.globalSeverity, 0.0F, 1.0F);

            target = Mth.clamp(
                    (severity - SymptomThresholds.VISION) / (1.0F - SymptomThresholds.VISION),
                    0.0F,
                    1.0F
            );

            // Harsher at high withdrawal
            target *= target;
        }

        smoothedStrength += (target - smoothedStrength) * 0.15F;
    }

    public float getStrength() {
        return smoothedStrength;
    }

    public boolean shouldRender() {
        return smoothedStrength > 0.002F || AddictionClientState.has(SymptomFlags.VISION);
    }

    @Override
    protected Std140SizeCalculator extendUboLayout(Std140SizeCalculator calc) {
        return calc
                .putFloat() // Strength
                .putFloat() // BlurPixels
                .putFloat() // Darkness
                .putFloat() // PulseAmount
                .putFloat(); // BeatPulse
    }

    @Override
    protected void writeExtraUniforms(Std140Builder builder, Minecraft mc) {
        float strength = Mth.clamp(smoothedStrength, 0.0F, 1.0F);
        float beat = HeartbeatPulse.getShaderPulse(strength);

        float blurPixels = Mth.lerp(strength, 0.5F, 10.0F) + beat * Mth.lerp(strength, 1.0F, 5.5F);
        float darkness = Mth.lerp(strength, 0.0F, 0.84F) + beat * 0.10F;
        float pulseAmount = Mth.lerp(strength, 0.002F, 0.012F) + beat * 0.020F;

        builder
                .putFloat(strength)
                .putFloat(blurPixels)
                .putFloat(darkness)
                .putFloat(pulseAmount)
                .putFloat(beat);
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