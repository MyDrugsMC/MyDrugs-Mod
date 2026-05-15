package org.mydrugs.mydrugs.mutation;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import org.mydrugs.mydrugs.core.drug.effect.EffectType;
import org.mydrugs.mydrugs.damage.ModDamageTypes;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.manager.effect.DrugEffectRuntimeManager;
import org.mydrugs.mydrugs.items.data.MutationPayloadData;
import org.mydrugs.mydrugs.mutation.network.MutationSyncPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class MutationManager {
    private static final int SYNC_INTERVAL_TICKS = 100;

    private MutationManager() {
    }

    public static float getValue(ServerPlayer player, MutationStat stat) {
        if (player == null || stat == null) {
            return 0.0F;
        }
        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());
        for (ActiveMutationStat active : attachment.activeStats()) {
            if (stat.serializedName().equals(active.statId())) {
                return Math.clamp(active.currentValue(), 0.0F, 1.0F);
            }
        }
        return 0.0F;
    }

    public static float scaleNegative(ServerPlayer player, MutationStat stat, float value) {
        float v = getValue(player, stat);
        return value * Math.max(0.0F, 1.0F - v);
    }

    public static float boostPositive(ServerPlayer player, MutationStat stat, float value) {
        return boostPositive(player, stat, value, 0.50F);
    }

    public static float boostPositive(ServerPlayer player, MutationStat stat, float value, float boostScale) {
        float v = getValue(player, stat);
        return value * (1.0F + v * boostScale);
    }

    public static boolean injectSterile(ServerPlayer player, MutationPayloadData payload) {
        if (containsSource(payload, player.getUUID().toString())) {
            player.displayClientMessage(Component.translatable("message.mydrugs.mutation.self_genetics_rejected").withStyle(ChatFormatting.RED), true);
            return false;
        }

        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());
        float geneticStability = getValue(player, MutationStat.GENETIC_STABILITY);
        attachment.injectPayload(payload, geneticStability);
        player.displayClientMessage(Component.translatable("message.mydrugs.mutation.injection_started").withStyle(ChatFormatting.LIGHT_PURPLE), true);
        float rejection = payload.rejectionRisk() * Math.max(0.10F, 1.0F - geneticStability * 0.50F);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CUSTOM_NAUSEA, Math.min(0.6F, 0.15F + rejection), 20 * 8);
        DrugEffectRuntimeManager.addEffect(player, EffectType.HEARTBEAT, 0.25F + rejection, 20 * 8);
        syncToClient(player);
        return true;
    }

    public static void injectDirty(ServerPlayer player) {
        startInfection(player, 0.45F);
        player.displayClientMessage(Component.translatable("message.mydrugs.mutation.dirty_injection").withStyle(ChatFormatting.RED), true);
    }

    public static void startInfection(ServerPlayer player, float severity) {
        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());
        attachment.infection().start(severity);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CUSTOM_NAUSEA, 0.35F, 20 * 8);
        DrugEffectRuntimeManager.addEffect(player, EffectType.CONFUSION, 0.25F, 20 * 8);
    }

    public static void cureInfection(ServerPlayer player, float strength) {
        player.getData(ModAttachments.PLAYER_MUTATIONS.get()).infection().cure(strength);
    }

    public static void tickPlayer(ServerPlayer player) {
        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());

        float geneticStability = getValue(player, MutationStat.GENETIC_STABILITY);
        float assimilationSpeed = 1.0F + geneticStability * 0.35F;
        attachment.tickAssimilation(assimilationSpeed);

        List<String> completed = attachment.drainCompletedAssimilations();
        if (!completed.isEmpty()) {
            announceCompletions(player, completed);
        }

        tickInfection(player, attachment);

        if (!attachment.activeStats().isEmpty() && player.tickCount % SYNC_INTERVAL_TICKS == 0) {
            syncToClient(player);
        } else if (!completed.isEmpty()) {
            syncToClient(player);
        }
    }

    public static void syncToClient(ServerPlayer player) {
        if (player == null) {
            return;
        }
        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());
        List<MutationSyncPayload.Entry> entries = new ArrayList<>();
        for (Map.Entry<String, Float> entry : attachment.snapshotCurrent().entrySet()) {
            entries.add(new MutationSyncPayload.Entry(entry.getKey(), entry.getValue()));
        }
        PacketDistributor.sendToPlayer(player, new MutationSyncPayload(entries));
    }

    private static void announceCompletions(ServerPlayer player, List<String> statIds) {
        for (String statId : statIds) {
            MutationStat stat = MutationStat.bySerializedNameOrNull(statId);
            Component statName = stat == null
                    ? Component.translatable("mutation.mydrugs.stat.unknown", statId)
                    : Component.translatable(stat.translationKey());
            player.displayClientMessage(
                    Component.translatable("message.mydrugs.mutation.assimilation_complete", statName)
                            .withStyle(ChatFormatting.LIGHT_PURPLE),
                    false
            );
        }
    }

    public static boolean containsSource(MutationPayloadData payload, String playerUuid) {
        if (payload == null || playerUuid == null || playerUuid.isBlank()) {
            return false;
        }
        for (String sourceUuid : payload.sourceUuids()) {
            if (playerUuid.equalsIgnoreCase(sourceUuid)) {
                return true;
            }
        }
        return false;
    }

    private static void tickInfection(ServerPlayer player, PlayerMutationsAttachment attachment) {
        InfectionState infection = attachment.infection();
        if (!infection.active()) {
            return;
        }

        float infectionResistance = getValue(player, MutationStat.INFECTION_RESISTANCE);
        float progressRate = Math.max(0.10F, 1.0F - infectionResistance * 0.60F);
        if (progressRate >= 1.0F || player.getRandom().nextFloat() < progressRate) {
            infection.tick();
        }

        int stage = infection.stage();
        if (infection.markMessageShown(stage)) {
            String key = switch (stage) {
                case 1 -> "message.mydrugs.mutation.injection_burns";
                case 2 -> "message.mydrugs.mutation.infection_spreading";
                case 3 -> "message.mydrugs.mutation.mutations_collapsing";
                default -> "message.mydrugs.mutation.collapse";
            };
            player.displayClientMessage(Component.translatable(key).withStyle(ChatFormatting.RED), true);
        }

        if (infection.ticks() % 100 == 0) {
            DrugEffectRuntimeManager.addEffect(player, EffectType.CUSTOM_NAUSEA, Math.min(0.8F, 0.25F + infection.severity() * 0.35F), 80);
            DrugEffectRuntimeManager.addEffect(player, EffectType.CONFUSION, Math.min(0.7F, 0.15F + infection.severity() * 0.30F), 80);
        }

        if (stage >= 2 && infection.ticks() % 80 == 0) {
            float damage = switch (stage) {
                case 2 -> 0.5F;
                case 3 -> 1.0F;
                default -> 2.0F;
            };
            float scaled = scaleNegative(player, MutationStat.HEALTH_STABILITY, damage * infection.severity());
            if (scaled > 0.0F) {
                player.hurt(ModDamageTypes.mutationInfection(player.level()), scaled);
            }
            if (!player.isAlive()) {
                attachment.clearMutations();
                infection.clear();
                return;
            }
        }

        float mutationLossResist = Math.max(0.0F, infectionResistance * 0.40F);
        if (stage >= 3 && infection.ticks() % 200 == 0) {
            float baseDecay = stage == 3 ? 0.03F : 0.10F;
            float decay = baseDecay * Math.max(0.20F, 1.0F - mutationLossResist);
            attachment.decayMutations(decay);
        }

        if (stage >= 4 && infection.ticks() % 400 == 0) {
            attachment.clearMutations();
            if (player.getHealth() <= 4.0F) {
                player.hurt(ModDamageTypes.mutationInfection(player.level()), 20.0F);
                infection.clear();
            }
        }
    }
}
