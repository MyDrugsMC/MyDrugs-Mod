package org.mydrugs.mydrugs.client.psy_mixer;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualAction;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualEngine;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualJudgement;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.PsyMixerRitualQuality;
import org.mydrugs.mydrugs.client.effects.HeartbeatPulse;
import org.mydrugs.mydrugs.network.PsyMixerRitualActionPayload;
import org.mydrugs.mydrugs.network.PsyMixerRitualSyncPayload;
import org.mydrugs.mydrugs.sounds.ModSounds;

public final class PsyMixerRitualClientState {
    private static boolean active;
    private static BlockPos corePos = BlockPos.ZERO;
    private static String formulaName = "";
    private static int actionIndex;
    private static int actionCount;
    private static PsyMixerRitualAction action = PsyMixerRitualAction.NONE;
    private static PsyMixerRitualQuality quality = PsyMixerRitualQuality.BASE;
    private static int mistakes;
    private static int maxMistakes;
    private static PsyMixerRitualJudgement lastJudgement = PsyMixerRitualJudgement.NONE;
    private static int feedbackTicks;
    private static int ritualTick;
    private static int ritualMaxTime = 1;
    private static int actionTick;
    private static int actionTimeout = 1;
    private static float targetPhase;
    private static float timingWindow;
    private static int badQualityHeartbeatCooldown;
    private static boolean completionAnimation;
    private static int completionTick;
    private static int completionDuration = 60;
    private static int completionReunionTick = 40;
    private static ItemStack completionPreviewStack = ItemStack.EMPTY;

    private PsyMixerRitualClientState() {
    }

    public static void handle(PsyMixerRitualSyncPayload payload, IPayloadContext context) {
        if (!payload.running()) {
            clear();
            return;
        }
        active = true;
        corePos = payload.corePos();
        formulaName = payload.formulaName();
        actionIndex = payload.actionIndex();
        actionCount = payload.actionCount();
        action = payload.action();
        quality = payload.quality();
        mistakes = payload.mistakes();
        maxMistakes = payload.maxMistakes();
        lastJudgement = payload.lastJudgement();
        feedbackTicks = payload.feedbackTicks();
        ritualTick = payload.ritualTick();
        ritualMaxTime = Math.max(1, payload.ritualMaxTime());
        actionTick = payload.actionTick();
        actionTimeout = Math.max(1, payload.actionTimeout());
        targetPhase = payload.targetPhase();
        timingWindow = payload.timingWindow();
        completionAnimation = payload.completionAnimation();
        completionTick = Math.max(0, payload.completionTick());
        completionDuration = Math.max(1, payload.completionDuration());
        completionReunionTick = Math.max(0, payload.completionReunionTick());
        completionPreviewStack = payload.completionPreviewStack().copy();
    }

    public static void tick() {
        if (!active) {
            return;
        }
        ritualTick++;
        actionTick++;
        if (completionAnimation) {
            completionTick = Math.min(completionDuration, completionTick + 1);
        }
        if (feedbackTicks > 0) {
            feedbackTicks--;
        }
        if (badQualityHeartbeatCooldown > 0) {
            badQualityHeartbeatCooldown--;
        }
        if (shouldHeartbeat() && badQualityHeartbeatCooldown <= 0) {
            HeartbeatPulse.triggerBeat();
            Minecraft mc = Minecraft.getInstance();
            if (mc.player != null && Config.CLIENT.enableHeartbeatSounds.get()) {
                mc.player.playNotifySound(ModSounds.SINGLE_HEARTBEAT.get(), SoundSource.PLAYERS, 0.45F, quality == PsyMixerRitualQuality.CRUDE ? 0.82F : 0.95F);
            }
            badQualityHeartbeatCooldown = quality == PsyMixerRitualQuality.CRUDE ? 24 : 36;
        }
    }

    public static void clear() {
        active = false;
        corePos = BlockPos.ZERO;
        formulaName = "";
        actionIndex = 0;
        actionCount = 0;
        action = PsyMixerRitualAction.NONE;
        quality = PsyMixerRitualQuality.BASE;
        mistakes = 0;
        maxMistakes = 0;
        lastJudgement = PsyMixerRitualJudgement.NONE;
        feedbackTicks = 0;
        ritualTick = 0;
        ritualMaxTime = 1;
        actionTick = 0;
        actionTimeout = 1;
        targetPhase = 0.0F;
        timingWindow = 0.0F;
        badQualityHeartbeatCooldown = 0;
        completionAnimation = false;
        completionTick = 0;
        completionDuration = 60;
        completionReunionTick = 40;
        completionPreviewStack = ItemStack.EMPTY;
    }

    public static boolean isActive() {
        return active;
    }

    public static BlockPos corePos() {
        return corePos;
    }

    public static String formulaName() {
        return formulaName;
    }

    public static int actionIndex() {
        return actionIndex;
    }

    public static int actionCount() {
        return actionCount;
    }

    public static PsyMixerRitualAction action() {
        return action;
    }

    public static PsyMixerRitualQuality quality() {
        return quality;
    }

    public static int mistakes() {
        return mistakes;
    }

    public static int maxMistakes() {
        return maxMistakes;
    }

    public static PsyMixerRitualJudgement lastJudgement() {
        return lastJudgement;
    }

    public static int feedbackTicks() {
        return feedbackTicks;
    }

    public static float actionProgress() {
        return Math.min(1.0F, actionTick / (float) actionTimeout);
    }

    public static float phase() {
        return PsyMixerRitualEngine.phase(ritualTick, ritualMaxTime);
    }

    public static float targetPhase() {
        return targetPhase;
    }

    public static float timingWindow() {
        return timingWindow;
    }

    public static boolean isCompletionAnimation() {
        return completionAnimation;
    }

    public static int completionTick() {
        return completionTick;
    }

    public static int completionDuration() {
        return completionDuration;
    }

    public static int completionReunionTick() {
        return completionReunionTick;
    }

    public static ItemStack completionPreviewStack() {
        return completionPreviewStack.copy();
    }

    public static void sendAction(PsyMixerRitualAction action) {
        if (!active) {
            return;
        }
        ClientPacketDistributor.sendToServer(new PsyMixerRitualActionPayload(corePos, action, phase()));
    }

    private static boolean shouldHeartbeat() {
        return active && (quality == PsyMixerRitualQuality.CRUDE || (maxMistakes > 0 && mistakes * 2 >= maxMistakes));
    }
}
