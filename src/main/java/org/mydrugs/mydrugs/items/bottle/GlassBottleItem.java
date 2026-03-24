package org.mydrugs.mydrugs.items.bottle;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.registry.ModDataComponents;

import java.util.function.Consumer;

public class GlassBottleItem extends Item {
    public static final int CAPACITY_MB = 100;

    public GlassBottleItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public static BottleFluidContent getContent(ItemStack stack) {
        return stack.get(ModDataComponents.BOTTLE_CONTENT.get());
    }

    @Nullable
    public static net.minecraft.resources.ResourceLocation getStoredFluidId(ItemStack stack) {
        BottleFluidContent content = getContent(stack);
        return content == null ? null : content.fluidId();
    }

    public static int getStoredAmount(ItemStack stack) {
        BottleFluidContent content = getContent(stack);
        return content == null ? 0 : content.amountMb();
    }

    public static boolean isEmptyBottle(ItemStack stack) {
        return getStoredAmount(stack) <= 0;
    }

    public static void setContent(ItemStack stack, @Nullable net.minecraft.resources.ResourceLocation fluidId, int amountMb) {
        if (fluidId == null || amountMb <= 0) {
            stack.remove(ModDataComponents.BOTTLE_CONTENT.get());
            return;
        }

        int clamped = Math.min(CAPACITY_MB, amountMb);
        stack.set(ModDataComponents.BOTTLE_CONTENT.get(), new BottleFluidContent(fluidId, clamped));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return getStoredAmount(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getStoredAmount(stack) / (float) CAPACITY_MB);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x6FA8FF;
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            TooltipContext context,
            TooltipDisplay tooltipDisplay,
            Consumer<Component> tooltipAdder,
            TooltipFlag flag
    ) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);

        BottleFluidContent content = getContent(stack);
        if (content == null) {
            tooltipAdder.accept(Component.literal("Empty"));
            return;
        }

        Fluid fluid = BuiltInRegistries.FLUID.getValue(content.fluidId());
        Component fluidName;

        if (fluid == null || fluid == Fluids.EMPTY) {
            fluidName = Component.literal(prettyName(content.fluidId().getPath()));
        } else {
            fluidName = fluid.getFluidType().getDescription();
        }

        tooltipAdder.accept(Component.literal(content.amountMb() + " / " + CAPACITY_MB + " mB"));
        tooltipAdder.accept(fluidName);
    }

    private static String prettyName(String path) {
        String[] parts = path.split("_");
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < parts.length; i++) {
            if (i > 0) builder.append(' ');
            builder.append(Character.toUpperCase(parts[i].charAt(0)));
            builder.append(parts[i].substring(1));
        }

        return builder.toString();
    }
}