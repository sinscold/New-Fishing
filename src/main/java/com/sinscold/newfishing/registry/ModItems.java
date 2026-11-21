package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.item.BlindEyeItem;
import com.sinscold.newfishing.item.VoidPikeFilletItem;

import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Все предметы мода. Этап 1: только простые предметы, логика (кейсы, удочка) придёт позже. */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewFishing.MODID);

    // --- Сумка для наживки (пояс Curios): предмет есть, только если установлен Curios ---
    public static final DeferredItem<Item> BAIT_BAG = net.neoforged.fml.ModList.get() != null && net.neoforged.fml.ModList.get().isLoaded("curios")
            ? ITEMS.registerSimpleItem("bait_bag", new Item.Properties().stacksTo(1))
            : null;

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

    // --- Рыба пустоты Края (ловится на фантомную леску) ---
    public static final DeferredItem<Item> PHANTOM_RAY = ITEMS.registerSimpleItem("phantom_ray");
    public static final DeferredItem<Item> VOID_PIKE = ITEMS.registerSimpleItem("void_pike");
    public static final DeferredItem<Item> VOID_PIKE_FILLET = ITEMS.register("void_pike_fillet",
            () -> new VoidPikeFilletItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.4f).alwaysEdible().build())));
    public static final DeferredItem<Item> VOID_PIKE_GUTS = ITEMS.registerSimpleItem("void_pike_guts");

    // --- Край ---
    public static final DeferredItem<Item> BLIND_EYE = ITEMS.register("blind_eye", () -> new BlindEyeItem(new Item.Properties()));
    /** Эндер-поплавок (слепое око + обычный крючок). Назначение уточняется (вопрос 26). */
    public static final DeferredItem<Item> ENDER_HOOK = ITEMS.registerSimpleItem("ender_hook");

    // --- Детали удочки (механика установки — этап 2) ---
    public static final DeferredItem<Item> FISHING_LINE = ITEMS.registerSimpleItem("fishing_line");
    public static final DeferredItem<Item> FISHING_HOOK = ITEMS.registerSimpleItem("fishing_hook");
    public static final DeferredItem<Item> NETHERITE_HOOK = ITEMS.registerSimpleItem("netherite_hook",
            new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> PHANTOM_LINE = ITEMS.registerSimpleItem("phantom_line");
    public static final DeferredItem<Item> NETHERITE_LINE = ITEMS.registerSimpleItem("netherite_line",
            new Item.Properties().fireResistant().rarity(Rarity.UNCOMMON));

    // --- Материалы для лески (рецепты в recipe/ и data/newfishing/recipe/) ---
    public static final DeferredItem<Item> GOLD_STRING = ITEMS.registerSimpleItem("gold_string");
    public static final DeferredItem<Item> NETHERITE_DUST = ITEMS.registerSimpleItem("netherite_dust",
            new Item.Properties().fireResistant());

    // --- Ящики (открытие по ПКМ — этап 5) ---
    public static final DeferredItem<BlockItem> WATER_CRATE = ITEMS.registerSimpleBlockItem("water_crate", ModBlocks.WATER_CRATE,
            new Item.Properties().stacksTo(16));
    public static final DeferredItem<BlockItem> LAVA_CRATE = ITEMS.registerSimpleBlockItem("lava_crate", ModBlocks.LAVA_CRATE,
            new Item.Properties().stacksTo(16).fireResistant());

    /** Запертые ящики: их и вылавливают; замок снимается ПКМ топором после установки. */
    public static final DeferredItem<Item> LOCKED_WATER_CRATE = ITEMS.register("locked_water_crate",
            () -> new com.sinscold.newfishing.item.LockedCrateItem(ModBlocks.WATER_CRATE.get(), new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> LOCKED_LAVA_CRATE = ITEMS.register("locked_lava_crate",
            () -> new com.sinscold.newfishing.item.LockedCrateItem(ModBlocks.LAVA_CRATE.get(), new Item.Properties().stacksTo(1).fireResistant()));

    /** Ящик пива (6 бутылок, как торт) и бутылка пива. */
    public static final DeferredItem<BlockItem> BEER_CRATE = ITEMS.registerSimpleBlockItem("beer_crate", ModBlocks.BEER_CRATE,
            new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> BEER_BOTTLE = ITEMS.register("beer_bottle",
            () -> new com.sinscold.newfishing.item.BeerBottleItem(new Item.Properties().stacksTo(16).craftRemainder(net.minecraft.world.item.Items.GLASS_BOTTLE)
                    .food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().build())));

    /** Пачка сигарет (секретный дроп из лавы): как мешок, 20 сигарет. Сигарета: стакаются свежие, надкуренная нет. */
    public static final DeferredItem<Item> CIGARETTE_PACK = ITEMS.register("cigarette_pack",
            () -> new com.sinscold.newfishing.item.CigarettePackItem(new Item.Properties().stacksTo(1).fireResistant()
                    .component(ModDataComponents.CIGARETTES.get(), 20)));
    public static final DeferredItem<Item> CIGARETTE = ITEMS.register("cigarette",
            () -> new com.sinscold.newfishing.item.CigaretteItem(new Item.Properties().stacksTo(20)
                    .food(new FoodProperties.Builder().nutrition(0).saturationModifier(0.0F).alwaysEdible().build())));

    private ModItems() {}
}
