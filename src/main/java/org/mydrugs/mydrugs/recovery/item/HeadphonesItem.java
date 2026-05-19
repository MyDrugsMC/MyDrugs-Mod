package org.mydrugs.mydrugs.recovery.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.addiction.manager.ItemEffectHandler;
import org.mydrugs.mydrugs.network.OpenHeadphonesMusicScreenPayload;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class HeadphonesItem extends Item {
    private static final long DOUBLE_CLICK_TICKS = 5L;
    private static final Map<UUID, PendingClick> PENDING_CLICKS = new HashMap<>();

    public HeadphonesItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            if (player.isShiftKeyDown()) {
                PENDING_CLICKS.remove(player.getUUID());
                PacketDistributor.sendToPlayer(serverPlayer, new OpenHeadphonesMusicScreenPayload());
            } else {
                long now = level.getGameTime();
                PendingClick pending = PENDING_CLICKS.get(player.getUUID());
                if (pending != null && now <= pending.deadlineTick()) {
                    ItemEffectHandler.cycleHeadphonesTrack(serverPlayer);
                    PENDING_CLICKS.remove(player.getUUID());
                    serverPlayer.displayClientMessage(Component.translatable("message.mydrugs.headphones.track_changed"), true);
                } else {
                    if (pending != null) {
                        applyPendingSingleClick(serverPlayer);
                    }
                    PENDING_CLICKS.put(player.getUUID(), new PendingClick(now + DOUBLE_CLICK_TICKS));
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    public static void tickPendingClick(ServerPlayer player) {
        PendingClick pending = PENDING_CLICKS.get(player.getUUID());
        if (pending == null || player.level().getGameTime() < pending.deadlineTick()) {
            return;
        }
        applyPendingSingleClick(player);
    }

    private static void applyPendingSingleClick(ServerPlayer player) {
        PENDING_CLICKS.remove(player.getUUID());
        boolean enabled = ItemEffectHandler.toggleHeadphones(player);
        player.displayClientMessage(Component.translatable(
                enabled ? "message.mydrugs.headphones.enabled" : "message.mydrugs.headphones.disabled"
        ), true);
    }

    private record PendingClick(long deadlineTick) {
    }
}
