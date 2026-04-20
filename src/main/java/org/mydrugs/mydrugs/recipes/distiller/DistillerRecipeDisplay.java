package org.mydrugs.mydrugs.recipes.distiller;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import org.mydrugs.mydrugs.recipes.ModRecipeDisplays;

import java.util.Optional;

public record DistillerRecipeDisplay(
        SlotDisplay input,
        SlotDisplay result,              // primary fluid output
        Optional<SlotDisplay> output2,   // optional second fluid output
        SlotDisplay craftingStation
) implements RecipeDisplay {
    public static final MapCodec<DistillerRecipeDisplay> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    SlotDisplay.CODEC.fieldOf("input").forGetter(DistillerRecipeDisplay::input),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(DistillerRecipeDisplay::result),
                    SlotDisplay.CODEC.optionalFieldOf("output2").forGetter(DistillerRecipeDisplay::output2),
                    SlotDisplay.CODEC.fieldOf("crafting_station").forGetter(DistillerRecipeDisplay::craftingStation)
            ).apply(instance, DistillerRecipeDisplay::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, DistillerRecipeDisplay> STREAM_CODEC =
            StreamCodec.composite(
                    SlotDisplay.STREAM_CODEC, DistillerRecipeDisplay::input,
                    SlotDisplay.STREAM_CODEC, DistillerRecipeDisplay::result,
                    ByteBufCodecs.optional(SlotDisplay.STREAM_CODEC), DistillerRecipeDisplay::output2,
                    SlotDisplay.STREAM_CODEC, DistillerRecipeDisplay::craftingStation,
                    DistillerRecipeDisplay::new
            );

    @Override
    public RecipeDisplay.Type<? extends RecipeDisplay> type() {
        return ModRecipeDisplays.DISTILLER.get();
    }
}