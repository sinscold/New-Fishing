package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

/** Клиентские события мод-шины. */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    static void onRegisterTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(RodMods.class, ClientRodModsTooltip::new);
    }

    private ClientModEvents() {}
}
