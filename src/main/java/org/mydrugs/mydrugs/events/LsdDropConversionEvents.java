package org.mydrugs.mydrugs.events;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;

@EventBusSubscriber(modid = MyDrugs.MODID)
public final class LsdDropConversionEvents {
    private static final int LSD_PER_DROP_MB = 5;

    private LsdDropConversionEvents() {
    }

    @SubscribeEvent
    public static void onItemEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        if (!(event.getTarget() instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack cardboard = itemEntity.getItem();
        if (!cardboard.is(ModItems.CUPBOARD_PIECE.get())) {
            return;
        }

        Player player = event.getEntity();
        ItemStack bottle = player.getItemInHand(event.getHand());
        if (!(bottle.getItem() instanceof GlassBottleItem)) {
            return;
        }
        if (!ModFluids.rl("lsd").equals(GlassBottleItem.getStoredFluidId(bottle))) {
            return;
        }

        int possibleDrops = Math.min(cardboard.getCount(), GlassBottleItem.getStoredAmount(bottle) / LSD_PER_DROP_MB);
        if (possibleDrops <= 0) {
            return;
        }

        if (!player.getAbilities().instabuild) {
            GlassBottleItem.drain(bottle, ModFluids.rl("lsd"), possibleDrops * LSD_PER_DROP_MB);
        }

        cardboard.shrink(possibleDrops);
        if (cardboard.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(cardboard);
        }

        int remaining = possibleDrops;
        while (remaining > 0) {
            int count = Math.min(remaining, ModItems.LSD_DROP.get().getDefaultMaxStackSize());
            Containers.dropItemStack(
                    event.getLevel(),
                    itemEntity.getX(),
                    itemEntity.getY(),
                    itemEntity.getZ(),
                    new ItemStack(ModItems.LSD_DROP.get(), count)
            );
            remaining -= count;
        }

        event.getLevel().playSound(null, itemEntity.blockPosition(), SoundEvents.BOTTLE_EMPTY, SoundSource.PLAYERS, 0.55F, 1.35F);
        event.setCancellationResult(InteractionResult.SUCCESS);
        event.setCanceled(true);
    }
}
