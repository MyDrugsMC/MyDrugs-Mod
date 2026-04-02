package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class EvaporationTrayRenderState extends BlockEntityRenderState {
    @Nullable
    public ResourceLocation fluidStillTexture = null;

    public int fluidTint = 0xFFFFFFFF;
    public float fluidRatio = 0.0f;
    public boolean hasFluid = false;

    public final ItemStackRenderState item = new ItemStackRenderState();
}