package org.mydrugs.mydrugs.recipes.stomp_crafting;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.recipes.grinder.GrindingRecipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StompCrafterRecipeResolver {

    private static final int WORK_UNIT_SCALE = 10;

    private StompCrafterRecipeResolver() {
    }

    public static boolean canAcceptPartial(ServerLevel level, List<ItemStack> partialItems) {
        List<ItemStack> items = normalize(partialItems);

        if (items.isEmpty()) {
            return true;
        }

        // Grinding: only valid when exactly one item is present
        if (items.size() == 1) {
            ItemStack stack = items.get(0);

            for (RecipeHolder<?> rawHolder : level.recipeAccess().recipeMap().byType(ModRecipeTypes.GRINDING.get())) {
                if (rawHolder.value() instanceof GrindingRecipe recipe) {
                    if (recipe.ingredient().test(stack)) {
                        return true;
                    }
                }
            }
        }

        // Stomp crafting: partial matching logic already exists
        for (RecipeHolder<?> rawHolder : level.recipeAccess().recipeMap().byType(ModRecipeTypes.STOMP_CRAFTING.get())) {
            if (rawHolder.value() instanceof StompCraftingRecipe recipe) {
                if (recipe.canAcceptPartial(items)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static @Nullable ProcessMatch findExactMatch(ServerLevel level, List<ItemStack> inputItems) {
        List<ItemStack> items = normalize(inputItems);

        if (items.isEmpty()) {
            return null;
        }

        // Priority rule:
        // for exactly one item, prefer grinding first.
        // If you want single-item stomp recipes to win instead, swap the order.
        if (items.size() == 1) {
            Optional<RecipeHolder<GrindingRecipe>> grinding =
                    level.recipeAccess().getRecipeFor(
                            ModRecipeTypes.GRINDING.get(),
                            new SingleRecipeInput(items.get(0)),
                            level
                    );

            if (grinding.isPresent()) {
                return new GrindingMatch(grinding.get());
            }
        }

        Optional<RecipeHolder<StompCraftingRecipe>> stomp =
                level.recipeAccess().getRecipeFor(
                        ModRecipeTypes.STOMP_CRAFTING.get(),
                        new StompCraftingInput(items),
                        level
                );

        if (stomp.isPresent()) {
            return new StompMatch(stomp.get());
        }

        return null;
    }

    private static List<ItemStack> normalize(List<ItemStack> stacks) {
        List<ItemStack> result = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (!stack.isEmpty()) {
                result.add(stack.copyWithCount(1));
            }
        }

        return result;
    }

    public sealed interface ProcessMatch permits GrindingMatch, StompMatch {
        int requiredWork();

        ItemStack assemble(ServerLevel level, List<ItemStack> items);
    }

    public record GrindingMatch(RecipeHolder<GrindingRecipe> holder) implements ProcessMatch {
        @Override
        public int requiredWork() {
            return holder.value().work() * WORK_UNIT_SCALE;
        }

        @Override
        public ItemStack assemble(ServerLevel level, List<ItemStack> items) {
            return holder.value().assemble(
                    new SingleRecipeInput(items.get(0)),
                    level.registryAccess()
            );
        }
    }

    public record StompMatch(RecipeHolder<StompCraftingRecipe> holder) implements ProcessMatch {
        @Override
        public int requiredWork() {
            return holder.value().clampedWork() * WORK_UNIT_SCALE;
        }

        @Override
        public ItemStack assemble(ServerLevel level, List<ItemStack> items) {
            return holder.value().assemble(
                    new StompCraftingInput(items),
                    level.registryAccess()
            );
        }
    }
}