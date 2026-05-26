package org.mydrugs.mydrugs.core.drug.use;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.advancement.DrugKnowledge;
import org.mydrugs.mydrugs.advancement.DrugKnowledgeResult;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.core.drug.DrugDurationScale;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.core.drug.runtime.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.core.drug.strategy.SmokingStrategy;
import org.mydrugs.mydrugs.diary.DiaryBlockerTypes;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.network.DrugVisualPayload;
import org.mydrugs.mydrugs.progression.DrugProgressionGate;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;

import java.util.ArrayList;
import java.util.List;

public final class DrugUseService {
    private static final float BASE_DOSE = 1.0F;
    private static final int SMOKING_FOG_DURATION = DrugDurationScale.seconds(5);
    private static final float SMOKING_FOG_INTENSITY = 1.0F;

    public DrugUseResult consumeStack(ServerPlayer player,
                                      ItemStack stack,
                                      @Nullable ConsumptionStrategy overrideStrategy,
                                      DrugUseSource source) {
        DrugUseResult result = DrugUseResult.noDrugModel();
        for (DrugStackResolver.ResolvedStackDrug resolved : DrugStackResolver.resolve(stack, overrideStrategy)) {
            DrugUseResult current = consume(player, resolved.model(), resolved.strategy(), source, stack);
            if (current.status() == DrugUseResult.Status.BLOCKED_MISSING_KNOWLEDGE) {
                return current;
            }
            if (current.consumed()) {
                result = current;
            }
        }
        return result;
    }

    public DrugUseResult consume(ServerPlayer player,
                                 @Nullable DrugModel model,
                                 @Nullable ConsumptionStrategy strategy,
                                 DrugUseSource source) {
        return consume(player, model, strategy, source, ItemStack.EMPTY);
    }

    public DrugUseResult consume(ServerPlayer player,
                                 @Nullable DrugModel model,
                                 @Nullable ConsumptionStrategy strategy,
                                 DrugUseSource source,
                                 ItemStack sourceStack) {
        if (model == null) {
            return DrugUseResult.noDrugModel();
        }

        DrugProgressionGate.Decision progression = DrugProgressionGate.evaluate(player, model);
        if (!progression.allowed()) {
            DrugProgressionGate.notifyBlocked(player, progression.blockedMessageKey());
            recordKnowledgeBlocker(player, progression.blockedMessageKey());
            return DrugUseResult.blocked(progression.blockedMessageKey());
        }

        float effectiveDose = strategy != null ? strategy.getNewDose(BASE_DOSE) : BASE_DOSE;
        ResolvedDrugUse use = new ResolvedDrugUse(
                player,
                model,
                strategy,
                BASE_DOSE,
                effectiveDose,
                source,
                sourceStack.copy()
        );

        applyEffects(use);
        applySmokingVisual(use);
        AddictionManager.consume(use);
        DrugKnowledgeResult knowledgeResult = DrugKnowledge.markConsumed(use);
        AdvancementEventHooks.drugConsumed(player, knowledgeResult);
        List<PsyKnowledgeKey> grantedKnowledge = new ArrayList<>(progression.grantedKnowledge().size());
        if (progression.knowledgeDeferred()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.mydrugs.knowledge.blocked.mushroom"), true);
            recordKnowledgeBlocker(player, "message.mydrugs.knowledge.blocked.mushroom");
        } else {
            for (PsyKnowledgeKey key : progression.grantedKnowledge()) {
                if (PsyKnowledgeManager.grant(player, key)) {
                    grantedKnowledge.add(key);
                }
            }
        }

        return DrugUseResult.success(grantedKnowledge);
    }

    private void recordKnowledgeBlocker(ServerPlayer player, @Nullable String messageKey) {
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY.get());
        String blockerType = "message.mydrugs.knowledge.blocked.mushroom".equals(messageKey)
                ? DiaryBlockerTypes.MUSHROOM_GATE
                : DiaryBlockerTypes.KNOWLEDGE_GATE;
        diary.recordBlocker(blockerType, player.level().getGameTime());
    }

    private void applyEffects(ResolvedDrugUse use) {
        for (DrugEffect effect : use.model().getDrugEffects()) {
            int duration = use.strategy() != null
                    ? use.strategy().getNewDuration(effect)
                    : effect.getBaseDuration();
            float intensity = use.strategy() != null
                    ? use.strategy().getNewIntensity(effect)
                    : effect.getBaseIntensity();

            DrugEffectRuntimeManager.addEffect(use.player(), effect.getEffectType(), intensity, duration);
        }
    }

    private void applySmokingVisual(ResolvedDrugUse use) {
        if (use.strategy() instanceof SmokingStrategy) {
            PacketDistributor.sendToPlayer(
                    use.player(),
                    new DrugVisualPayload(EffectType.FOG, SMOKING_FOG_DURATION, SMOKING_FOG_INTENSITY)
            );
        }
    }
}
