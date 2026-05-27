package org.mydrugs.mydrugs.blocks.entity.psy_mixer;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import org.mydrugs.mydrugs.Config;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.blocks.PsyMixerMultiblock;
import org.mydrugs.mydrugs.blocks.entity.FormedPsyMixerCoreBlockEntity;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.RecoveryRitualLogic.RecoveryKind;
import org.mydrugs.mydrugs.blocks.entity.psy_mixer.RecoveryRitualLogic.SlotKind;
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.core.drug.DrugModel;
import org.mydrugs.mydrugs.core.drug.integration.RecoveryProgressManager;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.items.ModItemTags;
import org.mydrugs.mydrugs.items.drugs.DrugItem;

/**
 * Survival soft-lock recovery rituals for the Psy Mixer. When the five input slots hold a
 * recovery loadout (one drug + four vines, or one drug + four copper ingots), activating the
 * mixer resolves immediately as a one-shot experience-and-chance roll instead of starting the
 * interactive ritual minigame.
 *
 * <p>Crucially, neither recovery route requires the item it rebuilds — see {@link RecoveryRitualLogic}.
 */
public final class PsyMixerRecoveryRitual {
    private PsyMixerRecoveryRitual() {
    }

    private static final int[] INPUT_SLOTS = {
            PsyMixerMultiblock.SLOT_BASE,
            PsyMixerMultiblock.SLOT_MATERIAL,
            PsyMixerMultiblock.SLOT_CATALYST,
            PsyMixerMultiblock.SLOT_STABILIZER,
            PsyMixerMultiblock.SLOT_VESSEL
    };

    /** Returns the recovery kind the five input slots resolve to, or {@code null} if not a recovery loadout. */
    public static @Nullable RecoveryKind detect(NonNullList<ItemStack> items) {
        List<SlotKind> slots = new ArrayList<>(INPUT_SLOTS.length);
        for (int slot : INPUT_SLOTS) {
            slots.add(classify(items.get(slot)));
        }
        return RecoveryRitualLogic.classify(slots);
    }

    private static SlotKind classify(ItemStack stack) {
        if (stack.isEmpty()) {
            return SlotKind.EMPTY;
        }
        if (stack.is(Items.VINE)) {
            return SlotKind.VINE;
        }
        if (stack.is(Items.COPPER_INGOT)) {
            return SlotKind.COPPER_INGOT;
        }
        if (stack.is(ModItemTags.INTEGRATION_CORE_SEED_SOURCES)) {
            return SlotKind.SEED;
        }
        if (stack.is(ModItems.PSY_RECEPTACLE.get())) {
            return SlotKind.RECEPTACLE;
        }
        if (stack.is(ModItems.INSULATED_WIRE.get())) {
            return SlotKind.WIRE;
        }
        if (stack.getItem() instanceof DrugItem) {
            return SlotKind.DRUG;
        }
        return SlotKind.OTHER;
    }

    /**
     * Resolves a recovery ritual: consumes experience and the five inputs, rolls the outcome, and
     * applies the result. Always returns {@code true} — the activation was handled as a recovery.
     */
    public static boolean resolve(ServerLevel level,
                                  FormedPsyMixerCoreBlockEntity core,
                                  ServerPlayer player,
                                  RecoveryKind kind) {
        if (kind == RecoveryKind.INTEGRATION_CORE
                && !player.getData(ModAttachments.PLAYER_INTEGRATION.get()).hasReceivedFirstIntegrationCore()) {
            player.displayClientMessage(
                    Component.translatable("message.mydrugs.psy_mixer.recovery.integration_core_locked"),
                    false
            );
            player.closeContainer();
            return true;
        }
        int maxLevels = Config.SERVER.recoveryRitualMaxLevels.get();
        DrugId drug = findDrug(core.getItems());
        double chance = RecoveryRitualLogic.successChance(player.experienceLevel, maxLevels,
                RecoveryRitualLogic.drugBonus(drug));
        boolean success = level.getRandom().nextDouble() < chance;

        consumeExperience(player, maxLevels);
        for (int slot : INPUT_SLOTS) {
            core.setItem(slot, ItemStack.EMPTY);
        }

        BlockPos pos = core.getBlockPos();
        if (success) {
            giveItem(player, recoveryOutput(kind));
            level.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.9F, 1.1F);
            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.7F, 1.4F);
            for (int i = 0; i < 40; i++) {
                level.sendParticles(ParticleTypes.END_ROD,
                        pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                        1, 0.35, 0.5, 0.35, 0.04);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                    18, 0.5, 0.5, 0.5, 0.0);
            player.displayClientMessage(
                    Component.translatable("message.mydrugs.psy_mixer.recovery.success"), false);
            RecoveryProgressManager.onProductiveAction(player, RecoveryProgressManager.ActionKind.PSY_MIXER_SUCCESS, 0.75F);
        } else {
            level.playSound(null, pos, SoundEvents.GLASS_BREAK, SoundSource.BLOCKS, 1.0F, 0.8F);
            level.playSound(null, pos, SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.BLOCKS, 0.6F, 0.4F);
            level.sendParticles(ParticleTypes.EXPLOSION,
                    pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                    6, 0.4, 0.3, 0.4, 0.0);
            for (int i = 0; i < 30; i++) {
                level.sendParticles(ParticleTypes.LARGE_SMOKE,
                        pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5,
                        1, 0.4, 0.4, 0.4, 0.05);
            }
            player.displayClientMessage(
                    Component.translatable("message.mydrugs.psy_mixer.recovery.fail"), false);
        }

        player.closeContainer();
        return true;
    }

    private static ItemStack recoveryOutput(RecoveryKind kind) {
        return switch (kind) {
            case RECEPTACLE -> new ItemStack(ModItems.PSY_RECEPTACLE.get());
            case WIRE -> new ItemStack(ModItems.INSULATED_WIRE.get(),
                    Config.SERVER.crudeWireOutputCount.get());
            case INTEGRATION_CORE -> new ItemStack(ModItems.INTEGRATION_CORE.get());
        };
    }

    private static @Nullable DrugId findDrug(NonNullList<ItemStack> items) {
        for (int slot : INPUT_SLOTS) {
            ItemStack stack = items.get(slot);
            if (stack.getItem() instanceof DrugItem drugItem) {
                List<DrugModel> models = drugItem.getDrugModels(stack);
                return models.isEmpty() ? null : models.get(0).getId();
            }
        }
        return null;
    }

    /** Consumes the player's experience: every level they hold, capped at {@code maxLevels}. */
    private static void consumeExperience(ServerPlayer player, int maxLevels) {
        int originalLevels = player.experienceLevel;
        int consumed = RecoveryRitualLogic.levelsConsumed(originalLevels, maxLevels);
        if (consumed <= 0) {
            return;
        }
        player.giveExperienceLevels(-consumed);
        if (consumed >= originalLevels && player.experienceProgress > 0.0F) {
            int residualPoints = Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
            if (residualPoints > 0) {
                player.giveExperiencePoints(-residualPoints);
            }
        }
    }

    private static void giveItem(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
