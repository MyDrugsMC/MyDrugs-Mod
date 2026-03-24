package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public class MixingVatRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState[] items = {
            new ItemStackRenderState(),
            new ItemStackRenderState(),
            new ItemStackRenderState(),
            new ItemStackRenderState()
    };

    public final ItemStackRenderState spatula = new ItemStackRenderState();


    public Fluid fluid = Fluids.EMPTY;
    public float fluidRatio;
    public boolean hasFluid;

    public ResourceLocation fluidStillTexture;
    public int fluidTint = 0xFFFFFFFF;

    public float stirPhase;
    public boolean showSpatula;
}