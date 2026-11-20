package com.sinscold.newfishing.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Эффект «Инферно»: удары носителя поджигают цель (логика в event/InfernoEvents), сам эффект без тика. */
public class InfernoEffect extends MobEffect {
    public InfernoEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
