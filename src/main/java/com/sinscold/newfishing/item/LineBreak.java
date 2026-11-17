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

    /** Поплавок попал в лаву без полного незеритового комплекта: леска (или поплавок) сгорает, рыбалка отменяется. */
    public static void burn(Player player, Entity hook, boolean lineBurned) {
        if (!(hook.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        level.playSound(null, hook.getX(), hook.getY(), hook.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_BURN, SoundSource.PLAYERS, 0.4F, 2.0F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.LARGE_SMOKE, hook.getX(), hook.getY() + 0.2, hook.getZ(), 6, 0.1, 0.1, 0.1, 0.02);
        if (player != null) {
            player.displayClientMessage(Component.translatable(lineBurned ? "message.newfishing.line_burned" : "message.newfishing.hook_burned"), true);
        }
        hook.discard();
    }

    private LineBreak() {}
}
