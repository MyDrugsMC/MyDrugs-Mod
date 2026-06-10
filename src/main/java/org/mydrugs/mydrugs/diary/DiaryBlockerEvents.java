package org.mydrugs.mydrugs.diary;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.blocks.entity.PsychotropeResonatorBlockEntity;
import org.mydrugs.mydrugs.diary.DiaryBlockerRoutes.Route;
import org.mydrugs.mydrugs.machine.MachineStatusProvider;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class DiaryBlockerEvents {
    private DiaryBlockerEvents() {
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        BlockEntity blockEntity = player.level().getBlockEntity(event.getPos());
        Route route = routeFor(blockEntity);
        if (route == null) {
            return;
        }
        player.getData(ModAttachments.PLAYER_DIARY.get())
                .recordBlocker(route.blockerType(), player.level().getGameTime());
    }

    private static Route routeFor(BlockEntity blockEntity) {
        if (blockEntity instanceof PsychotropeResonatorBlockEntity resonator) {
            Route reasonRoute = DiaryBlockerRoutes.fromSourceKey(resonator.getLastFailureReason().translationKey());
            if (reasonRoute != null) {
                return reasonRoute;
            }
        }
        if (blockEntity instanceof MachineStatusProvider provider) {
            return DiaryBlockerRoutes.fromSourceKey(provider.getMachineStatus().translationKey());
        }
        return null;
    }
}
