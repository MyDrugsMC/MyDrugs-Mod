package org.mydrugs.mydrugs.addiction.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.items.bottle.LightningBottleManager;
import org.mydrugs.mydrugs.mutation.MutationManager;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class PlayerTickEvents {
    private PlayerTickEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Profiler.get().push("mydrugs:runtime");
        try {
            Profiler.get().push("addiction");
            AddictionManager.tickPlayer(player);
            Profiler.get().popPush("drug_effect_runtime");
            DrugEffectRuntimeManager.tickServer(player);
            Profiler.get().popPush("lightning_bottle");
            LightningBottleManager.tick(player);
            Profiler.get().popPush("mutation");
            MutationManager.tickPlayer(player);
        } finally {
            Profiler.get().pop();
            Profiler.get().pop();
        }
    }
}
