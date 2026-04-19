package org.mydrugs.mydrugs.compat;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.recipes.ModRecipeTypes;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModRecipeSync {
    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        for (var holder : ModRecipeTypes.RECIPE_TYPES.getEntries()) {
            var recipeType = holder.get();
            event.sendRecipes(recipeType);
        }
    }
}