package org.mydrugs.mydrugs.client.compat;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Locale;

public final class JeiCompatUtil {
    private JeiCompatUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> List<T> cachedRecipes(String methodName) {
        try {
            Method method = ClientRecipesCache.class.getMethod(methodName);
            Object result = method.invoke(null);
            if (result instanceof List<?> list) {
                return (List<T>) list;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return List.of();
    }

    public static IDrawable iconFromField(IGuiHelper helper, Class<?> owner, String... fieldNames) {
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, stackFromField(owner, fieldNames));
    }

    public static void registerFieldCatalyst(
            IRecipeCatalystRegistration registration,
            RecipeType<?> recipeType,
            Class<?> owner,
            String... fieldNames
    ) {
        ItemStack stack = stackFromField(owner, fieldNames);
        if (!stack.isEmpty() && !stack.is(Items.BARRIER)) {
            registration.addRecipeCatalyst(stack, recipeType);
        }
    }

    public static ItemStack stackFromField(Class<?> owner, String... fieldNames) {
        for (String fieldName : fieldNames) {
            try {
                Field field = owner.getDeclaredField(fieldName);
                field.setAccessible(true);
                Object raw = field.get(null);
                Object unwrapped = unwrap(raw);

                if (unwrapped instanceof ItemStack stack) {
                    return stack.copy();
                }
                if (unwrapped instanceof ItemLike itemLike) {
                    return new ItemStack(itemLike);
                }
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return new ItemStack(Items.BARRIER);
    }

    private static Object unwrap(@Nullable Object raw) {
        if (raw == null) {
            return null;
        }

        try {
            Method get = raw.getClass().getMethod("get");
            return get.invoke(raw);
        } catch (ReflectiveOperationException ignored) {
            return raw;
        }
    }

    @Nullable
    public static ResourceLocation idOf(@Nullable Object wrapper, String... methodNames) {
        Object value = call(wrapper, methodNames);
        return value instanceof ResourceLocation id ? id : null;
    }

    public static int intOf(@Nullable Object wrapper, String... methodNames) {
        Object value = call(wrapper, methodNames);
        return value instanceof Number n ? n.intValue() : 0;
    }

    public static long longOf(@Nullable Object wrapper, String... methodNames) {
        Object value = call(wrapper, methodNames);
        return value instanceof Number n ? n.longValue() : 0L;
    }

    public static int countOf(@Nullable Object wrapper) {
        Object value = call(wrapper, "count", "amount");
        return value instanceof Number n ? Math.max(1, n.intValue()) : 1;
    }

    public static Ingredient ingredientOf(@Nullable Object wrapper) {
        if (wrapper instanceof Ingredient ingredient) {
            return ingredient;
        }

        Object value = call(wrapper, "ingredient", "input", "item");
        return value instanceof Ingredient ingredient ? ingredient : Ingredient.of((ItemLike) null);
    }

    public static ItemStack stackOf(@Nullable Object wrapper) {
        if (wrapper == null) {
            return ItemStack.EMPTY;
        }
        if (wrapper instanceof ItemStack stack) {
            return stack.copy();
        }

        Object directStack = call(wrapper, "stack", "itemStack", "result");
        if (directStack instanceof ItemStack stack) {
            return stack.copy();
        }

        ResourceLocation itemId = idOf(wrapper, "item", "itemId", "id", "result", "output");
        return stack(itemId, countOf(wrapper));
    }

    public static ItemStack stack(@Nullable ResourceLocation itemId, int count) {
        if (itemId == null) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.getOptional(itemId).orElse(Items.AIR);
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        return new ItemStack(item, Math.max(1, count));
    }

    public static Fluid fluid(@Nullable ResourceLocation fluidId) {
        if (fluidId == null) {
            return Fluids.EMPTY;
        }
        return BuiltInRegistries.FLUID.getOptional(fluidId).orElse(Fluids.EMPTY);
    }

    public static String gasText(@Nullable Object wrapper) {
        ResourceLocation gasId = idOf(wrapper, "gas", "gasId", "id");
        long amount = longOf(wrapper, "amount");
        if (gasId == null || amount <= 0) {
            return "";
        }
        return shortId(gasId) + " x" + amount;
    }

    public static String fluidText(@Nullable Object wrapper) {
        ResourceLocation fluidId = idOf(wrapper, "fluid", "fluidId", "id");
        int amount = intOf(wrapper, "amount");
        if (fluidId == null || amount <= 0) {
            return "";
        }
        return shortId(fluidId) + " " + amount + " mB";
    }

    public static String serializedName(@Nullable Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof StringRepresentable representable) {
            return representable.getSerializedName();
        }
        return value.toString().toLowerCase(Locale.ROOT);
    }

    public static String shortId(@Nullable ResourceLocation id) {
        if (id == null) {
            return "?";
        }
        return "minecraft".equals(id.getNamespace()) ? id.getPath() : id.toString();
    }

    @Nullable
    private static Object call(@Nullable Object target, String... methodNames) {
        if (target == null) {
            return null;
        }

        for (String methodName : methodNames) {
            try {
                Method method = target.getClass().getMethod(methodName);
                method.setAccessible(true);
                return method.invoke(target);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        return null;
    }
}