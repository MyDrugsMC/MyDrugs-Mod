package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * Per-frame snapshot of fluid-pump animation data.
 *
 * <p>Every field is recomputed from scratch in {@code extractRenderState} —
 * we never rely on values persisting between frames, because the NeoForge
 * 1.21+ batched-BER pipeline does not guarantee state-instance reuse.
 */
public class FluidPumpRenderState extends BlockEntityRenderState {
    /** Whether the crank is currently installed on the pump. */
    public boolean hasCrank = false;
    /** Which face the crank handle is mounted on. */
    public Direction crankFace = Direction.NORTH;
    /** Crank arm rotation, in degrees (0 = arm pointing straight up). */
    public float animAngle = 0.0f;
}
