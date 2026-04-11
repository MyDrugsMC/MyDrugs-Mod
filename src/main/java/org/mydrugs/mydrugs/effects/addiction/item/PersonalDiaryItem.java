package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemUseAnimation;
import org.mydrugs.mydrugs.effects.addiction.manager.ItemEffectHandler;

import java.util.List;

public final class PersonalDiaryItem extends AbstractRecoveryItem {
    private static final List<Component> DIARY_MESSAGES = List.of(
            Component.literal("I wrote it down. It feels a little lighter now."),
            Component.literal("Some thoughts stop screaming once they are on paper."),
            Component.literal("Today I chose to put it into words."),
            Component.literal("The page understood what I could not say out loud."),
            Component.literal("Writing it down made the noise inside me quieter."),
            Component.literal("Not everything is fixed, but at least it is named.")
    );

    public PersonalDiaryItem(Properties properties) {
        super(properties.stacksTo(1), 40, 1200, ItemUseAnimation.BOW, false);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        ItemEffectHandler.applyDiary(player);
    }

    @Override
    protected void afterUse(ServerPlayer player) {
        player.sendSystemMessage(DIARY_MESSAGES.get(player.getRandom().nextInt(DIARY_MESSAGES.size())));
    }
}