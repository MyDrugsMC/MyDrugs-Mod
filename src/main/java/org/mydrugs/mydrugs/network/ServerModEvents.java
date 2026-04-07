package org.mydrugs.mydrugs.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.BottleFluidHandler;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ServerModEvents {
    private ServerModEvents() {
    }

    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, access) -> new BottleFluidHandler(access),
                ModItems.GLASS_BOTTLE.get()
        );
    }
}