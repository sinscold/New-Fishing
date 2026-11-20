package com.sinscold.newfishing.loot;

import java.util.List;

import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

/**
 * ТАБЛИЦА БИОМОВ РЫБАЛКИ. Здесь меняются шансы и особые предметы.
 *
 * Биом поплавка задаёт шансы {рыба, мусор, сокровище} (в процентах, сумма не обязана быть 100).
 * Биом игрока добавляет бонус к сокровищу и особый мусор/рыбу. Сокровище превращается в водяной ящик.
 * Удача моря (luck) двигает шансы так же, как в ванили: рыба −1, мусор −2, сокровище +2 за уровень.
 */
public final class FishingBiomes {
    // {рыба, мусор, сокровище}
    private static final double[] RIVER = {88, 8, 4};
    private static final double[] OCEAN = {85, 9, 6};
    private static final double[] DEEP_OCEAN = {80, 10, 10};
    private static final double[] WARM_OCEAN = {85, 7, 8};
    private static final double[] SWAMP = {70, 25, 5};
    private static final double[] SHORE = {82, 13, 5};
    private static final double[] MUSHROOM = {75, 10, 15};
    private static final double[] DEFAULT = {85, 10, 5};

    /** Бонус к сокровищу от биома игрока (в процентных пунктах). */
    private static final double PLAYER_MUSHROOM_TREASURE_BONUS = 3.0;
    /** Шанс заменить мусор / рыбу особым предметом биома игрока. */
    public static final double PLAYER_BIOME_ITEM_CHANCE = 0.5;

    // особый мусор по биому игрока
    public static final List<Item> JUNGLE_JUNK = List.of(Items.BAMBOO, Items.COCOA_BEANS);
    public static final List<Item> TAIGA_JUNK = List.of(Items.SPRUCE_PLANKS, Items.ICE);
    public static final List<Item> DESERT_JUNK = List.of(Items.CACTUS, Items.BONE);

    private FishingBiomes() {}

    /** Особая рыба для холодных биомов игрока (больше лосося). */
    public static final Item COLD_FISH = Items.SALMON;

    public static double[] weights(Holder<Biome> floatBiome, Holder<Biome> playerBiome, float luck) {
        double[] base = pick(floatBiome);
        double fish = base[0] - luck;
        double junk = Math.max(0.0, base[1] - 2.0 * luck);
        double treasure = base[2] + 2.0 * luck + (playerBiome.is(Biomes.MUSHROOM_FIELDS) ? PLAYER_MUSHROOM_TREASURE_BONUS : 0.0);
        return new double[]{Math.max(0.0, fish), junk, treasure};
    }

    private static double[] pick(Holder<Biome> biome) {
        if (biome.is(Biomes.MUSHROOM_FIELDS)) return MUSHROOM;
        if (biome.is(Biomes.SWAMP) || biome.is(Biomes.MANGROVE_SWAMP)) return SWAMP;
        if (biome.is(BiomeTags.IS_RIVER)) return RIVER; // включает замёрзшую реку
        if (biome.is(BiomeTags.IS_DEEP_OCEAN)) return DEEP_OCEAN;
        if (biome.is(Biomes.WARM_OCEAN) || biome.is(Biomes.LUKEWARM_OCEAN)) return WARM_OCEAN;
        if (biome.is(BiomeTags.IS_OCEAN)) return OCEAN;
        if (biome.is(BiomeTags.IS_BEACH) || biome.is(Biomes.STONY_SHORE)) return SHORE;
        return DEFAULT;
    }

    public static boolean isJungle(Holder<Biome> b) {
        return b.is(BiomeTags.IS_JUNGLE);
    }

    public static boolean isCold(Holder<Biome> b) {
        return b.is(BiomeTags.IS_TAIGA) || b.is(Biomes.SNOWY_PLAINS) || b.is(Biomes.ICE_SPIKES) || b.is(Biomes.SNOWY_BEACH)
                || b.is(Biomes.FROZEN_RIVER) || b.is(Biomes.FROZEN_OCEAN) || b.is(Biomes.DEEP_FROZEN_OCEAN) || b.is(Biomes.GROVE)
                || b.is(Biomes.SNOWY_SLOPES) || b.is(Biomes.FROZEN_PEAKS);
    }

    public static boolean isDry(Holder<Biome> b) {
        return b.is(Biomes.DESERT) || b.is(BiomeTags.IS_SAVANNA);
    }
}
