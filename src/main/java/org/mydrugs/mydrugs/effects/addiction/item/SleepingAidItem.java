package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemUseAnimation;
import org.mydrugs.mydrugs.effects.addiction.manager.ItemEffectHandler;

public final class SleepingAidItem extends AbstractRecoveryItem {
    public SleepingAidItem(Properties properties) {
        super(properties.stacksTo(16), 24, 6000, ItemUseAnimation.DRINK, true);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        ItemEffectHandler.applySleepingAid(player);
    }
}