package org.mydrugs.mydrugs.progression;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemLore;
import org.mydrugs.mydrugs.advancement.AdvancementEventHooks;
import org.mydrugs.mydrugs.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.items.ModItems;

import java.util.List;
import java.util.Set;

public final class PsyKnowledgeManager {
    private PsyKnowledgeManager() {
    }

    public static boolean has(ServerPlayer player, PsyKnowledgeKey key) {
        return player.getData(ModAttachments.PLAYER_PSY_KNOWLEDGE.get()).has(key);
    }

    public static boolean grant(ServerPlayer player, PsyKnowledgeKey key) {
        boolean granted = player.getData(ModAttachments.PLAYER_PSY_KNOWLEDGE.get()).grant(key);
        if (!granted) {
            return false;
        }

        AdvancementEventHooks.psyKnowledgeUnlocked(player, key);
        player.displayClientMessage(Component.translatable(messageKey(key)), true);
        player.playNotifySound(SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.75F, pitch(key));

        if (key.equals(PsyKnowledgeKey.CAFFEINE)) {
            awardReceptacle(player);
        }
        if (key.equals(PsyKnowledgeKey.FERMENTED)) {
            awardFermentedWire(player);
        }
        return true;
    }

    public static Set<PsyKnowledgeKey> getKnown(ServerPlayer player) {
        return player.getData(ModAttachments.PLAYER_PSY_KNOWLEDGE.get()).getKnown();
    }

    public static boolean hasPrerequisiteFor(ServerPlayer player, PsyKnowledgeKey requiredKnowledge) {
        return has(player, requiredKnowledge);
    }

    private static void awardReceptacle(ServerPlayer player) {
        ItemStack stack = new ItemStack(ModItems.PSY_RECEPTACLE.get());
        if (player.getInventory().contains(stack)) {
            return;
        }

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void awardFermentedWire(ServerPlayer player) {
        ItemStack stack = new ItemStack(ModItems.INSULATED_WIRE.get(), 2);
        stack.set(DataComponents.LORE, new ItemLore(List.of(
                Component.translatable("tooltip.mydrugs.insulated_wire.fermented_reward.1").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.translatable("tooltip.mydrugs.insulated_wire.fermented_reward.2").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.translatable("tooltip.mydrugs.insulated_wire.fermented_reward.3").withStyle(ChatFormatting.LIGHT_PURPLE),
                Component.translatable("tooltip.mydrugs.insulated_wire.fermented_reward.4").withStyle(ChatFormatting.LIGHT_PURPLE)
        )));

        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static String messageKey(PsyKnowledgeKey key) {
        return "message.mydrugs.knowledge." + key.id().getPath();
    }

    private static float pitch(PsyKnowledgeKey key) {
        if (key.equals(PsyKnowledgeKey.CAFFEINE)) return 0.75F;
        if (key.equals(PsyKnowledgeKey.NICOTINIC)) return 0.8F;
        if (key.equals(PsyKnowledgeKey.CANNABINOID)) return 0.9F;
        if (key.equals(PsyKnowledgeKey.FERMENTED)) return 1.0F;
        if (key.equals(PsyKnowledgeKey.STIMULANT)) return 1.1F;
        if (key.equals(PsyKnowledgeKey.LYSERGIC)) return 1.2F;
        if (key.equals(PsyKnowledgeKey.OVERCLOCKED)) return 1.35F;
        if (key.equals(PsyKnowledgeKey.MYCELIAL)) return 1.5F;
        return 1.0F;
    }
}
