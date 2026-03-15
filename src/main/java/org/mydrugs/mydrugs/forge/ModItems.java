package org.mydrugs.mydrugs.forge;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MyDrugs.MODID);


    public static final DeferredItem<Item> WEED_LEAF =
            ITEMS.registerItem("weed_leaf", props ->
                    new WeedLeafItem(props.food(new FoodProperties.Builder()
                            .nutrition(2)
                            .saturationModifier(0.3f)
                            .alwaysEdible()
                            .build()
                    ))
            );
}
