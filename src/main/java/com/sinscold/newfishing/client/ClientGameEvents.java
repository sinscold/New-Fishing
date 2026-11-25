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
    /** Очки наживки зелёным в подсказке: «Очки приманки в рыбалке: N». */
    @SubscribeEvent
    static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        if (!RodMods.isBait(event.getItemStack())) return;
        double points = com.sinscold.newfishing.item.FishingScore.pointsOf(event.getItemStack());
        String text = points == Math.rint(points) ? String.valueOf((int) points) : String.valueOf(points);
        // как у атрибутов в ванили: пустая строка, серый заголовок, зелёное значение
        event.getToolTip().add(net.minecraft.network.chat.Component.empty());
        event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.newfishing.bait_when_on_hook")
                .withStyle(net.minecraft.ChatFormatting.GRAY));
        event.getToolTip().add(net.minecraft.network.chat.Component.translatable("tooltip.newfishing.bait_points", text)
                .withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
    }

    @SubscribeEvent
    static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        if (com.sinscold.newfishing.registry.ModItems.BAIT_BAG != null && event.getItemStack().is(com.sinscold.newfishing.registry.ModItems.BAIT_BAG.get())) {
            int index = Math.min(1, event.getTooltipElements().size());
            event.getTooltipElements().add(index, Either.right(new com.sinscold.newfishing.item.BaitBag.Contents(
                    com.sinscold.newfishing.item.BaitBag.contents(event.getItemStack()))));
            return;
        }
        if (!event.getItemStack().is(Items.FISHING_ROD)) return;
        // сразу под названием
        int index = Math.min(1, event.getTooltipElements().size());
        event.getTooltipElements().add(index, Either.right(RodMods.get(event.getItemStack())));
    }

    private ClientGameEvents() {}
}
