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
            com.sinscold.newfishing.item.LineBreak.burn(hook.getPlayerOwner(), hook, !hasNetheriteLine(hook.getPlayerOwner()));
        } else if (event.getEntity() instanceof FishingHook hook && !(hook instanceof NetheriteFishingHook)
                && hook.level() instanceof ServerLevel && hook.tickCount > 2 && hook.tickCount % 10 == 0) {
            // из удочки вытащили леску или крючок во время рыбалки: рыбалка отменяется
            net.minecraft.world.entity.player.Player owner = hook.getPlayerOwner();
            if (owner != null && !com.sinscold.newfishing.item.RodMods.holdsFishableRod(owner)) {
                hook.discard();
            }
        }
    }

    private static boolean hasNetheriteLine(net.minecraft.world.entity.player.Player player) {
        if (player == null) return false;
        net.minecraft.world.item.ItemStack rod = player.getMainHandItem().is(net.minecraft.world.item.Items.FISHING_ROD) ? player.getMainHandItem() : player.getOffhandItem();
        return rod.is(net.minecraft.world.item.Items.FISHING_ROD) && com.sinscold.newfishing.item.RodMods.get(rod).hasNetheriteLine();
    }

    private VanillaHookLavaEvents() {}
}
