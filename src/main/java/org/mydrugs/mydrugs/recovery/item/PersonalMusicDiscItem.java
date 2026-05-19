package org.mydrugs.mydrugs.recovery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.items.data.PersonalMusicDiscData;

import java.util.function.Consumer;

public final class PersonalMusicDiscItem extends Item {
    public PersonalMusicDiscItem(Properties properties) {
        super(properties.stacksTo(1));
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
        PersonalMusicDiscData data = stack.getOrDefault(ModDataComponents.PERSONAL_MUSIC_DISC.get(), PersonalMusicDiscData.EMPTY);
        if (!data.title().isBlank()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.title", data.title()).withStyle(ChatFormatting.GRAY));
        }
        if (!data.artist().isBlank()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.artist", data.artist()).withStyle(ChatFormatting.GRAY));
        }
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.requires_local_track").withStyle(ChatFormatting.YELLOW));
    }
}
