package org.mydrugs.mydrugs.entity;

import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

public final class ModEntityAttributes {
    private ModEntityAttributes() {
    }

    public static void register(EntityAttributeCreationEvent event) {
        event.put(ModEntities.INNER_DEMON.get(), InnerDemonEntity.createAttributes().build());
    }
}
