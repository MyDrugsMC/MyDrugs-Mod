package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

public class ClayVatRenderState extends BlockEntityRenderState {
    public Fluid fluid = Fluids.EMPTY;
    public float fluidRatio = 0.0f;
    public boolean hasFluid = false;

    @Nullable
    public ResourceLocation fluidStillTexture = null;

    public int fluidTint = 0xFFFFFFFF;
}