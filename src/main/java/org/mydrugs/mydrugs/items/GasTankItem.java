package org.mydrugs.mydrugs.items;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import org.mydrugs.mydrugs.gas.GasTankContents;
import org.mydrugs.mydrugs.gas.GasTankItemHandler;
import org.mydrugs.mydrugs.gas.GasType;
import org.mydrugs.mydrugs.gas.ModGases;
import org.mydrugs.mydrugs.registry.ModDataComponents;

import java.util.function.Consumer;

public class GasTankItem extends BlockItem {
    public GasTankItem(Block block, Item.Properties properties) {
        super(block, properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        GasTankContents contents = stack.getOrDefault(
                ModDataComponents.GAS_TANK_CONTENTS.get(),
                GasTankContents.EMPTY
        );

        GasType gas = null;
        if (!contents.gasId().isBlank()) {
            gas = ModGases.get(ResourceLocation.parse(contents.gasId()));
        }

        Component gasName = gas == null
                ? Component.translatable("tooltip.mydrugs.empty")
                : Component.translatable("gas." + gas.id().getNamespace() + "." + gas.id().getPath());

        tooltipAdder.accept(
                Component.translatable("tooltip.mydrugs.gas", gasName)
                        .withStyle(ChatFormatting.GRAY)
        );

        tooltipAdder.accept(
                Component.translatable(
                        "tooltip.mydrugs.amount",
                        contents.amount(),
                        GasTankItemHandler.CAPACITY
                ).withStyle(ChatFormatting.DARK_GRAY)
        );
    }
}