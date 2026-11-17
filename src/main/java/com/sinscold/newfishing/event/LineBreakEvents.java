package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.LineBreak;

import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;

/** Обрыв обычной лески при вытаскивании улова обычным (водным) поплавком. */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class LineBreakEvents {
    @SubscribeEvent
    static void onItemFished(ItemFishedEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide || !LineBreak.shouldSnap(player)) return;
        event.damageRodBy(0);
        event.setCanceled(true);
        LineBreak.announce(player, event.getHookEntity());
    }

    private LineBreakEvents() {}
}
