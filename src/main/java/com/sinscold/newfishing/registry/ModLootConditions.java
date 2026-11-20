package com.sinscold.newfishing.registry;

import com.sinscold.newfishing.NewFishing;
import com.sinscold.newfishing.loot.ConfigChanceCondition;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModLootConditions {
    public static final DeferredRegister<LootItemConditionType> CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, NewFishing.MODID);

    public static final DeferredHolder<LootItemConditionType, LootItemConditionType> CONFIG_CHANCE =
            CONDITIONS.register("config_chance", () -> new LootItemConditionType(ConfigChanceCondition.CODEC));

    private ModLootConditions() {}
}
