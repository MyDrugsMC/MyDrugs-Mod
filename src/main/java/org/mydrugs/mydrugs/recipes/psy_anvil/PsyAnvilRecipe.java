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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class PsyAnvilRecipe implements Recipe<PsyAnvilRecipeInput> {
    private final Optional<ResourceLocation> requiredKnowledge;
    private final List<String> pattern;
    private final Map<String, Ingredient> key;
    private final ItemStack result;
    private final int experienceCost;
    private final boolean showIfLocked;
    private final Optional<String> messageKey;
    private PlacementInfo placementInfo;

    public PsyAnvilRecipe(
            Optional<ResourceLocation> requiredKnowledge,
            List<String> pattern,
            Map<String, Ingredient> key,
            ItemStack result,
            int experienceCost,
            boolean showIfLocked,
            Optional<String> messageKey
    ) {
        this.requiredKnowledge = requiredKnowledge;
        this.pattern = List.copyOf(pattern);
        this.key = Map.copyOf(key);
        this.result = result.copy();
        this.experienceCost = experienceCost;
        this.showIfLocked = showIfLocked;
        this.messageKey = messageKey;
    }

    public Optional<ResourceLocation> requiredKnowledge() {
        return requiredKnowledge;
    }

    public Optional<PsyKnowledgeKey> requiredKnowledgeKey() {
        return this.requiredKnowledge.map(PsyKnowledgeKey::new);
    }

    public List<String> pattern() {
        return pattern;
    }

    public Map<String, Ingredient> key() {
        return key;
    }

    public ItemStack result() {
        return result.copy();
    }

    public int experienceCost() {
        return experienceCost;
    }

    public boolean showIfLocked() {
        return showIfLocked;
    }

    public Optional<String> messageKey() {
        return messageKey;
    }

    public boolean canCraft(ServerPlayer player) {
        return this.requiredKnowledgeKey().map(key -> PsyKnowledgeManager.has(player, key)).orElse(true);
    }

    @Override
    public boolean matches(PsyAnvilRecipeInput input, Level level) {
        if (this.pattern.isEmpty()) {
            return false;
        }

        int recipeHeight = this.pattern.size();
        int recipeWidth = this.pattern.stream().mapToInt(String::length).max().orElse(0);
        if (recipeWidth <= 0 || recipeWidth > input.width() || recipeHeight > input.height()) {
            return false;
        }

        for (int xOffset = 0; xOffset <= input.width() - recipeWidth; xOffset++) {
            for (int yOffset = 0; yOffset <= input.height() - recipeHeight; yOffset++) {
                if (matchesAt(input, xOffset, yOffset, recipeWidth, recipeHeight)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matchesAt(PsyAnvilRecipeInput input, int xOffset, int yOffset, int recipeWidth, int recipeHeight) {
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                boolean inside = x >= xOffset && x < xOffset + recipeWidth && y >= yOffset && y < yOffset + recipeHeight;
                Ingredient expected = null;
                if (inside) {
                    int patternX = x - xOffset;
                    int patternY = y - yOffset;
                    String row = this.pattern.get(patternY);
                    if (patternX < row.length()) {
                        String symbol = String.valueOf(row.charAt(patternX));
                        if (!" ".equals(symbol) && !this.key.containsKey(symbol)) {
                            return false;
                        }
                        expected = " ".equals(symbol) ? null : this.key.get(symbol);
                    }
                }

                ItemStack stack = input.getItem(x, y);
                if (expected == null) {
                    if (!stack.isEmpty()) {
                        return false;
                    }
                } else if (!expected.test(stack)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(PsyAnvilRecipeInput input, HolderLookup.Provider registries) {
        return this.result.copy();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.key.values().stream().toList());
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

    public static final class Serializer implements RecipeSerializer<PsyAnvilRecipe> {
        public static final MapCodec<PsyAnvilRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ResourceLocation.CODEC.optionalFieldOf("required_knowledge").forGetter(PsyAnvilRecipe::requiredKnowledge),
                Codec.STRING.listOf().fieldOf("pattern").forGetter(PsyAnvilRecipe::pattern),
                Codec.unboundedMap(Codec.STRING, Ingredient.CODEC).fieldOf("key").forGetter(PsyAnvilRecipe::key),
                ItemStack.CODEC.fieldOf("result").forGetter(PsyAnvilRecipe::result),
                Codec.INT.optionalFieldOf("experience_cost", 0).forGetter(PsyAnvilRecipe::experienceCost),
                Codec.BOOL.optionalFieldOf("show_if_locked", true).forGetter(PsyAnvilRecipe::showIfLocked),
                Codec.STRING.optionalFieldOf("message_key").forGetter(PsyAnvilRecipe::messageKey)
        ).apply(instance, PsyAnvilRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, PsyAnvilRecipe> STREAM_CODEC = StreamCodec.of(
                (buf, recipe) -> {
                    ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional).encode(buf, recipe.requiredKnowledge());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.pattern().size());
                    for (String row : recipe.pattern()) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, row);
                    }
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.key().size());
                    for (Map.Entry<String, Ingredient> entry : recipe.key().entrySet()) {
                        ByteBufCodecs.STRING_UTF8.encode(buf, entry.getKey());
                        Ingredient.CONTENTS_STREAM_CODEC.encode(buf, entry.getValue());
                    }
                    ItemStack.STREAM_CODEC.encode(buf, recipe.result());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.experienceCost());
                    ByteBufCodecs.BOOL.encode(buf, recipe.showIfLocked());
                    ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional).encode(buf, recipe.messageKey());
                },
                buf -> {
                    Optional<ResourceLocation> requiredKnowledge = ResourceLocation.STREAM_CODEC.apply(ByteBufCodecs::optional).decode(buf);
                    int patternSize = ByteBufCodecs.VAR_INT.decode(buf);
                    java.util.ArrayList<String> pattern = new java.util.ArrayList<>();
                    for (int i = 0; i < patternSize; i++) {
                        pattern.add(ByteBufCodecs.STRING_UTF8.decode(buf));
                    }
                    int keySize = ByteBufCodecs.VAR_INT.decode(buf);
                    Map<String, Ingredient> key = new LinkedHashMap<>();
                    for (int i = 0; i < keySize; i++) {
                        key.put(ByteBufCodecs.STRING_UTF8.decode(buf), Ingredient.CONTENTS_STREAM_CODEC.decode(buf));
                    }
                    return new PsyAnvilRecipe(
                            requiredKnowledge,
                            pattern,
                            key,
                            ItemStack.STREAM_CODEC.decode(buf),
                            ByteBufCodecs.VAR_INT.decode(buf),
                            ByteBufCodecs.BOOL.decode(buf),
                            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs::optional).decode(buf)
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
