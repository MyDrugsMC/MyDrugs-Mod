package org.mydrugs.mydrugs.recipes.psy_anvil;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PsyAnvilRecipe implements Recipe<PsyAnvilRecipeInput> {
    private final Optional<ResourceLocation> requiredKnowledge;
    private final List<PsyAnvilIngredient> ingredients;
    private final ItemStack result;
    private final int experienceCost;
    private final boolean showIfLocked;
    private final Optional<String> messageKey;
    private PlacementInfo placementInfo;

    public PsyAnvilRecipe(
            Optional<ResourceLocation> requiredKnowledge,
            List<PsyAnvilIngredient> ingredients,
            ItemStack result,
            int experienceCost,
            boolean showIfLocked,
            Optional<String> messageKey
    ) {
        this.requiredKnowledge = requiredKnowledge;
        this.ingredients = List.copyOf(ingredients);
        this.result = result.copy();
        this.experienceCost = experienceCost;
        this.showIfLocked = showIfLocked;
        this.messageKey = messageKey;
    }

    public Optional<ResourceLocation> requiredKnowledge() {
        return this.requiredKnowledge;
    }

    public Optional<PsyKnowledgeKey> requiredKnowledgeKey() {
        return this.requiredKnowledge.map(PsyKnowledgeKey::new);
    }

    public List<PsyAnvilIngredient> ingredients() {
        return this.ingredients;
    }

    public ItemStack result() {
        return this.result.copy();
    }

    public int experienceCost() {
        return this.experienceCost;
    }

    public boolean showIfLocked() {
        return this.showIfLocked;
    }

    public Optional<String> messageKey() {
        return this.messageKey;
    }

    public boolean canCraft(ServerPlayer player) {
        return this.requiredKnowledgeKey()
                .map(key -> PsyKnowledgeManager.has(player, key))
                .orElse(true);
    }

    @Override
    public boolean matches(PsyAnvilRecipeInput input, Level level) {
        if (this.ingredients.isEmpty()) {
            return false;
        }

        List<ItemStack> nonEmptyStacks = new ArrayList<>();

        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);

            if (!stack.isEmpty()) {
                nonEmptyStacks.add(stack);
            }
        }

        if (nonEmptyStacks.isEmpty()) {
            return false;
        }

        int requiredTotalCount = 0;

        for (PsyAnvilIngredient ingredient : this.ingredients) {
            if (ingredient.count() <= 0) {
                return false;
            }

            requiredTotalCount += ingredient.count();
        }

        int inputTotalCount = 0;

        for (ItemStack stack : nonEmptyStacks) {
            inputTotalCount += stack.getCount();
        }

        if (inputTotalCount != requiredTotalCount) {
            return false;
        }

        int[] remainingCounts = new int[nonEmptyStacks.size()];

        for (int i = 0; i < nonEmptyStacks.size(); i++) {
            remainingCounts[i] = nonEmptyStacks.get(i).getCount();
        }

        return this.matchesIngredient(0, nonEmptyStacks, remainingCounts);
    }

    private boolean matchesIngredient(
            int ingredientIndex,
            List<ItemStack> stacks,
            int[] remainingCounts
    ) {
        if (ingredientIndex >= this.ingredients.size()) {
            for (int remainingCount : remainingCounts) {
                if (remainingCount != 0) {
                    return false;
                }
            }

            return true;
        }

        PsyAnvilIngredient ingredient = this.ingredients.get(ingredientIndex);

        return this.consumeIngredient(
                ingredientIndex,
                ingredient.ingredient(),
                ingredient.count(),
                stacks,
                remainingCounts,
                0
        );
    }

    private boolean consumeIngredient(
            int ingredientIndex,
            Ingredient ingredient,
            int amountLeft,
            List<ItemStack> stacks,
            int[] remainingCounts,
            int slotIndex
    ) {
        if (amountLeft == 0) {
            return this.matchesIngredient(ingredientIndex + 1, stacks, remainingCounts);
        }

        if (slotIndex >= stacks.size()) {
            return false;
        }

        ItemStack stack = stacks.get(slotIndex);
        int availableCount = remainingCounts[slotIndex];

        if (availableCount > 0 && ingredient.test(stack)) {
            int maxToTake = Math.min(availableCount, amountLeft);

            for (int amountToTake = maxToTake; amountToTake >= 1; amountToTake--) {
                remainingCounts[slotIndex] -= amountToTake;

                if (this.consumeIngredient(
                        ingredientIndex,
                        ingredient,
                        amountLeft - amountToTake,
                        stacks,
                        remainingCounts,
                        slotIndex + 1
                )) {
                    remainingCounts[slotIndex] += amountToTake;
                    return true;
                }

                remainingCounts[slotIndex] += amountToTake;
            }
        }

        return this.consumeIngredient(
                ingredientIndex,
                ingredient,
                amountLeft,
                stacks,
                remainingCounts,
                slotIndex + 1
        );
    }

    @Override
    public ItemStack assemble(PsyAnvilRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            List<Ingredient> placementIngredients = new ArrayList<>();

            for (PsyAnvilIngredient ingredient : this.ingredients) {
                for (int i = 0; i < ingredient.count(); i++) {
                    placementIngredients.add(ingredient.ingredient());
                }
            }

            this.placementInfo = PlacementInfo.create(placementIngredients);
        }

        return this.placementInfo;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<? extends Recipe<PsyAnvilRecipeInput>> getSerializer() {
        return ModRecipeSerializers.PSY_ANVIL.get();
    }

    @Override
    public RecipeType<? extends Recipe<PsyAnvilRecipeInput>> getType() {
        return ModRecipeTypes.PSY_ANVIL.get();
    }

    public record PsyAnvilIngredient(Ingredient ingredient, int count) {
        public static final Codec<PsyAnvilIngredient> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Ingredient.CODEC.fieldOf("ingredient").forGetter(PsyAnvilIngredient::ingredient),
                Codec.INT.fieldOf("count").forGetter(PsyAnvilIngredient::count)
        ).apply(instance, PsyAnvilIngredient::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PsyAnvilIngredient> STREAM_CODEC = StreamCodec.of(
                (buf, ingredient) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient.ingredient());
                    ByteBufCodecs.VAR_INT.encode(buf, ingredient.count());
                },
                buf -> new PsyAnvilIngredient(
                        Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                        ByteBufCodecs.VAR_INT.decode(buf)
                )
        );
    }

    public static final class Serializer implements RecipeSerializer<PsyAnvilRecipe> {
        public static final MapCodec<PsyAnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("required_knowledge").forGetter(PsyAnvilRecipe::requiredKnowledge),
                PsyAnvilIngredient.CODEC.listOf().fieldOf("ingredients").forGetter(PsyAnvilRecipe::ingredients),
                ItemStack.CODEC.fieldOf("result").forGetter(PsyAnvilRecipe::result),
                Codec.INT.optionalFieldOf("experience_cost", 0).forGetter(PsyAnvilRecipe::experienceCost),
                Codec.BOOL.optionalFieldOf("show_if_locked", true).forGetter(PsyAnvilRecipe::showIfLocked),
                Codec.STRING.optionalFieldOf("message_key").forGetter(PsyAnvilRecipe::messageKey)
        ).apply(instance, PsyAnvilRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PsyAnvilRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.requiredKnowledge());

                    ByteBufCodecs.VAR_INT.encode(buf, recipe.ingredients().size());

                    for (PsyAnvilIngredient ingredient : recipe.ingredients()) {
                        PsyAnvilIngredient.STREAM_CODEC.encode(buf, ingredient);
                    }

                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.experienceCost());
                    ByteBufCodecs.BOOL.encode(buf, recipe.showIfLocked());
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional).encode(buf, recipe.messageKey());
                },
                buf -> {
                    Optional<ResourceLocation> requiredKnowledge =
                            ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf);

                    int ingredientCount = ByteBufCodecs.VAR_INT.decode(buf);
                    List<PsyAnvilIngredient> ingredients = new ArrayList<>();

                    for (int i = 0; i < ingredientCount; i++) {
                        ingredients.add(PsyAnvilIngredient.STREAM_CODEC.decode(buf));
                    }

                    ItemStack result = ItemStack.STREAM_CODEC.decode(buf);
                    int experienceCost = ByteBufCodecs.VAR_INT.decode(buf);
                    boolean showIfLocked = ByteBufCodecs.BOOL.decode(buf);
                    Optional<String> messageKey = ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional).decode(buf);

                    return new PsyAnvilRecipe(
                            requiredKnowledge,
                            ingredients,
                            result,
                            experienceCost,
                            showIfLocked,
                            messageKey
                    );
                }
        );

        @Override
        public MapCodec<PsyAnvilRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, PsyAnvilRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}