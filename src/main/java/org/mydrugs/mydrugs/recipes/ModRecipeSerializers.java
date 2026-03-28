package org.mydrugs.mydrugs.recipes;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.advanced_furnace.AdvancedFurnaceRecipe;
import org.mydrugs.mydrugs.recipes.distiller.DistillerRecipe;
import org.mydrugs.mydrugs.recipes.drying.DryingRecipe;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;
import org.mydrugs.mydrugs.recipes.mixing_vat.MixingVatRecipe;
import org.mydrugs.mydrugs.recipes.sieving.SieveRecipe;
import org.mydrugs.mydrugs.recipes.stompcrafting.StompCraftingRecipe;

import java.util.function.Supplier;

public class ModRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MyDrugs.MODID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<GrindingRecipe>> GRINDING =
            RECIPE_SERIALIZERS.register("grinding", GrindingRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<StompCraftingRecipe>> STOMP_CRAFTING =
            RECIPE_SERIALIZERS.register("stomp_crafting", StompCraftingRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<AdvancedFurnaceRecipe>> ADVANCED_FURNACE =
            RECIPE_SERIALIZERS.register("advanced_furnace", AdvancedFurnaceRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<MixingVatRecipe>> MIXING_VAT =
            RECIPE_SERIALIZERS.register("mixing_vat", MixingVatRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<DistillerRecipe>> DISTILLER =
            RECIPE_SERIALIZERS.register("distiller", DistillerRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<DryingRecipe>> DRYING_SERIALIZER =
            RECIPE_SERIALIZERS.register("drying", DryingRecipe.Serializer::new);

    public static final Supplier<RecipeSerializer<SieveRecipe>> SIEVING_SERIALIZER =
            RECIPE_SERIALIZERS.register("sieving", SieveRecipe.Serializer::new);

    private ModRecipeSerializers() {}
}
