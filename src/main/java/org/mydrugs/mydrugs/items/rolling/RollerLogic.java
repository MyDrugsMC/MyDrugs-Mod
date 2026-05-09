package org.mydrugs.mydrugs.items.rolling;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.menu.RollerMenu;
import org.mydrugs.mydrugs.items.data.ModDataComponents;

public final class RollerLogic {
    private RollerLogic() {
    }

    public static boolean isPaper(ItemStack stack) {
        return stack.is(Items.PAPER);
    }

    public static boolean isFilter(ItemStack stack) {
        return stack.is(ModItems.CIGARET_FILTER.get());
    }

    public static boolean isRollingIngredient(ItemStack stack) {
        return stack.getItem() instanceof RollingIngredient;
    }

    public static @Nullable RolledDrugContent readContent(Container container) {
        if (!isPaper(container.getItem(RollerMenu.PAPER_SLOT))) {
            return null;
        }
        if (!isFilter(container.getItem(RollerMenu.FILTER_SLOT))) {
            return null;
        }

        ItemStack first = container.getItem(RollerMenu.INGREDIENT_1_SLOT);
        ItemStack second = container.getItem(RollerMenu.INGREDIENT_2_SLOT);
        ItemStack third = container.getItem(RollerMenu.INGREDIENT_3_SLOT);

        DrugId a = readIngredient(first);
        DrugId b = readIngredient(second);
        DrugId c = readIngredient(third);

        if (a == null || b == null || c == null) {
            return null;
        }

        return new RolledDrugContent(a, b, c);
    }

    private static @Nullable DrugId readIngredient(ItemStack stack) {
        if (stack.isEmpty() || !(stack.getItem() instanceof RollingIngredient ingredient)) {
            return null;
        }
        return ingredient.getRollingDrug(stack);
    }

    public static ItemStack createResult(Container container) {
        RolledDrugContent content = readContent(container);
        if (content == null) {
            return ItemStack.EMPTY;
        }

        ItemStack result = new ItemStack(
                content.isAllTobacco() ? ModItems.CIGARETTE.get() : ModItems.JOINT.get()
        );
        result.set(ModDataComponents.ROLLED_CONTENT.get(), content);
        return result;
    }

    public static boolean canPlaceResult(ItemStack output, ItemStack result) {
        if (result.isEmpty()) {
            return false;
        }

        if (output.isEmpty()) {
            return true;
        }

        return ItemStack.isSameItemSameComponents(output, result)
                && output.getCount() < output.getMaxStackSize();
    }

    public static void craft(Container container) {
        ItemStack result = createResult(container);
        if (result.isEmpty()) {
            return;
        }

        ItemStack output = container.getItem(RollerMenu.OUTPUT_SLOT);
        if (!canPlaceResult(output, result)) {
            return;
        }

        container.removeItem(RollerMenu.PAPER_SLOT, 1);
        container.removeItem(RollerMenu.FILTER_SLOT, 1);
        container.removeItem(RollerMenu.INGREDIENT_1_SLOT, 1);
        container.removeItem(RollerMenu.INGREDIENT_2_SLOT, 1);
        container.removeItem(RollerMenu.INGREDIENT_3_SLOT, 1);

        if (output.isEmpty()) {
            container.setItem(RollerMenu.OUTPUT_SLOT, result);
        } else {
            output.grow(1);
            container.setItem(RollerMenu.OUTPUT_SLOT, output);
        }

        container.setChanged();
    }
}
