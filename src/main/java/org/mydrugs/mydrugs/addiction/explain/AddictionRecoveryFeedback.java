package org.mydrugs.mydrugs.addiction.explain;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;

public final class AddictionRecoveryFeedback {
    private static final long ACTIVE_FEEDBACK_COOLDOWN_TICKS = 35L;
    private static final long ROOM_FEEDBACK_COOLDOWN_TICKS = 600L;
    private static final long MOMENTUM_FEEDBACK_COOLDOWN_TICKS = 120L;
    private static final long PASSIVE_CAP_FEEDBACK_COOLDOWN_TICKS = 1200L;

    private AddictionRecoveryFeedback() {
    }

    public static void sendForAction(ServerPlayer player, ActionKind kind) {
        String key = keyFor(kind);
        if (key != null) {
            send(player, key, ACTIVE_FEEDBACK_COOLDOWN_TICKS);
        }
    }

    public static void sendProgressDetail(ServerPlayer player, ActionKind kind, int integrationPercent) {
        Component action = Component.translatable(actionLabelKey(kind));
        Component message = Component.translatable(
                "message.mydrugs.recovery_feedback.progress_detail",
                action,
                Math.max(0, Math.min(100, integrationPercent))
        );
        send(player, "message.mydrugs.recovery_feedback.progress_detail", message, ACTIVE_FEEDBACK_COOLDOWN_TICKS, false);
    }

    public static void sendHeadphones(ServerPlayer player) {
        send(player, "message.mydrugs.recovery_feedback.music", ACTIVE_FEEDBACK_COOLDOWN_TICKS);
    }

    public static void sendRoomSupport(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        if (now - stats.lastRoomFeedbackTick < ROOM_FEEDBACK_COOLDOWN_TICKS) {
            return;
        }
        if (send(player, "message.mydrugs.recovery_feedback.room", ACTIVE_FEEDBACK_COOLDOWN_TICKS)) {
            stats.lastRoomFeedbackTick = now;
        }
    }

    public static void sendAntidote(ServerPlayer player) {
        send(player, "message.mydrugs.recovery_feedback.antidote", ACTIVE_FEEDBACK_COOLDOWN_TICKS, true);
    }

    public static void sendRecoveryMomentumUsed(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        if (now - stats.lastRecoveryMomentumFeedbackTick < MOMENTUM_FEEDBACK_COOLDOWN_TICKS) {
            return;
        }
        stats.lastRecoveryMomentumFeedbackTick = now;
        player.displayClientMessage(Component.translatable("message.mydrugs.recovery_momentum.used"), true);
    }

    public static void sendPassiveCap(ServerPlayer player) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        if (now - stats.lastPassiveCapFeedbackTick < PASSIVE_CAP_FEEDBACK_COOLDOWN_TICKS) {
            return;
        }
        stats.lastPassiveCapFeedbackTick = now;
        player.displayClientMessage(Component.translatable("message.mydrugs.recovery_feedback.passive_cap"), true);
    }

    private static boolean send(ServerPlayer player, String key, long cooldownTicks) {
        return send(player, key, cooldownTicks, false);
    }

    private static boolean send(ServerPlayer player, String key, long cooldownTicks, boolean force) {
        return send(player, key, Component.translatable(key), cooldownTicks, force);
    }

    private static boolean send(
            ServerPlayer player,
            String key,
            Component message,
            long cooldownTicks,
            boolean force
    ) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        if (!force && !shouldShow(player, stats)) {
            return false;
        }

        long now = player.level().getGameTime();
        if (now - stats.lastRecoveryFeedbackTick < cooldownTicks) {
            return false;
        }

        stats.lastRecoveryFeedbackTick = now;
        stats.lastRecoveryFeedbackKey = key;
        player.displayClientMessage(message, true);
        return true;
    }

    private static boolean shouldShow(ServerPlayer player, PlayerAddictionStats stats) {
        long now = player.level().getGameTime();
        return AddictionManager.getGlobalSeverity(player) > 0.10F
                || stats.stressLevel > 0.15F
                || stats.badTrip.active
                || stats.sleepBlockedUntil > now
                || stats.overdoseDeathTimer > 0;
    }

    private static String keyFor(ActionKind kind) {
        if (kind == null) {
            return null;
        }
        return switch (kind) {
            case FOOD -> "message.mydrugs.recovery_feedback.stress_down";
            case SLEEP_REST -> "message.mydrugs.recovery_feedback.sleep";
            case DIARY_WRITTEN -> "message.mydrugs.recovery_feedback.diary";
            case HERBAL_TEA -> "message.mydrugs.recovery_feedback.tea";
            case CALMING_MIXTURE -> "message.mydrugs.recovery_feedback.calm";
            case SLEEPING_AID -> "message.mydrugs.recovery_feedback.sleep";
            case PET_CARE -> "message.mydrugs.recovery_feedback.calm";
            case THERAPY_SESSION, ORE_MINED, CROP_TENDED, MACHINE_OUTPUT, DISTILLERY_CYCLE, PSY_MIXER_SUCCESS ->
                    "message.mydrugs.recovery_feedback.recovery_plus";
            case MUSIC_SUPPORT, RECOVERY_ROOM_SUPPORT, PASSIVE_COMPANION, RECOVERY_RESONANCE -> null;
        };
    }

    private static String actionLabelKey(ActionKind kind) {
        if (kind == null) {
            return "message.mydrugs.recovery_action.unknown";
        }
        return "message.mydrugs.recovery_action." + kind.name().toLowerCase(java.util.Locale.ROOT);
    }
}
