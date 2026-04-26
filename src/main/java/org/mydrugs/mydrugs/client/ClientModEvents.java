package org.mydrugs.mydrugs.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.client.ber.*;
import org.mydrugs.mydrugs.client.item.BottleFillProperty;
import org.mydrugs.mydrugs.client.item.LiquidColorTintSource;
import org.mydrugs.mydrugs.client.model.SpaceOverlayItemModel;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.menu.client.*;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BANG_CONTAINER.get(), SingleSlotMenuScreen::new);
        event.register(ModMenus.ADVANCED_FURNACE.get(), AdvancedFurnaceScreen::new);
        event.register(ModMenus.DISTILLER.get(), DistillerScreen::new);
        event.register(ModMenus.SIEVE.get(), SieveScreen::new);
        event.register(ModMenus.ROLLER.get(), RollerScreen::new);
        event.register(ModMenus.FLUID_FILTERER.get(), FluidFiltererScreen::new);
        event.register(ModMenus.CENTRIFUGE.get(), CentrifugeScreen::new);
        event.register(ModMenus.BTX_FRACTIONATION_TOWER.get(), BTXFractionationTowerScreen::new);
        event.register(ModMenus.ELECTROLYZER.get(), ElectrolyzerScreen::new);
        event.register(ModMenus.GROWTH_CHAMBER.get(), GrowthChamberScreen::new);
        event.register(ModMenus.BIOCHEMICAL_REACTOR.get(), BiochemicalReactorScreen::new);
        event.register(ModMenus.GASIFIER.get(), GasifierScreen::new);
        event.register(ModMenus.CHEMICAL_REACTOR.get(), ChemicalReactorScreen::new);
        event.register(ModMenus.ADVANCED_MIXING_VAT.get(), AdvancedMixingVatScreen::new);
        event.register(ModMenus.CATALYTIC_REFORMER.get(), CatalyticReformerScreen::new);
        event.register(ModMenus.AROMATIC_EXTRACTOR.get(), AromaticExtractorScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.GRINDING_BOWL.get(), ctx -> new GrindingBowlRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.STOMP_CRAFTER.get(), StompCrafterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXING_VAT.get(), MixingVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIEVE.get(), SieveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EVAPORATION_TRAY.get(), EvaporationTrayRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CLAY_VAT.get(), ClayVatRenderer::new);
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
}
