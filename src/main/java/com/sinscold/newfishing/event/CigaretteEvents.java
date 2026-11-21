package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.CigaretteItem;
import com.sinscold.newfishing.item.CigarettePackItem;
import com.sinscold.newfishing.registry.ModDataComponents;
import com.sinscold.newfishing.registry.ModItems;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ItemStackedOnOtherEvent;

/** Пачка сигарет в инвентаре, как мешок: ПКМ пустым курсором достаёт сигарету, ПКМ свежей сигаретой на курсоре кладёт обратно. */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class CigaretteEvents {
    @SubscribeEvent
    static void onStackedOnPack(ItemStackedOnOtherEvent event) {
        ItemStack pack = event.getStackedOnItem();
        if (!pack.is(ModItems.CIGARETTE_PACK.get())) return;
        if (event.getClickAction() != ClickAction.SECONDARY) return;
        Player player = event.getPlayer();
        if (!event.getSlot().allowModification(player)) return;

        ItemStack carried = event.getCarriedItem();
        int count = CigarettePackItem.count(pack);
        if (carried.isEmpty()) {
            if (count <= 0) return;
            event.getCarriedSlotAccess().set(new ItemStack(ModItems.CIGARETTE.get()));
            pack.set(ModDataComponents.CIGARETTES.get(), count - 1);
            player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        } else if (carried.is(ModItems.CIGARETTE.get()) && !CigaretteItem.isSmoked(carried) && count < CigarettePackItem.MAX) {
            // надкуренная сигарета в пачку не возвращается
            carried.shrink(1);
            pack.set(ModDataComponents.CIGARETTES.get(), count + 1);
            player.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + player.level().getRandom().nextFloat() * 0.4F);
        } else {
            return;
        }
        event.setCanceled(true);
    }

    private CigaretteEvents() {}
}
