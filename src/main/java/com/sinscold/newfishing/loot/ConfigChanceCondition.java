package com.sinscold.newfishing.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sinscold.newfishing.Config;
import com.sinscold.newfishing.registry.ModLootConditions;

import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;

/** Условие таблицы лута: срабатывает с шансом, который игрок может менять в настройках (см. Config.CHANCES). */
public record ConfigChanceCondition(String key, double defaultChance) implements LootItemCondition {
    public static final MapCodec<ConfigChanceCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.STRING.fieldOf("key").forGetter(ConfigChanceCondition::key),
            Codec.DOUBLE.fieldOf("default").forGetter(ConfigChanceCondition::defaultChance)
    ).apply(i, ConfigChanceCondition::new));

    @Override
    public LootItemConditionType getType() {
        return ModLootConditions.CONFIG_CHANCE.get();
    }

    @Override
    public boolean test(LootContext context) {
        return context.getRandom().nextDouble() < Config.chance(this.key, this.defaultChance);
    }
}
