package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.effect.ReturnEffect;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, NewFishing.MODID);

    /** Мгновенный эффект: возвращает на точку возрождения. */
    public static final DeferredHolder<MobEffect, ReturnEffect> RETURN =
            EFFECTS.register("return", () -> new ReturnEffect(MobEffectCategory.BENEFICIAL, 0x6A2CA0));

    private ModEffects() {}
}
