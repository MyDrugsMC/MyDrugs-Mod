package org.mydrugs.mydrugs;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.BottleFluidHandler;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public class ServerModEvents {
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerItem(
                Capabilities.Fluid.ITEM,
                (stack, itemAccess) -> new BottleFluidHandler(stack),
                ModItems.GLASS_BOTTLE.get()
        );
    }
}
