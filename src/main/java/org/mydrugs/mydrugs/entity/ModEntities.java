package org.mydrugs.mydrugs.entity;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.mydrugs.mydrugs.MyDrugs;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, MyDrugs.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<InnerDemonEntity>> INNER_DEMON =
            ENTITY_TYPES.register("inner_demon", registryName -> {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "inner_demon");
                ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
                return EntityType.Builder.of(InnerDemonEntity::new, MobCategory.MONSTER)
                        .sized(0.4F, 0.8F)
                        .eyeHeight(0.55F)
                        .clientTrackingRange(8)
                        .updateInterval(3)
                        .build(key);
            });

    public static final DeferredHolder<EntityType<?>, EntityType<ShroomDefenderEntity>> SHROOM_DEFENDER =
            ENTITY_TYPES.register("shroom_defender", registryName -> {
                ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "shroom_defender");
                ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, id);
                return EntityType.Builder.of(ShroomDefenderEntity::new, MobCategory.MONSTER)
                        .sized(0.6F, 1.95F)
                        .eyeHeight(1.74F)
                        .clientTrackingRange(8)
                        .build(key);
            });

    private ModEntities() {
    }
}
