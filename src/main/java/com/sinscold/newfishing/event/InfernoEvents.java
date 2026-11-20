package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModEffects;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Удар носителя «Инферно» поджигает цель на 3 секунды и снимает с неё эффект огнестойкости. Срабатывает по событию удара. */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class InfernoEvents {
    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker == event.getEntity()) return;
        if (attacker.level().isClientSide || !attacker.hasEffect(ModEffects.INFERNO)) return;
        LivingEntity victim = event.getEntity();
        victim.removeEffect(MobEffects.FIRE_RESISTANCE);
        victim.igniteForSeconds(3.0F);
    }

    private InfernoEvents() {}
}
