package org.mydrugs.mydrugs.compat.gas;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.gas.ModGases;

import java.util.List;

public final class GasIngredientHelper implements IIngredientHelper<GasJeiIngredient> {
    @Override
    public IIngredientType<GasJeiIngredient> getIngredientType() {
        return GasJeiTypes.GAS;
    }

    @Override
    public String getDisplayName(GasJeiIngredient ingredient) {
        return GasJeiUtil.displayName(ingredient.id());
    }

    @Override
    public Object getUid(GasJeiIngredient ingredient, UidContext context) {
        // Ignore amount for JEI lookup identity
        return ingredient.id();
    }

    @Override
    public Object getGroupingUid(GasJeiIngredient ingredient) {
        return ingredient.id();
    }

    @Override
    public String getDisplayModId(GasJeiIngredient ingredient) {
        return ingredient.id().getNamespace();
    }

    @Override
    public long getAmount(GasJeiIngredient ingredient) {
        return ingredient.amount();
    }

    @Override
    public GasJeiIngredient copyWithAmount(GasJeiIngredient ingredient, long amount) {
        return ingredient.withAmount(amount);
    }

    @Override
    public Iterable<Integer> getColors(GasJeiIngredient ingredient) {
        return List.of(GasJeiUtil.color(ingredient.id()));
    }

    @Override
    public ResourceLocation getResourceLocation(GasJeiIngredient ingredient) {
        return ingredient.id();
    }

    @Override
    public ItemStack getCheatItemStack(GasJeiIngredient ingredient) {
        return ItemStack.EMPTY;
    }

    @Override
    public GasJeiIngredient copyIngredient(GasJeiIngredient ingredient) {
        return ingredient;
    }

    @Override
    public GasJeiIngredient normalizeIngredient(GasJeiIngredient ingredient) {
        return ingredient.normalized();
    }

    @Override
    public boolean isValidIngredient(GasJeiIngredient ingredient) {
        return ingredient != null
                && ingredient.amount() > 0
                && ModGases.get(ingredient.id()) != null;
    }

    @Override
    public String getErrorInfo(@Nullable GasJeiIngredient ingredient) {
        if (ingredient == null) {
            return "null gas ingredient";
        }
        return "GasJeiIngredient[" + ingredient.id() + ", amount=" + ingredient.amount() + "]";
    }
}