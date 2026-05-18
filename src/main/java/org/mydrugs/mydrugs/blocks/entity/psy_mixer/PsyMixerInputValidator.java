package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.core.drug.use.DrugStackResolver;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipeInput;

public final class PsyMixerInputValidator {
    public static final String ONE_VOICE_MESSAGE = "screen.mydrugs.psy_mixer.one_voice";

    private PsyMixerInputValidator() {
    }

    public static Result validateOneActiveVoice(PsyMixerRecipeInput input) {
        if (activeDrugCount(input.base()) != 1) {
            return Result.invalid(ONE_VOICE_MESSAGE);
        }
        if (activeDrugCount(input.material()) > 0
                || activeDrugCount(input.catalyst()) > 0
                || activeDrugCount(input.stabilizer()) > 0
                || activeDrugCount(input.vessel()) > 0) {
            return Result.invalid(ONE_VOICE_MESSAGE);
        }
        return Result.ok();
    }

    public static boolean slotMayContainActiveDrug(int slot) {
        return slot == PsyMixerMultiblock.SLOT_BASE;
    }

    private static int activeDrugCount(ItemStack stack) {
        return DrugStackResolver.resolve(stack, null).size();
    }

    public record Result(boolean valid, String messageKey) {
        private static Result ok() {
            return new Result(true, "");
        }

        private static Result invalid(String messageKey) {
            return new Result(false, messageKey);
        }
    }
}
