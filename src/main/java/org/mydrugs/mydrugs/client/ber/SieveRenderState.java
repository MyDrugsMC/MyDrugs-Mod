package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public final class SieveRenderState extends BlockEntityRenderState {
    public ItemStack input = ItemStack.EMPTY;
    public ItemStack result = ItemStack.EMPTY;
    public ItemStack bonus = ItemStack.EMPTY;
}