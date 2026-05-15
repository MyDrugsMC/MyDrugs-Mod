package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemUseAnimation;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.config.DoseConstants;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.effects.addiction.manager.dose.DoseManager;

/**
 * 3-second drink. Drops every drug-path dose by {@link DoseConstants#ANTIDOTE_DOSE_REDUCTION}
 * and interrupts the overdose death timer.
 */
public final class OverdoseAntidoteItem extends AbstractRecoveryItem {
    public OverdoseAntidoteItem(Properties properties) {
        super(properties.stacksTo(16),
                DoseConstants.ANTIDOTE_USE_TICKS,
                0,
                ItemUseAnimation.DRINK,
                true);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        // Intentional direct call to DoseManager: antidote is a recovery action
        // (reduces existing dose contributions) not a drug consumption, so it does
        // not belong on the DrugUseService canonical-consume path. Bypass is
        // documented; do not move this behind DrugUseService.
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DoseManager.applyAntidote(stats);
    }
}
