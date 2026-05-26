package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.DrugAddictionStats;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.state.BadTripManager;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipe;
import org.mydrugs.mydrugs.recipes.psy_mixer.PsyMixerRecipeInput;

import java.util.Locale;
import java.util.Optional;

public final class PsyMixerRecipeMatcher {
    private PsyMixerRecipeMatcher() {
    }

    public static PsyMixerRecipeInput input(NonNullList<ItemStack> items) {
        return new PsyMixerRecipeInput(
                items.get(PsyMixerMultiblock.SLOT_BASE),
                items.get(PsyMixerMultiblock.SLOT_MATERIAL),
                items.get(PsyMixerMultiblock.SLOT_CATALYST),
                items.get(PsyMixerMultiblock.SLOT_STABILIZER),
                items.get(PsyMixerMultiblock.SLOT_VESSEL)
        );
    }

    public static Optional<RecipeHolder<PsyMixerRecipe>> findMatchingRecipe(
            ServerLevel level,
            PsyMixerRecipeInput input
    ) {
        return level.recipeAccess().getRecipeFor(ModRecipeTypes.PSY_MIXER.get(), input, level);
    }

    public static Optional<RecipeHolder<PsyMixerRecipe>> findRecipeById(
            ServerLevel level,
            ResourceLocation recipeId
    ) {
        return level.recipeAccess().recipeMap()
                .byType(ModRecipeTypes.PSY_MIXER.get())
                .stream()
                .filter(holder -> holder.id().location().equals(recipeId))
                .findFirst();
    }

    public static boolean hasRequiredKnowledge(ServerPlayer player, PsyMixerRecipe recipe) {
        return recipe.requiredKnowledge()
                .map(PsyKnowledgeKey::new)
                .map(key -> PsyKnowledgeManager.has(player, key))
                .orElse(true);
    }

    public static LifetimeDoseCheck lifetimeDoseCheck(ServerPlayer player, PsyMixerRecipe recipe) {
        if (recipe.requiredDrug().isEmpty() || recipe.requiredLifetimeDose() <= 0.0F) {
            return LifetimeDoseCheck.met(0.0F, 0.0F);
        }

        DrugId drugId = DrugId.bySerializedNameOrNull(recipe.requiredDrug().get());
        if (drugId == null) {
            return LifetimeDoseCheck.unmet(recipe.requiredLifetimeDose(), 0.0F);
        }

        PlayerAddictionStats playerStats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugAddictionStats drugStats = playerStats.getDrugStats(drugId);
        float lifetime = drugStats == null ? 0.0F : drugStats.lifetimeDoseConsumed;
        return lifetime + 0.001F >= recipe.requiredLifetimeDose()
                ? LifetimeDoseCheck.met(recipe.requiredLifetimeDose(), lifetime)
                : LifetimeDoseCheck.unmet(recipe.requiredLifetimeDose(), lifetime);
    }

    public static boolean hasRequiredDrugCategory(
            PlayerAddictionStats stats,
            Optional<String> requiredCategory
    ) {
        if (requiredCategory.isEmpty()) {
            return true;
        }

        DrugCategory category;
        try {
            category = DrugCategory.valueOf(requiredCategory.get().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return false;
        }

        for (DrugId drugId : stats.getTrackedDrugIds()) {
            if (DrugRegistry.getCategory(drugId) != category) {
                continue;
            }
            DrugAddictionStats drugStats = stats.getDrugStats(drugId);
            if (drugStats != null && drugStats.currentDose() > 0.001F) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasRequiredActiveEffect(ServerPlayer player, Optional<String> requiredEffect) {
        if (requiredEffect.isEmpty()) {
            return true;
        }

        EffectType type = EffectType.bySerializedNameOrNull(requiredEffect.get());
        return type != null && DrugEffectRuntimeManager.getServerIntensity(player, type) > 0.001F;
    }

    public static boolean hasBadTripState(ServerPlayer player, PlayerAddictionStats stats) {
        return BadTripManager.isActive(stats);
    }

    public record LifetimeDoseCheck(boolean met, float required, float current) {
        private static LifetimeDoseCheck met(float required, float current) {
            return new LifetimeDoseCheck(true, required, current);
        }

        private static LifetimeDoseCheck unmet(float required, float current) {
            return new LifetimeDoseCheck(false, required, current);
        }
    }
}
