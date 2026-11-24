package com.sinscold.newfishing.item;

import java.util.Map;

import com.sinscold.newfishing.registry.ModItems;
import com.sinscold.newfishing.registry.ModTags;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

/**
 * Очки рыбалки и наживка.
 * <p>
 * Очки складываются из деталей удочки, наживки, сумки для наживки, эффекта «Удача» и чар (приманка и везучий рыбак
 * по 0,3 за уровень). Чем больше очков, тем быстрее рыба подходит к поплавку и тем выше удача улова (больше рыбы и
 * сокровищ); чем меньше, тем больше мусора. Ванильные бонусы чар отдельно не применяются: они входят в очки.
 * <p>
 * Без подходящей наживки рыба к поплавку не плывёт. Какая наживка где работает, задают теги
 * `newfishing:baits/overworld`, `baits/nether`, `baits/end` (по измерению) и `baits/any` (везде).
 */
public final class FishingScore {
    /** Очки за предметы (детали удочки и наживка). Всё, чего здесь нет, даёт 0. */
    private static Map<Item, Double> points;

    /**
     * Очки, при которых удача улова нулевая (как в ванили без чар). Самое простое снаряжение (обычные леска, крючок и
     * наживка новичка) даёт 3 очка, то есть удачу −1: мусора больше. Максимум (около 12) даёт удачу около +3,4.
     */
    private static final double NEUTRAL_SCORE = 5.0;
    /** Сколько удачи лута даёт одно очко сверх нейтрального (и сколько отнимает ниже него). */
    private static final double LUCK_PER_POINT = 0.5;
    /** Ожидание рыбы: множитель = 1 − очки × это значение, но не меньше {@link #MIN_WAIT_FACTOR}. */
    private static final double WAIT_PER_POINT = 0.06;
    private static final double MIN_WAIT_FACTOR = 0.3;

    private static Map<Item, Double> points() {
        if (points == null) {
            points = Map.ofEntries(
                    // леска и крючок
                    Map.entry(ModItems.FISHING_HOOK.get(), 1.0),
                    Map.entry(ModItems.FISHING_LINE.get(), 1.0),
                    Map.entry(ModItems.NETHERITE_HOOK.get(), 2.0),
                    Map.entry(ModItems.NETHERITE_LINE.get(), 2.0),
                    Map.entry(ModItems.ENDER_HOOK.get(), 3.0),
                    Map.entry(ModItems.PHANTOM_LINE.get(), 3.0),
                    // наживка
                    Map.entry(ModItems.BEGINNER_BAIT.get(), 1.0),
                    Map.entry(ModItems.AMATEUR_BAIT.get(), 2.0), // в задании очков нет, взято среднее между новичком и профи
                    Map.entry(ModItems.PRO_BAIT.get(), 4.0),
                    Map.entry(ModItems.WORM.get(), 2.0),
                    Map.entry(ModItems.FAT_GRUB.get(), 2.0),
                    Map.entry(ModItems.BEETLE.get(), 2.0),
                    Map.entry(Items.WARPED_FUNGUS, 2.0),
                    Map.entry(Items.PORKCHOP, 1.0), // в задании очков нет
                    Map.entry(Items.CHORUS_FRUIT, 1.0), // в задании очков нет
                    Map.entry(Items.POPPED_CHORUS_FRUIT, 2.0), // в задании очков нет
                    Map.entry(ModItems.ENDERMITE.get(), 3.0));
        }
        return points;
    }

    public static double pointsOf(ItemStack stack) {
        return stack.isEmpty() ? 0.0 : points().getOrDefault(stack.getItem(), 0.0);
    }

    /** Очки рыбалки игрока с этой удочкой (считается один раз при забросе). */
    public static double score(Player player, ItemStack rod, ServerLevel level, boolean baitBag) {
        RodMods mods = RodMods.get(rod);
        double score = pointsOf(mods.line()) + pointsOf(mods.hook()) + pointsOf(mods.bait());
        if (baitBag) score += 1.0;
        if (player.hasEffect(MobEffects.LUCK)) score += 1.0;
        var enchantments = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        score += 0.3 * rod.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.LURE));
        score += 0.3 * rod.getEnchantmentLevel(enchantments.getOrThrow(Enchantments.LUCK_OF_THE_SEA));
        return score;
    }

    /** Удача лута для таблицы рыбалки: ниже нейтральных очков отрицательная (больше мусора), выше положительная. */
    public static float lootLuck(double score) {
        return (float) ((score - NEUTRAL_SCORE) * LUCK_PER_POINT);
    }

    /** Множитель времени ожидания рыбы. */
    public static double waitFactor(double score) {
        return Math.max(MIN_WAIT_FACTOR, 1.0 - score * WAIT_PER_POINT);
    }

    /** Работает ли эта наживка в этом измерении (наживка профи и тег `baits/any` работают везде). */
    public static boolean baitWorks(ItemStack bait, Level level) {
        if (bait.isEmpty()) return false;
        if (bait.is(ModTags.BAITS_ANY)) return true;
        ResourceKey<Level> dimension = level.dimension();
        if (dimension == Level.OVERWORLD) return bait.is(ModTags.BAITS_OVERWORLD);
        if (dimension == Level.NETHER) return bait.is(ModTags.BAITS_NETHER);
        if (dimension == Level.END) return bait.is(ModTags.BAITS_END);
        return false;
    }

    private FishingScore() {}
}
