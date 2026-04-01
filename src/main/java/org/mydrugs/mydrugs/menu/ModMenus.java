package org.mydrugs.mydrugs.menu;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

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
}
