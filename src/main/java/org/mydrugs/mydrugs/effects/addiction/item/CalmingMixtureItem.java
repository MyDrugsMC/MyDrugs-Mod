package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemUseAnimation;
import org.mydrugs.mydrugs.effects.addiction.manager.ItemEffectHandler;

public final class CalmingMixtureItem extends AbstractRecoveryItem {
    public CalmingMixtureItem(Properties properties) {
        super(properties.stacksTo(16), 32, 2400, ItemUseAnimation.DRINK, true);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        ItemEffectHandler.applyCalmingMixture(player);
    }
}