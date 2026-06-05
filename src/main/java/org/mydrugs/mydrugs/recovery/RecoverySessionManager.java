package org.mydrugs.mydrugs.recovery;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager.ActionKind;

public final class RecoverySessionManager {
    private static final long ARRIVE_TICKS = 20L * 4L;
    private static final long MUSIC_GROUNDING_TICKS = 20L * 6L;
    private static final long BREATHING_TICKS = 20L * 5L;
    private static final long REPEATED_MESSAGE_COOLDOWN_TICKS = 20L * 35L;
    private static final long LEFT_EARLY_COOLDOWN_TICKS = 20L * 45L;
    private static final long RETURN_PROMPT_COOLDOWN_TICKS = 20L * 45L;
    private static final long MOMENTUM_DURATION_TICKS = 20L * 60L * 10L;
    private static final int MOMENTUM_CHARGES = 3;
    private static final double NEARLY_STILL_DISTANCE_SQR = 0.025D;

    private RecoverySessionManager() {
    }

    public static void tick(ServerPlayer player) {
        if (player == null) {
            return;
        }

        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoverySessionState session = stats.recoverySession;
        PlayerEnvironmentSnapshot environment = PlayerRecoveryEnvironmentCache.snapshot(player);
        RecoveryRoomReport room = environment.recoveryRoom();
        boolean inValidRoom = RecoveryRoomManager.isValidRecoveryRoom(room);
        long now = player.level().getGameTime();
        String dimension = dimensionId(player);

        if (session.stage == RecoverySessionStage.NONE) {
            if (inValidRoom && needsRecovery(player, stats, now)) {
                startSession(player, session, room, now, dimension);
            }
            updatePosition(session, player);
            return;
        }

        boolean inSessionRoom = inValidRoom && session.isSameRoom(room, dimension);
        if (session.stage != RecoverySessionStage.RETURN && !inSessionRoom) {
            sendLeftEarly(player, session, now);
            session.resetProgress();
            if (inValidRoom && needsRecovery(player, stats, now)) {
                startSession(player, session, room, now, dimension);
            }
            updatePosition(session, player);
            return;
        }

        if (session.stage == RecoverySessionStage.RETURN) {
            if (!inValidRoom) {
                sendReturnPrompt(player, session, now);
            }
            updatePosition(session, player);
            return;
        }

        if (session.stage == RecoverySessionStage.ARRIVE) {
            if (now - session.stageStartedAt >= ARRIVE_TICKS) {
                session.advance(RecoverySessionStage.GROUND, now);
                sendActionbar(player, session, "message.mydrugs.recovery_session.arrived", REPEATED_MESSAGE_COOLDOWN_TICKS);
            }
            updatePosition(session, player);
            return;
        }

        if (session.stage == RecoverySessionStage.GROUND) {
            tickGrounding(player, session, room, now);
        }

        updatePosition(session, player);
    }

