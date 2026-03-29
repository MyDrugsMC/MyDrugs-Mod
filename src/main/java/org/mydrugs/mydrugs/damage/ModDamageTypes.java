package org.mydrugs.mydrugs.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> BLOOD_DRAW = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("mydrugs", "blood_draw")
    );

    private ModDamageTypes() {
    }

    public static DamageSource bloodDraw(Entity causer) {
        return new DamageSource(
                causer.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(BLOOD_DRAW),
                causer
        );
    }
}