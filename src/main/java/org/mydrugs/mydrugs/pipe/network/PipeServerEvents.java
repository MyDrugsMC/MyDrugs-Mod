package org.mydrugs.mydrugs.pipe.network;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.Profiler;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.mydrugs.mydrugs.MyDrugs;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class PipeServerEvents {
    private PipeServerEvents() {
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            Profiler.get().push("mydrugs:world_tick");
            try {
                Profiler.get().push("pipe_networks");
                PipeNetworkManager.get(serverLevel).tick();
            } finally {
                Profiler.get().pop();
                Profiler.get().pop();
            }
        }
    }
}
