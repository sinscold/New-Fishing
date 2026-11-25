package com.sinscold.newfishing.item;

import java.util.List;

import com.sinscold.newfishing.compat.CuriosCompat;
import com.sinscold.newfishing.registry.ModDataComponents;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * Сумка для наживки (пояс Curios): 3 слота под наживку, в каждом стопка любой наживки (одинаковой или разной).
 * Содержимое — компонент `newfishing:bait_bag_contents`. Пока сумка надета, пустой слот наживки удочки
 * пополняется из сумки (первый непустой слот) при забросе и когда наживка потратилась.
 */
public final class BaitBag {
    public static final int SLOTS = 3;

    /** Содержимое сумки для подсказки (3 слота). */
    public record Contents(List<ItemStack> items) implements TooltipComponent {}

    public static NonNullList<ItemStack> contents(ItemStack bag) {
        NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        bag.getOrDefault(ModDataComponents.BAIT_BAG_CONTENTS.get(), ItemContainerContents.EMPTY).copyInto(items);
        return items;
    }

    private static void store(ItemStack bag, NonNullList<ItemStack> items) {
        bag.set(ModDataComponents.BAIT_BAG_CONTENTS.get(), ItemContainerContents.fromItems(items));
    }

    /** Кладёт наживку с курсора: сначала докладывает к такой же, потом в пустой слот. Возвращает true, если что-то легло. */
    public static boolean insert(ItemStack bag, ItemStack carried) {
        if (!RodMods.isBait(carried)) return false;
        NonNullList<ItemStack> items = contents(bag);
        int before = carried.getCount();
        for (int i = 0; i < SLOTS && !carried.isEmpty(); i++) {
            ItemStack slot = items.get(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, carried) && slot.getCount() < slot.getMaxStackSize()) {
                int moved = Math.min(carried.getCount(), slot.getMaxStackSize() - slot.getCount());
                items.set(i, slot.copyWithCount(slot.getCount() + moved));
                carried.shrink(moved);
            }
        }
        for (int i = 0; i < SLOTS && !carried.isEmpty(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, carried.split(carried.getCount()));
            }
        }
        if (carried.getCount() == before) return false;
        store(bag, items);
        return true;
    }

    /** Достаёт стопку из последнего непустого слота. */
    public static ItemStack removeLast(ItemStack bag) {
        NonNullList<ItemStack> items = contents(bag);
        for (int i = SLOTS - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                ItemStack removed = items.get(i);
                items.set(i, ItemStack.EMPTY);
                store(bag, items);
                return removed;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Одна наживка из первого непустого слота (или пусто). */
    private static ItemStack takeOne(ItemStack bag) {
        NonNullList<ItemStack> items = contents(bag);
        for (int i = 0; i < SLOTS; i++) {
            if (!items.get(i).isEmpty()) {
                ItemStack one = items.get(i).copyWithCount(1);
                items.set(i, items.get(i).copyWithCount(items.get(i).getCount() - 1));
                store(bag, items);
                return one;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Если в удочке крючок, а слот наживки пуст и на игроке сумка — кладёт в удочку одну наживку из сумки. */
    public static void refillRod(Player player, ItemStack rod) {
        RodMods mods = RodMods.get(rod);
        if (mods.hasBait() || !mods.hasFishingHook()) return;
        ItemStack bag = CuriosCompat.findBaitBag(player);
        if (bag.isEmpty()) return;
        ItemStack bait = takeOne(bag);
        if (!bait.isEmpty()) {
            RodMods.set(rod, mods.withBait(bait));
        }
    }

    private BaitBag() {}
}
