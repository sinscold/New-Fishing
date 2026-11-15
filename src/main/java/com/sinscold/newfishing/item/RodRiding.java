package com.sinscold.newfishing.item;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Управление свиньёй/лавоходом: удочка в руке с морковкой/грибом на палке в слоте крючка. */
public final class RodRiding {
    public static boolean holdsRidingStick(Player player, Item stick) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack held = player.getItemInHand(hand);
            if (held.is(Items.FISHING_ROD) && RodMods.get(held).hook().is(stick)) {
                return true;
            }
        }
        return false;
    }

    private RodRiding() {}
}
