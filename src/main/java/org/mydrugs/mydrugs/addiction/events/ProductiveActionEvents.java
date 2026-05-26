package org.mydrugs.mydrugs.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;

/**
 * Routes in-world productive labor into {@link RecoveryProgressManager} (Phase B).
 *
 * Mining ore and harvesting mature crops are the two block-break productive actions. Machine-craft
 * and distillery cycles call the funnel directly from their own code (Phase D).
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ProductiveActionEvents {
    private ProductiveActionEvents() {
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player) || player.isCreative()) {
            return;
        }

        BlockState state = event.getState();
        if (state.is(Tags.Blocks.ORES)) {
            RecoveryProgressManager.onProductiveAction(player, ActionKind.ORE_MINED, 1.0F);
        } else if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
            RecoveryProgressManager.onProductiveAction(player, ActionKind.CROP_TENDED, 1.0F);
        }
    }
}