    public static boolean onGroundingAction(ServerPlayer player, RecoverySessionAction action) {
        if (player == null || action == null || !action.grounding()) {
            return false;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoverySessionState session = stats.recoverySession;
        RecoveryRoomReport room = PlayerRecoveryEnvironmentCache.snapshot(player).recoveryRoom();
        boolean inValidRoom = RecoveryRoomManager.isValidRecoveryRoom(room);
        long now = player.level().getGameTime();
        String dimension = dimensionId(player);

        if (session.stage == RecoverySessionStage.NONE && inValidRoom && needsRecovery(player, stats, now)) {
            startSession(player, session, room, now, dimension);
        }
        if (session.stage != RecoverySessionStage.GROUND || !inValidRoom || !session.isSameRoom(room, dimension)) {
            return false;
        }

        completeGrounding(player, session, action, now);
        return true;
    }

    public static boolean onReflectionAction(ServerPlayer player, RecoverySessionAction action) {
        if (player == null || action == null || !action.reflection()) {
            return false;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoverySessionState session = stats.recoverySession;
        RecoveryRoomReport room = PlayerRecoveryEnvironmentCache.snapshot(player).recoveryRoom();
        boolean inValidRoom = RecoveryRoomManager.isValidRecoveryRoom(room);
        long now = player.level().getGameTime();
        String dimension = dimensionId(player);

        if (session.stage == RecoverySessionStage.NONE && inValidRoom && needsRecovery(player, stats, now)) {
            startSession(player, session, room, now, dimension);
        }
        if (session.stage != RecoverySessionStage.REFLECT || !inValidRoom || !session.isSameRoom(room, dimension)) {
            return false;
        }

        session.advance(RecoverySessionStage.RETURN, now);
        sendActionbar(player, session, action.messageKey(), REPEATED_MESSAGE_COOLDOWN_TICKS);
        return true;
    }

    public static void onProductiveAction(ServerPlayer player, ActionKind kind) {
        if (player == null || !isReturnAction(kind)) {
            return;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        RecoverySessionState session = stats.recoverySession;
        if (session.stage != RecoverySessionStage.RETURN) {
            return;
        }
        RecoveryRoomReport room = PlayerRecoveryEnvironmentCache.snapshot(player).recoveryRoom();
        if (RecoveryRoomManager.isValidRecoveryRoom(room)) {
            return;
        }

        long now = player.level().getGameTime();
        stats.temporaryEffects.recoveryMomentumUntil = Math.max(
                stats.temporaryEffects.recoveryMomentumUntil,
                now + MOMENTUM_DURATION_TICKS
        );
        stats.temporaryEffects.recoveryMomentumCharges = Math.max(
                stats.temporaryEffects.recoveryMomentumCharges,
                MOMENTUM_CHARGES
        );
        session.resetProgress();
        player.displayClientMessage(Component.translatable("message.mydrugs.recovery_session.complete"), true);
    }

    public static boolean sendContextualMessage(ServerPlayer player, String key, long cooldownTicks) {
        if (player == null || key == null || key.isBlank()) {
            return false;
        }
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        return sendActionbar(player, stats.recoverySession, key, cooldownTicks);
    }

    private static void tickGrounding(ServerPlayer player, RecoverySessionState session, RecoveryRoomReport room, long now) {
        if (room.hasModule(SanctuaryModule.MUSIC_CORNER) && room.hasActiveMusic()) {
            if (session.musicGroundingStartedAt <= 0L) {
                session.musicGroundingStartedAt = now;
            } else if (now - session.musicGroundingStartedAt >= MUSIC_GROUNDING_TICKS) {
                completeGrounding(player, session, RecoverySessionAction.MUSIC, now);
                return;
            }
        } else {
            session.musicGroundingStartedAt = 0L;
        }

        boolean nearlyStill = !session.lastPositionValid || distanceSqr(session, player) <= NEARLY_STILL_DISTANCE_SQR;
        if (room.hasModule(SanctuaryModule.PLANT_BREATHING_CORNER) && player.isShiftKeyDown() && nearlyStill) {
            if (session.breathingStartedAt <= 0L) {
                session.breathingStartedAt = now;
            } else if (now - session.breathingStartedAt >= BREATHING_TICKS) {
                completeGrounding(player, session, RecoverySessionAction.BREATHING, now);
            }
        } else {
            session.breathingStartedAt = 0L;
        }
    }

    private static void completeGrounding(
            ServerPlayer player,
            RecoverySessionState session,
            RecoverySessionAction action,
            long now
    ) {
        session.advance(RecoverySessionStage.REFLECT, now);
        sendActionbar(player, session, action.messageKey(), REPEATED_MESSAGE_COOLDOWN_TICKS);
    }

    private static void startSession(
            ServerPlayer player,
            RecoverySessionState session,
            RecoveryRoomReport room,
            long now,
            String dimension
    ) {
        session.start(room, dimension, now);
        String key = room.tier() == RecoveryRoomTier.SANCTUARY
                ? "message.mydrugs.recovery_session.enter_sanctuary"
                : "message.mydrugs.recovery_session.enter";
        sendActionbar(player, session, key, REPEATED_MESSAGE_COOLDOWN_TICKS);
    }

    private static boolean needsRecovery(ServerPlayer player, PlayerAddictionStats stats, long now) {
        return AddictionManager.getGlobalSeverity(player) > 0.10F
                || stats.stressLevel > 0.15F
                || stats.badTrip.active
                || stats.sleepBlockedUntil > now
                || stats.overdoseDeathTimer > 0;
    }

    private static void sendLeftEarly(ServerPlayer player, RecoverySessionState session, long now) {
        if (now - session.lastLeftEarlyMessageAt < LEFT_EARLY_COOLDOWN_TICKS) {
            return;
        }
        session.lastLeftEarlyMessageAt = now;
        player.displayClientMessage(Component.translatable("message.mydrugs.recovery_session.left_early"), true);
    }

    private static void sendReturnPrompt(ServerPlayer player, RecoverySessionState session, long now) {
        if (session.returnPromptSent && now - session.lastReturnPromptMessageAt < RETURN_PROMPT_COOLDOWN_TICKS) {
            return;
        }
        session.returnPromptSent = true;
        session.lastReturnPromptMessageAt = now;
        player.displayClientMessage(Component.translatable("message.mydrugs.recovery_session.return_prompt"), true);
    }

    private static boolean sendActionbar(
            ServerPlayer player,
            RecoverySessionState session,
            String key,
            long cooldownTicks
    ) {
        long now = player.level().getGameTime();
        if (key.equals(session.lastMessageKey) && now - session.lastMessageAt < cooldownTicks) {
            return false;
        }
        session.lastMessageKey = key;
        session.lastMessageAt = now;
        player.displayClientMessage(Component.translatable(key), true);
        return true;
    }

    private static boolean isReturnAction(ActionKind kind) {
        return kind != null && (kind.nextStageWork() || kind == ActionKind.PET_CARE);
    }

    private static double distanceSqr(RecoverySessionState session, ServerPlayer player) {
        double dx = player.getX() - session.lastX;
        double dy = player.getY() - session.lastY;
        double dz = player.getZ() - session.lastZ;
        return dx * dx + dy * dy + dz * dz;
    }

    private static void updatePosition(RecoverySessionState session, ServerPlayer player) {
        session.updateLastPosition(player.getX(), player.getY(), player.getZ());
    }

    private static String dimensionId(ServerPlayer player) {
        return player.level().dimension().location().toString();
    }
}
