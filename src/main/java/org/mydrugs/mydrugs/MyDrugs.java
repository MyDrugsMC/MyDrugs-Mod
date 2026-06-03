package org.mydrugs.mydrugs;

import com.mojang.logging.LogUtils;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.mydrugs.mydrugs.advancement.ModCriteriaTriggers;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.ModBlockTypes;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.crops.ModCrops;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugRegistry;
import org.mydrugs.mydrugs.core.drug.use.DrugUseService;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.recipes.ModRecipeDisplays;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.entity.ModEntityAttributes;
import org.mydrugs.mydrugs.entity.ModEntities;
import org.mydrugs.mydrugs.dimension.inner.worldgen.ModInnerWorldgen;
import org.mydrugs.mydrugs.sounds.ModSounds;
import org.mydrugs.mydrugs.worldgen.ModBiomeModifierSerializers;
import org.mydrugs.mydrugs.worldgen.ModPoiTypes;
import org.mydrugs.mydrugs.worldgen.ModVillagerProfessions;
import org.mydrugs.mydrugs.worldgen.WorldgenConfig;
import org.mydrugs.mydrugs.worldgen.biomes.ModRegions;
import org.mydrugs.mydrugs.worldgen.biomes.ModSurfaceRules;
import org.mydrugs.mydrugs.items.ModCreativeTabs;
import org.slf4j.Logger;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@Mod(MyDrugs.MODID)
public class MyDrugs {
    public static final String MODID = "mydrugs";
    public static final String NETWORK_VERSION = "1";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DrugUseService DRUG_USE_SERVICE = new DrugUseService();

    public MyDrugs(IEventBus modEventBus, ModContainer modContainer) {
        NeoForgeMod.enableMilkFluid();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModEntityAttributes::register);
        NeoForge.EVENT_BUS.register(this);
        ModBlocks.register(modEventBus);
        ModBlockTypes.BLOCK_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModRecipeDisplays.RECIPE_DISPLAYS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_BLOCKS.register(modEventBus);
        ModFluids.FLUID_ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModBiomeModifierSerializers.register(modEventBus);
        ModInnerWorldgen.register(modEventBus);
        ModPoiTypes.register(modEventBus);
        ModVillagerProfessions.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModCriteriaTriggers.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModCrops.register(modEventBus);
        DrugRegistry.registerDrugs();
        RitualDrugRegistry.registerDefaults();

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
        modContainer.registerConfig(ModConfig.Type.STARTUP, Config.WORLDGEN_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            for (String issue : org.mydrugs.mydrugs.recipes.ModRecipeContent.validateAll()) {
                LOGGER.warn("[recipe-content] {}", issue);
            }
            for (String issue : org.mydrugs.mydrugs.blocks.ModMachineContent.validateAll()) {
                LOGGER.warn("[machine-content] {}", issue);
            }
            WorldgenConfig.logPsychedelicBiomeConfigWarnings(LOGGER);
            if (WorldgenConfig.terraBlenderOverworldEnabled()) {
                Regions.register(new ModRegions(
                        ResourceLocation.fromNamespaceAndPath(MODID, "overworld"),
                        WorldgenConfig.psychedelicBiomeWeight()
                ));
            }
            if (WorldgenConfig.surfaceRulesEnabled()) {
                SurfaceRuleManager.addSurfaceRules(
                        SurfaceRuleManager.RuleCategory.OVERWORLD,
                        MODID,
                        ModSurfaceRules.makeRules()
                );
            }
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }

    public static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
