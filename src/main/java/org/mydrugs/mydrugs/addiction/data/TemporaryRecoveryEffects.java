package org.mydrugs.mydrugs.addiction.data;

import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public final class TemporaryRecoveryEffects implements ValueIOSerializable {
    private long diaryCalmUntil;
    private long calmingMixtureUntil;
    private long headphonesUntil;
    private boolean headphonesEnabled;
    private int headphonesTrackNonce;
    private long thoughtSuppressionUntil;
    private long sleepBonusUntil;
    private long preparedTeaUntil;
    private long recoveryMomentumUntil;
    private int recoveryMomentumCharges;

    @Override
    public void serialize(ValueOutput output) {
        output.putLong("diary_calm_until", diaryCalmUntil);
        output.putLong("calming_mixture_until", calmingMixtureUntil);
        output.putLong("headphones_until", headphonesUntil);
        output.putLong("thought_suppression_until", thoughtSuppressionUntil);
        output.putLong("sleep_bonus_until", sleepBonusUntil);
        output.putLong("prepared_tea_until", preparedTeaUntil);
        output.putLong("recovery_momentum_until", recoveryMomentumUntil);
        output.putInt("recovery_momentum_charges", recoveryMomentumCharges);
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
        preparedTeaUntil = input.getLongOr("prepared_tea_until", 0L);
        recoveryMomentumUntil = input.getLongOr("recovery_momentum_until", 0L);
        recoveryMomentumCharges = input.getIntOr("recovery_momentum_charges", 0);
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
        copy.preparedTeaUntil = preparedTeaUntil;
        copy.recoveryMomentumUntil = recoveryMomentumUntil;
        copy.recoveryMomentumCharges = recoveryMomentumCharges;
        copy.headphonesEnabled = headphonesEnabled;
        copy.headphonesTrackNonce = headphonesTrackNonce;
        return copy;
    }

    public void applyDiary(long calmUntil, long suppressionUntil) {
        diaryCalmUntil = calmUntil;
        thoughtSuppressionUntil = suppressionUntil;
    }

    public void extendHeadphonesUntil(long until) {
        headphonesUntil = Math.max(headphonesUntil, until);
    }

    public boolean toggleHeadphones(long refreshUntil) {
        headphonesEnabled = !headphonesEnabled;
        headphonesUntil = headphonesEnabled ? refreshUntil : 0L;
        return headphonesEnabled;
    }

    public void setHeadphonesPlaying(boolean playing, long refreshUntil) {
        headphonesEnabled = playing;
        headphonesUntil = playing ? refreshUntil : 0L;
    }

    public void disableHeadphones() {
        headphonesEnabled = false;
        headphonesUntil = 0L;
    }

    public void advanceHeadphonesTrack() {
        headphonesTrackNonce++;
    }

    public boolean headphonesEnabled() {
        return headphonesEnabled;
    }

    public long headphonesUntil() {
        return headphonesUntil;
    }

    public int headphonesTrackNonce() {
        return headphonesTrackNonce;
    }

    public void setSleepBonusUntil(long until) {
        sleepBonusUntil = until;
    }

    public void extendPreparedTeaUntil(long until) {
        preparedTeaUntil = Math.max(preparedTeaUntil, until);
    }

    public void setCalmingMixtureUntil(long until) {
        calmingMixtureUntil = until;
    }

    public void grantRecoveryMomentum(long until, int charges) {
        recoveryMomentumUntil = Math.max(recoveryMomentumUntil, until);
        recoveryMomentumCharges = Math.max(recoveryMomentumCharges, charges);
    }

    public boolean consumeRecoveryMomentum(long gameTime) {
        if (!hasRecoveryMomentum(gameTime)) {
            return false;
        }
        recoveryMomentumCharges = Math.max(0, recoveryMomentumCharges - 1);
        if (recoveryMomentumCharges == 0) {
            recoveryMomentumUntil = 0L;
        }
        return true;
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

    public boolean hasPreparedTea(long gameTime) {
        return preparedTeaUntil > gameTime;
    }

    public boolean hasRecoveryMomentum(long gameTime) {
        return recoveryMomentumUntil > gameTime && recoveryMomentumCharges > 0;
    }
}
