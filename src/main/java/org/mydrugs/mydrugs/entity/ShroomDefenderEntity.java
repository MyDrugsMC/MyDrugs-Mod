package org.mydrugs.mydrugs.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.mydrugs.mydrugs.items.ModItems;

public class ShroomDefenderEntity extends Zombie {
    private static final float DREAMCAP_DROP_CHANCE = 0.05F;

    public ShroomDefenderEntity(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 18.0D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.23D)
                .add(Attributes.FOLLOW_RANGE, 28.0D);
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        // Guaranteed 1 Calming Spores
        this.spawnAtLocation(level, new ItemStack(ModItems.CALMING_SPORES.get(), 1));
        // 5% Dreamcap Spores
        if (level.getRandom().nextFloat() < DREAMCAP_DROP_CHANCE) {
            this.spawnAtLocation(level, new ItemStack(ModItems.DREAMCAP_SPORES.get(), 1));
        }
    }
}
