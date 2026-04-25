package org.mydrugs.mydrugs.effects.addiction.manager;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.mydrugs.mydrugs.core.drug.DrugCategory;
import org.mydrugs.mydrugs.effects.addiction.attachment.ModAttachments;
import org.mydrugs.mydrugs.effects.addiction.data.PlayerAddictionStats;
import org.mydrugs.mydrugs.items.ModItems;

public final class WithdrawalHintManager {
    private static final String[] SAFE_ZONE_HINTS = {
            "You feel safer here. Staying near a recovery anchor helps calm the withdrawal.",
            "This place steadies you. Resting near an anchor makes the symptoms easier to bear.",
            "The anchor helps your body settle. Staying here for a while will make this easier."
    };
    private static final String[] FOOD_HAVE_HINTS = {
            "You should eat something. Food will help you hold yourself together.",
            "You need fuel. Eat now before the withdrawal gets heavier.",
            "Your body needs food. A proper meal will make this easier to endure."
    };
    private static final String[] FOOD_NEED_HINTS = {
            "You are running on empty. Find something to eat as soon as you can.",
            "Hunger is making this worse. You need food.",
            "Your body is starving. Try to find anything to eat."
    };
    private static final String[] HEALTH_HINTS = {
            "Your body is exhausted. Recovering health will make the stress easier to bear.",
            "You are badly hurt. Healing up will help you resist the withdrawal.",
            "Pain is making everything worse. Take care of your body first."
    };
    private static final String[] DIARY_HAVE_HINTS = {
            "Writing would help right now. Use your diary and get the thoughts out.",
            "Your mind is spiraling. Writing in your diary could steady you.",
            "Put it into words. Your diary can help you calm down."
    };
    private static final String[] DIARY_NEED_HINTS = {
            "Writing things down can help organize the noise in your head.",
            "Putting your thoughts into words would help you steady yourself.",
            "Journaling could help you regain control for a while."
    };
    private static final String[] HEADPHONES_HAVE_HINTS = {
            "Put your headphones on. Music could calm the stress a little.",
            "Your headphones could help right now. A familiar track might steady you.",
            "Try your headphones. Music can make the stress easier to endure."
    };
    private static final String[] HEADPHONES_NEED_HINTS = {
            "A little music could help quiet the stress.",
            "Something familiar to listen to would help you calm down.",
            "Calming music might make the withdrawal easier to bear."
    };
    private static final String[] SOCIAL_HINTS = {
            "Being around others helps. Isolation makes withdrawal harder.",
            "You do better with people nearby. Do not stay alone too long.",
            "Companionship helps steady you. Others can make this easier to endure."
    };
    private static final String[] SLEEP_HINTS = {
            "Withdrawal is keeping you awake. Lowering the stress may help you sleep later.",
            "Your body wants rest, but the withdrawal is fighting it. Calm down first if you can.",
            "You are too restless to sleep right now. Try to settle yourself before trying again."
    };
    private static final String[] GENERAL_HINTS = {
            "Small things help: food, rest, safety, music, writing, and staying near others.",
            "You can soften this a little: eat, calm down, stay safe, and do not isolate yourself.",
            "This will not vanish instantly, but food, rest, company, and grounding habits all help.",
            "Take care of the basics first. Food, safety, rest, and comfort all matter right now."
    };

    private WithdrawalHintManager() {
    }

    public static void tick(ServerPlayer player, float globalSeverity, boolean inSafeZone, int companions) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();

        float pressure = computeHintPressure(stats, globalSeverity);

        // No real withdrawal -> no hints.
        if (pressure < 0.12F) {
            stats.wasInSafeZoneLastTick = inSafeZone;
            if (!inSafeZone) {
                stats.anchorHintShownThisVisit = false;
            }
            return;
        }

        if (inSafeZone && !stats.wasInSafeZoneLastTick) {
            stats.anchorHintShownThisVisit = false;
        } else if (!inSafeZone) {
            stats.anchorHintShownThisVisit = false;
        }
        stats.wasInSafeZoneLastTick = inSafeZone;

        long urgentCooldown = (long) Mth.lerp(pressure, 20.0F * 28.0F, 20.0F * 7.0F);
        long topicCooldown = (long) Mth.lerp(pressure, 20.0F * 40.0F, 20.0F * 10.0F);
        long generalCooldown = (long) Mth.lerp(pressure, 20.0F * 65.0F, 20.0F * 18.0F);

        boolean movedEnoughSinceLastHint = player.position().distanceToSqr(
                stats.lastHintX, stats.lastHintY, stats.lastHintZ
        ) >= 9.0D;

        int foodLevel = player.getFoodData().getFoodLevel();
        float healthRatio = player.getHealth() / player.getMaxHealth();

        boolean hungry = foodLevel <= 6;
        boolean lowHealth = healthRatio <= 0.50F;
        boolean severe = pressure >= 0.60F;
        boolean hasFood = hasEdibleFood(player);
        boolean hasDiary = hasItem(player, ModItems.PERSONAL_DIARY.get());
        boolean hasHeadphones = hasItem(player, ModItems.HEADPHONES.get());
        boolean diaryActive = stats.temporaryEffects.hasCalmRelief(now);
        boolean headphonesActive = stats.temporaryEffects.hasHeadphones(now);
        boolean night = isNight(player);

        // Anchor hint once per visit only.
        if (inSafeZone && !stats.anchorHintShownThisVisit && now - stats.lastAnchorHintTick >= topicCooldown) {
            sendHint(player, stats, HintTopic.SAFE_ZONE, SAFE_ZONE_HINTS);
            stats.anchorHintShownThisVisit = true;
            stats.lastAnchorHintTick = now;
            return;
        }

        HintTopic bestTopic = null;
        String[] bestVariants = null;
        float bestScore = -999.0F;

