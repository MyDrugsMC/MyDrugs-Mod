package org.mydrugs.mydrugs.items.drugs;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

public final class MethPurityTooltip {
    private MethPurityTooltip() {
    }

    public static void append(ItemStack stack, Consumer<Component> tooltipAdder) {
        if (!Purity.has(stack)) {
            return;
        }
        Purity.Band band = Purity.band(Purity.of(stack));
        ChatFormatting color = switch (band) {
            case STREET -> ChatFormatting.RED;
            case CUT -> ChatFormatting.GOLD;
            case PURE -> ChatFormatting.AQUA;
        };
        tooltipAdder.accept(
                Component.translatable("tooltip.mydrugs.meth_purity",
                        Component.translatable("tooltip.mydrugs.meth_purity." + band.key())
                ).withStyle(color)
        );
    }
}
