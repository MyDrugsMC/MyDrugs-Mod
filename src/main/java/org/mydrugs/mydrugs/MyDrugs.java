package org.mydrugs.mydrugs;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.advancement.ModCriteriaTriggers;
import org.mydrugs.mydrugs.blocks.ModBlockEntities;
import org.mydrugs.mydrugs.blocks.ModBlocks;
import org.mydrugs.mydrugs.blocks.crops.ModCrops;
import org.mydrugs.mydrugs.core.drug.DrugRegistry;
import org.mydrugs.mydrugs.core.drug.ritual.RitualDrugRegistry;
import org.mydrugs.mydrugs.core.drug.use.DrugUseService;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.menu.ModMenus;
import org.mydrugs.mydrugs.recipes.ModRecipeSerializers;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;
import org.mydrugs.mydrugs.items.data.ModDataComponents;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.entity.ModEntityAttributes;
import org.mydrugs.mydrugs.entity.ModEntities;
import org.mydrugs.mydrugs.sounds.ModSounds;
import org.mydrugs.mydrugs.worldgen.biomes.ModRegions;
import org.mydrugs.mydrugs.worldgen.biomes.ModSurfaceRules;
import org.slf4j.Logger;
import terrablender.api.Regions;
import terrablender.api.SurfaceRuleManager;

import java.util.function.Supplier;

@Mod(MyDrugs.MODID)
public class MyDrugs {
    public static final String MODID = "mydrugs";
    public static final String NETWORK_VERSION = "1";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final DrugUseService DRUG_USE_SERVICE = new DrugUseService();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MyDrugs.MODID);

    public static final Supplier<CreativeModeTab> MYDRUGS_TAB = CREATIVE_MODE_TABS.register("main", () ->
            CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.mydrugs.main"))
                    .icon(() -> new ItemStack(ModItems.TOBACCO_LEAF.get()))
                    .displayItems((params, output) -> {
                        for (var holder : ModItems.ITEMS.getEntries()) {
                            output.accept(holder.get());
                        }
                        for (var holder : ModBlocks.ITEMS.getEntries()) {
                            output.accept(holder.get());
                        }
                        for (var holder : ModCrops.ITEMS.getEntries()) {
                            output.accept(holder.get());
                        }
                    })
                    .build()
    );

    public MyDrugs(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(ModEntityAttributes::register);
        NeoForge.EVENT_BUS.register(this);
        ModBlocks.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModRecipeSerializers.RECIPE_SERIALIZERS.register(modEventBus);
        ModRecipeTypes.RECIPE_TYPES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModFluids.FLUID_BLOCKS.register(modEventBus);
        ModFluids.FLUID_ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModCriteriaTriggers.register(modEventBus);
        ModAttachments.register(modEventBus);
        ModCrops.register(modEventBus);
        DrugRegistry.registerDrugs();
        RitualDrugRegistry.registerDefaults();

        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        modContainer.registerConfig(ModConfig.Type.SERVER, Config.SERVER_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
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
    }

    public static Logger getLOGGER() {
        return LOGGER;
    }
}
