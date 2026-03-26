package org.mydrugs.mydrugs.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.client.ber.GrindingBowlRenderer;
import org.mydrugs.mydrugs.client.ber.MixingVatBlockEntityRenderer;
import org.mydrugs.mydrugs.client.ber.StompCrafterRenderer;
import org.mydrugs.mydrugs.client.item.BottleFillProperty;
import org.mydrugs.mydrugs.client.item.LiquidColorTintSource;
import org.mydrugs.mydrugs.client.model.SpaceOverlayItemModel;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.menu.client.AdvancedFurnaceScreen;
import org.mydrugs.mydrugs.menu.client.DistillerScreen;
import org.mydrugs.mydrugs.menu.client.SingleSlotMenuScreen;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BANG_CONTAINER.get(), SingleSlotMenuScreen::new);
        event.register(ModMenus.ADVANCED_FURNACE.get(), AdvancedFurnaceScreen::new);
        event.register(ModMenus.DISTILLER.get(), DistillerScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GRINDING_BOWL.get(), ctx -> new GrindingBowlRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.STOMP_CRAFTER.get(), StompCrafterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXING_VAT.get(), MixingVatBlockEntityRenderer::new);
    }

    @SubscribeEvent
    public static void registerItemModels(RegisterItemModelsEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "space_overlay"),
                SpaceOverlayItemModel.Unbaked.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "bottle_fill"),
                BottleFillProperty.MAP_CODEC
        );
    }

    @SubscribeEvent
    public static void registerItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "liquid_color"),
                LiquidColorTintSource.MAP_CODEC
        );
    }

    private ClientModEvents() {}
}
