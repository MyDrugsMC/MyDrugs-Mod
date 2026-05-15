package org.mydrugs.mydrugs.recipes;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.mydrugs.mydrugs.MyDrugs;

/**
 * Server-side recipe sync. Pushes every registered recipe type to clients when
 * a datapack reload fires, so that client-side helpers (e.g. JEI hooks,
 * {@link org.mydrugs.mydrugs.client.compat.ClientRecipesCache}) see fresh data
 * after reload.
 *
 * Previously located under {@code client/compat/} which was misleading — this
 * runs on the dedicated server too and never touches client classes.
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class ModRecipeSync {
    private ModRecipeSync() {
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        for (var holder : ModRecipeTypes.RECIPE_TYPES.getEntries()) {
            var recipeType = holder.get();
            event.sendRecipes(recipeType);
        }
    }
}
