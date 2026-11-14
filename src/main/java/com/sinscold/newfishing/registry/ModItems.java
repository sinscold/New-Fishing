package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Все предметы мода. Этап 1: только простые предметы, логика (кейсы, удочка) придёт позже. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewFishing.MODID);

    // --- Рыбы из лавы (не горят: их вылавливают из лавы) ---
    public static final DeferredItem<Item> LAVACARP = ITEMS.registerSimpleItem("lavacarp",
            new Item.Properties().fireResistant());
    public static final DeferredItem<Item> OBSIDIAN_CATFISH = ITEMS.registerSimpleItem("obsidian_catfish",
            new Item.Properties().fireResistant());

    // --- Филе сома (разделка ножом — этап 6; пока еда, решение можно поменять) ---
    public static final DeferredItem<Item> OBSIDIAN_CATFISH_FILLET = ITEMS.registerSimpleItem("obsidian_catfish_fillet",
            new Item.Properties().fireResistant()
                    .food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.1f).build()));
    public static final DeferredItem<Item> COOKED_OBSIDIAN_CATFISH_FILLET = ITEMS.registerSimpleItem("cooked_obsidian_catfish_fillet",
            new Item.Properties().fireResistant()
                    .food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.8f).build()));

    // --- Детали удочки (механика установки — этап 2) ---
    public static final DeferredItem<Item> FISHING_HOOK = ITEMS.registerSimpleItem("fishing_hook");
    public static final DeferredItem<Item> NETHERITE_HOOK = ITEMS.registerSimpleItem("netherite_hook",
            new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> NETHERITE_LINE = ITEMS.registerSimpleItem("netherite_line",
            new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));

    // --- Кейсы (открытие по ПКМ — этап 5) ---
    public static final DeferredItem<Item> WATER_CRATE = ITEMS.registerSimpleItem("water_crate",
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<Item> LAVA_CRATE = ITEMS.registerSimpleItem("lava_crate",
            new Item.Properties().stacksTo(16).fireResistant());

    private ModItems() {}
}
