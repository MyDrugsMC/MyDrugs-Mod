package org.mydrugs.mydrugs.client.ber;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;

public final class PsyMixerRenderState extends BlockEntityRenderState {
    public final ItemStack[] stacks = new ItemStack[PsyMixerMultiblock.SLOT_COUNT];
    public float ageInTicks;
    public boolean running;
    public float progressFraction;
    public int focusSlot;
    public float resonance;
    public PsyMixerRitualQuality quality = PsyMixerRitualQuality.BASE;
    public int mistakes;
    public int maxMistakes;
    public boolean completionAnimation;
    public int completionAnimationTick;
    public int completionAnimationDuration = 60;
    public int completionReunionTick = 40;
    public ItemStack completionPreviewStack = ItemStack.EMPTY;
    public float completionAnimationStartAge = Float.NaN;

    public PsyMixerRenderState() {
        for (int i = 0; i < stacks.length; i++) {
            stacks[i] = ItemStack.EMPTY;
        }
    }
}