        // Food stays important even if the player has none.
        if (hungry && now - stats.lastFoodHintTick >= urgentCooldown) {
            float score = 1.05F + pressure * 0.35F + (hasFood ? 0.20F : 0.0F);
            if (score > bestScore) {
                bestScore = score;
                bestTopic = HintTopic.FOOD;
                bestVariants = hasFood ? FOOD_HAVE_HINTS : FOOD_NEED_HINTS;
            }
        }

        if (lowHealth && now - stats.lastHealthHintTick >= urgentCooldown) {
            float score = 1.00F + pressure * 0.30F;
            if (score > bestScore) {
                bestScore = score;
                bestTopic = HintTopic.HEALTH;
                bestVariants = HEALTH_HINTS;
            }
        }

        if (severe && !diaryActive && now - stats.lastDiaryHintTick >= topicCooldown) {
            float score = 0.55F + pressure * 0.25F + (hasDiary ? 0.30F : 0.0F);
            if (score > bestScore) {
                bestScore = score;
                bestTopic = HintTopic.DIARY;
                bestVariants = hasDiary ? DIARY_HAVE_HINTS : DIARY_NEED_HINTS;
            }
        }

        if (pressure >= 0.35F && !headphonesActive && now - stats.lastHeadphonesHintTick >= topicCooldown) {
            float score = 0.52F + pressure * 0.22F + (hasHeadphones ? 0.30F : 0.0F);
            if (score > bestScore) {
                bestScore = score;
                bestTopic = HintTopic.HEADPHONES;
                bestVariants = hasHeadphones ? HEADPHONES_HAVE_HINTS : HEADPHONES_NEED_HINTS;
            }
        }

        if (companions <= 0 && severe && now - stats.lastSocialHintTick >= topicCooldown) {
            float score = 0.50F + pressure * 0.20F;
            if (score > bestScore) {
                bestScore = score;
                bestTopic = HintTopic.SOCIAL;
                bestVariants = SOCIAL_HINTS;
            }
        }

        if (night && severe && now - stats.lastSleepHintTick >= topicCooldown) {
            float score = 0.46F + pressure * 0.20F;
            if (score > bestScore) {
                bestScore = score;
                bestTopic = HintTopic.SLEEP;
                bestVariants = SLEEP_HINTS;
            }
        }

        if (bestTopic != null) {
            sendHint(player, stats, bestTopic, bestVariants);
            markTopicTick(stats, bestTopic, now);
            return;
        }

        if ((movedEnoughSinceLastHint || severe) && now - stats.lastHintTick >= generalCooldown) {
            sendHint(player, stats, HintTopic.GENERAL, GENERAL_HINTS);
        }
    }

    public static void onSleepBlocked(ServerPlayer player, float globalSeverity) {
        PlayerAddictionStats stats = player.getData(ModAttachments.PLAYER_ADDICTION.get());
        long now = player.level().getGameTime();
        float pressure = computeHintPressure(stats, globalSeverity);

        if (pressure < 0.12F) {
            return;
        }

        long sleepCooldown = (long) Mth.lerp(pressure, 20.0F * 30.0F, 20.0F * 8.0F);
        if (now - stats.lastSleepHintTick < sleepCooldown) {
            return;
        }

        sendHint(player, stats, HintTopic.SLEEP, SLEEP_HINTS);
        stats.lastSleepHintTick = now;
    }

    private static void markTopicTick(PlayerAddictionStats stats, HintTopic topic, long now) {
        switch (topic) {
            case FOOD -> stats.lastFoodHintTick = now;
            case HEALTH -> stats.lastHealthHintTick = now;
            case DIARY -> stats.lastDiaryHintTick = now;
            case HEADPHONES -> stats.lastHeadphonesHintTick = now;
            case SOCIAL -> stats.lastSocialHintTick = now;
            case SLEEP -> stats.lastSleepHintTick = now;
            case SAFE_ZONE -> stats.lastAnchorHintTick = now;
            case GENERAL -> {
            }
        }
    }

    private static float computeHintPressure(PlayerAddictionStats stats, float globalSeverity) {
        float meterPressure = Mth.clamp(stats.getMaxWithdrawalMeter() / 100.0F, 0.0F, 1.0F);
        return Math.max(globalSeverity, meterPressure);
    }

    private static boolean hasEdibleFood(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.has(DataComponents.FOOD)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNight(ServerPlayer player) {
        long time = player.level().getDayTime() % 24000L;
        return time >= 13000L && time < 23000L;
    }

    private static void sendHint(ServerPlayer player, PlayerAddictionStats stats, HintTopic topic, String[] variants) {
        int variantIndex = pickVariant(player, stats, topic, variants.length);

        player.displayClientMessage(Component.literal(variants[variantIndex]), true);

        stats.lastHintTick = player.level().getGameTime();
        stats.lastHintX = player.getX();
        stats.lastHintY = player.getY();
        stats.lastHintZ = player.getZ();
        stats.lastHintTopicOrdinal = topic.ordinal();
        stats.lastHintVariantIndex = variantIndex;
    }

    private static int pickVariant(ServerPlayer player, PlayerAddictionStats stats, HintTopic topic, int count) {
        if (count <= 1) {
            return 0;
        }

        int chosen = player.getRandom().nextInt(count);

        if (stats.lastHintTopicOrdinal == topic.ordinal() && chosen == stats.lastHintVariantIndex) {
            chosen = (chosen + 1 + player.getRandom().nextInt(count - 1)) % count;
        }

        return chosen;
    }

    private enum HintTopic {
        SAFE_ZONE,
        FOOD,
        HEALTH,
        DIARY,
        HEADPHONES,
        SOCIAL,
        SLEEP,
        GENERAL
    }
}