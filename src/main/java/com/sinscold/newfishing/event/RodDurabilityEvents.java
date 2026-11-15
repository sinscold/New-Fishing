package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

/** Удар удочкой по существу стоит 1 прочность (в ванили удочка при ударе не ломается). */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class RodDurabilityEvents {
    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getDirectEntity() instanceof Player player && !player.level().isClientSide) {
            ItemStack stack = player.getMainHandItem();
            if (stack.is(Items.FISHING_ROD)) {
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
        }
    }

    private RodDurabilityEvents() {}
}
