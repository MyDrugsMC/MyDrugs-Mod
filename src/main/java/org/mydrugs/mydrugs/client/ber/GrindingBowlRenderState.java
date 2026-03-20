package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.item.ItemStack;

public class GrindingBowlRenderState extends BlockEntityRenderState {
    public final ItemStackRenderState itemRenderState = new ItemStackRenderState();
    public ItemStack displayedStack = ItemStack.EMPTY;
    public float progress01 = 0.0F;
}