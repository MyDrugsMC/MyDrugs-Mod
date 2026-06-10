package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.ritual.RitualBaseDrugResolver;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.progression.PsyMixerMasteryAttachment;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipe;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipeInput;

import java.util.ArrayList;
import java.util.List;

public final class PsyMixerRitualScoring {
    private PsyMixerRitualScoring() {
    }

    public static List<PsyMixerRitualAction> selectRitualActions(
            PsyMixerRecipe recipe,
            PsyMixerRecipeInput input,
            RandomSource random,
            PsyMixerMasteryAttachment mastery,
            ResourceLocation formulaMasteryId
    ) {
        DrugId baseDrug = RitualBaseDrugResolver.resolve(input.base()).orElse(null);
        int actionCount = java.util.Optional.ofNullable(baseDrug)
                .map(PsyMixerRitualScoring::baseDrugActionCount)
                .orElse(0);
        if (recipe.hasValidVessel(input.vessel())) {
            actionCount += 2;
        }
        if (recipe.hasValidStabilizer(input.stabilizer())) {
            actionCount += 2;
        }
        actionCount = Math.max(actionCount > 0 ? 1 : 0, actionCount - mastery.getRemovedActionCount(formulaMasteryId));
        if (actionCount <= 0) {
            return List.of();
        }

        List<PsyMixerRitualAction> configuredPool = recipe.availableRitualActions();
        List<PsyMixerRitualAction> pool = baseDrug != null
                && (configuredPool.isEmpty() || PsyMixerRitualProfiles.isLegacyGenericPool(configuredPool))
                ? PsyMixerRitualProfiles.forDrug(baseDrug).weightedActions()
                : configuredPool;
        if (pool.isEmpty()) {
            pool = PsyMixerRitualAction.defaultRandomPool();
        }
        List<PsyMixerRitualAction> selected = new ArrayList<>(actionCount);
        for (int i = 0; i < actionCount; i++) {
            PsyMixerRitualAction action = pool.get(random.nextInt(pool.size()));
            if (!selected.isEmpty() && pool.size() > 1 && selected.get(selected.size() - 1) == action) {
                action = pool.get((pool.indexOf(action) + 1 + random.nextInt(pool.size() - 1)) % pool.size());
            }
            selected.add(action);
        }
        return selected;
    }

    public static float optionalScoreMultiplier(PsyMixerRecipe recipe, PsyMixerRecipeInput input) {
        float bonus = 0.0F;
        if (recipe.hasValidCatalyst(input.catalyst())) {
            bonus += 0.20F;
        }
        if (recipe.hasValidStabilizer(input.stabilizer())) {
            bonus += 0.10F;
        }
        if (recipe.hasValidVessel(input.vessel())) {
            bonus += 0.10F;
        }
        return 1.0F + bonus;
    }

    public static int actionTimeoutFor(
            @Nullable ServerPlayer player,
            PsyMixerRitualAction action,
            float actionTimeoutMultiplier
    ) {
        float timeoutScale = 1.0F;
        if (player != null) {
            float patience = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_STABILITY);
            timeoutScale += Math.min(0.40F, patience * 0.20F);
        }
        return Math.max(30, Math.round(action.defaultTimeoutTicks() * timeoutScale * actionTimeoutMultiplier));
    }

    public static int computeMistakeForgiveness(ServerPlayer player) {
        float grace = DrugEffectRuntimeManager.getServerIntensity(player, EffectType.RITUAL_STABILITY);
        return grace >= 0.40F ? 1 : 0;
    }

    public static PsyMixerRitualQuality previewQuality(
            List<PsyMixerRitualAction> actions,
            int currentActionIndex,
            int qualityScore,
            int mistakes,
            int maxMistakes,
            float finalScoreMultiplier
    ) {
        int possibleScore = qualityScore + remainingScore(actions, currentActionIndex);
        if (mistakes == 0) {
            possibleScore += 2;
        }
        return qualityForScore(actions, possibleScore * finalScoreMultiplier, mistakes, maxMistakes);
    }

    public static PsyMixerRitualQuality finalQuality(
            List<PsyMixerRitualAction> actions,
            int qualityScore,
            int mistakes,
            int maxMistakes,
            float finalScoreMultiplier
    ) {
        if (actions.isEmpty()) {
            return PsyMixerRitualQuality.BASE;
        }
        int finalScore = qualityScore + (mistakes == 0 ? 2 : 0);
        return qualityForScore(actions, finalScore * finalScoreMultiplier, mistakes, maxMistakes);
    }

    private static int baseDrugActionCount(DrugId drugId) {
        return switch (drugId) {
            case COFFEE, TOBACCO, WEED, HASH -> 1;
            case ALCOHOL, COCAINE, CRACK -> 2;
            case LSD, METH -> 3;
            case MUSHROOMS -> 4;
            default -> 0;
        };
    }

    private static PsyMixerRitualQuality qualityForScore(
            List<PsyMixerRitualAction> actions,
            float score,
            int mistakeCount,
            int maxMistakes
    ) {
        int maxScore = maxScore(actions);
        if (maxScore <= 0) {
            return PsyMixerRitualQuality.BASE;
        }
        if (mistakeCount >= maxMistakes) {
            return PsyMixerRitualQuality.CRUDE;
        }
        float ratio = score / (float) maxScore;
        if (ratio >= 0.95F && mistakeCount == 0) {
            return PsyMixerRitualQuality.MASTERWORK;
        }
        if (ratio >= 0.75F && mistakeCount <= Math.max(0, maxMistakes / 2)) {
            return PsyMixerRitualQuality.PERFECT;
        }
        if (ratio >= 0.35F) {
            return PsyMixerRitualQuality.BASE;
        }
        return PsyMixerRitualQuality.CRUDE;
    }

    private static int maxScore(List<PsyMixerRitualAction> actions) {
        int actionScore = actions.stream().mapToInt(PsyMixerRitualAction::maxQualityPoints).sum();
        return actionScore + 2;
    }

    private static int remainingScore(List<PsyMixerRitualAction> actions, int currentActionIndex) {
        int remaining = 0;
        for (int i = Math.max(0, currentActionIndex); i < actions.size(); i++) {
            remaining += actions.get(i).maxQualityPoints();
        }
        return remaining;
    }
}
