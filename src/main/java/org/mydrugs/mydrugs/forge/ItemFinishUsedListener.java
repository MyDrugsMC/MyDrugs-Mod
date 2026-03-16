package org.mydrugs.mydrugs.forge;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.DrugService;
import org.mydrugs.mydrugs.forge.items.DrugItem;
import org.mydrugs.mydrugs.forge.effects.EffectAdapter;

@EventBusSubscriber(modid = MyDrugs.MODID)
public class ItemFinishUsedListener {
    @SubscribeEvent
    public static void onItemFinishUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getItem().getItem() instanceof DrugItem drugItem)) return;
        new DrugService(new EffectAdapter()).consume(drugItem.model());
    }
}
