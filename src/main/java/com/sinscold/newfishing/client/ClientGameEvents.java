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
    /**
     * Сила рыбалки в подсказках. У наживки: «Когда на крючке:», у лески и крючка: «Когда в удочке:» и « Сила рыбалки: +N».
     * У удочки после блока «Когда в ведущей руке» строка « Сила рыбалки: N» — сумма деталей (сохранена при вкладывании),
     * чар, надетой сумки для наживки и эффекта «Удача». Подсказка строится только когда её показывают, не каждый тик.
     */
    @SubscribeEvent
    static void onItemTooltip(net.neoforged.neoforge.event.entity.player.ItemTooltipEvent event) {
        net.minecraft.world.item.ItemStack stack = event.getItemStack();
        java.util.List<net.minecraft.network.chat.Component> tooltip = event.getToolTip();
        if (stack.is(Items.FISHING_ROD)) {
            net.minecraft.world.entity.player.Player player = event.getEntity();
            boolean bag = player != null && com.sinscold.newfishing.compat.CuriosCompat.hasBaitBag(player);
            double power = com.sinscold.newfishing.item.FishingScore.score(player, stack, bag);
            net.minecraft.network.chat.Component line = net.minecraft.network.chat.Component.translatable("tooltip.newfishing.rod_power",
                    com.sinscold.newfishing.item.FishingScore.format(power)).withStyle(net.minecraft.ChatFormatting.DARK_GREEN);
            tooltip.add(afterMainHandBlock(tooltip), line);
            return;
        }
        String header;
        if (RodMods.isBait(stack)) {
            header = "tooltip.newfishing.bait_when_on_hook";
        } else if (RodMods.isLine(stack) || stack.is(com.sinscold.newfishing.registry.ModTags.ROD_HOOKS)) {
            header = "tooltip.newfishing.part_when_in_rod";
        } else {
            return;
        }
        double points = com.sinscold.newfishing.item.FishingScore.pointsOf(stack);
        // как у атрибутов в ванили: пустая строка, серый заголовок, зелёное значение
        tooltip.add(net.minecraft.network.chat.Component.empty());
        tooltip.add(net.minecraft.network.chat.Component.translatable(header).withStyle(net.minecraft.ChatFormatting.GRAY));
        tooltip.add(net.minecraft.network.chat.Component.translatable("tooltip.newfishing.bait_points",
                com.sinscold.newfishing.item.FishingScore.format(points)).withStyle(net.minecraft.ChatFormatting.DARK_GREEN));
    }

    /** Индекс сразу после строк блока «Когда в ведущей руке:» (или конец подсказки, если блока нет). */
    private static int afterMainHandBlock(java.util.List<net.minecraft.network.chat.Component> tooltip) {
        for (int i = 0; i < tooltip.size(); i++) {
            if (tooltip.get(i).getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents t
                    && t.getKey().equals("item.modifiers.mainhand")) {
                int end = i + 1;
                while (end < tooltip.size() && tooltip.get(end).getString().startsWith(" ")) {
                    end++;
                }
                return end;
            }
        }
        return tooltip.size();
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
