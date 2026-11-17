package com.sinscold.newfishing.item;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModSounds;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Обычная леска рвётся с шансом 1 к 7 при попытке вытащить поклёвку (рыбалка заканчивается, удочка цела). */
public final class LineBreak {
    public static final int ONE_IN = 7;

    private static ItemStack rodOf(Player player) {
        if (player.getMainHandItem().is(Items.FISHING_ROD)) return player.getMainHandItem();
        if (player.getOffhandItem().is(Items.FISHING_ROD)) return player.getOffhandItem();
        return ItemStack.EMPTY;
    }

    /** Нужно ли порвать леску сейчас (только обычная леска; незеритовая не рвётся). */
    public static boolean shouldSnap(Player player) {
        ItemStack rod = rodOf(player);
        if (rod.isEmpty()) return false;
        return RodMods.get(rod).line().is(ModItems.FISHING_LINE.get()) && player.getRandom().nextInt(ONE_IN) == 0;
    }

    /** Звук и сообщение (вызывать на сервере). */
    public static void announce(Player player, Entity hook) {
        player.level().playSound(null, hook.getX(), hook.getY(), hook.getZ(), ModSounds.LINE_TORN.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        player.displayClientMessage(Component.translatable("message.newfishing.line_torn"), true);
    }

    private LineBreak() {}
}
