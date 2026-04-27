package org.mydrugs.mydrugs.compat.gas;

import mezz.jei.api.ingredients.IIngredientType;

public final class GasJeiTypes {
    public static final IIngredientType<GasJeiIngredient> GAS = new IIngredientType<>() {
        @Override
        public Class<? extends GasJeiIngredient> getIngredientClass() {
            return GasJeiIngredient.class;
        }
    };

    private GasJeiTypes() {
    }
}