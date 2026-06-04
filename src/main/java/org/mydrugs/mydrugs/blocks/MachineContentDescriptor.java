package org.mydrugs.mydrugs.blocks;

import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * Describes a single machine and which systems it is expected to own.
 *
 * <p>A {@code null} holder means that system is intentionally absent (e.g. a passive machine with no
 * menu, or a multiblock former with no recipe type). This lets audits distinguish "no menu expected"
 * and "no recipe expected" from actual missing-wiring bugs (where a system is expected but resolves
 * to {@code null} at runtime).</p>
 */
public record MachineContentDescriptor(
        String id,
        Supplier<? extends Block> block,
        @Nullable Supplier<? extends BlockEntityType<?>> blockEntity,
        @Nullable Supplier<? extends MenuType<?>> menu,
        @Nullable DeferredHolder<RecipeType<?>, ? extends RecipeType<?>> recipeType,
        @Nullable DeferredHolder<RecipeSerializer<?>, ? extends RecipeSerializer<?>> recipeSerializer,
        String blockLangKey
) {
    public boolean expectsBlockEntity() {
        return blockEntity != null;
    }

    public boolean expectsMenu() {
        return menu != null;
    }

    public boolean expectsRecipe() {
        return recipeType != null;
    }
}
