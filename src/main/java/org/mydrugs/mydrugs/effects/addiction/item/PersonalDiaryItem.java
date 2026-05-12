package org.mydrugs.mydrugs.effects.addiction.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.diary.DiaryEntry;
import org.mydrugs.mydrugs.effects.addiction.diary.DiaryEntryGenerator;
import org.mydrugs.mydrugs.effects.addiction.diary.DiarySnapshotBuilder;
import org.mydrugs.mydrugs.effects.addiction.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.effects.addiction.manager.ItemEffectHandler;
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
        player.playSound(ModSounds.WRITE.get());
        // No more random end-message; the diary entry itself is the feedback.
    }
}
