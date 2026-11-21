package com.sinscold.newfishing.client;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.registry.ModEffects;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

/** «Балдёж»: лёгкое покачивание камеры (крен и небольшие наклоны) и тёплый оранжевый фильтр поверх экрана. */
@EventBusSubscriber(modid = NewFishing.MODID, value = Dist.CLIENT)
public final class TipsyClient {
    /** Сила эффекта: плавно спадает в последние 5 секунд. */
    private static float strength() {
        var player = Minecraft.getInstance().player;
        if (player == null) return 0.0F;
        MobEffectInstance effect = player.getEffect(ModEffects.TIPSY);
        return effect == null ? 0.0F : Math.min(1.0F, effect.getDuration() / 100.0F);
    }

    @SubscribeEvent
    static void onCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float strength = strength();
        if (strength <= 0.0F) return;
        var player = Minecraft.getInstance().player;
        double t = (player.tickCount + event.getPartialTick()) * 0.06;
        event.setRoll(event.getRoll() + (float) Math.sin(t) * 2.0F * strength);
        event.setPitch(event.getPitch() + (float) Math.sin(t * 0.7 + 1.0) * 0.8F * strength);
        event.setYaw(event.getYaw() + (float) Math.sin(t * 0.45) * 1.0F * strength);
    }

    @SubscribeEvent
    static void onRegisterLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, "tipsy_filter"), (graphics, delta) -> {
            float strength = strength();
            if (strength <= 0.0F) return;
            int alpha = (int) (0x34 * strength);
            graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), (alpha << 24) | 0xFF9A2E);
        });
    }

    private TipsyClient() {}
}
