package org.mydrugs.mydrugs.damage;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public final class ModDamageTypes {
    public static final ResourceKey<DamageType> BLOOD_DRAW = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("mydrugs", "blood_draw")
    );

    public static final ResourceKey<DamageType> STRESS_OVERLOAD = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("mydrugs", "stress_overload")
    );

    public static final ResourceKey<DamageType> OVERDOSE = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("mydrugs", "overdose")
    );

    public static final ResourceKey<DamageType> MUTATION_INFECTION = ResourceKey.create(
            Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath("mydrugs", "mutation_infection")
    );

    private ModDamageTypes() {
    }

    public static DamageSource bloodDraw(net.minecraft.world.entity.Entity causer) {
        return new DamageSource(
                causer.level().registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(BLOOD_DRAW),
                causer
        );
    }

    public static DamageSource stressOverload(Level level) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(STRESS_OVERLOAD)
        );
    }

    public static DamageSource overdose(Level level) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(OVERDOSE)
        );
    }

    public static DamageSource mutationInfection(Level level) {
        return new DamageSource(
                level.registryAccess()
                        .lookupOrThrow(Registries.DAMAGE_TYPE)
                        .getOrThrow(MUTATION_INFECTION)
        );
    }
}
