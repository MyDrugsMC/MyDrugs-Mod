package org.mydrugs.mydrugs.core;

public abstract class EffectPort {
    public void applyEffect(DrugEffect effect) {
        switch (effect.getEffect().getEffectType()) {
            case SHADER -> applyShader(effect);
            case INGAME_EFFECT -> applyIngameEffect(effect);
            case INGAME_PERMANENT_BUFF -> applyIngamePermanentBuff(effect);
            case null, default -> applyMisc(effect);
        }
    }

    protected abstract void applyShader(DrugEffect effect);

    protected abstract void applyIngameEffect(DrugEffect effect);

    protected abstract void applyIngamePermanentBuff(DrugEffect effect);

    protected abstract void applyMisc(DrugEffect effect);
}
