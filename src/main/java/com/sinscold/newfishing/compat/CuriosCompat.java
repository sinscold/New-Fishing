package com.sinscold.newfishing.compat;

import com.sinscold.newfishing.registry.ModItems;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.neoforged.fml.ModList;

/** Необязательная совместимость с Curios: «Сумка для наживки» в поясном слоте. Без Curios всё возвращает false. */
public final class CuriosCompat {
    private static final boolean LOADED = ModList.get() != null && ModList.get().isLoaded("curios");

    public static boolean isLoaded() {
        return LOADED;
    }

    /** Надета ли сумка для наживки (проверка при забросе, не каждый тик). */
    public static boolean hasBaitBag(Player player) {
        return LOADED && ModItems.BAIT_BAG != null && Impl.hasBag(player);
    }

    /** Надетая сумка для наживки (сам стек из слота Curios, изменения в нём сохраняются) или пустой стек. */
    public static ItemStack findBaitBag(Player player) {
        return LOADED && ModItems.BAIT_BAG != null ? Impl.findBag(player) : ItemStack.EMPTY;
    }

    /** Уровень «Везучего рыбака» на удочке: ванильный бонус удачи равен уровню. */
    public static int vanillaLuck(ServerLevel level, ItemStack rod) {
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        return rod.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.LUCK_OF_THE_SEA));
    }

    /** Отдельный класс: API Curios подгружается, только если мод стоит. */
    private static final class Impl {
        static boolean hasBag(Player player) {
            return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                    .map(inv -> inv.isEquipped(ModItems.BAIT_BAG.get()))
                    .orElse(false);
        }

        static ItemStack findBag(Player player) {
            return top.theillusivec4.curios.api.CuriosApi.getCuriosInventory(player)
                    .flatMap(inv -> inv.findFirstCurio(ModItems.BAIT_BAG.get()))
                    .map(top.theillusivec4.curios.api.SlotResult::stack)
                    .orElse(ItemStack.EMPTY);
        }
    }

    private CuriosCompat() {}
}
