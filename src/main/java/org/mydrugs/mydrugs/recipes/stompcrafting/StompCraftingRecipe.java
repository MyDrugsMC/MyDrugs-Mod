package org.mydrugs.mydrugs.recipes.stompcrafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StompCraftingRecipe implements Recipe<StompCraftingInput> {
    private final List<CountedIngredient> ingredients;
    private final ItemStack result;

    public StompCraftingRecipe(List<CountedIngredient> ingredients, ItemStack result) {
        this.ingredients = List.copyOf(ingredients);
        this.result = result.copy();
    }

    public List<CountedIngredient> ingredients() {
        return ingredients;
    }

    public ItemStack result() {
        return result;
    }

    public List<Ingredient> expandedIngredients() {
        List<Ingredient> expanded = new ArrayList<>();
        for (CountedIngredient counted : ingredients) {
            for (int i = 0; i < counted.count(); i++) {
                expanded.add(counted.ingredient());
            }
        }
        return expanded;
    }

    @Override
    public boolean matches(StompCraftingInput input, Level level) {
        List<ItemStack> items = input.stacks().stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(1))
                .toList();

        List<Ingredient> expanded = expandedIngredients();
        if (items.size() != expanded.size()) {
            return false;
        }

        return canAssign(items, expanded);
    }

    public boolean canAcceptPartial(List<ItemStack> partialItems) {
        List<ItemStack> items = partialItems.stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(1))
                .toList();

        List<Ingredient> expanded = expandedIngredients();
        if (items.size() > expanded.size()) {
            return false;
        }

        return canAssign(items, expanded);
    }

    private static boolean canAssign(List<ItemStack> items, List<Ingredient> slots) {
        if (items.size() > slots.size()) {
            return false;
        }

        // slotToItem[slot] = which item is currently assigned to this slot, or -1 if none
        int[] slotToItem = new int[slots.size()];
        Arrays.fill(slotToItem, -1);

        for (int itemIndex = 0; itemIndex < items.size(); itemIndex++) {
            boolean[] visited = new boolean[slots.size()];
            if (!tryAssign(itemIndex, items, slots, visited, slotToItem)) {
                return false;
            }
        }

        return true;
    }

    private static boolean tryAssign(int itemIndex,
                                     List<ItemStack> items,
                                     List<Ingredient> slots,
                                     boolean[] visited,
                                     int[] slotToItem) {
        ItemStack stack = items.get(itemIndex);

        for (int slotIndex = 0; slotIndex < slots.size(); slotIndex++) {
            if (visited[slotIndex]) {
                continue;
            }

            if (!slots.get(slotIndex).test(stack)) {
                continue;
            }

            visited[slotIndex] = true;

            // If slot is free, take it
            if (slotToItem[slotIndex] == -1) {
                slotToItem[slotIndex] = itemIndex;
                return true;
            }

            // Otherwise try to move the currently assigned item elsewhere
            if (tryAssign(slotToItem[slotIndex], items, slots, visited, slotToItem)) {
                slotToItem[slotIndex] = itemIndex;
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack assemble(StompCraftingInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    public ItemStack getResult() {
        return result.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<StompCraftingInput>> getSerializer() {
        return ModRecipeSerializers.STOMP_CRAFTING.get();
    }

    @Override
    public RecipeType<? extends Recipe<StompCraftingInput>> getType() {
        return ModRecipeTypes.STOMP_CRAFTING.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}