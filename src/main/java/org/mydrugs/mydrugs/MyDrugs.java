package org.mydrugs.mydrugs;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.client.shaders.ShaderManager;
import org.mydrugs.mydrugs.core.client.ClientState;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.DrugService;
import org.mydrugs.mydrugs.effects.EffectAdapter;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.bottle.BottleFluidHandler;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.registry.ModDataComponents;
import org.mydrugs.mydrugs.worldgen.biomes.ModRegions;
import org.mydrugs.mydrugs.worldgen.biomes.ModSurfaceRules;
import org.slf4j.Logger;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

@Mod(MyDrugs.MODID)
public class MyDrugs {
    public static final String MODID = "mydrugs";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DrugService DRUG_SERVICE = new DrugService(new EffectAdapter());
    public static final ClientState CLIENT_STATE = new ClientState();

    public MyDrugs(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        NeoForge.EVENT_BUS.register(this);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ShaderManager.INSTANCE.registerShaders();
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_BLOCKS.register(modEventBus);
        ModFluids.FLUID_ITEMS.register(modEventBus);
        DrugRegistry.registerDrugs();

        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        LOGGER.info("HELLO FROM COMMON SETUP");
        event.enqueueWork(() -> {
            Regions.register(new ModRegions(ResourceLocation.fromNamespaceAndPath(MODID, "overworld"), 2));
            SurfaceRuleManager.addSurfaceRules(
                    SurfaceRuleManager.RuleCategory.OVERWORLD,
                    MODID,
                    ModSurfaceRules.makeRules()
            );
        });
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            LOGGER.info("HELLO FROM CLIENT SETUP");
            LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }
}
