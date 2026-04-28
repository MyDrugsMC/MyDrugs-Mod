package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.pipe.filter.PipeFilterMenu;
import org.mydrugs.mydrugs.pipe.machine.MachineTransferConfigMenu;

import java.util.function.Supplier;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, MyDrugs.MODID);

    public static final Supplier<MenuType<SingleSlotMenu>> BANG_CONTAINER =
            MENUS.register("bang_container", () -> new MenuType<>(SingleSlotMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<AdvancedFurnaceMenu>> ADVANCED_FURNACE =
            MENUS.register("advanced_furnace", () -> new MenuType<>(AdvancedFurnaceMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<DistillerMenu>> DISTILLER =
            MENUS.register("distiller", () -> new MenuType<>(DistillerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<SieveMenu>> SIEVE =
            MENUS.register("sieve", () -> new MenuType<>(SieveMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<RollerMenu>> ROLLER =
            MENUS.register("roller", () -> new MenuType<>(RollerMenu::new, FeatureFlags.DEFAULT_FLAGS));


    public static final Supplier<MenuType<FluidFiltererMenu>> FLUID_FILTERER = MENUS.register(
            "fluid_filterer",
            () -> new MenuType<>(FluidFiltererMenu::new, FeatureFlags.DEFAULT_FLAGS)
    );

    public static final Supplier<MenuType<CentrifugeMenu>> CENTRIFUGE =
            MENUS.register("centrifuge", () -> new MenuType<>(CentrifugeMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<BTXFractionationTowerMenu>> BTX_FRACTIONATION_TOWER =
            MENUS.register("btx_fractionation_tower",
                    () -> new MenuType<>(BTXFractionationTowerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<AromaticExtractorMenu>> AROMATIC_EXTRACTOR =
            MENUS.register("aromatic_extractor",
                    () -> new MenuType<>(AromaticExtractorMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<ElectrolyzerMenu>> ELECTROLYZER =
            MENUS.register("electrolyzer", () -> new MenuType<>(ElectrolyzerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<GrowthChamberMenu>> GROWTH_CHAMBER =
            MENUS.register("growth_chamber",
                    () -> new MenuType<>(GrowthChamberMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<BiochemicalReactorMenu>> BIOCHEMICAL_REACTOR =
            MENUS.register(
                    "biochemical_reactor",
                    () -> new MenuType<>(BiochemicalReactorMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static final Supplier<MenuType<GasifierMenu>> GASIFIER =
            MENUS.register(
                    "gasifier",
                    () -> new MenuType<>(GasifierMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static final Supplier<MenuType<ChemicalReactorMenu>> CHEMICAL_REACTOR =
            MENUS.register("chemical_reactor",
                    () -> IMenuTypeExtension.create(ChemicalReactorMenu::new));

    public static final Supplier<MenuType<AdvancedMixingVatMenu>> ADVANCED_MIXING_VAT =
            MENUS.register(
                    "advanced_mixing_vat",
                    () -> new MenuType<>(AdvancedMixingVatMenu::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static final Supplier<MenuType<CatalyticReformerMenu>> CATALYTIC_REFORMER =
            MENUS.register("catalytic_reformer",
                    () -> new MenuType<>(CatalyticReformerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<SteamCrackerMenu>> STEAM_CRACKER =
            MENUS.register("steam_cracker",
                    () -> new MenuType<>(SteamCrackerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<PipeFilterMenu>> PIPE_FILTER =
            MENUS.register("pipe_filter", () -> new MenuType<>(PipeFilterMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static final Supplier<MenuType<MachineTransferConfigMenu>> MACHINE_TRANSFER_CONFIG =
            MENUS.register(
                    "machine_transfer_config",
                    () -> IMenuTypeExtension.create(MachineTransferConfigMenu::new)
            );
}
