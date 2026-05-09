package org.mydrugs.mydrugs.events;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.mydrugs.mydrugs.fluids.ModFluids;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.bottle.GlassBottleItem;

import java.util.Optional;

public final class LsdDropConversion {
    private static final int LSD_PER_DROP_MB = 5;

    private LsdDropConversion() {
    }

    public static InteractionResult tryConvert(Level level, Player player, InteractionHand hand) {
        ItemStack bottle = player.getItemInHand(hand);

        if (!(bottle.getItem() instanceof GlassBottleItem)) {
            return InteractionResult.PASS;
        }

        if (!ModFluids.rl("lsd").equals(GlassBottleItem.getStoredFluidId(bottle))) {
            return InteractionResult.PASS;
        }

        ItemEntity cardboardEntity = findLookedAtCardboard(level, player);
        if (cardboardEntity == null) {
            return InteractionResult.PASS;
        }

        ItemStack cardboard = cardboardEntity.getItem();

        int possibleDrops = Math.min(
                cardboard.getCount(),
                GlassBottleItem.getStoredAmount(bottle) / LSD_PER_DROP_MB
        );

        if (possibleDrops <= 0) {
            return InteractionResult.PASS;
        }

        // Client: just tell Minecraft this item use is valid so the hand animation feels right.
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!player.getAbilities().instabuild) {
            GlassBottleItem.drain(
                    bottle,
                    ModFluids.rl("lsd"),
                    possibleDrops * LSD_PER_DROP_MB
            );
        }

        double x = cardboardEntity.getX();
        double y = cardboardEntity.getY();
        double z = cardboardEntity.getZ();

        cardboard.shrink(possibleDrops);

        if (cardboard.isEmpty()) {
            cardboardEntity.discard();
        } else {
            cardboardEntity.setItem(cardboard);
        }

        int remaining = possibleDrops;
        while (remaining > 0) {
            int count = Math.min(
                    remaining,
                    ModItems.LSD_DROP.get().getDefaultMaxStackSize()
            );

            Containers.dropItemStack(
                    level,
                    x,
                    y,
                    z,
                    new ItemStack(ModItems.LSD_DROP.get(), count)
            );

            remaining -= count;
        }

        level.playSound(
                null,
                x,
                y,
                z,
                SoundEvents.BOTTLE_EMPTY,
                SoundSource.PLAYERS,
                0.55F,
                1.35F
        );

        return InteractionResult.SUCCESS_SERVER;
    }

    private static ItemEntity findLookedAtCardboard(Level level, Player player) {
        double reach = player.getAttributeValue(Attributes.ENTITY_INTERACTION_RANGE);

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(reach));

        // Prevent converting through walls.
        BlockHitResult blockHit = level.clip(new ClipContext(
                eye,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));

        double maxDistanceSqr = reach * reach;

        if (blockHit.getType() != HitResult.Type.MISS) {
            maxDistanceSqr = eye.distanceToSqr(blockHit.getLocation());
        }

        AABB searchBox = player.getBoundingBox()
                .expandTowards(look.scale(reach))
                .inflate(1.0D);

        ItemEntity closest = null;
        double closestDistanceSqr = maxDistanceSqr;

        for (ItemEntity itemEntity : level.getEntitiesOfClass(ItemEntity.class, searchBox,
                entity -> entity.isAlive()
                        && entity.getItem().is(ModItems.CUPBOARD_PIECE.get()))) {

            // Dropped items have tiny hitboxes, so inflate a bit to make the click forgiving.
            AABB hitbox = itemEntity.getBoundingBox().inflate(0.35D);
            Optional<Vec3> hit = hitbox.clip(eye, end);

            if (hit.isEmpty()) {
                continue;
            }

            double distanceSqr = eye.distanceToSqr(hit.get());

            if (distanceSqr < closestDistanceSqr) {
                closestDistanceSqr = distanceSqr;
                closest = itemEntity;
            }
        }

        return closest;
    }
}