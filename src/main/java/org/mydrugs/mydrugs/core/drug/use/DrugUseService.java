package org.mydrugs.mydrugs.core.drug.use;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.advancement.DrugKnowledge;
import org.mydrugs.mydrugs.advancement.DrugKnowledgeResult;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.effect.DrugEffect;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.core.drug.strategy.ConsumptionStrategy;
import org.mydrugs.mydrugs.effects.addiction.manager.AddictionManager;
import org.mydrugs.mydrugs.effects.payloads.DrugVisualPayload;
import org.mydrugs.mydrugs.progression.DrugProgressionGate;
import org.mydrugs.mydrugs.progression.PsyKnowledgeKey;
import org.mydrugs.mydrugs.progression.PsyKnowledgeManager;

import java.util.Optional;

public final class DrugUseService {
    private static final float BASE_DOSE = 1.0F;

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
        AddictionManager.consume(use);
        DrugKnowledgeResult knowledgeResult = DrugKnowledge.markConsumed(use);
        AdvancementEventHooks.drugConsumed(player, knowledgeResult);
        Optional<PsyKnowledgeKey> grantedKnowledge = Optional.empty();
        if (progression.knowledgeDeferred()) {
            player.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.mydrugs.knowledge.blocked.mushroom"), true);
        } else if (progression.grantedKnowledge().isPresent()
                && PsyKnowledgeManager.grant(player, progression.grantedKnowledge().get())) {
            grantedKnowledge = progression.grantedKnowledge();
        }
        return DrugUseResult.success(grantedKnowledge);
    }

    private void applyEffects(ResolvedDrugUse use) {
        for (DrugEffect effect : use.model().getDrugEffects()) {
            int duration = use.strategy() != null
                    ? use.strategy().getNewDuration(effect)
                    : effect.getBaseDuration();
            int potency = use.strategy() != null
                    ? use.strategy().getNewPotency(effect)
                    : effect.getBasePotency();

            switch (effect.getEffectType().getCategory()) {
                case MINECRAFT_EFFECT -> applyMinecraftEffect(use.player(), effect.getEffectType(), duration, potency);
                case SHADER, SOUND_EFFECT -> PacketDistributor.sendToPlayer(
                        use.player(),
                        new DrugVisualPayload(effect.getEffectType(), duration, potency)
                );
            }
        }
    }

    private void applyMinecraftEffect(ServerPlayer player, EffectType effect, int duration, int potency) {
        switch (effect) {
            case NAUSEA -> player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, duration, potency));
            case SLOWNESS -> player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, duration, potency));
            default -> {
            }
        }
    }
}
