package com.sinscold.newfishing;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Настройки мода (файл config/newfishing-common.toml; в игре: Моды → New Fishing → Настройки, или мод Configured).
 * Шансы групп лута ящиков, шансы вылавливания ящиков и список отключённых предметов ящиков.
 */
public final class Config {
    /** Настраиваемый шанс (ключ используется в таблицах лута ящиков через условие newfishing:config_chance). */
    public record ChanceKey(String key, double def) {}

    /** Все настраиваемые шансы ящиков: «секция.название». Значения по умолчанию совпадают с JSON-таблицами. */
    public static final List<ChanceKey> CHANCES = List.of(
            new ChanceKey("water.planks", 1.0), new ChanceKey("water.rotten_flesh", 1.0), new ChanceKey("water.produce", 1.0),
            new ChanceKey("water.treasure_group", 1.0 / 6.0), new ChanceKey("water.leather_armor", 0.1),
            new ChanceKey("water.netherite_hook", 0.005), new ChanceKey("water.netherite_line", 0.005),
            new ChanceKey("lava.materials_1", 0.5), new ChanceKey("lava.materials_2", 0.25),
            new ChanceKey("lava.materials_3", 0.125), new ChanceKey("lava.materials_4", 0.0625),
            new ChanceKey("lava.food", 0.75),
            new ChanceKey("lava.gear", 0.5), new ChanceKey("lava.gear_1", 0.1875), new ChanceKey("lava.gear_2", 0.1875),
            new ChanceKey("lava.gear_3", 0.0625), new ChanceKey("lava.gear_4", 0.0625),
            new ChanceKey("lava.tools", 0.5), new ChanceKey("lava.tools_1", 0.4375), new ChanceKey("lava.tools_2", 0.0625));

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    private static final Map<String, ModConfigSpec.DoubleValue> VALUES = new LinkedHashMap<>();

    public static final ModConfigSpec.DoubleValue WATER_CRATE_CHANCE;
    public static final ModConfigSpec.DoubleValue LAVA_CRATE_CHANCE;
    public static final ModConfigSpec.DoubleValue BEER_CRATE_CHANCE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_CRATE_ITEMS;
    public static final ModConfigSpec SPEC;

    static {
        BUILDER.push("crate_catch");
        WATER_CRATE_CHANCE = BUILDER.comment("Шанс, что выловленное «сокровище» превратится в водяной ящик (иначе исчезнет).")
                .defineInRange("waterCrateChance", 1.0, 0.0, 1.0);
        LAVA_CRATE_CHANCE = BUILDER.comment("Шанс выловить лавовый ящик при любой удачной поклёвке в лаве.")
                .defineInRange("lavaCrateChance", 0.10, 0.0, 1.0);
        BEER_CRATE_CHANCE = BUILDER.comment("Шанс выловить ящик пива при любой удачной поклёвке в воде.")
                .defineInRange("beerCrateChance", 0.01, 0.0, 1.0);
        BUILDER.pop();

        BUILDER.comment("Шансы групп и подгрупп лута ящиков (0 = никогда, 1 = всегда).").push("crate_loot_chances");
        String section = null;
        for (ChanceKey chance : CHANCES) {
            int dot = chance.key().indexOf('.');
            String group = chance.key().substring(0, dot);
            if (!group.equals(section)) {
                if (section != null) BUILDER.pop();
                BUILDER.push(group);
                section = group;
            }
            VALUES.put(chance.key(), BUILDER.defineInRange(chance.key().substring(dot + 1), chance.def(), 0.0, 1.0));
        }
        BUILDER.pop();
        BUILDER.pop();

        DISABLED_CRATE_ITEMS = BUILDER.comment("Предметы, которые никогда не появляются в ящиках. Например: \"minecraft:saddle\".")
                .defineListAllowEmpty("disabledCrateItems", List.of(), () -> "minecraft:dirt", Config::validItem);
        SPEC = BUILDER.build();
    }

    private Config() {}

    private static boolean validItem(Object value) {
        return value instanceof String id && ResourceLocation.tryParse(id) != null;
    }

    /** Текущий шанс по ключу (если настройки ещё не загружены или ключа нет, значение по умолчанию). */
    public static double chance(String key, double fallback) {
        ModConfigSpec.DoubleValue value = VALUES.get(key);
        try {
            return value != null ? value.get() : fallback;
        } catch (IllegalStateException e) {
            return fallback;
        }
    }

    public static double waterCrateChance() {
        try {
            return WATER_CRATE_CHANCE.get();
        } catch (IllegalStateException e) {
            return 1.0;
        }
    }

    public static double beerCrateChance() {
        try {
            return BEER_CRATE_CHANCE.get();
        } catch (IllegalStateException e) {
            return 0.01;
        }
    }

    public static double lavaCrateChance() {
        try {
            return LAVA_CRATE_CHANCE.get();
        } catch (IllegalStateException e) {
            return 0.10;
        }
    }

    public static boolean isCrateItemDisabled(net.minecraft.world.item.ItemStack stack) {
        try {
            return DISABLED_CRATE_ITEMS.get().contains(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        } catch (IllegalStateException e) {
            return false;
        }
    }
}
