package org.mydrugs.mydrugs.diary;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.manager.ItemEffectHandler;
import org.mydrugs.mydrugs.recovery.item.AbstractRecoveryItem;
import org.mydrugs.mydrugs.sounds.ModSounds;

public final class PersonalDiaryItem extends AbstractRecoveryItem {

    public PersonalDiaryItem(Properties properties) {
        super(properties.stacksTo(1), 40, PlayerDiaryAttachment.WRITE_COOLDOWN_TICKS, ItemUseAnimation.BOW, false);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        // Shift + right-click: open the diary screen for reading; never consume cooldown.
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer sp) {
                PacketDistributor.sendToPlayer(sp, DiarySnapshotBuilder.build(sp));
                playWriteSound(sp, 0.70F);
            }
            return InteractionResult.SUCCESS;
        }
        // Otherwise fall through to the hold-to-use behavior (auto write on finish).
        return super.use(level, player, hand);
    }

    @Override
    protected void applyEffects(ServerPlayer player) {
        // 1. Auto-generate today's entry from the player state.
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        long gameTime = player.level().getGameTime();
        if (diary.canWrite(gameTime)) {
            DiaryEntry generated = DiaryEntryGenerator.generate(player);
            diary.append(generated);
            diary.markWritten(gameTime);
        }
        // 2. Apply the existing diary calming/recovery effects.
        ItemEffectHandler.applyDiary(player);
    }

    @Override
    protected void afterUse(ServerPlayer player) {
        playWriteSound(player, 0.80F);
        // No more random end-message; the diary entry itself is the feedback.
    }

    private static void playWriteSound(ServerPlayer player, float volume) {
        player.level().playSound(
                null,
                player.getX(),
                player.getY(),
                player.getZ(),
                ModSounds.WRITE.get(),
                SoundSource.PLAYERS,
                volume,
                0.95F + player.getRandom().nextFloat() * 0.10F
        );
    }
}
