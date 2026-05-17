package org.mydrugs.mydrugs.client;

import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.client.ber.*;
import org.mydrugs.mydrugs.client.entity.InnerDemonRenderer;
import org.mydrugs.mydrugs.client.entity.StonedCowRenderer;
import org.mydrugs.mydrugs.client.entity.StonedMooshroomRenderer;
import org.mydrugs.mydrugs.client.item.BottleFillProperty;
import org.mydrugs.mydrugs.client.item.LiquidColorTintSource;
import org.mydrugs.mydrugs.client.model.SpaceOverlayItemModel;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.entity.ModEntities;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.menu.client.*;
import org.mydrugs.mydrugs.pipe.client.MachineTransferConfigScreen;
import org.mydrugs.mydrugs.pipe.client.PipeBlockEntityRenderer;
import org.mydrugs.mydrugs.pipe.client.PipeFilterScreen;

@EventBusSubscriber(modid = MyDrugs.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ShaderManager.INSTANCE.registerShaders();
    }

    @SubscribeEvent
    public static void registerRenderPipelines(RegisterRenderPipelinesEvent event) {
        ShaderManager.INSTANCE.registerPipelines(event);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.BANG_CONTAINER.get(), SingleSlotMenuScreen::new);
        event.register(ModMenus.ADVANCED_FURNACE.get(), AdvancedFurnaceScreen::new);
        event.register(ModMenus.DISTILLER.get(), DistillerScreen::new);
        event.register(ModMenus.SIEVE.get(), SieveScreen::new);
        event.register(ModMenus.ROLLER.get(), RollerScreen::new);
        event.register(ModMenus.MANUAL_COFFEE_PULPER.get(), ManualCoffeePulperScreen::new);
        event.register(ModMenus.FLUID_FILTERER.get(), FluidFiltererScreen::new);
        event.register(ModMenus.CENTRIFUGE.get(), CentrifugeScreen::new);
        event.register(ModMenus.BTX_FRACTIONATION_TOWER.get(), BTXFractionationTowerScreen::new);
        event.register(ModMenus.ELECTROLYZER.get(), ElectrolyzerScreen::new);
        event.register(ModMenus.GROWTH_CHAMBER.get(), GrowthChamberScreen::new);
        event.register(ModMenus.GENE_EXTRACTOR.get(), GeneExtractorScreen::new);
        event.register(ModMenus.CRISPR_CAS9_COMBINATOR.get(), KrisprKas9CombinatorScreen::new);
        event.register(ModMenus.BACTERIAL_INCUBATOR.get(), BacterialIncubatorScreen::new);
        event.register(ModMenus.HEMOGENIC_INFUSER.get(), HemogenicInfuserScreen::new);
        event.register(ModMenus.AUTOCLAVE.get(), AutoclaveScreen::new);
        event.register(ModMenus.BIOCHEMICAL_REACTOR.get(), BiochemicalReactorScreen::new);
        event.register(ModMenus.GASIFIER.get(), GasifierScreen::new);
        event.register(ModMenus.CHEMICAL_REACTOR.get(), ChemicalReactorScreen::new);
        event.register(ModMenus.ADVANCED_MIXING_VAT.get(), AdvancedMixingVatScreen::new);
        event.register(ModMenus.CATALYTIC_REFORMER.get(), CatalyticReformerScreen::new);
        event.register(ModMenus.STEAM_CRACKER.get(), SteamCrackerScreen::new);
        event.register(ModMenus.PSYCHOTROPE_GENERATOR.get(), PsychotropeGeneratorScreen::new);
        event.register(ModMenus.PSY_MIXER.get(), PsyMixerScreen::new);
        event.register(ModMenus.AROMATIC_EXTRACTOR.get(), AromaticExtractorScreen::new);
        event.register(ModMenus.PIPE_FILTER.get(), PipeFilterScreen::new);
        event.register(ModMenus.MACHINE_TRANSFER_CONFIG.get(), MachineTransferConfigScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.INNER_DEMON.get(), InnerDemonRenderer::new);
        event.registerEntityRenderer(ModEntities.SHROOM_DEFENDER.get(), org.mydrugs.mydrugs.client.entity.ShroomDefenderRenderer::new);
        event.registerEntityRenderer(ModEntities.STONED_COW.get(), StonedCowRenderer::new);
        event.registerEntityRenderer(ModEntities.STONED_MOOSHROOM.get(), StonedMooshroomRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GRINDING_BOWL.get(), ctx -> new GrindingBowlRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.STOMP_CRAFTER.get(), StompCrafterRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.MIXING_VAT.get(), MixingVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SIEVE.get(), SieveRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EVAPORATION_TRAY.get(), EvaporationTrayRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DRYING_RACK.get(), DryingRackRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.COFFEE_DRYING_MAT.get(), CoffeeDryingMatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CLAY_VAT.get(), ClayVatRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PSY_ANVIL.get(), PsyAnvilRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FORMED_PSY_MIXER_CORE.get(), PsyMixerRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_PUMP.get(), FluidPumpRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PIPES.get(), PipeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAS_TANK.get(), GasTankRenderer::new);
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
