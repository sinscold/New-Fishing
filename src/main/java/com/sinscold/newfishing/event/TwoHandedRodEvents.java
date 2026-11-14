package com.sinscold.newfishing.event;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

/**
 * Удочка двуручная: пока она в одной руке, вторая рука ничего не использует
 * (ни предметы, ни блоки, ни существ).
 */
@EventBusSubscriber(modid = NewFishing.MODID)
public final class TwoHandedRodEvents {
    private static boolean isRod(ItemStack stack) {
        return stack.is(Items.FISHING_ROD);
    }

    /** Эта рука блокируется, если удочка лежит в другой руке. */
    private static boolean blocked(Player player, InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND
                ? isRod(player.getMainHandItem())
                : isRod(player.getOffhandItem());
    }

    /** Блок во второй руке: FAIL, чтобы остановить цепочку; блок основной руки (удочка в левой): PASS, чтобы сработала левая. */
    private static InteractionResult resultFor(InteractionHand hand) {
        return hand == InteractionHand.OFF_HAND ? InteractionResult.FAIL : InteractionResult.PASS;
    }

    @SubscribeEvent
    static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (blocked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(resultFor(event.getHand()));
        }
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (blocked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(resultFor(event.getHand()));
        }
    }

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (blocked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(resultFor(event.getHand()));
        }
    }

    @SubscribeEvent
    static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (blocked(event.getEntity(), event.getHand())) {
            event.setCanceled(true);
            event.setCancellationResult(resultFor(event.getHand()));
        }
    }

    private TwoHandedRodEvents() {}
}
