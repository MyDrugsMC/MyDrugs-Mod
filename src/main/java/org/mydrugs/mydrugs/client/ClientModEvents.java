package org.mydrugs.mydrugs.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.client.ber.GrindingBowlRenderer;
import org.mydrugs.mydrugs.client.ber.StompCrafterRenderer;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.menu.client.SingleSlotMenuScreen;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BANG_CONTAINER.get(), SingleSlotMenuScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GRINDING_BOWL.get(), ctx -> new GrindingBowlRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.STOMP_CRAFTER.get(), StompCrafterRenderer::new);
    }
}
