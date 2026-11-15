package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

/** Клиентские события мод-шины. */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    static void onRegisterTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(RodMods.class, ClientRodModsTooltip::new);
    }

    /** Свойство модели удочки: какой спрайт показывать (см. RodMods.visualState). */
    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(Items.FISHING_ROD,
                ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "rod_state"),
                (stack, level, entity, seed) -> RodMods.get(stack).visualState()));
    }

    private ClientModEvents() {}
}
