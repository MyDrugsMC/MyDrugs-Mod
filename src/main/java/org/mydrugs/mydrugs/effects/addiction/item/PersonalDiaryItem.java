package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemUseAnimation;
import org.mydrugs.mydrugs.ModSounds;
import org.mydrugs.mydrugs.effects.addiction.manager.ItemEffectHandler;

import java.util.List;

public final class PersonalDiaryItem extends AbstractRecoveryItem {
    private static final List<Component> DIARY_MESSAGES = List.of(
            Component.translatable("message.mydrugs.diary.0"),
            Component.translatable("message.mydrugs.diary.1"),
            Component.translatable("message.mydrugs.diary.2"),
            Component.translatable("message.mydrugs.diary.3"),
            Component.translatable("message.mydrugs.diary.4"),
            Component.translatable("message.mydrugs.diary.5")
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
        player.playSound(ModSounds.WRITE.get());
    }
}
