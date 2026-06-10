package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IModIngredientRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.client.compat.gas.*;

@JeiPlugin
public class JEIModPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var guiHelper = registration.getJeiHelpers().getGuiHelper();

        JeiCategoryDescriptors.ALL.forEach(descriptor -> descriptor.registerCategory(registration, guiHelper));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        JeiCategoryDescriptors.ALL.forEach(descriptor -> descriptor.registerRecipes(registration));
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        JeiCategoryDescriptors.ALL.forEach(descriptor -> descriptor.registerCatalyst(registration));
    }

    @Override
    public void registerIngredients(IModIngredientRegistration registration) {
        registration.register(
                GasJeiTypes.GAS,
                GasJeiUtil.allIngredients(),
                new GasIngredientHelper(),
                new GasIngredientRenderer(),
                GasJeiIngredient.CODEC
        );
    }
}
