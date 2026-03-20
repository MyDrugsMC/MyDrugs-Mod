package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class StompCrafterRenderState extends BlockEntityRenderState {
    public final List<ItemStack> exampleStacks = new ArrayList<>();
    public int progress = 0;
    public int lightCoords = 0;
}