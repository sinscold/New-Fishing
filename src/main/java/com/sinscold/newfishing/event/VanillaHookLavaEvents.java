package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.entity.NetheriteFishingHook;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.projectile.FishingHook;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/** Обычный поплавок лаву не выдерживает: попав в лаву, он сгорает, рыбалка отменяется. */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class VanillaHookLavaEvents {
    @SubscribeEvent
    static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity() instanceof FishingHook hook && !(hook instanceof NetheriteFishingHook)
                && hook.level() instanceof ServerLevel level && hook.isInLava()) {
            level.playSound(null, hook.getX(), hook.getY(), hook.getZ(), SoundEvents.GENERIC_BURN, hook.getSoundSource(), 0.4F, 2.0F);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, hook.getX(), hook.getY() + 0.2, hook.getZ(), 6, 0.1, 0.1, 0.1, 0.02);
            hook.discard();
        }
    }

    private VanillaHookLavaEvents() {}
}
