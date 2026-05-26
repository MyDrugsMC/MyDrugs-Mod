package org.mydrugs.mydrugs.mutation.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.mydrugs.mydrugs.MyDrugs;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.mutation.GeneticRarityTier;
import org.mydrugs.mydrugs.mutation.MutationManager;
import org.mydrugs.mydrugs.mutation.PlayerMutationsAttachment;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Phase F.2 — mob-spliced mutations are <i>fast, strong, fragile</i>.
 *
 * When a creature whose {@link GeneticRarityTier} is at or below the player's strongest active
 * mutation stat's tier-equivalent lands a hit, the player's mutation stats decay sharply. Integrated
 * traits (Phase A) are untouched — that's the contrast the design is built around.
 */
@EventBusSubscriber(modid = MyDrugs.MODID)
public final class MutationFragilityEvents {
    /** Decay applied to every mutation stat on a qualifying hit. */
    public static final float MUTATION_HIT_DECAY = 0.15F;

    /** Cooldown between fragility messages so the chat doesn't spam during melee swarms. */
    private static final long MESSAGE_COOLDOWN_TICKS = 60L;
    public static final float MYTHIC_TIER_MIN_STAT = 0.80F;
    public static final float DANGEROUS_TIER_MIN_STAT = 0.60F;
    public static final float RARE_TIER_MIN_STAT = 0.40F;
    public static final float UNCOMMON_TIER_MIN_STAT = 0.20F;

    private static final Map<UUID, Long> LAST_MESSAGE_TIME = new HashMap<>();

    private MutationFragilityEvents() {
    }

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == player) {
            return;
        }
        if (event.getNewDamage() <= 0.0F) {
            return;
        }

        PlayerMutationsAttachment attachment = player.getData(ModAttachments.PLAYER_MUTATIONS.get());
        GeneticRarityTier playerTier = strongestActiveStatTier(attachment);
        if (playerTier == null) {
            return;
        }

        GeneticRarityTier attackerTier = GeneticRarityTier.fromEntity(attacker);
        if (!shouldDecayForHit(attackerTier, playerTier)) {
            return;
        }

        if (attachment.decayMutations(MUTATION_HIT_DECAY)) {
            MutationManager.syncToClient(player);
            notifyDestabilized(player);
        }
    }

    /**
     * Maps the player's highest active mutation stat value to the equivalent {@link GeneticRarityTier}.
     * Returns null if the player has no active spliced stats (nothing fragile to decay).
     */
    public static GeneticRarityTier strongestActiveStatTier(PlayerMutationsAttachment attachment) {
        if (attachment == null) {
            return null;
        }
        float max = 0.0F;
        for (Map.Entry<String, Float> entry : attachment.snapshotCurrent().entrySet()) {
            max = Math.max(max, entry.getValue());
        }
        if (max <= 0.0F) {
            return null;
        }
        if (max >= MYTHIC_TIER_MIN_STAT) return GeneticRarityTier.MYTHIC;
        if (max >= DANGEROUS_TIER_MIN_STAT) return GeneticRarityTier.DANGEROUS;
        if (max >= RARE_TIER_MIN_STAT) return GeneticRarityTier.RARE;
        if (max >= UNCOMMON_TIER_MIN_STAT) return GeneticRarityTier.UNCOMMON;
        return GeneticRarityTier.COMMON;
    }

    public static boolean shouldDecayForHit(GeneticRarityTier attackerTier, GeneticRarityTier playerTier) {
        return attackerTier != null && playerTier != null && attackerTier.ordinal() <= playerTier.ordinal();
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LAST_MESSAGE_TIME.remove(player.getUUID());
        }
    }

    private static void notifyDestabilized(ServerPlayer player) {
        long now = player.level().getGameTime();
        long last = LAST_MESSAGE_TIME.getOrDefault(player.getUUID(), -MESSAGE_COOLDOWN_TICKS);
        if (now - last < MESSAGE_COOLDOWN_TICKS) {
            return;
        }
        LAST_MESSAGE_TIME.put(player.getUUID(), now);
        player.displayClientMessage(
                Component.translatable("message.mydrugs.mutation.fragility_destabilized")
                        .withStyle(ChatFormatting.RED),
                true
        );
    }
}
