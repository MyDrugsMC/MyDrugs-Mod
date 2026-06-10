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

    public static boolean isPersonalDisc(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PersonalMusicDiscItem && hasValidTrack(stack);
    }

    public static PersonalMusicDiscData getDiscData(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.PERSONAL_MUSIC_DISC.get(), PersonalMusicDiscData.EMPTY);
    }

    public static boolean hasValidTrack(ItemStack stack) {
        PersonalMusicDiscData data = getDiscData(stack);
        return data.trackId() != null && !data.trackId().isBlank();
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
        PersonalMusicDiscData data = getDiscData(stack);
        String title = clean(data.title(), "Personal Track");
        String artist = clean(data.artist(), Component.translatable("tooltip.mydrugs.personal_disc.unknown_artist").getString());
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.title", title).withStyle(ChatFormatting.GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.artist", artist).withStyle(ChatFormatting.GRAY));
        if (data.durationMs() > 0) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.duration", formatTime(data.durationMs())).withStyle(ChatFormatting.DARK_GRAY));
        }
        if (flag.isAdvanced() && data.trackId() != null && !data.trackId().isBlank()) {
            tooltipAdder.accept(Component.translatable("tooltip.mydrugs.personal_disc.track_id", shorten(data.trackId())).withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltipAdder.accept(Component.translatable(data.serverHosted()
                        ? "tooltip.mydrugs.personal_disc.shared_server_track"
                        : "tooltip.mydrugs.personal_disc.legacy_local_track")
                .withStyle(data.serverHosted() ? ChatFormatting.AQUA : ChatFormatting.YELLOW));
    }

    private static String clean(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private static String shorten(String value) {
        return value.length() <= 12 ? value : value.substring(0, 12) + "...";
    }

    private static String formatTime(int ms) {
        int total = Math.max(0, ms / 1000);
        int mins = total / 60;
        int secs = total % 60;
        return String.format(java.util.Locale.ROOT, "%d:%02d", mins, secs);
    }
}
