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
import org.mydrugs.mydrugs.core.drug.DrugId;
import org.mydrugs.mydrugs.diary.DiaryEntry;
import org.mydrugs.mydrugs.diary.DiaryEntryType;
import org.mydrugs.mydrugs.diary.PlayerDiaryAttachment;
import org.mydrugs.mydrugs.items.ModItems;
import org.mydrugs.mydrugs.psyche.PsycheMapManager;

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
        PsycheMapManager.unlock(player, key.id(), "psy_knowledge");
        writeDiaryEntry(player, key);
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

    private static void writeDiaryEntry(ServerPlayer player, PsyKnowledgeKey key) {
        PlayerDiaryAttachment diary = player.getData(ModAttachments.PLAYER_DIARY);

        DrugId required = DrugProgressionGate.required(key);

        if (required == null) return;

        String text = diaryEntry(required);

        String cleaned = PlayerDiaryAttachment.sanitizeCustomContent(text);

        long gameTime = player.level().getGameTime();
        diary.append(new DiaryEntry(
                PlayerDiaryAttachment.currentDay(gameTime),
                gameTime,
                DiaryEntryType.AUTO,
                cleaned,
                "custom",
                required.serializedName()
        ));
    }

    private static String diaryEntry(DrugId id) {
        return switch (id) {
            case COFFEE -> "Gosh what happened ? What is this thing that popped into my inventory ? Seems magical. " +
                    "I seem to have more energy, but at what cost ?";
            case TOBACCO -> "Ahhhh what a relief ! Grinding this tobacco reminded me that small things matter. " +
                    "What if i separated these small sticky things from the cannabis leaves ? I should check the magical anvil thing.";
            case WEED -> "That was way more powerful than tobacco ! Oh my god ! " +
                    "I feel relaxed, but also more motivated to discover more of these substances. " +
                    "I need more material. For example, what if I smashed copper into this anvil ?";
            case ALCOHOL -> "WOWW what happened ? Everything was weird and I made this wire thing. " +
                    "For sure I mustn't waste it. Gosh I feel heavy now. Heaviness ? Wait I got an idea !";
            case HASH -> "Hey that was even more powerful than weed ! Alright. More power, I get it. " +
                    "Let's smash this steel !";
            case COCAINE -> "STFGWAOSN WHAT IS THAT ? I NEVER FELT THIS HAPPY ! Oh, it already ended. " +
                    "Now I'm sad. I want to take it again... But I know I should not. With all this energy I understood something. " +
                    "Energy is key. And copper drives it. Let's make those damn wires !";
            case LSD -> "Wow. Now that was something else. I saw patterns inside patterns, machines inside thoughts, " +
                    "and thoughts inside machines. Chemistry is not just mixing anymore. It is structure, rhythm, intention. " +
                    "I think I can build circuits that understand more than simple control.";
            case METH -> "I can hear everything. Too fast. Too clear. My hands want to move before I even think. " +
                    "This power is terrifying, but I understand it now: speed, pressure, heat, overclocking. " +
                    "If machines can be pushed past their limits... maybe I can too. I should look for mushrooms. Something is calling from below.";
            case MUSHROOMS -> "This was not like the others. It did not push me forward. It pulled me inward. " +
                    "I felt roots, spores, memories, and something ancient breathing through the ground. " +
                    "The world is connected by threads I could not see before. I need to build something that can resonate with them.";
            default -> "I don't know what was that but that was awesome !";
        };
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
