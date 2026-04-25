package org.mydrugs.mydrugs.effects.addiction.data;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class TemporaryRecoveryEffects implements ValueIOSerializable {
    public long diaryCalmUntil;
    public long calmingMixtureUntil;
    public long headphonesUntil;
    public boolean headphonesEnabled;
    public int headphonesTrackNonce;
    public long thoughtSuppressionUntil;
    public long sleepBonusUntil;

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("diary_calm_until", diaryCalmUntil);
        output.putLong("calming_mixture_until", calmingMixtureUntil);
        output.putLong("headphones_until", headphonesUntil);
        output.putLong("thought_suppression_until", thoughtSuppressionUntil);
        output.putLong("sleep_bonus_until", sleepBonusUntil);
        output.putBoolean("headphones_enabled", headphonesEnabled);
        output.putInt("headphones_track_nonce", headphonesTrackNonce);
    }

    @Override
    public void deserialize(ValueInput input) {
        diaryCalmUntil = input.getLongOr("diary_calm_until", 0L);
        calmingMixtureUntil = input.getLongOr("calming_mixture_until", 0L);
        headphonesUntil = input.getLongOr("headphones_until", 0L);
        thoughtSuppressionUntil = input.getLongOr("thought_suppression_until", 0L);
        sleepBonusUntil = input.getLongOr("sleep_bonus_until", 0L);
        headphonesEnabled = input.getBooleanOr("headphones_enabled", false);
        headphonesTrackNonce = input.getIntOr("headphones_track_nonce", 0);
    }

    public TemporaryRecoveryEffects copy() {
        TemporaryRecoveryEffects copy = new TemporaryRecoveryEffects();
        copy.diaryCalmUntil = diaryCalmUntil;
        copy.calmingMixtureUntil = calmingMixtureUntil;
        copy.headphonesUntil = headphonesUntil;
        copy.thoughtSuppressionUntil = thoughtSuppressionUntil;
        copy.sleepBonusUntil = sleepBonusUntil;
        copy.headphonesEnabled = headphonesEnabled;
        copy.headphonesTrackNonce = headphonesTrackNonce;
        return copy;
    }

    public boolean hasDiaryCalm(long gameTime) {
        return diaryCalmUntil > gameTime;
    }

    public boolean hasCalmingMixture(long gameTime) {
        return calmingMixtureUntil > gameTime;
    }

    public boolean hasCalmRelief(long gameTime) {
        return hasDiaryCalm(gameTime) || hasCalmingMixture(gameTime);
    }

    public boolean hasHeadphones(long gameTime) {
        return headphonesUntil > gameTime;
    }

    public boolean hasThoughtSuppression(long gameTime) {
        return thoughtSuppressionUntil > gameTime;
    }

    public boolean hasSleepBonus(long gameTime) {
        return sleepBonusUntil > gameTime;
    }
}