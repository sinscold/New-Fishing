package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.RodMods;

import net.minecraft.client.renderer.item.ItemProperties;
import com.sinscold.newfishing.registry.ModBlockEntities;
import com.sinscold.newfishing.registry.ModEntities;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import com.sinscold.newfishing.registry.ModItems;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;

/** Клиентские события мод-шины. */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class ClientModEvents {
    @SubscribeEvent
    static void onRegisterTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(RodMods.class, ClientRodModsTooltip::new);
    }

    /** Ящики в инвентаре и в руке рисуются моделью, а не спрайтом. */
    @SubscribeEvent
    static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private CrateItemRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new CrateItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), new net.minecraft.client.model.geom.EntityModelSet());
                }
                return renderer;
            }
        }, ModItems.WATER_CRATE.get(), ModItems.LAVA_CRATE.get());
    }

    /** Лавовый поплавок рисуется ванильным рендерером (своя 3D-модель — позже). */
    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.NETHERITE_FISHING_HOOK.get(), BobberRenderer::new);
        // ванильный поплавок тоже рисуется нашей 3D-моделью
        event.registerEntityRenderer(net.minecraft.world.entity.EntityType.FISHING_BOBBER, BobberRenderer::new);
        event.registerEntityRenderer(ModEntities.BLIND_EYE.get(), net.minecraft.client.renderer.entity.ThrownItemRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CRATE.get(), CrateRenderer::new);
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
