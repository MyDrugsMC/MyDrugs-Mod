package org.mydrugs.mydrugs.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.entity.ModEntities;
import org.mydrugs.mydrugs.entity.StonedCowEntity;
import org.mydrugs.mydrugs.entity.StonedMooshroomEntity;
import org.mydrugs.mydrugs.items.ModItems;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class StonedCowInteractionEvents {
    private StonedCowInteractionEvents() {
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = player.getItemInHand(event.getHand());
        if (!stack.is(ModItems.CANNABIS_LEAF.get())) return;

        if (event.getTarget() instanceof MushroomCow mooshroom && !(mooshroom instanceof StonedMooshroomEntity)) {
            convertMooshroom(mooshroom, player, stack);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
            return;
        }

        if (event.getTarget() instanceof Cow cow && !(cow instanceof StonedCowEntity)) {
            convertCow(cow, player, stack);
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    private static void convertCow(Cow cow, ServerPlayer player, ItemStack stack) {
        StonedCowEntity converted = cow.convertTo(ModEntities.STONED_COW.get(), ConversionParams.single(cow, false, false),
                stonedCow -> stonedCow.setVariant(cow.getVariant()));
        if (converted != null) {
            setInLoveIfPossible(converted, player);
            consumeLeaf(player, stack);
        }
    }

    private static void convertMooshroom(MushroomCow mooshroom, ServerPlayer player, ItemStack stack) {
        StonedMooshroomEntity converted = mooshroom.convertTo(ModEntities.STONED_MOOSHROOM.get(), ConversionParams.single(mooshroom, false, false),
                stonedMooshroom -> stonedMooshroom.setStonedVariant(mooshroom.getVariant()));
        if (converted != null) {
            setInLoveIfPossible(converted, player);
            consumeLeaf(player, stack);
        }
    }

    private static void setInLoveIfPossible(Animal animal, ServerPlayer player) {
        if (animal.canFallInLove()) {
            animal.setInLove(player);
        }
    }

    private static void consumeLeaf(ServerPlayer player, ItemStack stack) {
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
