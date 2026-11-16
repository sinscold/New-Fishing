package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.client.renderer.entity.FishingHookRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import com.sinscold.newfishing.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

/** Клиентские события мод-шины. */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    static void onRegisterTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(RodMods.class, ClientRodModsTooltip::new);
    }

    /** Лавовый поплавок рисуется ванильным рендерером (своя 3D-модель — позже). */
    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.LAVA_FISHING_HOOK.get(), FishingHookRenderer::new);
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
