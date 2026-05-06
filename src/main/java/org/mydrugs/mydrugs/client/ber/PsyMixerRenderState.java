package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;

public final class PsyMixerRenderState extends BlockEntityRenderState {
    public final ItemStack[] stacks = new ItemStack[PsyMixerMultiblock.SLOT_COUNT];
    public float ageInTicks;
    public boolean running;
    public float progressFraction;
    public int focusSlot;
    public float resonance;

    public PsyMixerRenderState() {
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = ItemStack.EMPTY;
        }
    }
}
