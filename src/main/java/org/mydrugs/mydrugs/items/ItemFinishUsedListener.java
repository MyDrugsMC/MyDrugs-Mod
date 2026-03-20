package org.mydrugs.mydrugs.items;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.core.drug.DrugHolder;
import org.mydrugs.mydrugs.core.drug.strategy.EatingStrategy;

@EventBusSubscriber(modid = MyDrugs.MODID)
public class ItemFinishUsedListener {
    @SubscribeEvent
    public static void onItemFinishUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getItem().getItem() instanceof DrugHolder drugItem)) return;
        MyDrugs.DRUG_SERVICE.consume(drugItem.getDrugModel(), new EatingStrategy());
    }
}
