package org.mydrugs.mydrugs.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class ProgressionGuideItem extends Item {

    public ProgressionGuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            openGuideClient();
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
                                java.util.function.Consumer<Component> tooltipAdder, TooltipFlag flag) {
        tooltipAdder.accept(Component.translatable("tooltip.mydrugs.progression_guide"));
    }

    // Deliberately uses fully qualified client class names so that the JVM does
    // not need to load them on a dedicated server. This method is only ever
    // reached when level.isClientSide() is true — i.e., never on a server.
    @SuppressWarnings("all")
    private static void openGuideClient() {
        try {
            Class.forName("org.mydrugs.mydrugs.client.guide.GuideClientOpener")
                    .getMethod("open")
                    .invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
