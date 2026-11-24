package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class ModTags {
    /** Что можно вставить в слот «леска». */
    public static final TagKey<Item> ROD_LINES = item("rod_lines");
    /** Настоящие рыболовные крючки: с ними удочку можно закинуть. */
    public static final TagKey<Item> ROD_HOOKS = item("rod_hooks");

    /** Приманки для езды: морковка и искажённый гриб вставляются в слот крючка вместо него (рыбачить с ними нельзя). */
    public static final TagKey<Item> ROD_RIDING_STICKS = item("rod_riding_sticks");

    /** Всё, что можно положить в слот наживки удочки. */
    public static final TagKey<Item> ROD_BAITS = item("rod_baits");
    /** Наживка по измерениям: верхний мир, Незер, Край (пустота). Наживка из `baits/any` работает везде. */
    public static final TagKey<Item> BAITS_OVERWORLD = item("baits/overworld");
    public static final TagKey<Item> BAITS_NETHER = item("baits/nether");
    public static final TagKey<Item> BAITS_END = item("baits/end");
    public static final TagKey<Item> BAITS_ANY = item("baits/any");

    /** Биомы с подозрительной землёй: холодные (под деревьями, личинки) и умеренные леса (по всему биому, черви). */
    public static final TagKey<net.minecraft.world.level.biome.Biome> GRUB_DIRT_BIOMES = biome("has_grub_dirt");
    public static final TagKey<net.minecraft.world.level.biome.Biome> WORM_DIRT_BIOMES = biome("has_worm_dirt");
    /** Жаркие биомы, где в деревьях попадаются выеденные брёвна (жуки). */
    public static final TagKey<net.minecraft.world.level.biome.Biome> EATEN_LOG_BIOMES = biome("has_eaten_logs");

    /** Рыба из Незера (для статистики улова и будущей торговли). */
    public static final TagKey<Item> NETHER_FISH = item("nether_fish");

    /** Рыба из пустоты Края. */
    public static final TagKey<Item> VOID_FISH = item("void_fish");
    /** Ножи (для разделки): ножи Farmer's Delight и, как запасной вариант, мечи. */
    public static final TagKey<Item> KNIVES = item("knives");

    /** Зажигалки для сигарет (в левой руке): огниво. */
    public static final TagKey<Item> LIGHTERS = item("lighters");

    private static TagKey<Item> item(String name) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, name));
    }

    private static TagKey<net.minecraft.world.level.biome.Biome> biome(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(NewFishing.MODID, name));
    }

    private ModTags() {}
}
