package org.mydrugs.mydrugs.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.items.ModItems;

public class StonedMooshroomEntity extends MushroomCow {
    private static final float VOICE_PITCH = 0.65F;

    public StonedMooshroomEntity(EntityType<? extends MushroomCow> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Animal.createAnimalAttributes()
                .add(Attributes.MAX_HEALTH, 10.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D);
    }

    public void setStonedVariant(MushroomCow.Variant variant) {
        this.applyImplicitComponent(DataComponents.MOOSHROOM_VARIANT, variant);
    }

    @Override
    public float getVoicePitch() {
        return super.getVoicePitch() * VOICE_PITCH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.COW_STEP, 0.15F, VOICE_PITCH);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return stack.is(ModItems.CANNABIS_LEAF.get()) || super.isFood(stack);
    }

    @Nullable
    @Override
    public MushroomCow getBreedOffspring(ServerLevel level, AgeableMob partner) {
        StonedMooshroomEntity calf = ModEntities.STONED_MOOSHROOM.get().create(level, EntitySpawnReason.BREEDING);
        if (calf != null) {
            if (partner instanceof MushroomCow mooshroom && this.random.nextBoolean()) {
                calf.setStonedVariant(mooshroom.getVariant());
            } else {
                calf.setStonedVariant(this.getVariant());
            }
        }
        return calf;
    }
}
