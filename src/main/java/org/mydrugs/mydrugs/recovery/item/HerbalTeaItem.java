package org.mydrugs.mydrugs.recovery.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemUseAnimation;
import org.mydrugs.mydrugs.addiction.manager.ItemEffectHandler;

public final class HerbalTeaItem extends AbstractRecoveryItem {
    public HerbalTeaItem(Properties properties) {
        super(properties.stacksTo(16), 32, 1800, ItemUseAnimation.DRINK, true);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        ItemEffectHandler.applyHerbalTea(player);
    }
}