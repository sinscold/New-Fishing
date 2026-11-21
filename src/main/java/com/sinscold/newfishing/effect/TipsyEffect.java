package com.sinscold.newfishing.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** «Балдёж»: лёгкое покачивание камеры и тёплый фильтр (рисуются на клиенте, см. client/TipsyClient). Тика нет. */
public class TipsyEffect extends MobEffect {
    public TipsyEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        return false;
    }
}
