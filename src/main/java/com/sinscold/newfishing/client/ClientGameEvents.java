package com.sinscold.newfishing.client;

import com.mojang.datafixers.util.Either;
import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;

/** Клиентские игровые события: слоты модификаций в подсказке удочки. */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class ClientGameEvents {
    @SubscribeEvent
    static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        if (!event.getItemStack().is(Items.FISHING_ROD)) return;
        // сразу под названием
        int index = Math.min(1, event.getTooltipElements().size());
        event.getTooltipElements().add(index, Either.right(RodMods.get(event.getItemStack())));
    }

    private ClientGameEvents() {}
}
