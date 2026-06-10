package org.mydrugs.mydrugs.addiction.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.diary.DiaryEntry;
import org.mydrugs.mydrugs.diary.DiaryEntryType;
import org.mydrugs.mydrugs.diary.DiarySnapshotBuilder;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.addiction.manager.ItemEffectHandler;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.network.PayloadRateLimiter;

/**
 * Client -> server: submit a custom diary entry typed by the player.
 *
 * Server validates:
 * - Player still holds or carries a Personal Diary somewhere
 * - Cooldown is satisfied
 * - Content sanitizes to a non-empty string under the length cap
 *
 * On success the entry is appended, diary calming effects are applied,
 * cooldown is set, advancement is triggered, and an updated snapshot is sent back.
 */
public record SubmitPersonalDiaryEntryPayload(String content) implements CustomPacketPayload {
    public static final Type<SubmitPersonalDiaryEntryPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(MyDrugs.MODID, "submit_personal_diary_entry"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SubmitPersonalDiaryEntryPayload> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.stringUtf8(4096), SubmitPersonalDiaryEntryPayload::content,
                    SubmitPersonalDiaryEntryPayload::new
            );

    public static void handleOnServer(SubmitPersonalDiaryEntryPayload payload, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;
        ctx.enqueueWork(() -> handle(player, payload.content()));
    }

    private static void handle(ServerPlayer player, String rawContent) {
        if (!PayloadRateLimiter.accept(player, PayloadRateLimiter.Kind.PERSONAL_DIARY_SUBMIT)) {
            sendResult(player, PersonalDiarySubmitResultPayload.Result.UNKNOWN_FAILURE,
                    "screen.mydrugs.diary.save_failed.unknown", 0);
            return;
        }
        // Authorize: must have a Personal Diary somewhere
        if (DiarySnapshotBuilder.findDiaryStack(player) == null
                && player.getMainHandItem().getItem() != ModItems.PERSONAL_DIARY.get()
                && player.getOffhandItem().getItem() != ModItems.PERSONAL_DIARY.get()) {
            sendResult(player, PersonalDiarySubmitResultPayload.Result.NO_DIARY,
                    "screen.mydrugs.diary.save_failed.no_diary", 0);
            return;
        }

        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        long gameTime = player.level().getGameTime();
        if (!diary.canWrite(gameTime)) {
            sendResult(player, PersonalDiarySubmitResultPayload.Result.COOLDOWN,
                    "screen.mydrugs.diary.save_failed.cooldown", diary.remainingCooldownTicks(gameTime));
            return;
        }

        if (rawContent != null && rawContent.length() > PlayerDiaryAttachment.CUSTOM_MAX_LENGTH) {
            sendResult(player, PersonalDiarySubmitResultPayload.Result.TOO_LONG_OR_INVALID,
                    "screen.mydrugs.diary.save_failed.invalid", 0);
            return;
        }
        String cleaned = PlayerDiaryAttachment.sanitizeCustomContent(rawContent);
        if (cleaned == null) {
            sendResult(player, PersonalDiarySubmitResultPayload.Result.EMPTY_OR_INVALID,
                    "screen.mydrugs.diary.save_failed.invalid", 0);
            return;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        DrugId dominant = pickDominantDrug(stats);

        diary.append(new DiaryEntry(
                PlayerDiaryAttachment.currentDay(gameTime),
                gameTime,
                DiaryEntryType.CUSTOM,
                cleaned,
                "custom",
                dominant == null ? "" : dominant.serializedName()
        ));
        diary.markWritten(gameTime);

        // Apply diary recovery effects (same as auto write).
        ItemEffectHandler.applyDiary(player);
        AdvancementEventHooks.recoveryAction(player, "personal_diary");

        // Also reflect on the item cooldown so the UI feedback stays consistent.
        var stack = DiarySnapshotBuilder.findDiaryStack(player);
        if (stack != null) {
            player.getCooldowns().addCooldown(stack, PlayerDiaryAttachment.WRITE_COOLDOWN_TICKS);
        }

        // Send a fresh snapshot back so the open screen refreshes.
        PacketDistributor.sendToPlayer(player, DiarySnapshotBuilder.build(player));
        sendResult(player, PersonalDiarySubmitResultPayload.Result.SAVED,
                "screen.mydrugs.diary.saved_payoff", PlayerDiaryAttachment.WRITE_COOLDOWN_TICKS);
    }

    private static void sendResult(ServerPlayer player, PersonalDiarySubmitResultPayload.Result result,
                                   String messageKey, int cooldownTicksRemaining) {
        PacketDistributor.sendToPlayer(player, new PersonalDiarySubmitResultPayload(
                result,
                result == PersonalDiarySubmitResultPayload.Result.SAVED,
                messageKey,
                Math.max(0, cooldownTicksRemaining)
        ));
    }

    private static DrugId pickDominantDrug(PlayerAddictionStats stats) {
        DrugId best = null;
        float bestScore = -1.0F;
        for (DrugId id : stats.getTrackedDrugIds()) {
            var ds = stats.getDrugStats(id);
            if (ds == null) continue;
            float score = ds.currentDose() * 2.0F + ds.baseWithdrawalMeter * 1.2F + ds.addictionValue * 0.6F;
            if (score > bestScore) {
                bestScore = score;
                best = id;
            }
        }
        return best;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
