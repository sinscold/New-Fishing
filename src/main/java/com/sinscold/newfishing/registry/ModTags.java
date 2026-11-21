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

    private ModTags() {}
}
